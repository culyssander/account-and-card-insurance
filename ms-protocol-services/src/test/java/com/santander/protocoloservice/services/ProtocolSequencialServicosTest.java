package com.santander.protocoloservice.services;

import com.santander.protocoloservice.model.ProtocolSequencial;
import com.santander.protocoloservice.repository.ProtocolSequencialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProtocolSequencialServicosTest {

    private ProtocolSequencialRepository repository;
    private ProtocolSequencialServicos service;

    @BeforeEach
    void setup() {
        repository = mock(ProtocolSequencialRepository.class);
        service = new ProtocolSequencialServicos(repository);
    }

    @Test
    void shouldGenerateNewProtocolWhenSequenceDoesNotExist() {
        // Arrange
        when(repository.findProtocolByProductCodeAndYear(anyString(), anyInt()))
                .thenReturn(Optional.empty());

        when(repository.save(any(ProtocolSequencial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String protocol = service.generateProtocol("AUTO");

        // Assert
        assertThat(protocol)
                .startsWith("AUTO-")
                .endsWith("00001");

        ArgumentCaptor<ProtocolSequencial> captor =
                ArgumentCaptor.forClass(ProtocolSequencial.class);

        verify(repository).save(captor.capture());

        ProtocolSequencial saved = captor.getValue();

        assertThat(saved.getProductCode()).isEqualTo("AUTO");
        assertThat(saved.getLastNumber()).isEqualTo(1);
    }

    @Test
    void shouldIncrementExistingSequence() {
        // Arrange
        ProtocolSequencial existing = ProtocolSequencial.builder()
                .id(1L)
                .productCode("AUTO")
                .year(2026)
                .lastNumber(10)
                .build();

        when(repository.findProtocolByProductCodeAndYear(anyString(), anyInt()))
                .thenReturn(Optional.of(existing));

        when(repository.save(any(ProtocolSequencial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String protocol = service.generateProtocol("AUTO");

        // Assert
        assertThat(protocol)
                .endsWith("00011");

        verify(repository).save(existing);

        assertThat(existing.getLastNumber())
                .isEqualTo(11);
    }
}
