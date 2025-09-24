package com.parkmate.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "parking_floor")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParkingFloorEntity extends BaseEntity {
    @Column(name = "floor_number")
    Integer floorNumber;

    @Column(name = "floor_name", length = 100)
    String floorName;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    ParkingLotEntity parkingLot;

    @OneToMany(mappedBy = "parkingFloor")
    List<ParkingAreaEntity> parkingAreas;

    @OneToMany(mappedBy = "parkingFloor", cascade = CascadeType.ALL)
    List<ParkingFloorCapacityEntity> parkingFloorCapacity;

}
