package com.parkmate.rating;

import com.parkmate.common.BaseEntity;
import com.parkmate.parking_lot.ParkingLotEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "rating")
public class RatingEntity extends BaseEntity {
    @Column(name = "user_id")
    Long userId;

    @Column(name = "overall_rating")
    Integer overallRating;

    @Column(name = "title")
    String title;

    @Column(name = "comment")
    String comment;

    @Column(name = "is_visible")
    Boolean isVisible;

    @ManyToOne
    @JoinColumn(name = "lot_id")
    ParkingLotEntity parkingLot;
}
