package com.santander.msclaimsservices.services;

import com.santander.msclaimsservices.util.FileUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServicosTest {

    @Mock
    private S3Client s3Client;

    private S3Servicos s3Servicos;

    private static final String BUCKET = "test-bucket";

    @BeforeEach
    void setUp() {
        s3Servicos = new S3Servicos(s3Client);
        ReflectionTestUtils.setField(s3Servicos, "bucket", BUCKET);
    }

    @Nested
    @DisplayName("upload")
    class Upload {

        @Test
        @DisplayName("deve gerar a key, enviar o arquivo ao S3 e retornar a key")
        void shouldUploadFileAndReturnKey() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "documento.jpg", "image/jpeg", "conteudo".getBytes());

            try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
                fileUtilMock.when(() -> FileUtil.generateFileName("documento.jpg", "SIN-001"))
                        .thenReturn("SIN-001-documento.jpg");

                when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                        .thenReturn(PutObjectResponse.builder().build());

                String key = s3Servicos.upload(file, "SIN-001");

                assertThat(key).isEqualTo("SIN-001-documento.jpg");

                ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
                verify(s3Client, times(1)).putObject(requestCaptor.capture(), any(RequestBody.class));

                PutObjectRequest capturedRequest = requestCaptor.getValue();
                assertThat(capturedRequest.bucket()).isEqualTo(BUCKET);
                assertThat(capturedRequest.key()).isEqualTo("SIN-001-documento.jpg");
                assertThat(capturedRequest.contentType()).isEqualTo("image/jpeg");
            }
        }

        @Test
        @DisplayName("deve propagar IOException quando não é possível ler os bytes do arquivo")
        void shouldPropagateIOExceptionWhenFileBytesCannotBeRead() throws IOException {
            MultipartFile brokenFile = mock(MultipartFile.class);
            when(brokenFile.getOriginalFilename()).thenReturn("documento.jpg");
            when(brokenFile.getBytes()).thenThrow(new IOException("falha ao ler arquivo"));

            try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
                fileUtilMock.when(() -> FileUtil.generateFileName("documento.jpg", "SIN-001"))
                        .thenReturn("SIN-001-documento.jpg");

                assertThatThrownBy(() -> s3Servicos.upload(brokenFile, "SIN-001"))
                        .isInstanceOf(IOException.class)
                        .hasMessage("falha ao ler arquivo");
            }
        }
    }

    @Nested
    @DisplayName("download")
    class Download {

        @Test
        @DisplayName("deve retornar os bytes do objeto do S3")
        void shouldReturnObjectBytes() {
            byte[] expectedBytes = "conteudo do arquivo".getBytes();

            @SuppressWarnings("unchecked")
            ResponseBytes<GetObjectResponse> responseBytes = mock(ResponseBytes.class);
            when(responseBytes.asByteArray()).thenReturn(expectedBytes);
            when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

            byte[] result = s3Servicos.download("s3-key-1");

            assertThat(result).isEqualTo(expectedBytes);

            ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
            verify(s3Client, times(1)).getObjectAsBytes(requestCaptor.capture());

            GetObjectRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.bucket()).isEqualTo(BUCKET);
            assertThat(capturedRequest.key()).isEqualTo("s3-key-1");
        }
    }

    @Nested
    @DisplayName("visualizarTemporaria")
    class VisualizarTemporaria {

        @Test
        @DisplayName("deve retornar a URL pré-assinada gerada pelo S3Presigner")
        void shouldReturnPresignedUrl() throws URISyntaxException, MalformedURLException {
            S3Presigner presigner = mock(S3Presigner.class);
            PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
            when(presignedRequest.url()).thenReturn(new URI("https://s3.amazonaws.com/test-bucket/s3-key-1?signed=true").toURL());
            when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

            try (MockedStatic<S3Presigner> presignerMock = mockStatic(S3Presigner.class)) {
                presignerMock.when(S3Presigner::create).thenReturn(presigner);

                String url = s3Servicos.visualizarTemporaria("s3-key-1");

                assertThat(url).isEqualTo("https://s3.amazonaws.com/test-bucket/s3-key-1?signed=true");
            }

            verify(presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
        }
    }
}