package com.parkmate.walletTransaction;

import com.parkmate.config.MapStructConfig;
import com.parkmate.walletTransaction.dto.WalletTransactionResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(config = MapStructConfig.class)
public interface WalletTransactionMapper {

    @org.mapstruct.Mapping(target = "balanceBefore", ignore = true)
    @org.mapstruct.Mapping(target = "balanceAfter", ignore = true)
    WalletTransactionResponse toResponse(WalletTransaction walletTransaction);

    @AfterMapping
    default void calculateBalances(WalletTransaction transaction, @MappingTarget WalletTransactionResponse response) {
        if (transaction.getWallet() != null) {
            BigDecimal currentBalance = transaction.getWallet().getBalance();

            // balanceAfter is current balance
            response.setBalanceAfter(currentBalance);

            // balanceBefore = balanceAfter - netAmount (for DEPOSIT/REFUND)
            // balanceBefore = balanceAfter + netAmount (for PAYMENT/WITHDRAWAL)
            BigDecimal netAmount = transaction.getNetAmount() != null ? transaction.getNetAmount() : BigDecimal.ZERO;

            if (transaction.getTransactionType() == TransactionType.REFUND) {
                response.setBalanceBefore(currentBalance.subtract(netAmount));
            } else {
                response.setBalanceBefore(currentBalance.add(netAmount));
            }
        }
    }
}
