package com.parkmate.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.client.PaymentClient;
import com.parkmate.client.constants.TransactionConstants;
import com.parkmate.client.dto.request.CreateTransactionRequest;
import com.parkmate.client.dto.response.WalletTransactionResponse;
import com.parkmate.common.dto.ApiResponse;
import com.parkmate.common.enums.ReservationStatus;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.common.util.QRCodeGenerator;
import com.parkmate.reservation.dto.CreateReservationRequest;
import com.parkmate.reservation.dto.ReservationResponse;
import com.parkmate.reservation.dto.ReservationSearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final PaymentClient paymentClient;
    private final ReservationMapper reservationMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request, String userId) {

        if (userId != null && request.isOwnedByMe()) {
            long userIdLong = Long.parseLong(userId);
            request.setUserId(userIdLong);
        }

        if (request.getUserId() == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        // Create reservation with PENDING_PAYMENT status
        Reservation reservation = Reservation.builder()
                .userId(request.getUserId())
                .spotId(request.getSpotId())
                .reservedFrom(request.getReservedFrom())
                .reservationFee(request.getReservationFee())
                .vehicleId(request.getVehicleId())
                .parkingLotId(request.getParkingLotId())
                .status(ReservationStatus.PENDING)
                .build();

        reservation = reservationRepository.save(reservation);

        // Deduct wallet balance
        try {
            ResponseEntity<ApiResponse<WalletTransactionResponse>> paymentResult = paymentClient.deductWallet(
                    CreateTransactionRequest.builder()
                            .userId(request.getUserId())
                            .amount(request.getReservationFee())
                            .transactionType(TransactionConstants.TYPE_DEDUCTION)
                            .referenceId(reservation.getId().toString())
                            .description("Reservation fee for spot ID: " + request.getSpotId())
                            .build()
            );

            if (!paymentResult.hasBody()) {
                log.error("Payment service returned empty response for reservation ID: {}", reservation.getId());
                throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED);
            }
            if (paymentResult.getBody() != null && !paymentResult.getBody().success()) {
                log.info("Payment successful for reservation ID: {}, transaction ID: {}",
                        reservation.getId(), paymentResult.getBody().data().getSessionId());
                throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, paymentResult.getBody().message());
            }


            // Update reservation status to CONFIRMED
            reservation.setStatus(ReservationStatus.PENDING);
            reservationRepository.save(reservation);

        } catch (Exception e) {
            log.error("Payment failed for reservation ID: {}", reservation.getId(), e);
            // Update reservation status to PAYMENT_FAILED
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED);
        }

        // Generate QR code with reservation information
        return getReservationResponse(reservation);
    }

    /**
     * Generate QR code content as JSON string
     */
    private String generateQRCodeContent(Reservation reservation) {
        try {
            Map<String, Object> qrData = new HashMap<>();
            qrData.put("reservationId", reservation.getId());
            qrData.put("userId", reservation.getUserId());
            qrData.put("vehicleId", reservation.getVehicleId());
            qrData.put("parkingLotId", reservation.getParkingLotId());
            qrData.put("spotId", reservation.getSpotId());
            qrData.put("reservationFee", reservation.getReservationFee());
            qrData.put("reservedFrom", reservation.getReservedFrom().toString());
            qrData.put("status", reservation.getStatus().name());
            qrData.put("createdAt", reservation.getCreatedAt() != null ? reservation.getCreatedAt().toString() : null);

            return objectMapper.writeValueAsString(qrData);
        } catch (Exception e) {
            log.error("Error generating QR code content for reservation ID: {}", reservation.getId(), e);
            // Fallback to simple format
            return String.format("RESERVATION:%d|USER:%d|SPOT:%d|LOT:%d",
                    reservation.getId(),
                    reservation.getUserId(),
                    reservation.getSpotId(),
                    reservation.getParkingLotId());
        }
    }

    @Override
    public ReservationResponse getReservationById(Long id) {
        return null;
    }

    @Override
    public void cancelReservation(Long id) {

    }

    @Override
    public Page<ReservationResponse> getReservations(int page, int size, String sortBy, String sortOrder, ReservationSearchCriteria criteria, String userIdHeader) {
        // Handle ownedByMe flag
        if (criteria != null && criteria.isOwnedByMe() && userIdHeader != null) {
            long userIdLong = Long.parseLong(userIdHeader);
            criteria.setUserId(userIdLong);
        }

        // Create pageable
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);

        // Build predicate from criteria
        com.querydsl.core.types.Predicate predicate = ReservationSpecification.buildPredicate(criteria);

        // Query with predicate
        Page<Reservation> reservations = reservationRepository.findAll(predicate, pageable);

        // Map to response with QR code
        return reservations.map(this::getReservationResponse);
    }

    @NonNull
    private ReservationResponse getReservationResponse(Reservation reservation) {
        ReservationResponse response = reservationMapper.toResponse(reservation);
        String qrCodeContent = generateQRCodeContent(reservation);
        String qrCodeBase64 = QRCodeGenerator.generateQRCodeBase64(qrCodeContent);

        return new ReservationResponse(
                response.id(),
                response.userId(),
                response.vehicleId(),
                response.parkingLotId(),
                response.spotId(),
                response.reservationFee(),
                response.reservedFrom(),
                response.status(),
                qrCodeBase64
        );
    }

}
