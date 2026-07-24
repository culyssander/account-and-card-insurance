package com.santander.mspolicyservices.model;

public enum PolicyStatus {
    DRAFT,             // Em elaboração
    PENDING_ISSUANCE,  // Aguardando emissão
    ACTIVE,            // Vigente
    SUSPENDED,         // Suspensa
    CANCELLED,         // Cancelada
    EXPIRED,           // Vigência encerrada
    RENEWED            // Renovada
}