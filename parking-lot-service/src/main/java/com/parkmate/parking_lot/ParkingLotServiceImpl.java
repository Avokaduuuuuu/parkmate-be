package com.parkmate.parking_lot;

import com.parkmate.area.AreaRepository;
import com.parkmate.client.PaymentClient;
import com.parkmate.client.UserClient;
import com.parkmate.client.request.CreateDeviceItemPaymentRequest;
import com.parkmate.client.response.UserRatingResponse;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.device.DeviceEntity;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.floor.dto.resp.FloorResponse;
import com.parkmate.floor_capacity.FloorCapacityMapper;
import com.parkmate.floor_capacity.dto.resp.FloorCapacityResponse;
import com.parkmate.image.ImageMapper;
import com.parkmate.image.dto.resp.ImageResponse;
import com.parkmate.lot_capacity.LotCapacityEntity;
import com.parkmate.lot_capacity.LotCapacityMapper;
import com.parkmate.lot_capacity.dto.req.LotCapacityCreateRequest;
import com.parkmate.lot_capacity.dto.resp.LotCapacityResponse;
import com.parkmate.parking_lot.dto.req.ParkingLotCreateRequest;
import com.parkmate.parking_lot.dto.req.ParkingLotUpdateRequest;
import com.parkmate.parking_lot.dto.resp.ParkingLotAvailableReservationSpotResponse;
import com.parkmate.parking_lot.dto.resp.ParkingLotBasicInfo;
import com.parkmate.parking_lot.dto.resp.ParkingLotDetailedResponse;
import com.parkmate.parking_lot.dto.resp.ParkingLotResponse;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
import com.parkmate.policy.PolicyEntity;
import com.parkmate.policy.dto.req.PolicyCreateRequest;
import com.parkmate.policy.enums.PolicyType;
import com.parkmate.pricing_rule.PricingRuleEntity;
import com.parkmate.pricing_rule.PricingRuleMapper;
import com.parkmate.pricing_rule.PricingRuleRepository;
import com.parkmate.pricing_rule.dto.req.PricingRuleCreateRequest;
import com.parkmate.pricing_rule.dto.resp.PricingRuleResponse;
import com.parkmate.pricing_rule.dto.resp.PricingRuleSimpleResponse;
import com.parkmate.rating.RatingEntity;
import com.parkmate.rating.RatingMapper;
import com.parkmate.rating.dto.resp.RatingResponse;
import com.parkmate.s3.S3Service;
import com.parkmate.session.SessionRepository;
import com.parkmate.session.enums.SyncStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final S3Service s3Service;
    private final PricingRuleRepository pricingRuleRepository;
    private final SessionRepository sessionRepository;
    private final UserClient userClient;
    private final AreaRepository areaRepository;
    private final PaymentClient paymentClient;

    @Override
    public Page<ParkingLotResponse> fetchAllParkingLots(
            String userHeaderId,
            int page,
            int size,
            String sortBy,
            String sortOrder,
            ParkingLotFilterParams params
    ) {
        Long partnerId = userHeaderId != null ? Long.parseLong(userHeaderId) : null;
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ParkingLotEntity> parkingLotEntities = parkingLotRepository.findAll(params.getSpecification(partnerId), pageable);
        return parkingLotEntities.map(ParkingLotMapper.INSTANCE::toResponse);
    }

    @Override
    public ParkingLotDetailedResponse getParkingLotById(Long id) {
        log.info("getParkingLotById {}", id);
        ParkingLotDetailedResponse parkingLotResponse = ParkingLotMapper.INSTANCE.toDetailedResponse(
                parkingLotRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND)));

        parkingLotResponse.getImages().forEach(image -> image.setPath(s3Service.getPresignedUrl(image.getPath())));
        List<ParkingLotAvailableReservationSpotResponse> availableSpotResponses = new ArrayList<>();
        for (VehicleType vehicleType : VehicleType.values()) {
            ParkingLotAvailableReservationSpotResponse countAvailableSpot = countAvailableSpot(id, LocalDateTime.now(), 0, vehicleType);
            countAvailableSpot.setPricing(null);
            availableSpotResponses.add(countAvailableSpot);
        }
        List<RatingResponse> ratings = parkingLotResponse.getRatings();

        if (ratings != null && !ratings.isEmpty()) {
            log.info("First rating - ID: {}, Overall Rating: {}, Type: {}",
                    ratings.get(0).getId(),
                    ratings.get(0).getOverallRating(),
                    ratings.get(0).getOverallRating().getClass().getSimpleName()
            );
        }

        Long totalRatings = ratings != null ? (long) ratings.size() : 0L;

        Double averageRating = (ratings != null && !ratings.isEmpty())
                ? ratings.stream()
                .mapToInt(RatingResponse::getOverallRating)
                .average()
                .orElse(0.0)
                : 0.0;
        parkingLotResponse.setTotalRatings(totalRatings);
        parkingLotResponse.setAverageRating(averageRating);

        parkingLotResponse.setAvailableSpots(availableSpotResponses);

        List<RatingEntity> top3Ratings = parkingLotRepository.getTop3RatingsByParkingLotId(parkingLotResponse.getId(), PageRequest.of(0, 3));
        List<Long> userIds = top3Ratings.stream().map(RatingEntity::getUserId).toList();
        log.info("Top3 Ratings {}", userIds);

        Map<Long, UserRatingResponse> userRatingResponseMap = userClient.getUserRating(userIds).getData();
        log.info(userRatingResponseMap.toString());
        List<RatingResponse> top3RatingResponses = top3Ratings.stream()
                .map(rating -> {
                    RatingResponse ratingResponse = RatingMapper.INSTANCE.toResponse(rating);
                    ratingResponse.setFullName(userRatingResponseMap.get(rating.getUserId()).getFullName());
                    ratingResponse.setAvatarUrl(userRatingResponseMap.get(rating.getUserId()).getAvatarUrl());
                    return ratingResponse;
                })
                .toList();
        parkingLotResponse.setRatings(top3RatingResponses);

        return parkingLotResponse;
    }


    @Override
    @Transactional
    public ParkingLotResponse addParkingLot(String userHeaderId, ParkingLotCreateRequest request) {
        if (userHeaderId == null || userHeaderId.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        validatePolicies(request.policyCreateRequests());
        validatePricingRules(request);

        Long partnerId = Long.parseLong(userHeaderId);
        ParkingLotEntity parkingLotEntity = ParkingLotMapper.INSTANCE.toEntity(request);
        parkingLotEntity.setLotSquare(request.lotSquare());
        parkingLotEntity.setIs24Hour(request.is24Hour());
        parkingLotEntity.setHorizonTime(request.horizonTime());
        parkingLotEntity.setPartnerId(partnerId);
        parkingLotEntity.setStatus(ParkingLotStatus.PENDING);
        parkingLotEntity.setLotCapacity(toLotCapacity(request.lotCapacityRequests(), parkingLotEntity));
        parkingLotEntity.setPricingRules(toPricingRules(request.pricingRuleCreateRequests(), parkingLotEntity));
        parkingLotEntity.setPolicies(toPolicies(request.policyCreateRequests(), parkingLotEntity));
        return ParkingLotMapper.INSTANCE.toResponse(parkingLotRepository.save(parkingLotEntity));
    }

    private List<LotCapacityEntity> toLotCapacity(List<LotCapacityCreateRequest> requests, ParkingLotEntity parkingLotEntity) {
        return requests.stream()
                .map(req -> LotCapacityEntity.builder()
                        .capacity(req.capacity())
                        .parkingLot(parkingLotEntity)
                        .vehicleType(req.vehicleType())
                        .supportElectricVehicle(req.supportElectricVehicle())
                        .build()).toList();
    }

    private List<PricingRuleEntity> toPricingRules(List<PricingRuleCreateRequest> requests, ParkingLotEntity parkingLotEntity) {
        return requests.stream()
                .map(req -> PricingRuleEntity.builder()
                        .parkingLot(parkingLotEntity)
                        .ruleName(req.ruleName())
                        .stepRate(req.stepRate())
                        .stepMinute(req.stepMinute())
                        .initialDurationMinute(req.initialDurationMinute())
                        .vehicleType(req.vehicleType())
                        .initialCharge(req.initialCharge())
                        .validFrom(req.validFrom())
                        .validUntil(req.validTo())
                        .syncStatus(SyncStatus.PENDING)
                        .isActive(true)
                        .build()
                )
                .toList();
    }

    private void validatePricingRules(ParkingLotCreateRequest request) {
        List<VehicleType> requestedVehicleTypes = request.pricingRuleCreateRequests()
                .stream()
                .map(PricingRuleCreateRequest::vehicleType)
                .toList();

        Set<VehicleType> pricingRuleVehicleTypes = new HashSet<>(requestedVehicleTypes);

        if (requestedVehicleTypes.size() != pricingRuleVehicleTypes.size()) {
            throw new AppException(
                    ErrorCode.DUPLICATE_PRICING_RULE
            );
        }

        Set<VehicleType> capacityVehicleTypes = request.lotCapacityRequests().stream().map(LotCapacityCreateRequest::vehicleType).collect(Collectors.toSet());

        if (!capacityVehicleTypes.containsAll(pricingRuleVehicleTypes)) {
            throw new AppException(ErrorCode.PRICING_RULE_CAPACITY_MISMATCH);
        }
    }

    private List<PolicyEntity> toPolicies(List<PolicyCreateRequest> requests, ParkingLotEntity parkingLotEntity) {
        return requests.stream()
                .map(req -> PolicyEntity.builder()
                        .parkingLot(parkingLotEntity)
                        .policyType(req.policyType())
                        .value(req.value())
                        .isActive(true)
                        .syncStatus(SyncStatus.PENDING)
                        .build())
                .toList();
    }

    private void validatePolicies(List<PolicyCreateRequest> policies) {
        if (policies.size() != PolicyType.values().length) {
            throw new AppException(ErrorCode.POLICY_NOT_ENOUGH, "There must be " + PolicyType.values().length + " policies");
        }

        Set<PolicyType> providedPolicies = policies.stream().map(PolicyCreateRequest::policyType).collect(Collectors.toSet());

        if (providedPolicies.size() != policies.size()) {
            throw new AppException(ErrorCode.DUPLICATE_POLICY, "Duplicate policy is not allowed");
        }

        Set<PolicyType> allTypes = EnumSet.allOf(PolicyType.class);
        if (!providedPolicies.containsAll(allTypes)) {
            Set<PolicyType> missingTypes = new HashSet<>(allTypes);
            missingTypes.removeAll(providedPolicies);
            throw new AppException(ErrorCode.MISSING_POLICY, "Missing policies: " + missingTypes);
        }
    }

    @Override
    @Transactional
    public ParkingLotResponse updateParkingLot(Long id, ParkingLotUpdateRequest request) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        com.parkmate.client.response.OperationalPaymentResponse paymentResponse = null;

        if (request.name() != null) parkingLotEntity.setName(request.name());
        if (request.city() != null) parkingLotEntity.setCity(request.city());
        if (request.streetAddress() != null) parkingLotEntity.setStreetAddress(request.streetAddress());
        if (request.ward() != null) parkingLotEntity.setWard(request.ward());
        if (request.operatingHoursEnd() != null) parkingLotEntity.setOperatingHoursEnd(request.operatingHoursEnd());
        if (request.operatingHoursStart() != null)
            parkingLotEntity.setOperatingHoursStart(request.operatingHoursStart());
        if (request.latitude() != null) parkingLotEntity.setLatitude(request.latitude());
        if (request.longitude() != null) parkingLotEntity.setLongitude(request.longitude());
        if (request.totalFloors() != null) parkingLotEntity.setTotalFloors(request.totalFloors());
        if (request.status() != null) {
            parkingLotEntity.setStatus(request.status());
            if ((request.status() == ParkingLotStatus.REJECTED || request.status() == ParkingLotStatus.MAP_DENIED) && request.reason() == null) {
                throw new AppException(ErrorCode.REASON_REQUIRED, "Reason is required for REJECTED or MAP_DENIED parking lot");
            }

            parkingLotEntity.setReason(request.reason());
            if (request.status() == ParkingLotStatus.PENDING_PAYMENT) {
                paymentResponse = createInitialOperationalFeePaymentRequest(parkingLotEntity);
            }
        }
        if (request.is24Hour() != null) parkingLotEntity.setIs24Hour(request.is24Hour());
        if (request.horizonTime() != null) parkingLotEntity.setHorizonTime(request.horizonTime());

        ParkingLotResponse response = ParkingLotMapper.INSTANCE.toResponse(parkingLotRepository.save(parkingLotEntity));

        // Populate payment information if payment was created
        if (paymentResponse != null) {
            response.setPaymentUrl(paymentResponse.paymentLink());
            response.setPaymentQrCode(paymentResponse.qrCode());
            response.setOperationalPaymentId(paymentResponse.id());
            response.setPaymentStatus(paymentResponse.paymentStatus());
            response.setOperationalFee(paymentResponse.totalFee());
            response.setPaymentDueDate(paymentResponse.dueDate());
            response.setPaymentPaidAt(paymentResponse.paidAt());
            response.setAreaFee(paymentResponse.areaFee());
            response.setDeviceFee(paymentResponse.deviceFee());
            response.setDevicePaymentItems(paymentResponse.devicePaymentItems());

        }

        return response;
    }


    @Override
    public ParkingLotResponse deleteParkingLot(Long id) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        if (parkingLotEntity.getStatus().equals(ParkingLotStatus.PENDING)) {
            throw new AppException(ErrorCode.INVALID_PARKING_LOT_STATUS_TRANSITION);
        }

        parkingLotEntity.setStatus(ParkingLotStatus.INACTIVE);
        return ParkingLotMapper.INSTANCE.toResponse(parkingLotRepository.save(parkingLotEntity));
    }

    @Override
    public ParkingLotDetailedResponse getParkingLotByIdAndVehicleType(Long id, VehicleType vehicleType) {
        ParkingLotEntity parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        ParkingLotDetailedResponse parkingLotDetailedResponse = ParkingLotDetailedResponse.builder()
                .id(parkingLot.getId())
                .partnerId(parkingLot.getPartnerId())
                .name(parkingLot.getName())
                .streetAddress(parkingLot.getStreetAddress())
                .ward(parkingLot.getWard())
                .city(parkingLot.getCity())
                .latitude(parkingLot.getLatitude())
                .longitude(parkingLot.getLongitude())
                .openTime(parkingLot.getOperatingHoursStart())
                .closeTime(parkingLot.getOperatingHoursEnd())
                .is24Hour(parkingLot.getIs24Hour())
                .status(parkingLot.getStatus())
                .reason(parkingLot.getReason())
                .createdAt(parkingLot.getCreatedAt())
                .updatedAt(parkingLot.getUpdatedAt())
                .build();

        List<LotCapacityResponse> filteredLotCapacity = parkingLot.getLotCapacity().stream()
                .filter(capacity -> capacity.getVehicleType() == vehicleType)
                .map(LotCapacityMapper.INSTANCE::toResponse)
                .toList();
        parkingLotDetailedResponse.setLotCapacity(filteredLotCapacity);

        List<FloorResponse> floorResponses = parkingLot.getParkingFloors().stream().map(
                floor -> {
                    List<FloorCapacityResponse> floorCapacityResponses = floor.getParkingFloorCapacity().stream()
                            .filter(capacity -> capacity.getVehicleType() == vehicleType)
                            .map(FloorCapacityMapper.INSTANCE::toResponse)
                            .toList();
                    if (floorCapacityResponses.isEmpty()) return null;

                    return FloorResponse.builder()
                            .id(floor.getId())
                            .floorNumber(floor.getFloorNumber())
                            .floorName(floor.getFloorName())
                            .isActive(floor.getIsActive())
                            .parkingFloorCapacity(floorCapacityResponses)
                            .build();
                }
        ).toList();

        List<PricingRuleResponse> pricingRuleResponses = parkingLot.getPricingRules()
                .stream().filter(pricingRule -> pricingRule.getVehicleType() == vehicleType && pricingRule.getIsActive())
                .map(PricingRuleMapper.INSTANCE::toResponse)
                .toList();

        parkingLotDetailedResponse.setImages(parkingLot.getImages().stream().map(image -> {
            ImageResponse imageResponse = ImageMapper.INSTANCE.toResponse(image);
            imageResponse.setPath(s3Service.getPresignedUrl(image.getPath()));
            return imageResponse;
        }).toList());
        parkingLotDetailedResponse.setPricingRules(pricingRuleResponses);
        parkingLotDetailedResponse.setParkingFloors(floorResponses);

        return parkingLotDetailedResponse;
    }

    @Override
    public Long count() {
        return parkingLotRepository.count();
    }

    @Override
    public ParkingLotAvailableReservationSpotResponse countAvailableSpot(Long id, LocalDateTime reservedFrom, Integer assumedStayMinute, VehicleType vehicleType) {
        ParkingLotEntity parkingLot = parkingLotRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));
        int occupiedSpotByWalkIns = getCurrentWalkIn(id, reservedFrom, Math.round(parkingLot.getHorizonTime()), vehicleType);
        log.info("Occupied Spot By WalkIns: {}", occupiedSpotByWalkIns);
        Long emergencySpots = areaRepository.countEmergencySpot(id);
        if (emergencySpots == null) emergencySpots = 0L;
        log.info("Emergency Spots: {}", emergencySpots);
        try {
            PricingRuleEntity pricingRule = pricingRuleRepository.findByParkingLot_IdAndIsActiveAndVehicleType(id, true, vehicleType)
                    .orElse(null);

            // Nếu không tìm thấy pricing rule (vehicleType không được support), trả về 0/0
            if (pricingRule == null) {
                return ParkingLotAvailableReservationSpotResponse.builder()
                        .vehicleType(vehicleType)
                        .pricing(null)
                        .totalCapacity(0L)
                        .availableCapacity(0L)
                        .build();
            }

            Double estimateTotalFee = pricingRule.getInitialCharge() + Math.ceil(((float) (assumedStayMinute - pricingRule.getInitialDurationMinute()) / pricingRule.getStepMinute())) * pricingRule.getStepRate();
            log.info("Estimate Total Fee: {}", estimateTotalFee);
            if (assumedStayMinute < parkingLot.getHorizonTime()) {
                assumedStayMinute = Math.toIntExact(Math.round(parkingLot.getHorizonTime()));
            }
            log.info("Assumed Stay Minute: {}", assumedStayMinute);
            long totalCapacity = parkingLot.getLotCapacity()
                    .stream().filter(capacity -> (capacity.getVehicleType() == vehicleType && capacity.getIsActive())).mapToLong(LotCapacityEntity::getCapacity).sum();
            log.info("Total Capacity: {}", totalCapacity);
            Long overLapReservations = userClient.countReservation(id, reservedFrom, assumedStayMinute, vehicleType).getData();
            if (overLapReservations == null) overLapReservations = 0L;
            log.info("OverLap Reservations: {}", overLapReservations);

            Long temporaryHolds;
            try {
                temporaryHolds = userClient.countTemporaryHolds(
                        id,
                        vehicleType.name(),
                        reservedFrom.toString(),
                        assumedStayMinute
                ).getData();
                if (temporaryHolds == null) temporaryHolds = 0L;
                log.info("Temporary Holds: {}", temporaryHolds);
            } catch (Exception e) {
                log.warn("Failed to count temporary holds, assuming 0: {}", e.getMessage());
                temporaryHolds = 0L;
            }

            Long availableSpots = totalCapacity - (emergencySpots + occupiedSpotByWalkIns + overLapReservations + temporaryHolds);
            log.info("Available Spots: {} (Total: {} - Emergency: {} - WalkIns: {} - Reservations: {} - TempHolds: {})",
                    availableSpots, totalCapacity, emergencySpots, occupiedSpotByWalkIns, overLapReservations, temporaryHolds);

            PricingRuleSimpleResponse pricingRuleSimpleResponse = PricingRuleSimpleResponse.builder()
                    .id(pricingRule.getId())
                    .initialCharge(pricingRule.getInitialCharge())
                    .initialDurationMinute(pricingRule.getInitialDurationMinute())
                    .stepRate(pricingRule.getStepRate())
                    .stepMinute(pricingRule.getStepMinute())
                    .estimateTotalFee(estimateTotalFee)
                    .build();

            return ParkingLotAvailableReservationSpotResponse.builder()
                    .vehicleType(vehicleType)
                    .pricing(pricingRuleSimpleResponse)
                    .totalCapacity(totalCapacity)
                    .availableCapacity(availableSpots)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getCurrentWalkIn(Long parkingLotId, LocalDateTime entryTimeFrom, Long horizon, VehicleType vehicleType) {
        return sessionRepository.countActiveWalkInsSince(parkingLotId, entryTimeFrom.minusMinutes(horizon), vehicleType);
    }

    @Override
    public boolean supportsVehicleType(Long parkingLotId, VehicleType vehicleType) {
        ParkingLotEntity parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        return parkingLot.getLotCapacity().stream()
                .anyMatch(capacity -> capacity.getVehicleType() == vehicleType
                        && capacity.getIsActive()
                        && capacity.getCapacity() != null
                        && capacity.getCapacity() > 0);
    }

    private com.parkmate.client.response.OperationalPaymentResponse createInitialOperationalFeePaymentRequest(ParkingLotEntity parkingLot) {
        log.info("Creating operational fee payment for parking lot: {}, partner: {}, area: {} sqm",
                parkingLot.getId(), parkingLot.getPartnerId(), parkingLot.getLotSquare());

        try {
            List<CreateDeviceItemPaymentRequest> createDeviceItemPaymentRequests =
                    parkingLot.getDevices().stream().collect(Collectors.groupingBy(
                            DeviceEntity::getDeviceType,
                            Collectors.counting()
                    )).entrySet().stream().map(entry ->
                            new CreateDeviceItemPaymentRequest(entry.getKey(), Math.toIntExact(entry.getValue())))
                            .toList();
            com.parkmate.client.request.CreateOperationalPaymentRequest request =
                    com.parkmate.client.request.CreateOperationalPaymentRequest.builder()
                            .lotId(parkingLot.getId())
                            .partnerId(parkingLot.getPartnerId())
                            .lotAreaSqm(parkingLot.getLotSquare())
                            .deviceItemPaymentRequests(createDeviceItemPaymentRequests)
                            .build();

            var response = paymentClient.createOperationalPayment(request);

            if (response != null && response.getBody() != null && response.getBody().isSuccess()) {
                var paymentData = response.getBody().getData();
                log.info("✓ Operational payment created successfully - paymentId: {}, link: {}, fee: {}",
                        paymentData.id(), paymentData.paymentLink(), paymentData.totalFee());

                // Return payment data to be included in the response
                return paymentData;

            } else {
                log.error("Failed to create operational payment - response: {}", response);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION,
                        "Failed to create operational payment for parking lot");
            }

        } catch (Exception e) {
            log.error("Error creating operational payment for lot {}", parkingLot.getId(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION,
                    "Failed to create operational payment: " + e.getMessage());
        }
    }

    /**
     * Activates a parking lot after operational fee payment is confirmed
     * This method is called from payment-service via internal API
     *
     * @param lotId The parking lot ID to activate
     */
    @Override
    @Transactional
    public void activateParkingLot(Long lotId) {
        log.info("Activating parking lot: {}", lotId);

        ParkingLotEntity parkingLot = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        // Validate current status
        if (parkingLot.getStatus() != ParkingLotStatus.PENDING_PAYMENT) {
            log.warn("Cannot activate parking lot {} - current status: {}",
                    lotId, parkingLot.getStatus());
            throw new AppException(ErrorCode.INVALID_PARKING_LOT_STATUS_TRANSITION,
                    "Parking lot must be in PENDING_PAYMENT status to activate");
        }

        // Update status to ACTIVE
        parkingLot.setStatus(ParkingLotStatus.ACTIVE);
        parkingLotRepository.save(parkingLot);

        log.info("✓ Parking lot {} activated successfully", lotId);
    }

    @Override
    public List<ParkingLotBasicInfo> getAllParkingLotsBasicInfo() {
        log.debug("Getting all parking lots basic info");

        List<ParkingLotEntity> parkingLots = parkingLotRepository.findAll();

        return parkingLots.stream()
                .map(lot -> ParkingLotBasicInfo.builder()
                        .id(lot.getId())
                        .name(lot.getName())
                        .partnerId(lot.getPartnerId())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ParkingLotBasicInfo> getParkingLotsByPartner(Long partnerId) {
        log.debug("Getting parking lots for partner: {}", partnerId);

        List<ParkingLotEntity> parkingLots = parkingLotRepository.findByPartnerId(partnerId);
        List<ParkingLotBasicInfo> lotBasicInfos = new ArrayList<>();
        parkingLots.forEach(parkingLot -> {
            List<CreateDeviceItemPaymentRequest> createDeviceItemPaymentRequests =
                    parkingLot.getDevices().stream().collect(Collectors.groupingBy(
                                    DeviceEntity::getDeviceType,
                                    Collectors.counting()
                            )).entrySet().stream().map(entry ->
                                    new CreateDeviceItemPaymentRequest(entry.getKey(), Math.toIntExact(entry.getValue())))
                            .toList();
            lotBasicInfos.add(ParkingLotBasicInfo.builder()
                    .id(parkingLot.getId())
                    .name(parkingLot.getName())
                    .partnerId(parkingLot.getPartnerId())
                    .deviceItemPaymentRequests(createDeviceItemPaymentRequests)
                    .build());
        });


        return lotBasicInfos;
    }

    @Override
    public Map<Long, Long> getParkingLotCountByPartner(List<Long> partnerIds) {
        List<ParkingLotCountProjection> results = parkingLotRepository.countByPartnerIds(partnerIds);
        Map<Long, Long> dbCounts = results.stream()
                .collect(Collectors.toMap(
                        ParkingLotCountProjection::getPartnerId,
                        ParkingLotCountProjection::getTotal
                ));
        Map<Long, Long> finalCountMap = new HashMap<>();

        for (Long id : partnerIds) {
            finalCountMap.put(id, dbCounts.getOrDefault(id, 0L));
        }

        return finalCountMap;
    }
}
