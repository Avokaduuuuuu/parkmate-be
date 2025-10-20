package com.parkmate.payos.dto;

import com.parkmate.walletTransaction.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {

    Long orderCode;
    TransactionStatus transactionStatus;
    BigDecimal amount;

}
