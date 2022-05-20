package per.qy.minio.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import per.qy.minio.service.MinioService;

/**
 * minio bean配置
 *
 * @author : QY
 * @date : 2022/5/19
 */
@Configuration
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
public class MinioAutoConfiguration {

    @Autowired
    private MinioConfig minioConfig;

    @Bean
    public MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getUsername(), minioConfig.getPassword())
                .region(minioConfig.getRegion())
                .build();
    }

    @Bean
    public MinioService getMinioService() {
        return new MinioService(getMinioClient(), minioConfig);
    }
}
