package com.github.conanchen.gedit.payment.GrpcService;


import com.github.conanchen.gedit.store.profile.grpc.StoreProfileApiGrpc;
import com.github.conanchen.gedit.user.profile.grpc.GetRequest;
import com.github.conanchen.gedit.user.profile.grpc.UserProfileApiGrpc;
import com.github.conanchen.gedit.user.profile.grpc.UserProfileResponse;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UserService {


    private UserProfileApiGrpc.UserProfileApiBlockingStub profileApiStub;

    @PostConstruct
    public void initRequest(){
        Channel channel =  ManagedChannelBuilder.forAddress("118.178.237.46", 9980).usePlaintext(true).build();
        profileApiStub = UserProfileApiGrpc.newBlockingStub(channel);
    }

    public UserProfileResponse getUser(String userId, Metadata header){
        profileApiStub = MetadataUtils.attachHeaders(profileApiStub,header);
        GetRequest getStoreRequest = GetRequest.newBuilder().setUuid(userId).build();
        UserProfileResponse response = profileApiStub.get(getStoreRequest);
        return response;

    }
}
