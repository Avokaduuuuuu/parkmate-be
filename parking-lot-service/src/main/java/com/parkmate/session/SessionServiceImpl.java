package com.parkmate.session;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.parking_lot.ParkingLotRepository;
import com.parkmate.pricing_rule.PricingRuleEntity;
import com.parkmate.pricing_rule.PricingRuleRepository;
import com.parkmate.session.dto.req.SessionCreateRequest;
import com.parkmate.session.dto.req.SessionSyncRequest;
import com.parkmate.session.dto.req.SessionUpdateRequest;
import com.parkmate.session.dto.resp.SessionDetailedResponse;
import com.parkmate.session.dto.resp.SessionResponse;
import com.parkmate.session.enums.SessionStatus;
import com.parkmate.session.enums.SessionType;
import com.parkmate.session.enums.SyncStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService{
    private final SessionRepository sessionRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final PricingRuleRepository pricingRuleRepository;

    @Override
    public SessionResponse createSession(Long lotId,SessionCreateRequest request) {
        SessionEntity sessionEntity = new SessionEntity();
        if (request.userId() != null) {
            sessionEntity.setUserId(request.userId());
            sessionEntity.setSessionType(SessionType.MEMBER);
        }else {
            sessionEntity.setSessionType(SessionType.OCCASIONAL);
        }
        sessionEntity.setLicensePlate(request.licensePlate());
        sessionEntity.setEntryTime(request.entryTime());
        sessionEntity.setAuthMethod(request.authMethod());
        sessionEntity.setStatus(SessionStatus.ACTIVE);
        sessionEntity.setSyncStatus(SyncStatus.PENDING);
        sessionEntity.setParkingLot(parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND))
        );
        PricingRuleEntity pricingRuleEntity = pricingRuleRepository.findById(request.pricingRuleId())
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_FOUND));
        sessionEntity.setPricingRule(pricingRuleEntity);
        sessionEntity.setCardUUID(request.cardUUID());

        if (pricingRuleEntity.getInitialCharge() != null && pricingRuleEntity.getInitialCharge() != 0) {
            sessionEntity.setTotalAmount(BigDecimal.valueOf(pricingRuleEntity.getInitialCharge()));
        }
        return SessionMapper.INSTANCE.toResponse(sessionRepository.save(sessionEntity));
    }

    @Override
    public Page<SessionResponse> getSessions(
            String userIdHeader,
            int page, int size, String sortBy, String sortOrder, SessionFilterParams params) {
        Long userId = userIdHeader == null ? null : Long.parseLong(userIdHeader);
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SessionEntity> sessionEntities = sessionRepository.findAll(params.getSpecification(userId),pageable);
        return sessionEntities.map(SessionMapper.INSTANCE::toResponse);
    }

    @Override
    public SessionDetailedResponse getSession(String id) {
        return SessionMapper.INSTANCE.toDetailedResponse(
                sessionRepository.findById(UUID.fromString(id))
                        .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND, "Session with Card UUID " + id + " not found"))
        );
    }

    @Override
    public SessionResponse updateSession(String cardUUID, SessionUpdateRequest request) {
        SessionEntity sessionEntity = sessionRepository.findByCardUUIDAndStatus(cardUUID, SessionStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND, "Session with Card UUID " + cardUUID + " not found"));
        PricingRuleEntity pricingRuleEntity = sessionEntity.getPricingRule();
        long durationMinutes = ChronoUnit.MINUTES.between(sessionEntity.getEntryTime(), request.exitTime());
        long remainingMinutes = durationMinutes - pricingRuleEntity.getInitialDurationMinute();
        if (remainingMinutes > 0) {
            BigDecimal total = sessionEntity.getTotalAmount();
            Long block = (long) Math.ceil((double) remainingMinutes / pricingRuleEntity.getStepMinute());
            total = total.add(BigDecimal.valueOf(block * pricingRuleEntity.getStepRate()));
            sessionEntity.setTotalAmount(total);
        } else {
            sessionEntity.setTotalAmount(BigDecimal.valueOf(pricingRuleEntity.getInitialCharge()));
        }

        sessionEntity.setExitTime(sessionEntity.getEntryTime());
        sessionEntity.setNote(request.note());
        sessionEntity.setDurationMinute(Math.toIntExact(durationMinutes));
        sessionEntity.setStatus(SessionStatus.COMPLETED);
        return SessionMapper.INSTANCE.toResponse(sessionRepository.save(sessionEntity));
    }

    @Override
    public Long count() {
        return sessionRepository.count();
    }

    @Override
    public void deleteSession(String cardUUID) {
        SessionEntity sessionEntity = sessionRepository.findByCardUUIDAndStatus(cardUUID, SessionStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND, "Session with Card UUID " + cardUUID + " not found"));
        sessionRepository.delete(sessionEntity);
    }

    @Override
    public Integer syncSessions(Long lotId, List<SessionSyncRequest> requests) {
        ParkingLotEntity parkingLot = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        // Group sessions by pricingRuleId (skip nulls)
        Map<Long, List<SessionSyncRequest>> groupedByRule = requests.stream()
                .filter(req -> req.pricingRuleId() != null)
                .collect(Collectors.groupingBy(SessionSyncRequest::pricingRuleId));

        if (groupedByRule.isEmpty()) return 0;

        // Load all pricing rules once
        Map<Long, PricingRuleEntity> pricingRules = pricingRuleRepository.findAllById(groupedByRule.keySet())
                .stream()
                .collect(Collectors.toMap(PricingRuleEntity::getId, r -> r));

        // Flatten grouped map -> list of SessionEntity
        List<SessionEntity> sessions = groupedByRule.entrySet().stream()
                .flatMap(entry -> {
                    Long ruleId = entry.getKey();
                    PricingRuleEntity rule = Optional.ofNullable(pricingRules.get(ruleId))
                            .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_FOUND));
                    return entry.getValue().stream()
                            .map(req -> getSessionEntity(req, parkingLot, rule));
                })
                .toList();

        sessionRepository.saveAll(sessions);
        return sessions.size();
    }


    private static SessionEntity getSessionEntity(SessionSyncRequest request, ParkingLotEntity parkingLotEntity, PricingRuleEntity pricingRuleEntity) {
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setId(request.id());
        sessionEntity.setParkingLot(parkingLotEntity);
        sessionEntity.setUserId(request.userId());
        sessionEntity.setSessionType(request.sessionType());
        sessionEntity.setLicensePlate(request.licensePlate());
        sessionEntity.setEntryTime(request.entryTime());
        sessionEntity.setExitTime(request.exitTime());
        sessionEntity.setAuthMethod(request.authMethod());
        sessionEntity.setStatus(request.status());
        sessionEntity.setSyncStatus(SyncStatus.SYNCED);
        sessionEntity.setPricingRule(pricingRuleEntity);
        sessionEntity.setCardUUID(request.cardUUID());
        sessionEntity.setVehicleType(request.vehicleType());
        sessionEntity.setDurationMinute(request.durationMinute());
        sessionEntity.setTotalAmount(request.totalAmount());
        sessionEntity.setReferenceId(request.referenceId());
        sessionEntity.setNote(request.note());
        sessionEntity.setReferenceType(request.referenceType());
        sessionEntity.setSyncedFromLocal(LocalDateTime.now());
        return sessionEntity;
    }
}
