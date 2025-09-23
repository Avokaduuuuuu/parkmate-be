package com.parkmate.entity;

import com.parkmate.entity.enums.ParkingLotStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

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

    @Column(name = "name", length = 255)
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
    LocalTime operatingHoursStart;

    @Column(name = "operating_hours_end")
    LocalTime operatingHoursEnd;

    @Column(name = "is_24_hours")
    @Builder.Default
    Boolean is24Hours = false;

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
    ParkingLotStatus status;

}
