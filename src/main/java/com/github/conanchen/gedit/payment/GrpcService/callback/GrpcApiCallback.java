package com.github.conanchen.gedit.payment.GrpcService.callback;

import com.github.conanchen.gedit.common.grpc.Status;

public interface GrpcApiCallback {
    void onGrpcApiError(Status status);

    void onGrpcApiCompleted();
}
