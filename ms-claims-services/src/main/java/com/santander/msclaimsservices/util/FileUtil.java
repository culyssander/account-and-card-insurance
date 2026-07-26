package com.santander.msclaimsservices.util;

import java.util.UUID;

public class FileUtil {

    public static String generateFileName(String originalFilename, String protocolo) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return UUID.randomUUID().toString();
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String randomName = UUID.randomUUID().toString().replace("-", "");

        return String.format("%s_%s%s", protocolo, randomName, extension.toLowerCase());
    }

}