// Copyright (c) 2025 AccelByte Inc. All Rights Reserved.
// This is licensed software from AccelByte Inc, for limitations
// and restrictions contact your company contract manager.

package net.accelbyte.session.sessiondsm.service.demo;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import net.accelbyte.sdk.api.session.wrappers.GameSession;
import net.accelbyte.sdk.core.AccelByteSDK;
import net.accelbyte.sdk.api.session.models.ApimodelsUpdateGamesessionDSInformationRequest;
import net.accelbyte.sdk.api.session.operations.game_session.AdminUpdateDSInformation;
import net.accelbyte.session.sessiondsm.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

@Slf4j
@GRpcService
@Profile("DEMO")
public class SessionDsm extends SessionDsmGrpc.SessionDsmImplBase {

    private AccelByteSDK sdk;

    @Autowired
    public SessionDsm(AccelByteSDK sdk) {
        this.sdk = sdk;

        this.sdk.loginClient();

        log.info("SessionDsm DEMO service initialized");
    }

    @Override
    public void createGameSession(RequestCreateGameSession request,
            StreamObserver<ResponseCreateGameSession> responseObserver) {
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
                .setIp("10.10.10.11")
                .setPort(8080)
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
    public void terminateGameSession(RequestTerminateGameSession request,
            StreamObserver<ResponseTerminateGameSession> responseObserver) {
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

    @Override
    public void createGameSessionAsync(RequestCreateGameSession request,
            StreamObserver<ResponseCreateGameSessionAsync> responseObserver) {
        ResponseCreateGameSessionAsync response = ResponseCreateGameSessionAsync.newBuilder()
                .setSuccess(true)
                .setMessage("success")
                .build();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    GameSession wrapper = new GameSession(sdk);
                    AdminUpdateDSInformation operation = AdminUpdateDSInformation
                            .builder()
                            .namespace(request.getNamespace())
                            .sessionId(request.getSessionId())
                            .body(ApimodelsUpdateGamesessionDSInformationRequest.builder()
                                    .status("AVAILABLE")
                                    .port(1223)
                                    .serverId("123455")
                                    .ip("192.168.1.1")
                                    .description("testing")
                                    .build())
                            .build();

                    wrapper.adminUpdateDSInformation(operation);
                } catch (Exception e) {
                    log.error("Error updating DS information", e);
                }
            }
        });
        thread.start();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}