package com.parkmate.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.f4b6a3.uuid.UuidCreator;
import com.parkmate.entity.enums.DeviceOs;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "mobile_device")
@Entity
public class MobileDevice {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @PrePersist
    public void assignIdIfNull() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch(); //v7
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mobile_device_user"))
    private User user;

    @Column(name = "device_id", unique = true, nullable = false, length = 100)
    private String deviceId;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_os", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DeviceOs deviceOs;

    @Column(name = "push_token", length = 500)
    private String pushToken;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_active_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    private LocalDateTime createdAt;

}
