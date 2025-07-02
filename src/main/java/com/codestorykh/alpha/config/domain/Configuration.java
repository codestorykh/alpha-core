package com.codestorykh.alpha.config.domain;

import com.codestorykh.alpha.common.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "configurations", indexes = {
    @Index(name = "idx_config_key", columnList = "config_key"),
    @Index(name = "idx_config_category", columnList = "category")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuration extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "config_key", unique = true, nullable = false)
    private String key;

    @Size(max = 1000)
    @Column(name = "config_value", nullable = false)
    private String value;

    @Size(max = 100)
    @Column(name = "category")
    private String category;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Column(name = "is_encrypted")
    private boolean encrypted = false;

    @Column(name = "is_system")
    private boolean system = false;

    @Column(name = "is_readonly")
    private boolean readonly = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false)
    private ConfigurationValueType valueType = ConfigurationValueType.STRING;

    public enum ConfigurationValueType {
        STRING, INTEGER, LONG, DOUBLE, BOOLEAN, JSON, ENCRYPTED
    }
} 