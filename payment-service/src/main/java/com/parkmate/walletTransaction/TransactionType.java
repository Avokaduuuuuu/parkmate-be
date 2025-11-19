package com.parkmate.walletTransaction;

public enum TransactionType {
    TOP_UP,                   // Nạp tiền vào ví
    CASH_OUT,                 // Rút tiền (withdrawal)
    DEDUCTION,                // Trừ tiền (đặt cọc, thanh toán, subscription)
    REFUND,                   // Hoàn tiền
    REVERSAL,                 // Đảo ngược giao dịch
    PENALTY,                  // Phạt
    SUBSCRIPTION_PAYMENT,     // Thanh toán subscription
    RESERVATION_PAYMENT       // Thanh toán reservation
}