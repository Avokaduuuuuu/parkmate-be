package com.parkmate.image;

import com.parkmate.common.BaseEntity;
import com.parkmate.parking_lot.ParkingLotEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "image")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageEntity extends BaseEntity {

    @Column(name = "path", length = 500)
    String path;

    @Column(name = "is_active")
    Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    ParkingLotEntity parkingLot;
}
