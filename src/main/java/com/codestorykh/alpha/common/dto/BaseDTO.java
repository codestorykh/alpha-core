package com.codestorykh.alpha.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class BaseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
} 