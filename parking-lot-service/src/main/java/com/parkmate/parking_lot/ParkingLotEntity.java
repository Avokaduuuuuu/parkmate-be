package com.parkmate.parking_lot;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.common.BaseEntity;
import com.parkmate.floor.FloorEntity;
import com.parkmate.image.ImageEntity;
import com.parkmate.lot_capacity.LotCapacityEntity;
import com.parkmate.pricing_rule.PricingRuleEntity;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
import com.parkmate.session.SessionEntity;
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
    Boolean is24Hour;

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

    @Column(name = "reason")
    String reason;


    @OneToMany(mappedBy = "parkingLot")
    List<FloorEntity> parkingFloors;

    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL)
    List<PricingRuleEntity> pricingRules;

    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL)
    List<LotCapacityEntity> lotCapacity;

    @OneToMany(mappedBy = "parkingLot")
    List<SessionEntity> sessions;

    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL)
    List<ImageEntity> images;

}
