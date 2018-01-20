package com.github.conanchen.gedit.payment.store;

import com.github.conanchen.gedit.store.profile.grpc.GetStoreRequest;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileApiGrpc;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileResponse;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.lognet.springboot.grpc.GRpcService;

/**
 * Created by ZhouZeshao on 2018/1/15.
 */
@GRpcService
public class StoreService {

    public StoreProfileResponse getStoreProfile(String storeUuid){
        GetStoreRequest getStoreRequest = GetStoreRequest.newBuilder().setUuid(storeUuid).build();
        Channel channel =  ManagedChannelBuilder.forAddress("localhost", 8980).usePlaintext(true).build();
        StoreProfileApiGrpc.StoreProfileApiBlockingStub profileApiStub = StoreProfileApiGrpc.newBlockingStub(channel);
        StoreProfileResponse response = profileApiStub.get(getStoreRequest);
        return response;
    }

}
