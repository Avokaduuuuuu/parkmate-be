package com.parkmate.subscription;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.parking_lot.ParkingLotRepository;
import com.parkmate.subscription.dto.req.SubscriptionCreateRequest;
import com.parkmate.subscription.dto.req.SubscriptionUpdateRequest;
import com.parkmate.subscription.dto.resp.SubscriptionResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final ParkingLotRepository parkingLotRepository;

    @Override
    public Page<SubscriptionResponse> fetchAllSubscriptions(int page, int size, String sortBy, String sortOrder, SubscriptionFilterParams filterParams) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SubscriptionEntity> subscriptions = subscriptionRepository.findAll(filterParams.getSpecification(), pageable);
        return subscriptions.map(SubscriptionMapper.INSTANCE::toResponse);
    }

    @Override
    public SubscriptionResponse fetchSubscriptionById(Long id) {
        return SubscriptionMapper.INSTANCE.toResponse(subscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Subscription with id " + id + " not found")));
    }

    @Override
    public SubscriptionResponse addSubscription(SubscriptionCreateRequest request) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(request.lotId())
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND, "Parking lot with id " + request.lotId() + " not found"));
        SubscriptionEntity entity = SubscriptionEntity.builder()
                .name(request.name())
                .description(request.description())
                .vehicleType(request.vehicleType())
                .durationType(request.durationType())
                .parkingLot(parkingLotEntity)
                .price(request.price())
                .isActive(false)
                .build();

        switch (request.durationType()) {
            case MONTHLY -> entity.setDurationValue(30);
            case YEARLY -> entity.setDurationValue(365);
            case QUARTERLY -> entity.setDurationValue(90);
        }

        return SubscriptionMapper.INSTANCE.toResponse(subscriptionRepository.save(entity));
    }

    @Override
    public SubscriptionResponse updateSubscription(SubscriptionUpdateRequest request, Long id) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Subscription with id " + id + " not found"));

        if (request.name() != null) subscriptionEntity.setName(request.name());
        if (request.description() != null) subscriptionEntity.setDescription(request.description());
        if (request.isActive() != null) subscriptionEntity.setIsActive(request.isActive());
        if (request.price() != null) subscriptionEntity.setPrice(request.price());
        return SubscriptionMapper.INSTANCE.toResponse(subscriptionRepository.save(subscriptionEntity));
    }

    @Override
    public SubscriptionResponse deleteSubscription(Long id) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Subscription with id " + id + " not found"));
        subscriptionEntity.setIsActive(false);
        return SubscriptionMapper.INSTANCE.toResponse(subscriptionRepository.save(subscriptionEntity));
    }
}
