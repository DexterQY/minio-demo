package per.qy.minio;

import io.minio.MinioClient;
import org.junit.Test;
import per.qy.minio.config.MinioConfig;
import per.qy.minio.service.MinioService;

import java.io.File;
import java.io.FileInputStream;

public class MinioTest {

    private static final MinioConfig minioConfig = new MinioConfig();

    static {
        minioConfig.setEndpoint("http://192.168.133.110:9001");
        minioConfig.setUsername("minioadmin");
        minioConfig.setPassword("minioadmin");
        minioConfig.setBucket("my-bucket");
    }

    private final MinioClient minioClient = MinioClient.builder()
            .endpoint(minioConfig.getEndpoint())
            .credentials(minioConfig.getUsername(), minioConfig.getPassword())
            .build();
    private final MinioService minioService = new MinioService(minioClient, minioConfig);

    @Test
    public void upload() throws Exception {
        String name = minioService.upload("C:\\Users\\HASEE\\Downloads\\ccc.jpg");
        System.out.println(name);
    }

    @Test
    public void put() throws Exception {
        FileInputStream inputStream = new FileInputStream(
                new File("C:\\Users\\HASEE\\Downloads\\xxx.jpg"));
        String name = minioService.put("xxx.jpg", inputStream);
        System.out.println(name);
    }
}
