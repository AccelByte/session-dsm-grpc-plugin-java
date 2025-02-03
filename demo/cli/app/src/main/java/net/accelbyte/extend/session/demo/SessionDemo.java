/*
 * Copyright (c) 2025 AccelByte Inc. All Rights Reserved
 * This is licensed software from AccelByte Inc, for limitations
 * and restrictions contact your company contract manager.
 */
package net.accelbyte.extend.session.demo;

import net.accelbyte.sdk.api.iam.operations.users.PublicGetMyUserV3;
import net.accelbyte.sdk.api.iam.models.ModelUserResponseV3;
import net.accelbyte.sdk.api.iam.wrappers.Users;
import net.accelbyte.sdk.core.AccelByteConfig;
import net.accelbyte.sdk.core.AccelByteSDK;
import net.accelbyte.sdk.core.client.OkhttpClient;
import net.accelbyte.sdk.core.repository.DefaultConfigRepository;
import net.accelbyte.sdk.core.repository.DefaultTokenRepository;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import javax.swing.text.Style;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "SessionDSMGrpcPluginDemo",
        description = "Session DSM Function Grpc Plugin Demo for Session Service",
        mixinStandardHelpOptions = true
)
public class SessionDemo implements Callable<Integer> {

    @Mixin
    private AppConfigRepository configRepo;

    public Integer call() {
        int exitCode = 0;

        AccelByteSDK abSdk = null;
        try {
            AccelByteConfig abConfig = new AccelByteConfig(
                    new OkhttpClient(),
                    DefaultTokenRepository.getInstance(),
                    configRepo
            );

            abSdk = new AccelByteSDK(abConfig);

            System.out.println("Log in to Accelbyte...");
            System.out.println("\tBaseUrl: " + configRepo.getBaseURL());

            final String abUsername = configRepo.getUsername();
            final String abPassword = configRepo.getPassword();
            final boolean loginResult = abSdk.loginUser(abUsername, abPassword);
            if (!loginResult) {
                throw new Exception("Login failed!");
            }
            System.out.println("Login success!");

            Users userWrapper = new Users(abSdk);
            ModelUserResponseV3 userInfo = userWrapper.publicGetMyUserV3(PublicGetMyUserV3.builder().build());
            if (userInfo == null)
                throw new Exception("Could not retrieve login user info.");
            System.out.println("User: " + userInfo.getUserName());

            SessionDataUnit sdu = new SessionDataUnit(abSdk,configRepo);
            try {
                System.out.print("Configuring session service grpc target... ");
                sdu.setSessionServiceGrpcTarget();
                System.out.println("[OK]");

                System.out.print("Creating game session... ");
                sdu.createGameSession(userInfo.getUserId());
                System.out.println("[OK]");

                System.out.print("Waiting game session... ");
                sdu.getGameSession();
                System.out.println("[FOUND]");

                System.out.println("[OK]");

            } catch (Exception ix) {
                System.out.println("[FAILED] " + ix.getMessage());
            } finally {

                System.out.print("Deleting game session... ");
                sdu.deleteGameSession();
                System.out.println("[OK]");

                sdu.unsetSessionServiceGrpcTarget();
            }
        } catch (Exception x) {
            System.out.println("There are some error(s). " + x.getMessage());
            exitCode = 1;
        } finally {
            if (abSdk != null)
                abSdk.logout();
        }

        return exitCode;
    }
}
