package com.innovawebJT.lacsc.service;

import com.innovawebJT.lacsc.enums.FileCategory;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {

    String store(
        Long userId,
        FileCategory category,
        Long entityId,
        MultipartFile file
    );

    String replace(String oldPath, MultipartFile newFile);

    Resource load(String path);

    void delete(String path);
}
