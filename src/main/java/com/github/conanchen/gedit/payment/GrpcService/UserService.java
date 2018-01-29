package com.github.conanchen.gedit.payment.GrpcService;


import com.github.conanchen.gedit.user.profile.grpc.GetMyProfileRequest;
import com.github.conanchen.gedit.user.profile.grpc.GetRequest;
import com.github.conanchen.gedit.user.profile.grpc.UserProfileApiGrpc;
import com.github.conanchen.gedit.user.profile.grpc.UserProfileResponse;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class UserService {


    private UserProfileApiGrpc.UserProfileApiBlockingStub profileApiStub;

    @PostConstruct
    public void initRequest(){
        Channel channel =  ManagedChannelBuilder.forAddress("118.178.237.46", 9980).usePlaintext(true).build();
        profileApiStub = UserProfileApiGrpc.newBlockingStub(channel);
    }

    public UserProfileResponse getUser(Metadata header){
        profileApiStub = MetadataUtils.attachHeaders(profileApiStub,header);
        log.info("getUser header:{}",header.toString());
        GetMyProfileRequest getMyProfileRequest = GetMyProfileRequest.newBuilder().build();
        UserProfileResponse response = profileApiStub.getMyProfile(getMyProfileRequest);
        log.info("getUser response:{}",response.toString());
        return response;
    }
}
