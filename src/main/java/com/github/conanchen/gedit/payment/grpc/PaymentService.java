package com.github.conanchen.gedit.payment.grpc;

import com.alipay.api.AlipayClient;
import com.github.conanchen.gedit.common.grpc.PaymentChannel;
import com.github.conanchen.gedit.payment.PaymentEnum.PaymentStatusEnum;
import com.github.conanchen.gedit.payment.config.alipay.alipayUnit.AliPayUnit;
import com.github.conanchen.gedit.payment.config.wxpay.weixinPayConfig.WeixinPayConfig;
import com.github.conanchen.gedit.payment.config.wxpay.weixinPayUtil.WXPAySign;
import com.github.conanchen.gedit.payment.model.PointsItem;
import com.github.conanchen.gedit.payment.repository.PointsItemRepository;
import com.github.conanchen.gedit.payment.repository.page.OffsetBasedPageRequest;
import com.github.conanchen.gedit.payment.store.StoreService;
import com.github.conanchen.gedit.payment.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.payment.config.alipay.alipayUnit.AliPayRequest;
import com.github.conanchen.gedit.payment.model.Payment;
import com.github.conanchen.gedit.payment.model.Points;
import com.github.conanchen.gedit.payment.repository.PaymentRepository;
import com.github.conanchen.gedit.payment.repository.PointsRepository;
import com.github.conanchen.gedit.payment.unit.SerialNumber;
import com.github.conanchen.gedit.payment.validate.PaymentValidate;
import com.github.wxpay.sdk.WXPay;
import com.google.gson.Gson;
import com.martiansoftware.validation.Hope;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Created by ZhouZeshao on 2018/1/10.
 */
@GRpcService(interceptors = {LogInterceptor.class})
public class PaymentService extends PaymentApiGrpc.PaymentApiImplBase{
    private static Gson gson = new Gson();

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PointsRepository pointsRepository;
    @Autowired
    private StoreService storeService;

//    @Autowired
//    private AlipayClient alipayClient;
    @Autowired
    private PointsItemRepository itemRepository;


