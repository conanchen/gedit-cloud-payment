package com.github.conanchen.gedit.payment.GrpcService;

import com.github.conanchen.gedit.store.profile.grpc.GetStoreRequest;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileApiGrpc;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileResponse;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by ZhouZeshao on 2018/1/15.
 */
@Component
public class StoreService {

    private StoreProfileApiGrpc.StoreProfileApiBlockingStub profileApiStub;

    @PostConstruct
    public void initRequest(){
        Channel channel =  ManagedChannelBuilder.forAddress("118.178.237.46", 9981).usePlaintext(true).build();
        profileApiStub = StoreProfileApiGrpc.newBlockingStub(channel);
    }

    public StoreProfileResponse getStoreProfile(String storeUuid, Metadata header){
        GetStoreRequest getStoreRequest = GetStoreRequest.newBuilder().setUuid(storeUuid).build();
        MetadataUtils.attachHeaders(profileApiStub, header);
        StoreProfileResponse response = profileApiStub.get(getStoreRequest);
        return response;
    }

}
