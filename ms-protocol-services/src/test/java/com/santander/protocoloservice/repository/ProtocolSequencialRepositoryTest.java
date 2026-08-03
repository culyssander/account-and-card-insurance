package com.santander.protocoloservice.repository;

import com.santander.protocoloservice.model.ProtocolSequencial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProtocolSequencialRepositoryTest {

    @Autowired
    private ProtocolSequencialRepository repository;

    @Test
    @DisplayName("Deve encontrar protocolo por productCode e year")
    void shouldFindProtocolByProductCodeAndYear() {
        // Arrange
        ProtocolSequencial protocol = new ProtocolSequencial();
        protocol.setProductCode("AUTO");
        protocol.setYear(2026);
        protocol.setLastNumber(1);

        repository.save(protocol);

        // Act
        Optional<ProtocolSequencial> result =
                repository.findProtocolByProductCodeAndYear("AUTO", 2026);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getProductCode()).isEqualTo("AUTO");
        assertThat(result.get().getYear()).isEqualTo(2026);
        assertThat(result.get().getLastNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("Não deve encontrar protocolo quando não existir")
    void shouldReturnEmptyWhenProtocolDoesNotExist() {

        Optional<ProtocolSequencial> result =
                repository.findProtocolByProductCodeAndYear("AUTO", 2026);

        assertThat(result).isEmpty();
    }
}
