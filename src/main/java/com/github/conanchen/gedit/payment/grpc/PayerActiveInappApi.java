package com.github.conanchen.gedit.payment.grpc;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.github.conanchen.gedit.accounting.account.grpc.LockPointsResponse;
import com.github.conanchen.gedit.accounting.account.grpc.UnlockPointsResponse;
import com.github.conanchen.gedit.accounting.rewardsif.grpc.RewardIfEventResponse;
import com.github.conanchen.gedit.common.grpc.PaymentChannel;
import com.github.conanchen.gedit.common.grpc.Status;
import com.github.conanchen.gedit.payer.activeinapp.grpc.*;
import com.github.conanchen.gedit.payment.GrpcService.AccountingService;
import com.github.conanchen.gedit.payment.GrpcService.StoreService;
import com.github.conanchen.gedit.payment.GrpcService.UserService;
import com.github.conanchen.gedit.payment.PaymentEnum.PaymentStatusEnum;
import com.github.conanchen.gedit.payment.common.grpc.PaymentResponse;
import com.github.conanchen.gedit.payment.config.alipay.alipayConfig.AlipayConfig;
import com.github.conanchen.gedit.payment.config.wxpay.weixinPayConfig.WeixinPayConfig;
import com.github.conanchen.gedit.payment.config.wxpay.weixinPayUtil.MD5Util;
import com.github.conanchen.gedit.payment.config.wxpay.weixinPayUtil.WXPAySign;
import com.github.conanchen.gedit.payment.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.payment.model.Payment;
import com.github.conanchen.gedit.payment.model.PaymentCode;
import com.github.conanchen.gedit.payment.model.Points;
import com.github.conanchen.gedit.payment.model.PointsItem;
import com.github.conanchen.gedit.payment.repository.PaymentRepository;
import com.github.conanchen.gedit.payment.repository.PointsItemRepository;
import com.github.conanchen.gedit.payment.repository.PointsRepository;
import com.github.conanchen.gedit.payment.unit.EntToMapUnit;
import com.github.conanchen.gedit.payment.unit.RedisSetMap;
import com.github.conanchen.gedit.payment.unit.SerialNumber;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfile;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileResponse;
import com.github.conanchen.gedit.user.profile.grpc.UserProfile;
import com.github.conanchen.gedit.user.profile.grpc.UserProfileResponse;
import com.github.wxpay.sdk.WXPay;
import com.google.gson.Gson;
import com.martiansoftware.validation.Hope;
import com.martiansoftware.validation.UncheckedValidationException;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@GRpcService
public class PayerActiveInappApi extends PayerActiveInappApiGrpc.PayerActiveInappApiImplBase{
    private static Gson gson = new Gson();

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PointsRepository pointsRepository;
    @Autowired
    private StoreService storeService;
    @Autowired
    private UserService userService;
    @Autowired
    private WXPAySign wxpAySign;
    @Autowired
    private WeixinPayConfig wxPayConfig;
    @Autowired
    private PointsItemRepository itemRepository;
    @Autowired
    private AccountingService accountingService;

    @Autowired
    private AlipayConfig alipayConfig;
    @Autowired
    private RedisSetMap redisSetMap;

