package com.github.conanchen.gedit.hello.grpc;

import com.google.gson.Gson;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by conanchen on 9/7/16.
 */
@Component
public class LogInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);
    private static final Gson gson = new Gson();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {

        log.info(String.format(
                "call.getMethodDescriptor().getFullMethodName()=[%s]\n headers.keys=[%s],\n remote=%s",
                call.getMethodDescriptor().getFullMethodName(),
                "-" + gson.toJson(headers.keys()),
                call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR)
                )
        );
        //TODO: https://stackoverflow.com/questions/40112374/how-do-i-access-request-metadata-for-a-java-grpc-service-i-am-defining
        return next.startCall(call, headers);
    }
}
