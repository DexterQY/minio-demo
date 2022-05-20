package per.qy.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * minio配置
 *
 * @author : QY
 * @date : 2022/5/19
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /** 端点 */
    private String endpoint;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 区域 */
    private String region;
    /** 桶 */
    private String bucket;
    /** 公有读目录 */
    private String publicReadDir;
    /** 签名超时时间（分钟） */
    private Integer signTimeout;
}
