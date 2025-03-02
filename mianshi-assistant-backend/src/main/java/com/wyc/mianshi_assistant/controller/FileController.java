package com.wyc.mianshi_assistant.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.model.ObjectMetadata;
import com.wyc.mianshi_assistant.common.BaseResponse;
import com.wyc.mianshi_assistant.common.ErrorCode;
import com.wyc.mianshi_assistant.common.ResultUtils;
import com.wyc.mianshi_assistant.constant.UserConstant;
import com.wyc.mianshi_assistant.exception.BusinessException;
import com.wyc.mianshi_assistant.manager.AliOssManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.wyc.mianshi_assistant.constant.FileConstant.ALIYUN_OSS_HOST;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private AliOssManager aliOssManager;

    /**
     * 业务的对象名称
     */
    private static final List<String> OBJECT_NAMES = Arrays.asList("user", "questionbank");

    /**
     * 文件最大大小
     */
    private static final int maxSize = 5 * 1024 * 1024;

    /**
     * 允许的文件后缀名
     */
    private static final List<String> POST_SUFFIX = Arrays.asList("jpg", "png",  "gif", "bmp", "jpeg", "tiff");

    /**
     * 上传图片文件接口
     *
     * @param multipartFile
     * @param objectName
     * @return
     */
    @PostMapping("/upload")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<String> upload(@RequestParam("file") MultipartFile multipartFile, String objectName) {
        // 文件不能为空
        if(multipartFile == null || multipartFile.isEmpty()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }
        // 业务名称不能为空
        if(StrUtil.isBlank(objectName)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对象名称不能为空");
        }
        // 文件名不能为空
        String originalFilename = multipartFile.getOriginalFilename();
        if(StrUtil.isBlank(originalFilename)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }
        String postSuffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        // 非法的文件格式
        if(!POST_SUFFIX.contains(postSuffix)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式错误");
        }
        // 非法的文件大小
        if(multipartFile.getSize() > maxSize){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件过大");
        }
        // 非法的业务名称
        if(!OBJECT_NAMES.contains(objectName)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "业务名称不存在");
        }

        // 生成唯一的文件标识
        String prefix = RandomUtil.randomString(8);
        String filename = prefix + "-" + originalFilename;
        String filepath = String.format("%s/%s", objectName, filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            // 将 multipartFile 文件传输至新文件 file 中
            multipartFile.transferTo(file);
            // 设置文件的 Content-Type
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(getContentType(postSuffix));
            // 调用 aliOssManager 上传文件
            String uri = aliOssManager.putObject(filepath, file, objectMetadata);
            return ResultUtils.success(ALIYUN_OSS_HOST + uri);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 获取文件格式
     *
     * @param postSuffix
     * @return
     */
    public static String getContentType(String postSuffix) {
        if (postSuffix.equalsIgnoreCase("bmp")) {
            return "image/bmp";
        }
        if (postSuffix.equalsIgnoreCase("gif")) {
            return "image/gif";
        }
        if (postSuffix.equalsIgnoreCase("tiff")) {
            return "image/tiff";
        }
        if (StrUtil.equalsAny("jpg", "png", "jpeg")) {
            return "image/jpg";
        }

        return "image/jpg";
    }
}
