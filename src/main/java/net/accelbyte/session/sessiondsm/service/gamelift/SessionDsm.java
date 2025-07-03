// Copyright (c) 2025 AccelByte Inc. All Rights Reserved.
// This is licensed software from AccelByte Inc, for limitations
// and restrictions contact your company contract manager.

package net.accelbyte.session.sessiondsm.service.gamelift;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.gamelift.AmazonGameLift;
import com.amazonaws.services.gamelift.AmazonGameLiftClientBuilder;
import com.amazonaws.services.gamelift.model.CreateGameSessionRequest;
import com.amazonaws.services.gamelift.model.CreateGameSessionResult;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import net.accelbyte.sdk.api.session.models.ApimodelsUpdateGamesessionDSInformationRequest;
import net.accelbyte.sdk.api.session.operations.game_session.AdminUpdateDSInformation;
import net.accelbyte.sdk.api.session.wrappers.GameSession;
import net.accelbyte.sdk.core.AccelByteSDK;
import net.accelbyte.session.sessiondsm.*;
import net.accelbyte.session.sessiondsm.model.ServerStatusType;
import net.accelbyte.session.sessiondsm.model.ServerServiceType;
import net.accelbyte.session.sessiondsm.config.AppConfigRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GRpcService
@Profile("GAMELIFT")
public class SessionDsm extends SessionDsmGrpc.SessionDsmImplBase {

    private AccelByteSDK sdk;

    private final AmazonGameLift gameLiftClient;

    private final AppConfigRepository config = new AppConfigRepository();

    @Autowired
    public SessionDsm(AccelByteSDK sdk) {
        this.sdk = sdk;

        this.sdk.loginClient();

        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                config.getAwsAccessKeyId(),
                config.getAwsSecretAccessKey());

        this.gameLiftClient = AmazonGameLiftClientBuilder.standard()
                .withRegion(config.getAwsRegion())
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        log.info("SessionDsm GAMELIFT service initialized");
    }

    @Override
    public void createGameSession(RequestCreateGameSession request,
            StreamObserver<ResponseCreateGameSession> responseObserver) {
        if (request.getRequestedRegionList().isEmpty()) {
            log.info("Please provide requested region.");
        }

        log.info("createGameSession in GAMELIFT");

        String selectedRegion = request.getRequestedRegion(0);

        try {
            CreateGameSessionRequest cgsRequest = new CreateGameSessionRequest()
                    .withAliasId(request.getDeployment())
                    .withGameSessionData(request.getSessionData())
                    .withIdempotencyToken(request.getSessionId())
                    .withMaximumPlayerSessionCount((int) request.getMaximumPlayer())
                    .withLocation(selectedRegion);

            CreateGameSessionResult cgsResponse = gameLiftClient.createGameSession(cgsRequest);

            if (cgsResponse == null || cgsResponse.getGameSession() == null) {
                throw new RuntimeException("CreateGameSession response is NULL");
            }

            ResponseCreateGameSession response = ResponseCreateGameSession.newBuilder()
                    .setSessionId(request.getSessionId())
                    .setNamespace(request.getNamespace())
                    .setDeployment(cgsResponse.getGameSession().getFleetId())
                    .setSessionData(request.getSessionData())
                    .setStatus(ServerStatusType.READY)
                    .setIp(cgsResponse.getGameSession().getIpAddress())
                    .setPort(cgsResponse.getGameSession().getPort())
                    .setServerId(cgsResponse.getGameSession().getGameSessionId())
                    .setSource(ServerServiceType.GAMELIFT)
                    .setRegion(selectedRegion)
                    .setClientVersion(request.getClientVersion())
                    .setGameMode(request.getGameMode())
                    .setCreatedRegion(cgsResponse.getGameSession().getLocation())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error creating GameLift session: ", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void terminateGameSession(RequestTerminateGameSession request,
            StreamObserver<ResponseTerminateGameSession> responseObserver) {
        log.info("createGameSession in GAMELIFT");
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
