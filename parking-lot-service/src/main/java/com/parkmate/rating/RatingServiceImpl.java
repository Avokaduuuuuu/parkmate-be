package com.parkmate.rating;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.parking_lot.ParkingLotRepository;
import com.parkmate.rating.dto.req.RatingCreateRequest;
import com.parkmate.rating.dto.req.RatingUpdateRequest;
import com.parkmate.rating.dto.resp.RatingResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final ParkingLotRepository parkingLotRepository;
    @Override
    public Page<RatingResponse> getRatings(int page, int size, String sortBy, String sortOrder, RatingFilterParams filterParams) {
        Page<RatingEntity> ratingEntities = ratingRepository
                .findAll(filterParams.getSpecification(), PageRequest.of(page, size, Sort.Direction.valueOf(sortOrder), sortBy));
        return ratingEntities.map(RatingMapper.INSTANCE::toResponse);
    }

    @Override
    public RatingResponse getRatingById(Long id) {
        return RatingMapper.INSTANCE.toResponse(ratingRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RATING_NOT_FOUND, "Rating with id " + id + " not found")
        ));
    }

    @Override
    public RatingResponse createRating(Long lotId, RatingCreateRequest request) {
        ParkingLotEntity lotEntity = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, "Device with id " + lotId + " not found"));
        RatingEntity ratingEntity = new RatingEntity();
        ratingEntity.setUserId(request.userId());
        ratingEntity.setParkingLot(lotEntity);
        ratingEntity.setOverallRating(request.overallRating());
        ratingEntity.setTitle(request.title());
        ratingEntity.setComment(request.comment());
        return RatingMapper.INSTANCE.toResponse(ratingRepository.save(ratingEntity));
    }

    @Override
    public RatingResponse updateRating(Long id, RatingUpdateRequest request) {
        RatingEntity ratingEntity = ratingRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RATING_NOT_FOUND, "Rating with id " + id + " not found"));

        if (request.overallRating()!= null) ratingEntity.setOverallRating(request.overallRating());
        if (request.title()!= null) ratingEntity.setTitle(request.title());
        if (request.comment()!= null) ratingEntity.setComment(request.comment());
        if (request.isVisible() != null) ratingEntity.setIsVisible(request.isVisible());
        return RatingMapper.INSTANCE.toResponse(ratingRepository.save(ratingEntity));
    }

    @Override
    public void deleteRating(Long id) {
        RatingEntity ratingEntity = ratingRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RATING_NOT_FOUND, "Rating with id " + id + " not found"));
        ratingRepository.delete(ratingEntity);
    }

    @Override
    public Long countRatings() {
        return ratingRepository.count();
    }
}
