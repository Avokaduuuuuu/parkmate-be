package com.parkmate.dto.req;

import com.parkmate.entity.enums.RuleScope;
import com.parkmate.entity.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record PricingRuleUpdateRequest(
        @Schema(
                description = "Name of the pricing rule for identification",
                example = "Standard Weekday Pricing",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "Rule name must not be empty")
        @Size(min = 1, max = 100, message = "Rule name must not over 100 characters")
        String ruleName,

        @Schema(
                description = "Type of vehicle this pricing rule applies to",
                example = "CAR_4_SEATS",
                allowableValues = {"BIKE", "MOTORBIKE", "CAR_4_SEATS", "CAR_7_SEATS", "CAR_9_SEATS", "OTHER"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        VehicleType vehicleType,

        @Schema(
                description = "Base hourly rate in VND for parking",
                example = "15000.0",
                minimum = "0.0",
                type = "number",
                format = "double",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @DecimalMin(value = "0.0", message = "Base rate must be non-negative")
        Double baseRate,

        @Schema(
                description = "Deposit fee required upfront in VND (refundable)",
                example = "50000.0",
                minimum = "0.0",
                type = "number",
                format = "double",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @DecimalMin(value = "0.0", message = "Deposit fee must be non-negative")
        Double depositFee,

        @Schema(
                description = "Initial charge applied immediately upon entry in VND",
                example = "5000.0",
                minimum = "0.0",
                type = "number",
                format = "double",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @DecimalMin(value = "0.0", message = "Initial charge must be non-negative")
        Double initialCharge,

        @Schema(
                description = "Duration in minutes for which the initial charge applies",
                example = "30",
                minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Min(value = 0, message = "Initial duration minute could not be negative")
        Integer initialDurationMinute,

        @Schema(
                description = "Number of free parking minutes before charges apply",
                example = "15",
                minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Min(value = 0, message = "Free minute could not be negative")
        Integer freeMinute,

        @Schema(
                description = "Grace period in minutes after the paid time expires before penalties apply",
                example = "10",
                minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Min(value = 0, message = "Grace period minute could not be negative")
        Integer gracePeriodMinute,

        @Schema(
                description = "Date and time when this pricing rule becomes effective",
                example = "2025-01-01T00:00:00",
                type = "string",
                format = "date-time",
                pattern = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalDateTime validFrom,

        @Schema(
                description = "Date and time when this pricing rule expires (null means no expiration)",
                example = "2025-12-31T23:59:59",
                type = "string",
                format = "date-time",
                pattern = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        LocalDateTime validTo,

        @Schema(
                description = "Id to apply rule for a specific area (null for lot-wide rule)",
                example = "1",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Long areaId,

        @Schema(
                description = "Scope for this pricing rule",
                example = "AREA_SPECIFIC",
                allowableValues = {"LOT_WIDE", "AREA_SPECIFIC"}
        )
        RuleScope ruleScope
) {
}
