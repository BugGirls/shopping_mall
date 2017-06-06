package com.girl.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by girl on 2017/6/2.
 */
public interface IFileService {

    String upload(MultipartFile multipartFile, String path);
}
