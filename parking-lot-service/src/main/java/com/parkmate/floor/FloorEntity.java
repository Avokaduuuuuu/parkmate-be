package com.parkmate.floor;

import com.parkmate.area.AreaEntity;
import com.parkmate.common.BaseEntity;
import com.parkmate.floor_capacity.FloorCapacityEntity;
import com.parkmate.parking_lot.ParkingLotEntity;
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
@Table(name = "floor")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloorEntity extends BaseEntity {
    @Column(name = "floor_number")
    Integer floorNumber;

    @Column(name = "floor_name", length = 100)
    String floorName;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    ParkingLotEntity parkingLot;

    @OneToMany(mappedBy = "parkingFloor")
    List<AreaEntity> parkingAreas;

    @OneToMany(mappedBy = "parkingFloor", cascade = CascadeType.ALL)
    List<FloorCapacityEntity> parkingFloorCapacity;

}
