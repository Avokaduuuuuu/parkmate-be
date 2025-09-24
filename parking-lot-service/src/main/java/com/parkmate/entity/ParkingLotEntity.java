package com.parkmate.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.entity.enums.ParkingLotStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalTime;
import java.util.List;

@Builder
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "parking_lot")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParkingLotEntity extends BaseEntity {

    @Column(name = "partner_id")
    Long partnerId;

    @Column(name = "name")
    String name;

    @Column(name = "street_address")
    String streetAddress;

    @Column(name = "ward", length = 100)
    String ward;

    @Column(name = "city", length = 100)
    String city;

    @Column(name = "latitude")
    Double latitude;

    @Column(name = "longitude")
    Double longitude;

    @Column(name = "total_floor")
    Integer totalFloors;

    @Column(name = "operating_hours_start")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime operatingHoursStart;

    @Column(name = "operating_hours_end")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime operatingHoursEnd;

    @Column(name = "is_24_hour")
    @Builder.Default
    Boolean is24Hour = false;

    @Column(name = "boundary_top_left_x")
    Double boundaryTopLeftX;

    @Column(name = "boundary_top_left_y")
    Double boundaryTopLeftY;

    @Column(name = "boundary_width")
    Double boundaryWidth;

    @Column(name = "boundary_height")
    Double boundaryHeight;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    ParkingLotStatus status;

    @OneToMany(mappedBy = "parkingLot")
    List<ParkingFloorEntity> parkingFloors;

    @OneToMany(mappedBy = "parkingLot")
    List<PricingRuleEntity> pricingRules;

}
