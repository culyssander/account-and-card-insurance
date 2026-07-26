package com.santander.msclaimsservices.util;

import com.santander.msclaimsservices.constants.ClaimConstants;
import com.santander.msclaimsservices.exception.BusinessException;
import org.apache.tika.Tika;
import org.jspecify.annotations.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

public class FileValidator {

    private static final long TAMANHO_MAXIMO_EM_BYTES = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png", "gif", "webp"
    );

    public static void validate(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(ClaimConstants.CLAIM_FILE_IS_EMPTY);
        }

        validarTamanho(file);

        String extension = getExtension(file);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(ClaimConstants.CLAIM_FILE_EXTENSION_NOT_ALLOWED);
        }

        Tika tika = new Tika();
        String detectedType = tika.detect(file.getInputStream());

        if (!ALLOWED_TYPES.contains(detectedType)) {
            throw new IllegalArgumentException(ClaimConstants.CLAIM_FILE_INVALID_TYPE);
        }
    }

    private static @NonNull String getExtension(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    ClaimConstants.CLAIM_FILE_ONLY_PDF_AND_IMAGE_ARE_ALLOWED
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException(ClaimConstants.CLAIM_FILE_INVALID_TYPE);
        }

        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private static void validarTamanho(MultipartFile file) {
        if (file.getSize() > TAMANHO_MAXIMO_EM_BYTES) {
            throw new BusinessException(ClaimConstants.CLAIM_FILE_EXCEEDS_MAXIMUM);
        }
    }
}