package com.parkmate.rating;

import com.parkmate.client.UserClient;
import com.parkmate.client.response.UserRatingResponse;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.parking_lot.ParkingLotRepository;
import com.parkmate.rating.dto.req.RatingCreateRequest;
import com.parkmate.rating.dto.req.RatingUpdateRequest;
import com.parkmate.rating.dto.resp.RatingResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final UserClient userClient;
    @Override
    public Page<RatingResponse> getRatings(int page, int size, String sortBy, String sortOrder, RatingFilterParams filterParams, String userHeaderId) {
        Long userId = Long.valueOf(userHeaderId);
        Page<RatingEntity> ratingEntities = ratingRepository
                .findAll(filterParams.getSpecification(userId), PageRequest.of(page, size, Sort.Direction.valueOf(sortOrder), sortBy));

        List<Long> userIds = ratingEntities.getContent().stream().map(RatingEntity::getId).toList();
        Map<Long, UserRatingResponse> userRatingResponseMap = userClient.getUserRating(userIds).getData();
        log.info(userRatingResponseMap.toString());
        Page<RatingResponse> ratingResponses = ratingEntities.map(RatingMapper.INSTANCE::toResponse);
        ratingResponses.map(rating -> {
            rating.setFullName(userRatingResponseMap.get(rating.getId()).getFullName());
            rating.setAvatarUrl(userRatingResponseMap.get(rating.getId()).getAvatarUrl());
            return rating;
        });
        return ratingResponses;
    }

    @Override
    public RatingResponse getRatingById(Long id) {
        return RatingMapper.INSTANCE.toResponse(ratingRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RATING_NOT_FOUND, "Rating with id " + id + " not found")
        ));
    }

    @Override
    public RatingResponse createRating(Long lotId, RatingCreateRequest request, String userHeaderId) {
        if (userHeaderId == null || userHeaderId.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = Long.parseLong(userHeaderId);
        ParkingLotEntity lotEntity = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, "Device with id " + lotId + " not found"));
        RatingEntity ratingEntity = new RatingEntity();
        ratingEntity.setUserId(userId);
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
