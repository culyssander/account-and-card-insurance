package com.santander.msclaimsservices.services;

import com.santander.msclaimsservices.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Service
public class S3Servicos {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private int tempoDeVisualizacaoDaImagem = 10;

    public S3Servicos(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String upload(MultipartFile file, String protocolo) throws IOException {

        String key = FileUtil.generateFileName(file.getOriginalFilename(), protocolo);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(file.getBytes())
        );

        return key;
    }

    public byte[] download(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> object =
                s3Client.getObjectAsBytes(request);

        return object.asByteArray();
    }

    public String visualizarTemporaria(String key) {
        S3Presigner presigner = S3Presigner.create();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(tempoDeVisualizacaoDaImagem))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presigned =
                presigner.presignGetObject(presignRequest);

        return presigned.url().toString();
    }
}