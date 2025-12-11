package com.jayesh.ecom.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Override
    public String uploadImage(String path, MultipartFile image) throws IOException {
        // Get the file name of the current / original file
        String originalFilename = image.getOriginalFilename();

        // Generate a unique file name
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFilename.substring(originalFilename.lastIndexOf('.')));
        String filepath = path + File.separator + fileName;

        // Check if path exist and create if not exist
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        // Upload to server
        Files.copy(image.getInputStream(), Paths.get(filepath));

        // return file name
        return fileName;
    }
}
