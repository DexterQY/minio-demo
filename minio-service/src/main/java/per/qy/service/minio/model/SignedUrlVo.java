package per.qy.service.minio.model;

import lombok.Data;

/**
 * 签名url
 *
 * @author : QY
 * @date : 2022/5/20
 */
@Data
public class SignedUrlVo {

    /** url */
    private String url;
    /** 对象名 */
    private String name;
}
