package net.accelbyte.session.sessiondsm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.accelbyte.sdk.core.AccelByteSDK;
import net.accelbyte.sdk.core.client.OkhttpClient;
import net.accelbyte.sdk.core.repository.DefaultConfigRepository;
import net.accelbyte.sdk.core.repository.DefaultTokenRefreshRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {
    @Bean
    public AccelByteSDK accelbyteSdk() {
        final OkhttpClient okhttpClient = new OkhttpClient();
        final DefaultTokenRefreshRepository tokenRefreshRepository = new DefaultTokenRefreshRepository();
        final DefaultConfigRepository configRepository = new DefaultConfigRepository();

        configRepository.setLocalTokenValidationEnabled(true);
        configRepository.setJwksRefreshInterval(300);
        configRepository.setRevocationListRefreshInterval(300);

        final AccelByteSDK sdk = new AccelByteSDK(okhttpClient, tokenRefreshRepository, configRepository);

        return sdk;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
