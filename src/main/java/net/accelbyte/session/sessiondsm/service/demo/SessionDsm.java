// Copyright (c) 2025 AccelByte Inc. All Rights Reserved.
// This is licensed software from AccelByte Inc, for limitations
// and restrictions contact your company contract manager.

package net.accelbyte.session.sessiondsm.service.demo;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import net.accelbyte.session.sessiondsm.*;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@GRpcService
@Profile("DEMO")
public class SessionDsm extends SessionDsmGrpc.SessionDsmImplBase {

    private static final String DEFAULT_IP = "10.10.10.11";
    private static final int DEFAULT_PORT = 8080;

    @Override
    public void createGameSession(RequestCreateGameSession request, StreamObserver<ResponseCreateGameSession> responseObserver) {
        if (request.getRequestedRegionList().isEmpty()) {
            log.error("Please provide requested region.");
        }

        log.info("createGameSession in DEMO");

        String selectedRegion = request.getRequestedRegion(0);

        ResponseCreateGameSession response = ResponseCreateGameSession.newBuilder()
            .setSessionId(request.getSessionId())
            .setNamespace(request.getNamespace())
            .setDeployment(request.getDeployment())
            .setSessionData(request.getSessionData())
            .setStatus("READY")
            .setIp(DEFAULT_IP)
            .setPort(DEFAULT_PORT)
            .setServerId("demo-local-" + request.getSessionId())
            .setSource("DEMO")
            .setRegion(selectedRegion)
            .setClientVersion(request.getClientVersion())
            .setGameMode(request.getGameMode())
            .setCreatedRegion(selectedRegion)
            .build();

        log.info("successful on server side");

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void terminateGameSession(RequestTerminateGameSession request, StreamObserver<ResponseTerminateGameSession> responseObserver) {
        log.info("terminateGameSession in DEMO");
        ResponseTerminateGameSession response = ResponseTerminateGameSession.newBuilder()
            .setNamespace(request.getNamespace())
            .setReason("")
            .setSessionId(request.getSessionId())
            .setSuccess(true)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}