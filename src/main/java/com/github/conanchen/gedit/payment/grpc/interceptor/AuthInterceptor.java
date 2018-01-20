package com.github.conanchen.gedit.payment.grpc.interceptor;

import com.google.gson.Gson;
import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.crypto.MacProvider;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.core.annotation.Order;

import javax.crypto.SecretKey;

/**
 * Interceptor that validates user's identity.
 */
@Slf4j
@Order(20)
@GRpcGlobalInterceptor
public class AuthInterceptor implements ServerInterceptor {
    private final static Gson gson = new Gson();

    public static final Context.Key<Claims> USER_CLAIMS
            = Context.key("identity"); // "identity" is just for debugging
    private static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("authorization",
            Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<byte[]> EXTRA_AUTHORIZATION = Metadata.Key.of(
            "Extra-Authorization-bin", Metadata.BINARY_BYTE_MARSHALLER);
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // You need to implement validateIdentity
        Claims identity = validateIdentity(headers);
        // TODO expiration check
        if (identity == null) { // this is optional, depending on your needs
            // Assume user not authenticated
            call.close(Status.UNAUTHENTICATED.withDescription("authorization failed"),
                    new Metadata());
            return new ServerCall.Listener() {
            };
        }
        Context context = Context.current().withValue(USER_CLAIMS, identity);
        return Contexts.interceptCall(context, call, headers, next);
    }

    private Claims validateIdentity(Metadata headers) {
        String authorizationHeader = headers.get(AUTHORIZATION);
        if (authorizationHeader != null) {
            // Extract the token from the Authorization header
            String accessToken = authorizationHeader
                    .substring(AUTHENTICATION_SCHEME.length()).trim();

//            log.info(String.format("authorization=%s, accessToken=%s", authorizationHeader, accessToken));

            SecretKey secretKey = MacProvider.generateKey();
            Jwt<Header, Claims> claimsJwt = Jwts.parser().parseClaimsJwt(accessToken);
//            log.info(String.format("secretKey.algorithm=%s,secretKey.format=%s,secretKey.encoded=%s,jwt1.id=%s",
//                    secretKey.getAlgorithm(),
//                    secretKey.getFormat(), new String(secretKey.getEncoded()), claimsJwt.getBody().getId()));
            return claimsJwt.getBody();

        }

        return null;
    }
}