    @Override
    public void create(CreatePaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        Hope.that(request.getShouldPay()).named("shouldPay").isNotNullOrEmpty()
                .isTrue(n->n <= 0,"应付金额有误，请确认后重试").value();
        Hope.that(request.getActualPay()).named("shouldPay").isNotNullOrEmpty()
                .isTrue(n->n <= 0,"实付金额有误，请确认后重试").value();
        Long orderNo = SerialNumber.digitalSerialNumberAndDate();
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        String payerUid = claims.getSubject();
//        String payerUid = UUID.randomUUID().toString();
        Points points = pointsRepository.findByUserId(payerUid);
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
            String mesg = updateRow < 1 ? "请稍后重试" : "余额不足" ;
            PaymentResponse paymentResponse = PaymentResponse.newBuilder()
                    .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(Status.OUT_OF_RANGE.getCode().toString()).setDetails(mesg)).build();
            responseObserver.onNext(paymentResponse);
            responseObserver.onCompleted();
            return;
        }
        addPointItem(orderNo.toString(),itemPoints,remark,payerUid,type);
        Payment payObject = addPayment(request,orderNo,payerUid,returnPoints,actualPay);
        Map<String,String> map = new HashMap<>();
            if(request.getChannelValue() == PaymentChannel.ALIPAY_VALUE){
                String returnStr = aliPayRequest(payObject,orderNo.toString(), actualPay);
                map.put("returnStr",returnStr);
            }else if(request.getChannelValue() == PaymentChannel.WXPAY_VALUE){
                SortedMap sortedMap = null;
                try {
                    sortedMap = wxPayRequest(orderNo.toString(),actualPay+"",request.getPayerIp(),"");
                } catch (Exception e) {
                }
            map.put("returnStr",gson.toJson(sortedMap));
        }
        PaymentResponse paymentResponse = PaymentResponse.newBuilder().setPayment(com.github.conanchen.gedit.payment.grpc.Payment.newBuilder()
                .setChannel(PaymentChannel.valueOf(payObject.getChannel()))
                .setActualPay(payObject.getActualPay())
                .setUuid(payObject.getUuid()).setSignature(gson.toJson(map)))
                .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(Status.OK.getCode().toString()).setDetails("sucess")).build();
        responseObserver.onNext(paymentResponse);
        responseObserver.onCompleted();
    }

    private String aliPayRequest(Payment payment,String orderNo,int shouldPAy){
        String amount = (((double)shouldPAy) / 100)+"";
        String subject = "尝试支付";
        String desc = "尝试支付";
        String notify_url = "";
        Map<String,String> signMap = AliPayUnit.builderAliPay(notify_url);
        AliPayRequest aliPayRequest =new AliPayRequest(amount,subject,desc,orderNo);
        Map<String, String> pcont = EntToMap(aliPayRequest,aliPayRequest.getClass());
        //自定义公从参数！需要的时候放开即可
//     PayPassParam payPassParam = new PayPassParam();
//     Map<String, String> passback = EntToMap(payPassParam,payPassParam.getClass());
////   map.put("passback_params",gson.toJson(passback));
//        AliPayUnit aliPayUnit = new AliPayUnit();
        //禁止支付类型包括,如果需要禁止更多，请追加Array参数，参数来源AliPayChannelsEnum
//        Integer [] array = new Integer[]{8};
//        map.put("disable_pay_channels",aliPayUnit.payChannels(array));
//        //可用支付类型,与禁用支付类型必须相斥，请追加Array参数，参数来源AliPayChannelsEnum
//        map.put("enable_pay_channels",aliPayUnit.payChannels(array));
        signMap.put("biz_content",gson.toJson(pcont));
        return AliPayUnit.createSign(signMap);
    }

    private SortedMap wxPayRequest(String orderNo,String shouldPay,String spbillCreateIp,String notifyUrl) throws Exception {
        WeixinPayConfig wxconfig = new WeixinPayConfig();
        WXPay wxPay = new WXPay(wxconfig);
        String body = "尝试支付";
        Map<String,String> data = WXPAySign.createMapToSign(body,orderNo,shouldPay,spbillCreateIp,notifyUrl,"APP");
        Map<String, String> resp = wxPay.unifiedOrder(data);
        SortedMap  map_weixin = new TreeMap();
        if(resp.get("return_code").equals("SUCCESS")&&resp.get("result_code").equals("SUCCESS")) {
            String prepayid = resp.get("prepay_id");
            String sign_two = WXPAySign.WXPAY2Sign(prepayid,map_weixin);
            map_weixin.put("sign", sign_two);
        }
        return map_weixin;
    }

    private <T> Map<String,String> EntToMap(Object model, Class<T> t){
        Map<String,String> map = null;
        try{
            Field[] fields =  t.getDeclaredFields();
            if(fields.length > 0 && map == null)
                map = new HashMap<String,String>();
            for(Field f:fields){
                String name = f.getName();
                name = name.substring(0,1).toUpperCase()+name.substring(1); //将属性的首字符大写，方便构造get，set方法
                Method m = model.getClass().getMethod("get"+name);
                String value = String.valueOf(m.invoke(model));
                if(map!=null && value!=null)
                    map.put(f.getName(), value);
                else
                    map.put(f.getName(), "");
            }
            if(t.getSuperclass()!=null){
                EntToMap(model, t.getSuperclass());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return map;
    }

    public int updatePointsByVersion(String userId,CreatePaymentRequest request,int returnPoints){
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

    private Payment addPayment(CreatePaymentRequest request,Long orderNo,String payerUid,Integer returnPoints,Integer actualPay){
        com.github.conanchen.gedit.payment.model.Payment payment =
                Payment.builder()
                        .uuid(UUID.randomUUID().toString())
                        .payeeId(request.getPayeeUuid())
                        .channel(request.getChannel().toString())
                        .orderNo(orderNo.toString())
                        .payeeStoreId(request.getPayeeStoreUuid())
                        .payeeWorkerId(request.getPayeeWorkerUuid())
                        .payerId(payerUid)
                        .status(PaymentStatusEnum.NEW.getCode())
                        .points(returnPoints)
                        .shouldPay(request.getShouldPay())
                        .actualPay(actualPay)
                        .pointsPay(request.getPointsPay())
                        .updateDate(new Date())
                        .createDate(new Date()).build();
        return  (Payment)paymentRepository.save(payment);
    }
    @Override
    public void prepare(PreparPaymentRequest request, StreamObserver<PreparePaymentResponse> responseObserver){

    }

    @Override
    public void get(GetPaymentRequest request, StreamObserver<PaymentResponse> responseObserver){
        Payment payment  = (Payment)paymentRepository.findOne(request.getUuid());
        if(payment == null){
            PaymentResponse paymentResponse = PaymentResponse.newBuilder()
                    .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(Status.NOT_FOUND.getCode().toString()).setDetails("没有找到记录呢")).build();
            responseObserver.onNext(paymentResponse);
            responseObserver.onCompleted();
            return;
        }
        PaymentResponse paymentResponse = PaymentResponse.newBuilder().setPayment(
                com.github.conanchen.gedit.payment.grpc.Payment.newBuilder()
                        .setUuid(payment.getUuid())
                        .setActualPay(payment.getActualPay())
                        .setChannelValue(Integer.parseInt(payment.getChannel()))
                        .setPayeeStoreUuid(payment.getPayeeStoreId())
                        .setPayeeWorkerUuid(payment.getPayeeWorkerId())
                        .setPayerUuid(payment.getPayerId())
                        .setPoints(payment.getPoints())
                        .setPointsPay(payment.getPointsPay())
//                        .setStatus(payment.getStatus())
                        .setCreated(System.currentTimeMillis()).build())
                .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(Status.OK.getCode().toString()).setDetails("success")).build();
        responseObserver.onNext(paymentResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void list(ListPaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        Pageable pageable = getPageable(request.getFrom(),request.getSize(),"createDate",Sort.Direction.DESC);
        List<Payment> paymentList = paymentRepository.findAll(new Specification<Payment>() {
            @Override
            public Predicate toPredicate(Root<Payment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if(request.getPayeeUuid() != null){
                    list.add(cb.equal(root.get("payeeUuid").as(String.class),request.getPayeeUuid()));
                }
                if(request.getPayerUuid() != null){
                    list.add(cb.equal(root.get("payerUuid").as(String.class),request.getPayerUuid()));
                }
                if(request.getPayeeStoreUuid() != null){
                    list.add(cb.equal(root.get("payeeStoreUuid").as(String.class),request.getPayeeUuid()));
                }
                Predicate[] processes = new Predicate[list.size()];
                return cb.and(list.toArray(processes));
            }
        },pageable).getContent();
        Hope.that(paymentList).isNotNullOrEmpty().isTrue(n->n.size() < 1,"没有更多数据了");
        for (Payment payment : paymentList){
            responseObserver.onNext(modelToRes(payment));
            try { Thread.sleep(500); } catch (InterruptedException e) {}
        }
        responseObserver.onCompleted();
    }

    @Override
    public void listMy(ListMyPaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        String myId = claims.getSubject();
//        String myId = UUID.randomUUID().toString();
        Pageable pageable = getPageable(request.getFrom(),request.getSize(),"createDate",Sort.Direction.DESC);
        List<Payment> paymentList = paymentRepository.findAll(new Specification<Payment>() {
            @Override
            public Predicate toPredicate(Root<Payment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<>();
                if(request.getIncludePayee()){
                    list.add(cb.equal(root.get("payeeUuid").as(String.class),myId));
                }else{
                    list.add(cb.equal(root.get("payerUuid").as(String.class),myId));
                }
                Predicate[] processes = new Predicate[list.size()];
                return cb.and(list.toArray(processes));
            }
        },pageable).getContent();
        Hope.that(paymentList).isNotNullOrEmpty().isTrue(n->n.size() < 1,"没有更多数据了");
        for (Payment payment : paymentList){
            responseObserver.onNext(modelToRes(payment));
            try { Thread.sleep(500);} catch (InterruptedException e) {}
        }
        responseObserver.onCompleted();
    }

    private PaymentResponse modelToRes(Payment payment){
        com.github.conanchen.gedit.payment.grpc.Payment retryPayment = com.github.conanchen.gedit.payment.grpc.Payment.newBuilder().build();
        BeanUtils.copyProperties(payment,retryPayment);
        PaymentResponse paymentResponse = PaymentResponse.newBuilder().setPayment(retryPayment)
                .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(Status.OK.getCode().toString()).setDetails("success").build()).build();
        return paymentResponse;
    }

    private Pageable getPageable(Integer oldFrom,Integer oldSize,String orderName,Sort.Direction orderType){
        Integer from = Hope.that(oldFrom).isNotNull().isTrue(n -> n >= 0,"from must be greater than or equals：%s",0).value();
        Integer size = Hope.that(oldSize).isNotNull().isTrue(n -> n > 0,"size must be greater than %s",0).value();
        Integer tempFrom = from == 0 ? 0 :from - 1 ;
        Pageable pageable = new OffsetBasedPageRequest(tempFrom,size,new Sort(orderType,orderName));
        return pageable;
    }
}
