/*
 * Copyright (c) 2025 AccelByte Inc. All Rights Reserved
 * This is licensed software from AccelByte Inc, for limitations
 * and restrictions contact your company contract manager.
 */
package net.accelbyte.session.sessiondsm.config;

import net.accelbyte.sdk.core.repository.DefaultConfigRepository;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class AppConfigRepository extends DefaultConfigRepository {

    private String baseUrl = "";
    private String clientId = "";
    private String clientSecret = "";
    private String appName = "";
    private String traceIdVersion = "";
    private String namespace = "";
    private boolean enableTraceId = false;
    private boolean enableUserAgentInfo = false;
    private String resourceName = "";
    private String dsProvider = "DEMO";

    private String gcpServiceAccountFile = "";
    private String gcpProjectId = "";
    private String gcpMachineType = "";
    private String gcpNetworkName = "";
    private String gcpRepositoryName = "";
    private int gcpRetry = 3;
    private int gcpWaitInterval = 1; // in seconds
    private int gcpImageOpenPort = 8080;

    private String awsAccessKeyId = "";
    private String awsSecretAccessKey = "";
    private String awsRegion = "";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getTraceIdVersion() { return traceIdVersion; }
    public void setTraceIdVersion(String traceIdVersion) { this.traceIdVersion = traceIdVersion; }

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }

    public boolean isEnableTraceId() { return enableTraceId; }
    public void setEnableTraceId(boolean enableTraceId) { this.enableTraceId = enableTraceId; }

    public boolean isEnableUserAgentInfo() { return enableUserAgentInfo; }
    public void setEnableUserAgentInfo(boolean enableUserAgentInfo) { this.enableUserAgentInfo = enableUserAgentInfo; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getGcpServiceAccountFile() { return gcpServiceAccountFile; }
    public void setGcpServiceAccountFile(String gcpServiceAccountFile) { this.gcpServiceAccountFile = gcpServiceAccountFile; }

    public String getGcpProjectId() { return gcpProjectId; }
    public void setGcpProjectId(String gcpProjectId) { this.gcpProjectId = gcpProjectId; }

    public String getGcpMachineType() { return gcpMachineType; }
    public void setGcpMachineType(String gcpMachineType) { this.gcpMachineType = gcpMachineType; }

    public String getGcpNetworkName() { return gcpNetworkName; }
    public void setGcpNetworkName(String gcpNetworkName) { this.gcpNetworkName = gcpNetworkName; }

    public String getGcpRepositoryName() { return gcpRepositoryName; }
    public void setGcpRepositoryName(String gcpRepositoryName) { this.gcpRepositoryName = gcpRepositoryName; }

    public int getGcpRetry() { return gcpRetry; }
    public void setGcpRetry(int gcpRetry) { this.gcpRetry = gcpRetry; }

    public int getGcpWaitInterval() { return gcpWaitInterval; }
    public void setGcpWaitInterval(int gcpWaitInterval) { this.gcpWaitInterval = gcpWaitInterval; }

    public int getGcpImageOpenPort() { return gcpImageOpenPort; }
    public void setGcpImageOpenPort(int gcpImageOpenPort) { this.gcpImageOpenPort = gcpImageOpenPort; }

    public String getAwsAccessKeyId() { return awsAccessKeyId; }
    public void setAwsAccessKeyId(String awsAccessKeyId) { this.awsAccessKeyId = awsAccessKeyId; }

    public String getAwsSecretAccessKey() { return awsSecretAccessKey; }
    public void setAwsSecretAccessKey(String awsSecretAccessKey) { this.awsSecretAccessKey = awsSecretAccessKey; }

    public String getAwsRegion() { return awsRegion; }
    public void setAwsRegion(String awsRegion) { this.awsRegion = awsRegion; }

    public void readEnvironmentVariables() {
        baseUrl = getEnv("AB_BASE_URL", baseUrl);
        clientId = getEnv("AB_CLIENT_ID", clientId);
        clientSecret = getEnv("AB_CLIENT_SECRET", clientSecret);
        namespace = getEnv("AB_NAMESPACE", namespace);

        dsProvider = getEnv("DS_PROVIDER", dsProvider);

        resourceName = getEnv("APP_RESOURCE_NAME", "SESSIONDSMGRPCSERVICE");
        gcpServiceAccountFile = getEnv("GCP_SERVICE_ACCOUNT_FILE", gcpServiceAccountFile);
        gcpProjectId = getEnv("GCP_PROJECT_ID", gcpProjectId);
        gcpMachineType = getEnv("GCP_MACHINE_TYPE", "e2-micro");
        gcpNetworkName = getEnv("GCP_NETWORK", "public");
        gcpRepositoryName = getEnv("GCP_REPOSITORY", gcpRepositoryName);
        gcpRetry = Integer.parseInt(getEnv("GCP_RETRY", String.valueOf(gcpRetry)));
        gcpWaitInterval = Integer.parseInt(getEnv("GCP_WAIT_GET_IP", String.valueOf(gcpWaitInterval)));
        gcpImageOpenPort = Integer.parseInt(getEnv("GCP_IMAGE_OPEN_PORT", String.valueOf(gcpImageOpenPort)));

        awsAccessKeyId = getEnv("AWS_ACCESS_KEY_ID", awsAccessKeyId);
        awsSecretAccessKey = getEnv("AWS_SECRET_ACCESS_KEY", awsSecretAccessKey);
        awsRegion = getEnv("GAMELIFT_REGION", getEnv("AWS_REGION", awsRegion));
    }

    private String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }
}
