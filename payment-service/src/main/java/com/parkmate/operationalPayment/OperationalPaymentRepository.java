package com.parkmate.operationalPayment;

import com.parkmate.operationalPayment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperationalPaymentRepository extends JpaRepository<OperationalPaymentEntity, Long> {

    List<OperationalPaymentEntity> findByLotId(Long lotId);

    Optional<OperationalPaymentEntity> findByPaymentTransactionId(String paymentTransactionId);

    List<OperationalPaymentEntity> findByPartnerId(Long partnerId);

    List<OperationalPaymentEntity> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT op FROM OperationalPaymentEntity op WHERE op.lotId = :lotId ORDER BY op.createdAt DESC LIMIT 1")
    Optional<OperationalPaymentEntity> findLatestByLotId(Long lotId);

    @Query("SELECT COUNT(op) > 0 FROM OperationalPaymentEntity op WHERE op.lotId = :lotId AND op.paymentStatus = 'PAID'")
    boolean hasAnyPaidPayment(Long lotId);
}