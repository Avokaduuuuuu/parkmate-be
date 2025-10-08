package com.parkmate.session;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.ParkingLotRepository;
import com.parkmate.pricing_rule.PricingRuleEntity;
import com.parkmate.pricing_rule.PricingRuleRepository;
import com.parkmate.session.dto.req.SessionCreateRequest;
import com.parkmate.session.dto.req.SessionUpdateRequest;
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
import java.time.temporal.ChronoUnit;

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
        sessionEntity.setVehicleId(request.vehicleId());
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
    public Page<SessionResponse> getSessions(int page, int size, String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SessionEntity> sessionEntities = sessionRepository.findAll(pageable);
        return sessionEntities.map(SessionMapper.INSTANCE::toResponse);
    }

    @Override
    public SessionResponse getSession(String cardUUID) {
        return SessionMapper.INSTANCE.toResponse(
                sessionRepository.findByCardUUID(cardUUID)
                        .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND, "Session with Card UUID " + cardUUID + " not found"))
        );
    }

    @Override
    public SessionResponse updateSession(String cardUUID, SessionUpdateRequest request) {
        SessionEntity sessionEntity = sessionRepository.findByCardUUIDAndStatus(cardUUID, SessionStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND, "Session with Card UUID " + cardUUID + " not found"));
        PricingRuleEntity pricingRuleEntity = sessionEntity.getPricingRule();
        Long durationMinutes = ChronoUnit.MINUTES.between(sessionEntity.getEntryTime(), request.exitTime());
        if (pricingRuleEntity.getFreeMinute() < durationMinutes) {
            long remainingMinutes = durationMinutes - pricingRuleEntity.getInitialDurationMinute();
            if (remainingMinutes > 0) {
                BigDecimal total = sessionEntity.getTotalAmount();
                Long block = (long) Math.ceil((double) remainingMinutes / pricingRuleEntity.getGracePeriodMinute());
                total = total.add(BigDecimal.valueOf(block * pricingRuleEntity.getBaseRate()));
                sessionEntity.setTotalAmount(total);
            } else {
                sessionEntity.setTotalAmount(BigDecimal.valueOf(pricingRuleEntity.getInitialCharge()));
            }
        } else {
            sessionEntity.setTotalAmount(BigDecimal.ZERO);
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
    public SessionResponse deleteSession(String cardUUID) {
        SessionEntity sessionEntity = sessionRepository.findByCardUUIDAndStatus(cardUUID, SessionStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND, "Session with Card UUID " + cardUUID + " not found"));
        sessionEntity.setStatus(SessionStatus.DELETED);

        return SessionMapper.INSTANCE.toResponse(sessionRepository.save(sessionEntity));
    }
}
