package com.parkmate.systemConfig;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "system_config")
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "config_key", columnDefinition = "config_key not null")
    private Object configKey;

    @NotNull
    @Column(name = "config_value", nullable = false, length = Integer.MAX_VALUE)
    private String configValue;

    @Size(max = 20)
    @NotNull
    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;


}