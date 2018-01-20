package com.github.conanchen.gedit.payment.validate;

import com.github.conanchen.gedit.payment.grpc.CreatePaymentRequest;
import com.github.conanchen.gedit.payment.grpc.PaymentResponse;
import com.github.conanchen.gedit.payment.model.Points;
import com.github.conanchen.gedit.payment.repository.PointsRepository;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by ZhouZeshao on 2018/1/15.
 */
public class PaymentValidate {

    @Autowired
    private PointsRepository pointsRepository;


    public void CreateValidate(CreatePaymentRequest request, StreamObserver<PaymentResponse> responseObserver, Points points){
        if(points.getUsable() < request.getPointsPay()){
            responseObserver.onNext(PaymentResponse.newBuilder()
                    .setStatus(com.github.conanchen.gedit.common.grpc.Status.newBuilder()
                            .setCode(String.valueOf(Status.OUT_OF_RANGE.getCode().value()))
                            .setDetails("积分余额不足")).build());
            return;
        }
        //继续查询店铺,并验证店铺的存在与否

    }
}
