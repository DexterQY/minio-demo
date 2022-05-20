package per.qy.service.minio.controller;

import cn.hutool.core.util.StrUtil;
import io.minio.GetObjectResponse;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import per.qy.minio.config.MinioConfig;
import per.qy.minio.service.MinioService;
import per.qy.service.minio.model.GetSignedUrlDto;
import per.qy.service.minio.model.SignedUrlVo;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * MinioController
 *
 * @author : QY
 * @date : 2022/5/19
 */
@Slf4j
@RequestMapping("/minio")
@RestController
public class MinioController {

    @Autowired
    private MinioService minioService;
    @Autowired
    private MinioConfig minioConfig;

    @GetMapping("/getSignedUrl")
    public SignedUrlVo getSignedUrl(HttpServletRequest request, @Valid GetSignedUrlDto dto) throws Exception {
        // 模拟判断是否登录
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            return null;
        }

        Method method = getMethod(dto.getMethod());
        // 生成临时带签名信息的url
        String url;
        if (method == Method.PUT && dto.getPublicRead() != null && dto.getPublicRead()) {
            // 公有读上传到公有读目录
            url = minioService.getPutUrl(dto.getName(), Map.of("token", token), minioConfig.getPublicReadDir());
        } else {
            url = minioService.getSignedUrl(method, dto.getName(), Map.of("token", token));
        }

        // 截取路径部分
        url = url.substring(url.indexOf("/", 10));
        // 去掉token信息，前端使用时拼上
        String tokenParam = url.substring(url.indexOf("token="));
        tokenParam = tokenParam.substring(0, tokenParam.indexOf("&") + 1);
        url = url.replace(tokenParam, "");

        SignedUrlVo vo = new SignedUrlVo();
        vo.setUrl(url);
        vo.setName(url.substring(1, url.indexOf("?")));
        return vo;
    }

    @GetMapping("download")
    public void download(HttpServletResponse response, String name, String filename) throws Exception {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.setCharacterEncoding("UTF-8");
        try (GetObjectResponse objectResponse = minioService.get(name);
             ServletOutputStream outputStream = response.getOutputStream()) {
            objectResponse.transferTo(outputStream);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    private Method getMethod(String method) {
        method = method.toLowerCase();
        if ("get".equals(method)) {
            return Method.GET;
        }
        if ("put".equals(method)) {
            return Method.PUT;
        }
        if ("delete".equals(method)) {
            return Method.DELETE;
        }
        return null;
    }
}
