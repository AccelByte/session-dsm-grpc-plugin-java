/*
 * Copyright (c) 2025 AccelByte Inc. All Rights Reserved
 * This is licensed software from AccelByte Inc, for limitations
 * and restrictions contact your company contract manager.
 */
package net.accelbyte.extend.session.demo;

import net.accelbyte.sdk.api.session.operations.game_session.CreateGameSession;
import net.accelbyte.sdk.api.session.operations.game_session.GetGameSession;
import net.accelbyte.sdk.api.session.operations.game_session.DeleteGameSession;
import net.accelbyte.sdk.api.session.operations.configuration_template.AdminCreateConfigurationTemplateV1;
import net.accelbyte.sdk.api.session.operations.configuration_template.AdminDeleteConfigurationTemplateV1;
import net.accelbyte.sdk.api.session.models.*;
import net.accelbyte.sdk.api.session.models.ApimodelsCreateGameSessionRequest;
import net.accelbyte.sdk.api.session.models.ApimodelsGameSessionResponse;
import net.accelbyte.sdk.api.session.models.ApimodelsCreateConfigurationTemplateRequest;
import net.accelbyte.sdk.api.session.wrappers.*;
import net.accelbyte.sdk.api.session.wrappers.GameSession;
import net.accelbyte.sdk.api.session.wrappers.ConfigurationTemplate;
import net.accelbyte.sdk.core.AccelByteSDK;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SessionDataUnit {
    private final AccelByteSDK abSdk;

    private final AppConfigRepository config;

    private final String prefix = "session_dsm_grpc_java_";

    private final String configName = prefix + getRandomString("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",3);

    private final String abNamespace;

    private String sessionId;

    public SessionDataUnit(AccelByteSDK sdk, AppConfigRepository configRepo) throws Exception {
        abSdk = sdk;
        config = configRepo;
        abNamespace = configRepo.getNamespace();
    }

    protected String getRandomString(String characters, int length) {
        final Random random = new Random();
        final char[] result = new char[length];
        for (int i = 0; i < result.length; i++) {
            while (true) {
                result[i] = characters.charAt(random.nextInt(characters.length()));
                if (i > 0 && result[i - 1] == result[i])
                    continue;
                else break;
            }
        }
        return new String(result);
    }

    public void createGameSession(String userId) throws Exception {
        if (userId.equals(""))
            throw new Exception("userId is empty.");

        List<String> userIDs = Arrays.asList(userId);
        GameSession gameSessionWrapper = new GameSession(abSdk);
        final ApimodelsGameSessionResponse createGameSession = gameSessionWrapper.createGameSession(CreateGameSession.builder()
                .namespace(abNamespace)
                .body(ApimodelsCreateGameSessionRequest.builder()
                        .configurationName(configName)
                        .teams(Collections.singletonList(ModelsTeam.builder()
                                .parties(Collections.singletonList(ModelsPartyMembers.builder()
                                        .partyID("")
                                        .userIDs(userIDs)
                                        .build()))
                                .userIDs(userIDs)
                                .build()))
                        .build())
                .build());

    this.sessionId = createGameSession.getId();

    }

    public void getGameSession() throws Exception {
        GameSession gameSessionWrapper = new GameSession(abSdk);

        boolean isDsAvailable = false;
        int dsChecks = 0;
        int maxDsChecks = Integer.parseInt(config.getDsCheckCount());
        double checkInterval = Double.parseDouble(config.getDsWaitInterval());

        while (dsChecks < maxDsChecks) {

            final ApimodelsGameSessionResponse getGameSession = gameSessionWrapper.getGameSession(GetGameSession.builder()
                    .namespace(abNamespace)
                    .sessionId(sessionId)
                    .build());

            if ("AVAILABLE".equals(getGameSession.getDsInformation().getStatusV2())) {
                isDsAvailable = true;
                System.out.println("DS is AVAILABLE");
                break;
            }

            Thread.sleep((long) (checkInterval * 10 * 1000)); // Convert seconds to milliseconds
            dsChecks++;
            System.out.printf("check %d/%d: DS not available yet. Retrying...%n", dsChecks, maxDsChecks);
        }

        if (!isDsAvailable) {
            throw new Exception(String.format("Dedicated Server is not available after maximum checks (%d)", maxDsChecks));
        }
    }

    public void setSessionServiceGrpcTarget() throws Exception {
        ConfigurationTemplate wrapper = new ConfigurationTemplate(abSdk);

        final String abGrpcServerUrl = config.getGrpcServerUrl();
        if (abGrpcServerUrl.equals(""))
        {
            final String abExtendAppName = config.getExtendAppName();
            if (abExtendAppName.equals(""))
                throw new Exception("Grpc Server Url or extend app name is not specified!");

                final ApimodelsCreateConfigurationTemplateRequest createConfigBody = ApimodelsCreateConfigurationTemplateRequest.builder()
                        .clientVersion("test")
                        .deployment("test")
                        .persistent(false)
                        .textChat(false)
                        .name(configName)
                        .minPlayers(0)
                        .maxPlayers(2)
                        .joinability("OPEN")
                        .inviteTimeout(60)
                        .inactiveTimeout(60)
                        .autoJoin(true)
                        .type("DS")
                        .dsSource("custom")
                        .dsManualSetReady(false)
                        .appName(abExtendAppName)
                        .requestedRegions(Arrays.asList("us-west-2"))
                        .build();

            wrapper.adminCreateConfigurationTemplateV1(AdminCreateConfigurationTemplateV1.builder()
                    .namespace(abNamespace)
                    .body(createConfigBody)
                    .build());
        } else {
            final ApimodelsCreateConfigurationTemplateRequest createConfigBody = ApimodelsCreateConfigurationTemplateRequest.builder()
                    .clientVersion("test")
                    .deployment("test")
                    .persistent(false)
                    .textChat(false)
                    .name(configName)
                    .minPlayers(0)
                    .maxPlayers(2)
                    .joinability("OPEN")
                    .inviteTimeout(60)
                    .inactiveTimeout(60)
                    .autoJoin(true)
                    .type("DS")
                    .dsSource("custom")
                    .dsManualSetReady(false)
                    .customURLGRPC(abGrpcServerUrl)
                    .requestedRegions(Arrays.asList("us-west-2"))
                    .build();

            wrapper.adminCreateConfigurationTemplateV1(AdminCreateConfigurationTemplateV1.builder()
                    .namespace(abNamespace)
                    .body(createConfigBody)
                    .build());
        }
    }

    public void unsetSessionServiceGrpcTarget() throws Exception {
        ConfigurationTemplate wrapper = new ConfigurationTemplate(abSdk);
        wrapper.adminDeleteConfigurationTemplateV1(AdminDeleteConfigurationTemplateV1.builder()
                .namespace(abNamespace)
                .name(configName)
                .build());
    }

    public void deleteGameSession() throws Exception {
        if (sessionId == null)
            return;

        GameSession gameSessionWrapper = new GameSession(abSdk);
        gameSessionWrapper.deleteGameSession(DeleteGameSession.builder()
                .namespace(abNamespace)
                .sessionId(sessionId)
                .build());
    }
}