    @Override
    public void getMyPayeeCode (GetMyPayeeCodeRequest request, StreamObserver<GetMyPayeeCodeResponse> streamObserver){
        //only called by me,例如店员/收银员app调用本api生成收款码供顾客扫码支付
        GetMyPayeeCodeResponse.Builder responseBuild = GetMyPayeeCodeResponse.newBuilder();
        String payeeStoreUuid = request.getPayeeStoreUuid();
        String code ="";
        UserProfile payeeProfile = UserProfile.newBuilder().build();
        Metadata header = AuthInterceptor.HEADERS.get();//获取header并将其传入调用方法中
        log.info("header:{}",header.toString());
        UserProfileResponse userProfileResponse = userService.getUser(header);
        if(userProfileResponse.hasUserProfile()){
            payeeProfile = userProfileResponse.getUserProfile();
        }
        try {
            code = MD5Util.MD5Encode(payeeStoreUuid,"utf-8").toUpperCase();
            log.info("payeeStoreUuid:{},code:{}",payeeStoreUuid,code);
        } catch (Exception e) {
            Status status = Status.newBuilder().setCode(Status.Code.NOT_FOUND).setDetails("系统繁忙,稍后重试！").build();
            streamObserver.onNext(responseBuild.setStatus(status).build());
            streamObserver.onCompleted();
            return;
        }
        StoreProfile storeProfile = StoreProfile.newBuilder().build();
        StoreProfileResponse response = storeService.getStoreProfile(request.getPayeeStoreUuid(),header);
        if(response.hasStoreProfile()){
            storeProfile = response.getStoreProfile();
        }
        PayeeCode payeeCode = buildReceiptCode(storeProfile,code,payeeProfile);
        PaymentCode paymentCode = new PaymentCode();
        BeanUtils.copyProperties(payeeCode,paymentCode);
        log.info("paymentCode:{}",paymentCode);
        log.info("getMyPayeeCode code:{}",code);
        Map<String,String> redisMap = EntToMapUnit.EntToMap(paymentCode,PaymentCode.class);
        log.info("redisMap:{}",redisMap.toString());
//        long deleteNum = redisSetMap.deleteMap(code);
//        log.info("deleteNum:{}",deleteNum);
        redisSetMap.setCacheMap(code,redisMap);
        GetMyPayeeCodeResponse receiptCodeResponse = responseBuild.setPayeeCode(payeeCode)
                .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().
                        setCode(com.github.conanchen.gedit.common.grpc.Status.Code.OK).
                        setDetails("success")).build();
        streamObserver.onNext(receiptCodeResponse);
        streamObserver.onCompleted();
    }
    @Override
    public void getPayeeCode (GetPayeeCodeRequest request, StreamObserver<GetPayeeCodeResponse> streamObserver) {
            String code = request.getPayeeCode();
        Map<String,String> map = redisSetMap.getCacheMap(code);
        log.info("redis get map :{}",map.toString());
        PayeeCode receiptCode = MapToEnt(map).build();
        com.github.conanchen.gedit.common.grpc.Status.Code returnCode;
        String msg = "";
        GetPayeeCodeResponse.Builder responseBuild = GetPayeeCodeResponse.newBuilder();
        if(null != receiptCode && !"".equals(receiptCode)){
            returnCode = com.github.conanchen.gedit.common.grpc.Status.Code.OK;
            msg = "success";
            responseBuild.setPayeeCode(receiptCode);
        }else{
            returnCode = com.github.conanchen.gedit.common.grpc.Status.Code.OUT_OF_RANGE;
            msg = "支付码过期,请收银员刷新二维码！";
        }

        responseBuild.setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(returnCode)
                        .setDetails(msg).build()).build();
        streamObserver.onNext(responseBuild.build());
    }
    @Override
    public void prepare(PreparePayerInappPaymentRequest request,StreamObserver<PreparePayerInappPaymentResponse> streamObserver){
        //only called by me, 顾客扫码店员/收银员的收款码后，如果支付一定金额将会获取多少积分返还等信息
        String code = request.getPayeeCode();
        log.info("prepare code:{}",code);
        int shouldPay = request.getShouldPay();
        int pointsPay = 0;
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        String payerUuid = claims.getSubject();
        Map<String,String> map = redisSetMap.getCacheMap(code);
        log.info("redis get map :{}",map.toString());
        //todo 询问accounting系统用户积分情况
        accountingService.askReward(payerUuid
                , map.get("payeeUuid"), map.get("payeeStoreUuid"),
                map.get("payeeWorkerUuid"), shouldPay, new AccountingService.AskRewardCallback() {
                    @Override
                    public void onAccountResponse(List<RewardIfEventResponse> responseList) {
                        log.info("responseList:{}",responseList.toString());
                        PreparePayerInappPaymentResponse response = PreparePayerInappPaymentResponse.newBuilder()
                                .setActualPay(0)
                                .setPayeeCode(code)
                                .setPayeeName("dsa")
                                .setPayeeStoreName("kdhas")
                                .setPayeeWorkerName("kdhsajkgduk")
                                .setShouldPay(1)
                                .setActualPay(0)
                                .setPointsPay(0)
                                .setPointsRepay(0)
                                .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder()
                                        .setCode(com.github.conanchen.gedit.common.grpc.Status.Code.OK)
                                        .setDetails("success")
                                        .build())
                                .build();
                        streamObserver.onNext(response);
                        streamObserver.onCompleted();
                    }

                    @Override
                    public void onGrpcApiError(Status status) {
                        log.error("询问积分系统出错，{}",status.getDetails());
                        PreparePayerInappPaymentResponse response = PreparePayerInappPaymentResponse.newBuilder()
                                .setStatus(Status.newBuilder().setCode(Status.Code.NOT_FOUND).setDetails(status.getDetails())).build();
                        streamObserver.onNext(response);
                        streamObserver.onCompleted();
                    }

                    @Override
                    public void onGrpcApiCompleted() {
                        log.info("call method askReward is completed");
                    }
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CreatePayerInappPaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        try {
            Hope.that(request.getShouldPay()).named("shouldPay")
                    .isTrue(n-> n > 0,"应付金额应该大于0，请确认后重试").value();
            Hope.that(request.getActualPay()).named("actualPay")
                    .isTrue(n->n > 0,"实付金额应该大于0，请确认后重试").value();
        }catch (Exception e){
            if(e instanceof UncheckedValidationException){
                PaymentResponse paymentResponse = PaymentResponse.newBuilder()
                        .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder()
                                .setCode(Status.Code.OUT_OF_RANGE)
                                .setDetails(e.getMessage())).build();
                responseObserver.onNext(paymentResponse);
                responseObserver.onCompleted();
                return;
            }
        }
        Long orderNo = SerialNumber.digitalSerialNumberAndDate();
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        String payerUid = claims.getSubject();
        Integer actualPay = request.getActualPay();
        Payment payObject = addPayment(request,orderNo,payerUid,request.getPointsRepay(),actualPay);
        if(request.getPointsPay() < 0){
            accountingService.lockPoints(payerUid, payObject.getUuid(), request.getPointsPay(), new AccountingService.LockPointCallback() {
                @Override
                public void onAccountResponse(LockPointsResponse response) {
                    if(response.getStatus().getCode().equals(Status.Code.OK)){
                        String signature = "";
                        log.info("channel:{}",request.getPaymentChannelValue());
                        if(request.getPaymentChannelValue()== PaymentChannel.ALIPAY_VALUE){
                            String returnStr = null;
                            try {
                                Double AlipayActualPay =(((double)actualPay) / 100);
                                returnStr = aliPayRequest(orderNo.toString(), AlipayActualPay.toString());
                            } catch (AlipayApiException e) {
                                e.printStackTrace();
                            }
                            signature = returnStr;
                        }else if(request.getPaymentChannelValue() == PaymentChannel.WECHAT_VALUE){
                            SortedMap sortedMap = null;
                            try {
                                sortedMap = wxPayRequest(orderNo.toString(),actualPay.toString(),request.getPayerIp());
                            } catch (Exception e) {
                                log.error("e:{}",e.getMessage());
                            }
                            signature = gson.toJson(sortedMap);
                        }
                        log.info("signature:{}",signature);
                        PaymentResponse paymentResponse = PaymentResponse.newBuilder().setPayment(com.github.conanchen.gedit.payment.common.grpc.Payment.newBuilder()
                                .setPaymentChannel(PaymentChannel.valueOf(payObject.getChannel()))
                                .setActualPay(payObject.getActualPay())
                                .setUuid(payObject.getUuid()).setPaymentChannelSignature(signature))
                                .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(com.github.conanchen.gedit.common.grpc.Status.Code.OK).setDetails("sucess")).build();
                        responseObserver.onNext(paymentResponse);
                        responseObserver.onCompleted();
                    }else{
                        return;
                    }
                }

                @Override
                public void onGrpcApiError(Status status) {
                    PaymentResponse paymentResponse = PaymentResponse.newBuilder()
                            .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder()
                                    .setCode(Status.Code.OUT_OF_RANGE).setDetails(status.getDetails())).build();
                    responseObserver.onNext(paymentResponse);
                    responseObserver.onCompleted();
                    return;
                }

                @Override
                public void onGrpcApiCompleted() {
                    responseObserver.onCompleted();
                }
            });
        }


    }

    @Override
    public void cancel (CancelPayerInappPaymentRequest request,StreamObserver<PaymentResponse> streamObserver) {
        String paymentUuid = request.getPaymentUuid();
        Payment payment = (Payment)paymentRepository.findOne(paymentUuid);
        log.info("payment:{}",payment.toString());
        accountingService.unLockPoints(paymentUuid, payment.getPayerId(), new AccountingService.UnLockPointCallback() {
            @Override
            public void onAccountResponse(UnlockPointsResponse response) {
                if(response.getStatus().getCode().equals(Status.Code.OK)){
                    PaymentResponse paymentResponse = PaymentResponse.newBuilder().build();
                    streamObserver.onNext(paymentResponse);
                    streamObserver.onCompleted();
                }
                return;
            }

            @Override
            public void onGrpcApiError(Status status) {
                PaymentResponse paymentResponse = PaymentResponse.newBuilder()
                        .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder()
                                .setCode(Status.Code.OUT_OF_RANGE).setDetails(status.getDetails())).build();
                streamObserver.onNext(paymentResponse);
                streamObserver.onCompleted();
                return;
            }

            @Override
            public void onGrpcApiCompleted() {
                streamObserver.onCompleted();
            }
        });
    }


    private PayeeCode.Builder MapToEnt(Map<String,String> map){
        PayeeCode.Builder  payeeCodeBuild = PayeeCode.newBuilder();
        payeeCodeBuild.setExpiresIn(Long.parseLong(map.get("expiresIn")== null ? "-1":map.get("expiresIn")));
        payeeCodeBuild.setPayeeLogo(map.get("payeeLogo") == null?"":map.get("payeeLogo"));
        payeeCodeBuild.setPayeeName(map.get("payeeName") == null?"":map.get("payeeName"));
        payeeCodeBuild.setPayeeStoreLogo(map.get("payeeStoreLogo") == null?"":map.get("payeeStoreLogo"));
        payeeCodeBuild.setPayeeStoreNamee(map.get("payeeStoreName")==null?"":map.get("payeeStoreName"));
        payeeCodeBuild.setPayeeStoreUuid(map.get("payeeStoreUuid") == null? "" :map.get("payeeStoreUuid"));
        payeeCodeBuild.setPayeeUuid(map.get("payeeUuid") == null ? "" :map.get("payeeStoreUuid"));
        payeeCodeBuild.setPayeeCode(map.get("payeeCode") == null ? "" : map.get("payeeCode"));
        payeeCodeBuild.setPayeeWorkerLogo(map.get("payeeWorkerLogo") == null ? "" : map.get("payeeWorkerLogo"));
        payeeCodeBuild.setPayeeWorkerUuid(map.get("payeeWorkerUuid") == null ? "" :map.get("payeeWorkerUuid"));
        return payeeCodeBuild;
    }

    private PayeeCode buildReceiptCode(StoreProfile storeProfile,String code,UserProfile payeeProfile){
        PayeeCode.Builder receiptCode = PayeeCode.newBuilder();
        if(storeProfile != null && payeeProfile != null){
            receiptCode.setPayeeCode(code);
            receiptCode.setExpiresIn(-1);
            receiptCode.setPayeeUuid(payeeProfile.getUuid());
            receiptCode.setPayeeLogo(payeeProfile.getLogo());
            receiptCode.setPayeeName(payeeProfile.getUsername());
            receiptCode.setPayeeWorkerName(payeeProfile.getUsername());
            receiptCode.setPayeeWorkerUuid(payeeProfile.getUuid());
            receiptCode.setPayeeLogo(payeeProfile.getLogo());
            receiptCode.setPayeeStoreLogo(storeProfile.getLogo());
            receiptCode.setPayeeStoreNamee(storeProfile.getName());
            receiptCode.setPayeeStoreUuid(storeProfile.getUuid());
        }
        return receiptCode.build();
    }

    private String aliPayRequest(String orderNo,String shouldPAy) throws AlipayApiException {
        String notify_url = "http://dev.jifenpz.com/aliPay/notify";
        String serviceURl = "https://openapi.alipay.com/gateway.do";
        AlipayClient alipayClient = new DefaultAlipayClient(serviceURl,alipayConfig.app_id ,
                alipayConfig.private_key,"json","utf-8", alipayConfig.ali_public_key,"RSA2");
        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
//      PayPassParam payPassParam = new PayPassParam();
//      Map<String, String> passback = EntToMap(payPassParam,payPassParam.getClass());
//      model.setPassbackParams(gson.toJson(passback));
//      AliPayUnit aliPayUnit = new AliPayUnit();
        //禁止支付类型包括,如果需要禁止更多，请追加Array参数，参数来源AliPayChannelsEnum
//      Integer [] array = new Integer[]{8};
//      model.setDisablePayChannels(aliPayUnit.payChannels(array));
//        //可用支付类型,与禁用支付类型必须相斥，请追加Array参数，参数来源AliPayChannelsEnum
//      model.setEnablePayChannels(aliPayUnit.payChannels(array));
        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。

        model.setBody("我是测试数据");
        model.setSubject("App支付测试Java");
        model.setOutTradeNo(orderNo.toString());
        model.setTimeoutExpress("30m");
        model.setTotalAmount(shouldPAy);
        model.setProductCode("QUICK_MSECURITY_PAY");
        request.setBizModel(model);
        request.setNotifyUrl(notify_url);
        //这里和普通的接口调用不同，使用的是sdkExecute
        AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
        return response.getBody();
    }

    private SortedMap wxPayRequest(String orderNo,String shouldPay,String spbillCreateIp) throws Exception {
        WXPay wxPay = new WXPay(wxPayConfig);
        String notifyUrl =wxPayConfig.getNotify_url();
        String body = "尝试支付";
        Map<String,String> data = wxpAySign.createMapToSign(body,orderNo,shouldPay,spbillCreateIp,notifyUrl,"APP");
        log.info("data:{}",data);
        Map<String, String> resp = wxPay.unifiedOrder(data);
        SortedMap  map_weixin = new TreeMap();
        log.info("resp:{}",resp.toString());
        if(resp.get("return_code").equals("SUCCESS")&&resp.get("result_code").equals("SUCCESS")) {
            String prepayid = resp.get("prepay_id");
            String sign_two = wxpAySign.WXPAY2Sign(prepayid,map_weixin);
            map_weixin.put("sign", sign_two);
        }
        return map_weixin;
    }



    public int updatePointsByVersion(String userId,CreatePayerInappPaymentRequest request,int returnPoints){
        int retryCount = 0;
        int updateRow = 0;
        while (retryCount < 3){
            Points points = pointsRepository.findByUserId(userId);
            int usable = points.getUsable();
            int newPoints = request.getPointsPay() == 0 ? (returnPoints + usable) : usable - request.getPointsPay();
            if(newPoints < 0){
                return 5;
            }
            Long version = 0l;
            if(points.getVersion() != null){
                version = points.getVersion();
            }
            updateRow = pointsRepository.updateByVersion(points.getUsable(),points.getUuid(),version);
            if(updateRow != 1){
                retryCount ++;
            }else{
                break;
            }
        }
        return updateRow;
    }

    public void addPointItem(String orderNo,int points,String remark,String userId,Integer type){
        PointsItem pointsItem = PointsItem.builder().orderNo(orderNo).points(points).remark(remark).userId(userId).type(type).status(PaymentStatusEnum.NEW.getCode()).uuid(UUID.randomUUID().toString()).createDate(new Date()).updateDate(new Date()).build();
        itemRepository.save(pointsItem);
    }

    private Payment addPayment(CreatePayerInappPaymentRequest request,Long orderNo,String payerUid,Integer returnPoints,Integer actualPay){
        com.github.conanchen.gedit.payment.model.Payment payment =
                Payment.builder()
                        .channel(request.getPaymentChannel().toString())
                        .orderNo(orderNo.toString())
                        .payerId(payerUid)
                        .status(PaymentStatusEnum.INPROGRESS.getCode())
                        .points(returnPoints)
                        .shouldPay(request.getShouldPay())
                        .actualPay(request.getActualPay())
                        .pointsPay(request.getPointsPay())
                        .updateDate(new Date())
                        .createDate(new Date()).build();
        return  (Payment)paymentRepository.save(payment);
    }



}
