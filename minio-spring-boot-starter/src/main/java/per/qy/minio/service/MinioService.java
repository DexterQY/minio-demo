package per.qy.minio.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.minio.DownloadObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
import per.qy.minio.config.MinioConfig;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * minio服务
 *
 * @author : QY
 * @date : 2022/5/19
 */
public class MinioService {

    private final MinioClient minioClient;
    private final String bucket;
    private final Integer signTimeout;

    public MinioService(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.bucket = minioConfig.getBucket();
        if (minioConfig.getSignTimeout() == null) {
            // 默认5分钟
            this.signTimeout = 5;
        } else {
            this.signTimeout = minioConfig.getSignTimeout();
        }
    }

    public String upload(String filename) throws IOException, ServerException,
            InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException {
        String object = getObjectName(filename);
        UploadObjectArgs args = UploadObjectArgs.builder()
                .bucket(bucket)
                .object(object)
                .filename(filename)
                .build();
        minioClient.uploadObject(args);
        return bucket + "/" + object;
    }

    public String put(String filename, InputStream inputStream) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        String object = getObjectName(filename);
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucket)
                .object(object)
                .stream(inputStream, -1L, 10485760L)
                .build();
        minioClient.putObject(args);
        return bucket + "/" + object;
    }

    public void download(String fullName, String filename) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        String object = fullName.substring(fullName.indexOf("/") + 1);
        DownloadObjectArgs args = DownloadObjectArgs.builder()
                .bucket(bucket)
                .object(object)
                .filename(filename)
                .build();
        minioClient.downloadObject(args);
    }

    public GetObjectResponse get(String fullName) throws IOException, InvalidKeyException,
            InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        String object = fullName.substring(fullName.indexOf("/") + 1);
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(bucket)
                .object(object)
                .build();
        return minioClient.getObject(args);
    }

    public void remove(String fullName) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        if (StrUtil.isEmpty(fullName)) {
            return;
        }
        String object = fullName.substring(fullName.indexOf("/") + 1);
        RemoveObjectArgs args = RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(object)
                .build();
        minioClient.removeObject(args);
    }

    public void removes(Collection<String> fullNames) {
        if (CollUtil.isEmpty(fullNames)) {
            return;
        }
        List<DeleteObject> deleteObjects = fullNames.stream().map(fullName ->
                new DeleteObject(fullName.substring(fullName.indexOf("/") + 1)))
                .collect(Collectors.toList());
        RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                .bucket(bucket)
                .objects(deleteObjects)
                .build();
        minioClient.removeObjects(args);
    }

    public String getSignedUrl(Method method, String name, Map<String, String> params) throws IOException,
            InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        String object = method == Method.PUT ? getObjectName(name)
                : name.substring(name.indexOf("/") + 1);
        GetPresignedObjectUrlArgs getUrlArgs = GetPresignedObjectUrlArgs.builder()
                .method(method)
                .bucket(bucket)
                .object(object)
                .expiry(signTimeout, TimeUnit.MINUTES)
                .extraQueryParams(params)
                .build();
        return minioClient.getPresignedObjectUrl(getUrlArgs);
    }

    public String getPutUrl(String filename, Map<String, String> params, String dir) throws IOException,
            InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        String object = getObjectName(filename);
        if (StrUtil.isNotBlank(dir)) {
            object = dir + "/" + object;
        }
        GetPresignedObjectUrlArgs getUrlArgs = GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucket)
                .object(object)
                .expiry(signTimeout, TimeUnit.MINUTES)
                .extraQueryParams(params)
                .build();
        return minioClient.getPresignedObjectUrl(getUrlArgs);
    }

    private String getObjectName(String filename) {
        LocalDate now = LocalDate.now();
        String objectName = "" + now.getYear();
        if (now.getMonthValue() < 10) {
            objectName += "0";
        }
        objectName += now.getMonthValue() + "/";
        String name = FileNameUtil.mainName(filename);
        if (StrUtil.isNotBlank(name)) {
            objectName += name + "-";
        }
        objectName += IdUtil.getSnowflakeNextId() + "." + FileNameUtil.extName(filename);
        return objectName;
    }
}
