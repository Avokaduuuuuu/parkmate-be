package com.parkmate.scheduler;

import com.parkmate.client.UserClient;
import com.parkmate.pricing_rule.PricingRuleEntity;
import com.parkmate.session.SessionEntity;
import com.parkmate.session.SessionRepository;
import com.parkmate.session.enums.SessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionBalanceScheduler {

    private final SessionRepository sessionRepository;
    private final UserClient userClient;

    /**
     * Scheduled task to check wallet balance for active sessions
     * Runs every 5 minutes to check if users have sufficient balance for ongoing sessions
     */
    @Scheduled(fixedRate = 300000, zone = "Asia/Ho_Chi_Minh") // Every 5 minutes
    @Transactional(readOnly = true)
    public void checkActiveSessionsWalletBalance() {
        LocalDateTime now = LocalDateTime.now().plusHours(7);

        log.info("💰 [SCHEDULER] Task: Starting wallet balance check for active sessions at {}", now);

        try {
            // Find all ACTIVE sessions with userId (member sessions only)
            List<SessionEntity> activeSessions = sessionRepository.findAll()
                    .stream()
                    .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                    .filter(s -> s.getUserId() != null)
                    .toList();

            log.info("💰 [SCHEDULER] Task: Found {} active member sessions to check", activeSessions.size());

            int checkedCount = 0;
            int insufficientBalanceCount = 0;
            int errorCount = 0;

            for (SessionEntity session : activeSessions) {
                try {
                    // Calculate current projected cost based on pricing rule
                    BigDecimal projectedCost = calculateProjectedCost(session, now);

                    log.debug("💰 [SCHEDULER] Checking session {} - User: {}, Current cost: {}",
                            session.getId(), session.getUserId(), projectedCost);

                    // Check wallet balance via user-service
                    try {
                        userClient.checkWalletBalance(
                                session.getUserId(),
                                session.getId().toString(),
                                projectedCost
                        );
                        checkedCount++;
                    } catch (Exception e) {
                        log.warn("⚠️ [SCHEDULER] Insufficient balance for session {} - User: {}, Required: {}",
                                session.getId(), session.getUserId(), projectedCost);
                        insufficientBalanceCount++;
                    }

                } catch (Exception e) {
                    log.error("❌ [SCHEDULER] Error checking session {}", session.getId(), e);
                    errorCount++;
                }
            }

            log.info("✅ [SCHEDULER] Task completed. Checked: {}, Insufficient balance: {}, Errors: {}",
                    checkedCount, insufficientBalanceCount, errorCount);

        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Fatal error in checkActiveSessionsWalletBalance", e);
        }
    }

    /**
     * Calculate projected cost for a session based on current time and pricing rule
     *
     * @param session The active session
     * @param currentTime Current timestamp
     * @return Projected total cost
     */
    private BigDecimal calculateProjectedCost(SessionEntity session, LocalDateTime currentTime) {
        PricingRuleEntity pricingRule = session.getPricingRule();

        if (pricingRule == null) {
            log.warn("⚠️ [SCHEDULER] Session {} has no pricing rule, using stored totalAmount", session.getId());
            return session.getTotalAmount() != null ? session.getTotalAmount() : BigDecimal.ZERO;
        }

        // Calculate duration from entry time to current time
        long durationMinutes = ChronoUnit.MINUTES.between(session.getEntryTime(), currentTime);

        // Calculate remaining minutes after initial period
        long remainingMinutes = durationMinutes - pricingRule.getInitialDurationMinute();

        BigDecimal projectedCost;

        if (remainingMinutes > 0) {
            // Calculate number of step blocks
            Long stepBlocks = (long) Math.ceil((double) remainingMinutes / pricingRule.getStepMinute());

            // Initial charge + (step blocks * step rate)
            BigDecimal initialCharge = BigDecimal.valueOf(
                    pricingRule.getInitialCharge() != null ? pricingRule.getInitialCharge() : 0.0
            );
            BigDecimal stepCharges = BigDecimal.valueOf(stepBlocks * pricingRule.getStepRate());

            projectedCost = initialCharge.add(stepCharges);
        } else {
            // Still within initial duration
            projectedCost = BigDecimal.valueOf(
                    pricingRule.getInitialCharge() != null ? pricingRule.getInitialCharge() : 0.0
            );
        }

        return projectedCost;
    }
}
