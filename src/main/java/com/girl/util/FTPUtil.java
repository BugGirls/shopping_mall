package com.girl.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * 上传文件的FTP服务器
 *
 * Created by girl on 2017/6/2.
 */
public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21, ftpUser, ftpPass);

        logger.info("开始连接ftp服务器");
        boolean result = ftpUtil.uploadFile("img/", fileList);
        logger.info("FTP服务器文件上传结束，上传结果：{}", result);

        return result;
    }

    // 多文件上传
    private boolean uploadFile(String remotePath, List<File> files) throws IOException {
        boolean uploaded = false;
        FileInputStream fis = null;
        // 连接FTP服务器
        if (connectServer(this.ip, this.port, this.user, this.pwd)) {
            // 设置参数
            ftpClient.changeWorkingDirectory(remotePath);
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            // 执行多文件上传
            for(File fileItem : files) {
                logger.info("正在上传文件：{}", fileItem.getName());

                try {
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(), fis);

                    uploaded = true;
                } catch (Exception e) {
                    logger.error("文件上传异常", e);
                    logger.error("异常文件名称：{}", fileItem.getName());

                    uploaded = false;
                } finally {
                    fis.close();
                    ftpClient.disconnect();
                }
            }
        }

        return uploaded;
    }

    // 连接FTP服务器
    private boolean connectServer(String ip, int port, String user, String pass) {
        boolean isSuccess  = false;

        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pass);
        } catch (IOException e) {
            logger.error("连接FTP服务器异常", e);
        }

        return isSuccess;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }



}
