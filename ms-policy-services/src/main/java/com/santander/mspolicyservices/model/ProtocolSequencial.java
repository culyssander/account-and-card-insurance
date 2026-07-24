package com.santander.mspolicyservices.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "protocol_sequencial")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolSequencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code")
    private String productCode;
    private Integer year;

    @Column(name = "last_number")
    private Integer lastNumber;
}