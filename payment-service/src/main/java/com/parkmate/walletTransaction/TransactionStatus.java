package com.parkmate.walletTransaction;

/**
 * Transaction status enum for internal use in payment-service
 * Note: DTOs use String to avoid serialization issues with other services
 */
public enum TransactionStatus {
    PENDING,         // Đang chờ xử lý
    PROCESSING,      // Đang xử lý
    COMPLETED,       // Hoàn thành
    FAILED,          // Thất bại (includes expired/timeout cases)
    CANCELLED,       // Đã hủy
    REVERSED         // Hoàn trả
}