package per.qy.service.minio.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 获取签名url
 *
 * @author : QY
 * @date : 2022/5/20
 */
@Data
public class GetSignedUrlDto {

    /** 方法：get put delete */
    @NotBlank
    private String method;
    /** 对象名称 */
    @NotBlank
    private String name;
    /** 是否公有读 */
    private Boolean publicRead;
}
