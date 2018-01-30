package com.github.conanchen.gedit.payment.GrpcService;


import com.github.conanchen.gedit.accounting.account.grpc.AccountingAccountApiGrpc;
import com.github.conanchen.gedit.accounting.event.grpc.AccountingEventApiGrpc;
import com.github.conanchen.gedit.accounting.event.grpc.PaymentCreatedEvent;
import com.github.conanchen.gedit.accounting.journal.grpc.AccountingJournalApiGrpc;
import com.github.conanchen.gedit.accounting.journal.grpc.JournalResponse;
import com.github.conanchen.gedit.accounting.journal.grpc.UpsertJournalRequest;
import com.github.conanchen.gedit.accounting.rewardsif.grpc.AccountingRewardsIfEventApiGrpc;
import com.github.conanchen.gedit.accounting.rewardsif.grpc.IfPaymentCreatedEvent;
import com.github.conanchen.gedit.accounting.rewardsif.grpc.QueryRewardsIfEventRequest;
import com.github.conanchen.gedit.accounting.rewardsif.grpc.RewardIfEventResponse;
import com.github.conanchen.gedit.common.grpc.PaymentChannel;
import com.github.conanchen.gedit.common.grpc.Status;
import com.github.conanchen.gedit.payment.GrpcService.callback.GrpcApiCallback;
import com.github.conanchen.gedit.payment.model.Points;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AccountingService {

    private AccountingJournalApiGrpc.AccountingJournalApiStub accountApiStub;

    private AccountingRewardsIfEventApiGrpc.AccountingRewardsIfEventApiStub eventApiStub;

    private JournalResponse journalResponse = JournalResponse.newBuilder().build();

    @PostConstruct
    public void initRequest(){
        Channel channel =  ManagedChannelBuilder.forAddress("118.178.237.46", 9983).usePlaintext(true).build();
        accountApiStub = AccountingJournalApiGrpc.newStub(channel);
        eventApiStub = AccountingRewardsIfEventApiGrpc.newStub(channel);
    }

    public interface askRewardCallback{
        void onAccountResponse(List<RewardIfEventResponse> response);
    }

    public void askReward(String payerUuid,String payeeUuid,
                          String payeeStoreUuid,String payeeWorkerUuid,
                          int shouldPay,askRewardCallback callback,
                          GrpcApiCallback grpcApiCallback){
        QueryRewardsIfEventRequest.Builder request = QueryRewardsIfEventRequest.newBuilder();
        IfPaymentCreatedEvent.Builder createdEvent = IfPaymentCreatedEvent.newBuilder()
                .setPayeeUuid(payeeUuid)
                .setPayeeStoreUuid(payeeStoreUuid)
                .setPayeeWorkerUuid(payeeWorkerUuid)
                .setPayerUuid(payerUuid)
                .setShouldpay(shouldPay);
        request.setIfPaymentCreatedEvent(createdEvent).build();
        eventApiStub.queryRewardsIfEvent(request.build(), new StreamObserver<RewardIfEventResponse>() {
            @Override
            public void onNext(RewardIfEventResponse value) {
                List<RewardIfEventResponse> responseList = new ArrayList<>();
                callback.onAccountResponse(responseList);
            }

            @Override
            public void onError(Throwable t) {
                log.info("call queryRewardsIfEvent error,cause:{},message:{}",t.getCause(),t.getMessage());
                grpcApiCallback.onGrpcApiError(Status.newBuilder()
                        .setCode(Status.Code.OK)
                        .setDetails("系统繁忙,稍后重试")
                        .build());
            }

            @Override
            public void onCompleted() {
                grpcApiCallback.onGrpcApiCompleted();
            }
        });

    }

    public JournalResponse addJournal(int pointRepay,int pointsPay,String payerUuid,
                                      String payeeWorkerUuid,String payeeStoreUuid,String PayeeUuid,int actualPay,
                                      PaymentChannel channel,String paymentUuid){
        UpsertJournalRequest.Builder request = UpsertJournalRequest.newBuilder();
        PaymentCreatedEvent.Builder createdEvent = PaymentCreatedEvent.newBuilder();
        if(pointRepay == 0) {
            createdEvent.setPointsPay(pointsPay);
        }
        else {
            createdEvent.setPointsRepay(pointRepay);
        }
        createdEvent.setPayerUuid(payerUuid);
        createdEvent.setPayeeWorkerUuid(payeeWorkerUuid);
        createdEvent.setPayeeStoreUuid(payeeStoreUuid);
        createdEvent.setActualPay(actualPay);
        createdEvent.setChannel(channel.toString());
        createdEvent.setCreated(System.currentTimeMillis());
        createdEvent.setUuid(paymentUuid);
        createdEvent.setPayeeUuid(PayeeUuid);
        request.setPaymentCreatedEvent(createdEvent);
        accountApiStub.upsertJournal(request.build(), new StreamObserver<JournalResponse>() {
            @Override
            public void onNext(JournalResponse value) {
                journalResponse = value;
            }

            @Override
            public void onError(Throwable t) {
                log.info("call getJournal error,cause:{},error:{}",t.getCause(),t.getMessage());
            }

            @Override
            public void onCompleted() {
                return;
            }
        });
        return journalResponse;
    }



}
