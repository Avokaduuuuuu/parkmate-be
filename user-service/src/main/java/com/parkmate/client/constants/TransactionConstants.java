package com.parkmate.client.constants;

/**
 * Constants for wallet transaction types
 * These must match the enum values in payment-service
 */
public class TransactionConstants {

    // Transaction Types
    public static final String TYPE_TOP_UP = "TOP_UP";
    public static final String TYPE_DEDUCTION = "DEDUCTION";
    public static final String TYPE_REFUND = "REFUND";
    public static final String TYPE_REVERSAL = "REVERSAL";
    public static final String TYPE_PENALTY = "PENALTY";
    public static final String TYPE_SUBSCRIPTION = "SUBSCRIPTION";

    // Transaction Status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private TransactionConstants() {
        // Private constructor to prevent instantiation
    }
}