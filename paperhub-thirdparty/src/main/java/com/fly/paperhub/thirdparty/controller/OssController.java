package com.fly.paperhub.thirdparty.controller;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import io.netty.handler.codec.base64.Base64;
import jdk.internal.util.xml.impl.Input;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Bytes;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/oss")
public class OssController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${oss.bucket}")
    private String bucket;

    @Value("${oss.access-key}")
    private String accessKeyId;

    @Value("${oss.secret-key}")
    private String accessKeySecret;

    @Value("${oss.endpoint}")
    private String endpoint;

    @RequestMapping("/upload_paper")
    public Map<String, Object> uploadPaper(@RequestParam("file") MultipartFile file) {
        log.debug("accessKeyId: " + accessKeyId);
        log.debug("accessKeySecret: " + accessKeySecret);
        log.debug("endpoint: " + endpoint);
        String uuid = UUID.randomUUID().toString();
        String save_path = "papers/" + uuid + ".pdf";
        String url = "https://" + bucket + "." + endpoint + "/" + save_path;
        Map<String, Object> ret = new HashMap<>();
        ret.put("code", 200);
        Map<String, Object> data = new HashMap<>();

        // 暂存 file 到 redis 中
        try {
            byte[] fileBytes = file.getBytes();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(fileBytes);
            String value = baos.toString("ISO-8859-1");
            System.out.println(value);

            redisTemplate.opsForValue().set("temp_pdf_" + url, value, 1, TimeUnit.DAYS);
            data.put("success", true);
            data.put("url", url);
        } catch (IOException e) {
            log.warn(e.getMessage());
            data.put("success", false);
        }

        ret.put("data", data);
        return ret;
    }


    @RequestMapping("/upload_paper_to_oss")
    public Map<String, Object> uploadPaperToOss(@RequestBody Map<String, Object> params) {
        log.debug("upload paper to oss");
        String url = (String) params.get("url");
        String key = "temp_pdf_" + url;
        String uuid = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf(".pdf"));
        Map<String, Object> data = new HashMap<>();

        byte[] fileBytes = ((String) redisTemplate.opsForValue().get(key)).getBytes(StandardCharsets.ISO_8859_1);
        System.out.println((String) redisTemplate.opsForValue().get(key));
        InputStream inputStream = new ByteArrayInputStream(fileBytes);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 创建PutObjectRequest对象。
            String save_path = "papers/" + uuid + ".pdf";
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, save_path, inputStream);
            // 设置该属性可以返回response。如果不设置，则返回的response为空。
            putObjectRequest.setProcess("true");
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            // 如果上传成功，则返回200。
            System.out.println(result.getResponse().getStatusCode());

            data.put("success", true);

        } catch (OSSException oe) {
            String errorMsg = "Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.";
            System.out.println(errorMsg);
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            data.put("success", false);
            data.put("error_msg", errorMsg);
        } catch (ClientException ce) {
            String errorMsg = "Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.";
            log.warn(errorMsg);
            System.out.println("Error Message:" + ce.getMessage());
            data.put("success", false);
            data.put("error_msg", errorMsg);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return data;
    }
}
