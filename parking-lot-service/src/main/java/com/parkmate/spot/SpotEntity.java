package com.parkmate.spot;


import com.parkmate.area.AreaEntity;
import com.parkmate.common.BaseEntity;
import com.parkmate.spot.enums.SpotStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "spot")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpotEntity extends BaseEntity {

    @Column(name = "name", length = 20)
    String name;

    @Column(name = "spot_top_left_x")
    Double lotTopLeftX;

    @Column(name = "spot_top_left_y")
    Double lotTopLeftY;

    @Column(name = "spot_width")
    Double lotWidth;

    @Column(name = "spot_height")
    Double lotHeight;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    SpotStatus status;

    @Column(name = "block_reason", length = 255)
    String blockReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    AreaEntity parkingArea;
}
