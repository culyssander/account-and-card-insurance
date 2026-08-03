package com.santander.msnotificationservices.dto;

import java.time.Instant;

public record ApplicationLogDocumentDto (
        String payload,
        String routingKey,
        String queue,
        Instant timestamp
) {}