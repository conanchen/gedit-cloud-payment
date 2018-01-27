package com.github.conanchen.gedit.payment.GrpcService;


import com.github.conanchen.gedit.accounting.account.grpc.AccountingAccountApiGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AccountingService {

    private AccountingAccountApiGrpc.AccountingAccountApiBlockingStub accountApiBlockingStub;

    @PostConstruct
    public void initRequest(){
        Channel channel =  ManagedChannelBuilder.forAddress("118.178.237.46", 9981).usePlaintext(true).build();
        accountApiBlockingStub = AccountingAccountApiGrpc.newBlockingStub(channel);
    }

}
