package com.example.app.batch.domain;

public record ProductDto(
        Long id,
        String name,
        int price,
        String type
) {
}
