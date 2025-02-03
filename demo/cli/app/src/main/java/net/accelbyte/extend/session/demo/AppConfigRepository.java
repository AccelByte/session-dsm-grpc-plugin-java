/*
 * Copyright (c) 2025 AccelByte Inc. All Rights Reserved
 * This is licensed software from AccelByte Inc, for limitations
 * and restrictions contact your company contract manager.
 */
package net.accelbyte.extend.session.demo;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import net.accelbyte.sdk.core.repository.DefaultConfigRepository;

public class AppConfigRepository extends DefaultConfigRepository {

    @Option(names = {"-b","--baseurl"}, description = "AGS base URL", defaultValue = "")
    private String abBaseUrl;

    @Option(names = {"-c","--client"}, description = "AGS client id", defaultValue = "")
    private String abClientId;

    @Option(names = {"-s","--secret"}, description = "AGS client secret", defaultValue = "")
    private String abClientSecret;

    @Option(names = {"-n","--namespace"}, description = "AGS namespace", defaultValue = "")
    private String abNamespace;

    @Option(names = {"-u","--username"}, description = "AGS Username", defaultValue = "")
    private String abUsername;

    @Option(names = {"-p","--password"}, description = "AGS User's password", defaultValue = "")
    private String abPassword;

    @Option(names = {"-g","--grpc-target"}, description = "Grpc plugin target server url.", defaultValue = "")
    private String grpcServerUrl;

    @Option(names = {"-a","--extend-app"}, description = "Extend app name.", defaultValue = "")
    private String extendAppName;

    @Option(names = {"-w","--ds-wait-interval"}, description = "Wait interval (in ms) between session check for DS status.", defaultValue = "500")
    private String dsWaitInterval;

    @Option(names = {"-o","--ds-check-count"}, description = "How many times app need to check session data for DS status.", defaultValue = "10")
    private String dsCheckCount;

    @Override
    public String getClientId() {
        if (!abClientId.equals(""))
            return abClientId;
        else
            return super.getClientId();
    }

    @Override
    public String getClientSecret() {
        if (!abClientSecret.equals(""))
            return abClientSecret;
        else
            return super.getClientSecret();
    }

    @Override
    public String getBaseURL() {
        if (!abBaseUrl.equals(""))
            return abBaseUrl;
        else
            return super.getBaseURL();
    }

    public String getNamespace() throws Exception {
        if (abNamespace.equals("")) {
            abNamespace = System.getenv("AB_NAMESPACE");
            if (abNamespace == null) {
                throw new Exception("Namespace is required.");
            }
        }
        return abNamespace;
    }

    public String getGrpcServerUrl() {
        if (grpcServerUrl.equals("")) {
            grpcServerUrl = System.getenv("AB_GRPC_SERVER_URL");
            if (grpcServerUrl == null)
                grpcServerUrl = "";
        }
        return grpcServerUrl;
    }

    public String getExtendAppName() {
        if (extendAppName.equals("")) {
            extendAppName = System.getenv("AB_EXTEND_APP_NAME");
            if (extendAppName == null)
                extendAppName = "";
        }
        return extendAppName;
    }

    public String getUsername() throws Exception {
        if (abUsername.equals("")) {
            abUsername = System.getenv("AB_USERNAME");
            if (abUsername == null) {
                throw new Exception("Username is not specified.");
            }
        }
        return abUsername;
    }

    public String getDsWaitInterval() throws Exception {
        if (dsWaitInterval == null) {
            dsWaitInterval = System.getenv("DS_WAITING_INTERVAL");
            return "500";
        }
        return dsWaitInterval;
    }

    public String getDsCheckCount() throws Exception {
        if (dsCheckCount == null) {
            dsCheckCount = System.getenv("DS_CHECK_COUNT");
            return "10";
        }
        return dsCheckCount;
    }

    public String getPassword() throws Exception {
        if (abPassword.equals("")) {
            abPassword = System.getenv("AB_PASSWORD");
            if (abPassword == null) {
                throw new Exception("User's password is not specified.");
            }
        }
        return abPassword;
    }
}
