package com.parkmate.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RatingRepository extends JpaRepository<RatingEntity, Long>, JpaSpecificationExecutor<RatingEntity> {
}
