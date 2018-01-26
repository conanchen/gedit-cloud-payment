package com.github.conanchen.gedit.payment.grpc;

import com.github.conanchen.gedit.common.grpc.PaymentChannel;
import com.github.conanchen.gedit.payer.activeinapp.grpc.*;
import com.github.conanchen.gedit.payment.GrpcService.StoreService;
import com.github.conanchen.gedit.payment.GrpcService.UserService;
import com.github.conanchen.gedit.payment.PaymentEnum.PaymentStatusEnum;
import com.github.conanchen.gedit.payment.common.grpc.PaymentResponse;
import com.github.conanchen.gedit.payment.config.alipay.alipayUnit.AliPayRequest;
import com.github.conanchen.gedit.payment.config.alipay.alipayUnit.AliPayUnit;
import com.github.conanchen.gedit.payment.config.wxpay.weixinPayConfig.WeixinPayConfig;
import com.github.conanchen.gedit.payment.config.wxpay.weixinPayUtil.MD5Util;
import com.github.conanchen.gedit.payment.config.wxpay.weixinPayUtil.WXPAySign;
import com.github.conanchen.gedit.payment.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.payment.model.Payment;
import com.github.conanchen.gedit.payment.model.Points;
import com.github.conanchen.gedit.payment.model.PointsItem;
import com.github.conanchen.gedit.payment.repository.PaymentRepository;
import com.github.conanchen.gedit.payment.repository.PointsItemRepository;
import com.github.conanchen.gedit.payment.repository.PointsRepository;
import com.github.conanchen.gedit.payment.unit.EntToMapUnit;
import com.github.conanchen.gedit.payment.unit.SerialNumber;
import com.github.conanchen.gedit.payment.validate.PaymentValidate;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfile;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileResponse;
import com.github.conanchen.gedit.user.profile.grpc.UserProfile;
import com.github.conanchen.gedit.user.profile.grpc.UserProfileResponse;
import com.github.wxpay.sdk.WXPay;
import com.google.gson.Gson;
import com.martiansoftware.validation.Hope;
import com.martiansoftware.validation.UncheckedValidationException;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private RedisTemplate redisTemplate;

    @Autowired
    private PointsItemRepository itemRepository;
    @Override
    public void getMyPayeeCode (GetMyPayeeCodeRequest request, StreamObserver<GetMyPayeeCodeResponse> streamObserver){
        //only called by me,例如店员/收银员app调用本api生成收款码供顾客扫码支付
        String payeeStoreUuid = request.getPayeeStoreUuid();
        String code ="";
        UserProfile payeeProfile = UserProfile.newBuilder().build();
        Metadata header = AuthInterceptor.HEADERS.get();//获取header并将其传入调用方法中
        log.info("header:{}",header.toString());
        UserProfileResponse userProfileResponse = userService.getUser(payeeStoreUuid,header);
        if(userProfileResponse.hasUserProfile()){
            payeeProfile = userProfileResponse.getUserProfile();
        }
        try {
            code = MD5Util.MD5Encode(payeeStoreUuid,"utf-8");
        } catch (Exception e) {
        }
        StoreProfile storeProfile = StoreProfile.newBuilder().build();
        StoreProfileResponse response = storeService.getStoreProfile(request.getPayeeStoreUuid(),header);
        if(response.hasStoreProfile()){
            storeProfile = response.getStoreProfile();
        }
        PayeeCode receiptCode = buildReceiptCode(storeProfile,code,payeeProfile);
        ValueOperations<String, PayeeCode> operations = redisTemplate.opsForValue();
        operations.set(code,receiptCode,24*60*60, TimeUnit.SECONDS);
        GetMyPayeeCodeResponse receiptCodeResponse = GetMyPayeeCodeResponse.newBuilder().setPayeeCode(receiptCode).setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(com.github.conanchen.gedit.common.grpc.Status.Code.OK).setDetails("success")).build();
        streamObserver.onNext(receiptCodeResponse);
        streamObserver.onCompleted();
    }
    @Override
    public void getPayeeCode (GetPayeeCodeRequest request, StreamObserver<GetPayeeCodeResponse> streamObserver) {
            String code = request.getPayeeCode();
        ValueOperations<String, PayeeCode> operations = redisTemplate.opsForValue();
        PayeeCode o = operations.get(code);
        PayeeCode receiptCode = PayeeCode.newBuilder().build();
        String returnCode ="";
        String msg = "";
        if(o != null){
            receiptCode = gson.fromJson(o.toString(),PayeeCode.class);
            returnCode = Status.OK.getCode().toString();
            msg = "success";
        }else{
            returnCode = Status.OUT_OF_RANGE.getCode().toString();
            msg = "支付码过期,请收银员刷新二维码！";
        }
        GetPayeeCodeResponse response = GetPayeeCodeResponse.newBuilder().setPayeeCode(receiptCode)
                .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder()
                        .setDetails(msg).build()).build();
        streamObserver.onNext(response);
    }
    @Override
    public void prepare (PreparePayerInappPaymentRequest request,StreamObserver<PreparePayerInappPaymentResponse> streamObserver){
        //only called by me, 顾客扫码店员/收银员的收款码后，如果支付一定金额将会获取多少积分返还等信息
        String code = request.getPayeeCode();
        boolean isPointsPay = request.getIsPointsPay();
        int shouldPay = request.getShouldPay();
        int pointsPay = 0;
        if(isPointsPay){
            //todo 差一段远程调用--计算积分的方法
        }else{
            //todo 计算不使用积分支付时候要应该返还的积分
        }

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
                .setIsPointsPay(true)
                .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder()
                        .setCode(com.github.conanchen.gedit.common.grpc.Status.Code.OK)
                        .setDetails("success")
                        .build())
                .build();
        streamObserver.onNext(response);
        streamObserver.onCompleted();
    }

    @Override
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
                                .setCode(com.github.conanchen.gedit.common.grpc.Status.Code.OK)
                                .setDetails(e.getMessage())).build();
                responseObserver.onNext(paymentResponse);
                responseObserver.onCompleted();
                return;
            }
        }

        Long orderNo = SerialNumber.digitalSerialNumberAndDate();
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        String payerUid = claims.getSubject();
//        String payerUid = UUID.randomUUID().toString();
        Points points = pointsRepository.findByUserId(payerUid);
        if(points == null){
            initPoint(payerUid);
        }
        PaymentValidate paymentValidate = new PaymentValidate();
        paymentValidate.CreateValidate(request,responseObserver,points);
        Integer actualPay = request.getShouldPay();
        int returnPoints = 0;
        int itemPoints = 0;
        String remark = "";
        int type = 10;
        int updateRow = updatePointsByVersion(payerUid,request,returnPoints);
        Hope.that(request.getPointsPay()).isNotNull();
        if(updateRow != 1 ){
            String msg = updateRow < 1 ? "请稍后重试" : "余额不足" ;
            PaymentResponse paymentResponse = PaymentResponse.newBuilder()
                    .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(com.github.conanchen.gedit.common.grpc.Status.Code.OUT_OF_RANGE).setDetails(msg)).build();
            responseObserver.onNext(paymentResponse);
            responseObserver.onCompleted();
            return;
        }
        addPointItem(orderNo.toString(),itemPoints,remark,payerUid,type);
        Payment payObject = addPayment(request,orderNo,payerUid,returnPoints,actualPay);
        String signature = "";
        log.info("channel:{}",request.getPaymentChannelValue());
        if(request.getPaymentChannelValue()== PaymentChannel.ALIPAY_VALUE){
            String returnStr = aliPayRequest(orderNo.toString(), actualPay);
            signature = returnStr;
        }else if(request.getPaymentChannelValue() == PaymentChannel.WECHAT_VALUE){
            SortedMap sortedMap = null;
            try {
                sortedMap = wxPayRequest(orderNo.toString(),actualPay+"",request.getPayerIp());
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
    }

    private PayeeCode buildReceiptCode(StoreProfile storeProfile,String code,UserProfile payeeProfile){
        PayeeCode.Builder receiptCode = PayeeCode.newBuilder();
        if(storeProfile != null && payeeProfile != null){
            receiptCode.setPayeeCode(code);
            receiptCode.setExpiresIn(-1);
            receiptCode.setPayeeUuid(payeeProfile.getUuid());
            receiptCode.setPayeeLogo(payeeProfile.getLogo());
            receiptCode.setPayeeName(payeeProfile.getName());
            receiptCode.setPayeeWorkerName(payeeProfile.getName());
            receiptCode.setPayeeWorkerUuid(payeeProfile.getUuid());
            receiptCode.setPayeeLogo(payeeProfile.getLogo());
            receiptCode.setPayeeStoreLogo(storeProfile.getLogo());
            receiptCode.setPayeeStoreNamee(storeProfile.getName());
            receiptCode.setPayeeStoreUuid(storeProfile.getUuid());
        }
        return receiptCode.build();
    }

    private void initPoint(String userId){
        Points points = new Points();
        points.setUsable(0);
        points.setFreeze(0);
        points.setRollIn(0);
        points.setRollOut(0);
        points.setUserId(userId);
        points.setVersion(0l);
        points.setCreateDate(new Date());
        points.setUpdateDate(new Date());
        pointsRepository.save(points);
    }

    private String aliPayRequest(String orderNo,int shouldPAy){
        String amount = (((double)shouldPAy) / 100)+"";
        String subject = "尝试支付";
        String desc = "尝试支付";
        String notify_url = "http://dev.jifenpz.com/aliPay/notify";
        //封装公共请求参数,这里不需要改动.
        Map<String,String> signMap = AliPayUnit.builderAliPay(notify_url);
        Map<String,String> map = new HashMap<>();
        map.put("desc",desc);
        String body = gson.toJson(map);
        AliPayRequest aliPayRequest =new AliPayRequest(amount,subject,orderNo,body);
        Map<String, String> bizContent = EntToMapUnit.EntToMap(aliPayRequest,aliPayRequest.getClass());
        //自定义公共参数！需要的时候放开即可
//     PayPassParam payPassParam = new PayPassParam();
//     Map<String, String> passback = EntToMap(payPassParam,payPassParam.getClass());
////   bizContent.put("passback_params",gson.toJson(passback));
//        AliPayUnit aliPayUnit = new AliPayUnit();
        //禁止支付类型包括,如果需要禁止更多，请追加Array参数，参数来源AliPayChannelsEnum
//        Integer [] array = new Integer[]{8};
//        map.put("disable_pay_channels",aliPayUnit.payChannels(array));
//        //可用支付类型,与禁用支付类型必须相斥，请追加Array参数，参数来源AliPayChannelsEnum
//        map.put("enable_pay_channels",aliPayUnit.payChannels(array));
        signMap.put("biz_content",gson.toJson(bizContent));
        return AliPayUnit.createSign(signMap);
    }

    private SortedMap wxPayRequest(String orderNo,String shouldPay,String spbillCreateIp) throws Exception {
        WeixinPayConfig wxconfig = new WeixinPayConfig();
        WXPay wxPay = new WXPay(wxconfig);
        String notifyUrl =wxconfig.getNotify_url();
        String body = "尝试支付";
        Map<String,String> data = WXPAySign.createMapToSign(body,orderNo,shouldPay,spbillCreateIp,notifyUrl,"APP");
        log.info("data:{}",data);
        Map<String, String> resp = wxPay.unifiedOrder(data);
        SortedMap  map_weixin = new TreeMap();
        log.info("resp:{}",resp.toString());
        if(resp.get("return_code").equals("SUCCESS")&&resp.get("result_code").equals("SUCCESS")) {
            String prepayid = resp.get("prepay_id");
            String sign_two = WXPAySign.WXPAY2Sign(prepayid,map_weixin);
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
                        .actualPay(actualPay)
                        .pointsPay(request.getPointsPay())
                        .updateDate(new Date())
                        .createDate(new Date()).build();
        return  (Payment)paymentRepository.save(payment);
    }



}
