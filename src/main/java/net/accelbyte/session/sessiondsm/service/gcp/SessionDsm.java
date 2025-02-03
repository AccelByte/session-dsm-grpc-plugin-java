// Copyright (c) 2025 AccelByte Inc. All Rights Reserved.
// This is licensed software from AccelByte Inc, for limitations
// and restrictions contact your company contract manager.

import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.*;
import com.google.common.collect.ImmutableMap;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.context.annotation.Profile;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import net.accelbyte.session.sessiondsm.*;
import net.accelbyte.session.sessiondsm.model.ServerStatusType;
import net.accelbyte.session.sessiondsm.model.ServerServiceType;
import net.accelbyte.session.sessiondsm.config.AppConfigRepository;

@Slf4j
@GRpcService
@Profile("GCP")
public class SessionDsm extends SessionDsmGrpc.SessionDsmImplBase {

    private final AppConfigRepository config = new AppConfigRepository();
    private final InstancesClient instancesClient;
    private final Map<String, String> awsToGcpRegionMap;
    private final Map<String, List<String>> gcpZones;

    public SessionDsm() throws IOException {
        this.instancesClient = InstancesClient.create(
            InstancesSettings.newBuilder()
                .setCredentialsProvider(() -> GoogleCredentials.fromStream(new FileInputStream(config.getGcpServiceAccountFile())))
                .build()
        );

        awsToGcpRegionMap = ImmutableMap.<String, String>builder()
            .put("us-east-1", "us-east1")
            .put("us-east-2", "us-east4")
            .put("us-west-1", "us-west1")
            .put("us-west-2", "us-west2")
            .put("ca-central-1", "northamerica-northeast1")
            .put("sa-east-1", "southamerica-east1")
            .put("eu-central-1", "europe-west3")
            .put("eu-west-1", "europe-west1")
            .put("eu-west-2", "europe-west2")
            .put("eu-west-3", "europe-west9")
            .put("eu-north-1", "europe-north1")
            .put("me-south-1", "me-west1")
            .put("af-south-1", "africa-north1")
            .put("ap-east-1", "asia-east2")
            .put("ap-south-1", "asia-south1")
            .put("ap-northeast-3", "asia-northeast2")
            .put("ap-northeast-2", "asia-northeast3")
            .put("ap-southeast-1", "asia-southeast1")
            .put("ap-southeast-2", "australia-southeast1")
            .put("ap-northeast-1", "asia-northeast1")
            .build();

        gcpZones = new HashMap<>();
        gcpZones.put("us-east1", Arrays.asList("us-east1-b", "us-east1-c", "us-east1-d"));
        gcpZones.put("us-east4", Arrays.asList("us-east4-a", "us-east4-b", "us-east4-c"));
    }

    @Override
    public void createGameSession(RequestCreateGameSession request, StreamObserver<ResponseCreateGameSession> responseObserver) {
        if (request.getRequestedRegionList().isEmpty()) {
            log.error("Please provide requested region.");
        }

        log.info("createGameSession in GCP");

        String selectedRegion = request.getRequestedRegion(0);
        String gcpRegion = awsToGcpRegionMap.get(selectedRegion);
        if (gcpRegion == null) {
            log.error("Unknown AWS region: " + selectedRegion);
        }

        List<String> zones = gcpZones.get(gcpRegion);
        if (zones == null || zones.isEmpty()) {
            log.error("Unknown AWS region: " + selectedRegion);
        }

        String gcpZone = zones.get(ThreadLocalRandom.current().nextInt(zones.size()));
        String instanceName = request.getNamespace() + "-" + request.getSessionId();
        String projectId = config.getGcpProjectId();

        Instance instance = Instance.newBuilder()
            .setName(instanceName)
            .setMachineType(String.format("zones/%s/machineTypes/%s", gcpZone, config.getGcpMachineType()))

            .build();

        try {
            OperationFuture<Operation, Operation> operation = instancesClient.insertAsync(projectId, gcpZone, instance);
            operation.get(300, TimeUnit.SECONDS);

            Instance fetchedInstance = instancesClient.get(projectId, gcpZone, instanceName);
            String externalIp = fetchedInstance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();

            ResponseCreateGameSession response = ResponseCreateGameSession.newBuilder()
                .setSessionId(request.getSessionId())
                .setNamespace(request.getNamespace())
                .setDeployment(request.getDeployment())
                .setSessionData(request.getSessionData())
                .setStatus(ServerStatusType.READY)
                .setIp(externalIp)
                .setPort(config.getGcpImageOpenPort())
                .setServerId(instanceName)
                .setSource(ServerServiceType.GCP)
                .setRegion(selectedRegion)
                .setClientVersion(request.getClientVersion())
                .setGameMode(request.getGameMode())
                .setCreatedRegion(gcpZone)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to create game session: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription("Failed to create instance: " + e.getMessage())));
        }
    }

    @Override
    public void terminateGameSession(RequestTerminateGameSession request, StreamObserver<ResponseTerminateGameSession> responseObserver) {
        log.info("createGameSession in GCP");
        String instanceName = request.getNamespace() + "-" + request.getSessionId();
        try {
            OperationFuture<Operation, Operation> operation = instancesClient.deleteAsync(config.getGcpProjectId(), request.getZone(), instanceName);
            operation.get(300, TimeUnit.SECONDS);

            ResponseTerminateGameSession response = ResponseTerminateGameSession.newBuilder()
                .setNamespace(request.getNamespace())
                .setReason("Instance terminated successfully")
                .setSessionId(request.getSessionId())
                .setSuccess(true)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to terminate instance: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription("Failed to terminate instance: " + e.getMessage())));
        }
    }
}
