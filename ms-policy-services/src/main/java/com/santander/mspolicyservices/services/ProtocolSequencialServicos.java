package com.santander.mspolicyservices.services;

import com.santander.mspolicyservices.model.ProtocolSequencial;
import com.santander.mspolicyservices.repository.ProtocolSequencialRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ProtocolSequencialServicos {

    private static final int TAMANHO_SEQUENCIAL = 5;
    private final ProtocolSequencialRepository protocoloSequencialRepository;

    public ProtocolSequencialServicos(ProtocolSequencialRepository protocolSequencialRepository) {
        this.protocoloSequencialRepository = protocolSequencialRepository;
    }

    @Transactional
    public String generateProtocol(String productCode) {
        int year = LocalDate.now().getYear();

        ProtocolSequencial sequencial = protocoloSequencialRepository
                .findProtocolByProductCodeAndYear(productCode, year)
                .orElseGet(() -> newProtocolSequencial(productCode, year));

        sequencial.setLastNumber(sequencial.getLastNumber() + 1);
        protocoloSequencialRepository.save(sequencial);

        return montarProtocolo(productCode, year, sequencial.getLastNumber());
    }

    private ProtocolSequencial newProtocolSequencial(String productCode, int year) {
        return ProtocolSequencial.builder()
                .productCode(productCode)
                .year(year)
                .lastNumber(0)
                .build();
    }

    private String montarProtocolo(String productCode, int year, int number) {
        return String.format("%s-%d-%0" + TAMANHO_SEQUENCIAL + "d", productCode, year, number);
    }
}
