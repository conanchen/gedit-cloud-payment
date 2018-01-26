package com.github.conanchen.gedit.payment.grpc.interceptor;

import com.google.gson.Gson;
import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;

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
    public static final Context.Key<Metadata> HEADERS
            = Context.key("HEADERS");
    private static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("authorization",
            Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<byte[]> EXTRA_AUTHORIZATION = Metadata.Key.of(
            "Extra-Authorization-bin", Metadata.BINARY_BYTE_MARSHALLER);
    public static final String AUTHENTICATION_SCHEME = "Bearer";
    @Value("${jjwt.sigin.key:shuai}")
    private String signinKey;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        Claims identity = null;
        try {
            // You need to implement validateIdentity
            identity = validateIdentity(headers);
        }catch (JwtException e){
            //ignore
            log.error("parse token occur an exception:",e);
        }
        if (identity == null) { // this is optional, depending on your needs
            // Assume user not authenticated
            call.close(Status.UNAUTHENTICATED.withDescription("authorization failed"),
                    new Metadata());
            return new ServerCall.Listener() {
            };
        }
        Context context = Context.current().withValues(USER_CLAIMS, identity,HEADERS, headers);
        return Contexts.interceptCall(context, call, headers, next);
    }

    private Claims validateIdentity(Metadata headers) {
        String authorizationHeader = headers.get(AUTHORIZATION);
        if (authorizationHeader != null) {
            // Extract the token from the Authorization header
            String accessToken = authorizationHeader
                    .substring(AUTHENTICATION_SCHEME.length() + 1).trim();

            log.info(String.format("authorization=%s, accessToken=%s", authorizationHeader, accessToken));

            Jws<Claims> claimsJwt = Jwts.parser().setSigningKey(signinKey).parseClaimsJws(accessToken);
            log.info(String.format("signinKey=%s,jwt1.id=%s",signinKey, claimsJwt.getBody().getId()));
            return claimsJwt.getBody();
        }

        return null;
    }
}