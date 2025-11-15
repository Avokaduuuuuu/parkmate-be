package com.parkmate.walletTransaction;

public enum TransactionType {
    TOP_UP,
    CASH_OUT,        // Rút tiền (withdrawal)
    DEDUCTION,       // Trừ tiền (đặt cọc, thanh toán)
    REFUND,          // Hoàn tiền
    REVERSAL,        // Đảo ngược giao dịch
    PENALTY,
    SUBSCRIPTION
}