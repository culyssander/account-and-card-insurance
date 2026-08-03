package com.santander.msclaimsservices.services;

import com.santander.msclaimsservices.model.Attachment;
import com.santander.msclaimsservices.repository.AttachmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServicesTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentServices attachmentServices;

    private Attachment attachment;

    @BeforeEach
    void setUp() {
        attachment = Attachment.builder()
                .fileName("documento.jpg")
                .documentType("image/jpeg")
                .url("s3-key-1")
                .size(1024L)
                .build();
    }

    @Nested
    @DisplayName("newAttachment")
    class NewAttachment {

        @Test
        @DisplayName("deve salvar e retornar a lista de anexos")
        void shouldSaveAndReturnAttachments() {
            List<Attachment> input = List.of(attachment);
            when(attachmentRepository.saveAll(input)).thenReturn(input);

            List<Attachment> result = attachmentServices.newAttachment(input);

            assertThat(result).isEqualTo(input);
            verify(attachmentRepository, times(1)).saveAll(input);
        }

        @Test
        @DisplayName("deve lançar NullPointerException quando a lista de anexos é nula (@NonNull)")
        void shouldThrowNullPointerExceptionWhenAttachmentsIsNull() {
            assertThatThrownBy(() -> attachmentServices.newAttachment(null))
                    .isInstanceOf(NullPointerException.class);

            verify(attachmentRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("deve repassar lista vazia ao repositório sem erro")
        void shouldPassEmptyListToRepository() {
            when(attachmentRepository.saveAll(List.<Attachment>of())).thenReturn(List.of());

            List<Attachment> result = attachmentServices.newAttachment(List.of());

            assertThat(result).isEmpty();
            verify(attachmentRepository, times(1)).saveAll(List.of());
        }
    }

    @Nested
    @DisplayName("deleteAttachment")
    class DeleteAttachment {

        @Test
        @DisplayName("deve delegar a exclusão para o repositório")
        void shouldDelegateDeleteToRepository() {
            attachmentServices.deleteAttachment(attachment);

            verify(attachmentRepository, times(1)).delete(attachment);
        }
    }
}