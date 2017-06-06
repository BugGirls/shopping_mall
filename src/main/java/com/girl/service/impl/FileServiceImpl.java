package com.girl.service.impl;

import com.girl.service.IFileService;
import com.girl.util.FTPUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by girl on 2017/6/2.
 */
@Service
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile multipartFile, String path) {
        // 获取文件的原始名称
        String fileName = multipartFile.getOriginalFilename();
        // 获取文件的扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 重新组装上传文件的名称
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;

        // 创建文件夹
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            // 赋予可写的权限
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        // 创建文件
        File targetFile = new File(path, uploadFileName);

        // 上传FTP服务器是否成功
        boolean isSuccess = false;

        try {
            logger.info("开始上传文件到本地服务器，上传文件的文件名：{}，重新组装的文件名：{}，上传文件的路径：{}", fileName, uploadFileName, path);
            // 开始文件上传
            multipartFile.transferTo(targetFile);
            logger.info("本地服务器文件上传成功");

            logger.info("开始上传文件到FTP服务器");
            // 将targetFile上传到FTP服务器上
            isSuccess = FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            // 将文件上传到FTP服务器上后，删除upload下面的文件
            targetFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // FTP服务器上传失败，返回null
        if (!isSuccess) {
            return null;
        }

        return targetFile.getName();
    }

}
