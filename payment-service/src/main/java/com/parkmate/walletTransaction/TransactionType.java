package com.parkmate.walletTransaction;

public enum TransactionType {
    TOP_UP,
    DEDUCTION,       // Trừ tiền (đặt cọc, thanh toán)
    REFUND,          // Hoàn tiền
    REVERSAL,        // Đảo ngược giao dịch
    PENALTY,
    SUBSCRIPTION
}