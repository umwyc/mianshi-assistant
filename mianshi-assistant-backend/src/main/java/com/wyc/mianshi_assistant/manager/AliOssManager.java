package com.wyc.mianshi_assistant.manager;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;

@Component
public class AliOssManager implements DisposableBean {

    @Value("${alioss.bucket-name}")
    private String bucketName;

    @Resource
    private OSS ossClient;

    /**
     * 上传一个文件
     *
     * @param objectName
     * @param file
     * @return
     */
    public String putObject(String objectName, File file, ObjectMetadata objectMetadata) {
        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file, objectMetadata);
            // 创建PutObject请求。
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
            System.out.println(putObjectResult);
            return objectName;
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        }
        return null;
    }

    /**
     * 在 spring boot 退出的时候销毁 ossClient
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        if(ossClient != null) {
            ossClient.shutdown();
        }
    }
}
