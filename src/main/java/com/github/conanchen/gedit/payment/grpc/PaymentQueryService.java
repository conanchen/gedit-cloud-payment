package com.github.conanchen.gedit.payment.grpc;

import com.github.conanchen.gedit.common.grpc.PaymentChannel;
import com.github.conanchen.gedit.payment.common.grpc.PaymentResponse;
import com.github.conanchen.gedit.payment.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.payment.model.Payment;
import com.github.conanchen.gedit.payment.query.grpc.*;
import com.github.conanchen.gedit.payment.repository.PaymentRepository;
import com.github.conanchen.gedit.payment.repository.PointsItemRepository;
import com.github.conanchen.gedit.payment.repository.PointsRepository;
import com.github.conanchen.gedit.payment.repository.page.OffsetBasedPageRequest;
import com.github.conanchen.gedit.payment.GrpcService.StoreService;
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
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ZhouZeshao on 2018/1/10.
 */
@GRpcService
public class PaymentQueryService extends PaymentQueryApiGrpc.PaymentQueryApiImplBase{
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
    public void get(GetPaymentRequest request, StreamObserver<PaymentResponse> responseObserver){
        Payment payment  = (Payment)paymentRepository.findOne(request.getUuid());
        if(payment == null){
            PaymentResponse paymentResponse = PaymentResponse.newBuilder()
                    .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(com.github.conanchen.gedit.common.grpc.Status.Code.NOT_FOUND).setDetails("没有找到记录呢")).build();
            responseObserver.onNext(paymentResponse);
            responseObserver.onCompleted();
            return;
        }
        PaymentResponse paymentResponse = PaymentResponse.newBuilder().setPayment(
                com.github.conanchen.gedit.payment.common.grpc.Payment.newBuilder()
                        .setUuid(payment.getUuid())
                        .setActualPay(payment.getActualPay())
                        .setPaymentChannel(PaymentChannel.valueOf(payment.getChannel()))
                        .setPayeeStoreUuid(payment.getPayeeStoreId())
                        .setPayeeWorkerUuid(payment.getPayeeWorkerId())
                        .setPayerUuid(payment.getPayerId())
                        .setPointsPay(payment.getPointsPay())
//                        .setStatus(payment.getStatus())
                        .setCreated(System.currentTimeMillis()).build())
                .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder()
                        .setCode(com.github.conanchen.gedit.common.grpc.Status.Code.OK).setDetails("success")).build();
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
    public void listMyPayerPayment(ListMyPayerPaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        String myId = claims.getSubject();
//        String myId = UUID.randomUUID().toString();
        Pageable pageable = getPageable(request.getFrom(),request.getSize(),"createDate",Sort.Direction.DESC);
        List<Payment> paymentList = paymentRepository.findAll(new Specification<Payment>() {
            @Override
            public Predicate toPredicate(Root<Payment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<>();
                list.add(cb.equal(root.get("payerUuid").as(String.class),myId));
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

    @Override
    public void listMyPayeePayment (ListMyPayeePaymentRequest request, StreamObserver<PaymentResponse> streamObserver) {
        //only called by me，获取我的收款单
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        String myId = claims.getSubject();
//        String myId = UUID.randomUUID().toString();
        Pageable pageable = getPageable(request.getFrom(),request.getSize(),"createDate",Sort.Direction.DESC);
        List<Payment> paymentList = paymentRepository.findAll(new Specification<Payment>() {
            @Override
            public Predicate toPredicate(Root<Payment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<>();
                list.add(cb.equal(root.get("payeeUuid").as(String.class),myId));
                Predicate[] processes = new Predicate[list.size()];
                return cb.and(list.toArray(processes));
            }
        },pageable).getContent();
        Hope.that(paymentList).isNotNullOrEmpty().isTrue(n->n.size() < 1,"没有更多数据了");
        for (Payment payment : paymentList){
            streamObserver.onNext(modelToRes(payment));
            try { Thread.sleep(500);} catch (InterruptedException e) {}
        }
        streamObserver.onCompleted();
    }

    private PaymentResponse modelToRes(Payment payment){
        com.github.conanchen.gedit.payment.common.grpc.Payment retryPayment = com.github.conanchen.gedit.payment.common.grpc.Payment.newBuilder().build();
        BeanUtils.copyProperties(payment,retryPayment);
        PaymentResponse paymentResponse = PaymentResponse.newBuilder().setPayment(retryPayment)
                .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder().setCode(com.github.conanchen.gedit.common.grpc.Status.Code.OK).setDetails("success").build()).build();
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
