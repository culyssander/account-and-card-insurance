package com.santander.msclaimsservices.services;

import com.santander.msclaimsservices.model.Attachment;
import com.santander.msclaimsservices.repository.AttachmentRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AttachmentServices {

    private AttachmentRepository attachmentRepository;

    public List<Attachment> newAttachment(@NonNull List<Attachment> attachments) {
        return attachmentRepository.saveAll(attachments);
    }

    public void deleteAttachment(Attachment anexos) {
        attachmentRepository.delete(anexos);
    }
}
