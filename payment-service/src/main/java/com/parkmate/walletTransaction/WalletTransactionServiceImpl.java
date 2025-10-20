package com.parkmate.walletTransaction;

import com.github.f4b6a3.uuid.UuidCreator;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.wallet.Wallet;
import com.parkmate.wallet.WalletRepository;
import com.parkmate.walletTransaction.dto.CreateTransactionRequest;
import com.parkmate.walletTransaction.dto.WalletTransactionResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletTransactionServiceImpl implements WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionMapper walletTransactionMapper;

    @Override
    @Transactional
    public WalletTransactionResponse createWalletTransaction(CreateTransactionRequest request) {

        // 1. Get wallet with lock to prevent race condition
        log.info("Creating wallet transaction for user {}: type={}, amount={}",
                request.getUserId(), request.getTransactionType(), request.getAmount());
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal currentBalance = wallet.getBalance();
        BigDecimal amount = request.getAmount();

        // 2. Parse transaction type
        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(request.getTransactionType());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }

        // 3. Calculate new balance based on transaction type
        BigDecimal newBalance;
        switch (transactionType) {
            case DEDUCTION, PENALTY, SUBSCRIPTION -> {
                // Check sufficient balance for deduction
                if (currentBalance.compareTo(amount) < 0) {
                    log.warn("Insufficient balance for user {}. Current: {}, Required: {}",
                            request.getUserId(), currentBalance, amount);
                    throw new AppException(ErrorCode.INSUFFICIENT_WALLET_BALANCE);
                }
                newBalance = currentBalance.subtract(amount);
            }
            case TOP_UP, REFUND, REVERSAL -> {
                // Add money to wallet
                newBalance = currentBalance.add(amount);
            }
            default -> throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }

        // 4. Update wallet balance
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.info("Wallet balance updated for user {}: {} -> {}",
                request.getUserId(), currentBalance, newBalance);

        // 5. Create transaction record
        WalletTransaction walletTransaction = WalletTransaction.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .sessionId(UuidCreator.getTimeOrderedEpoch())
                .userId(request.getUserId())
                .walletId(wallet.getId())
                .amount(amount)
                .fee(BigDecimal.ZERO)
                .netAmount(amount)
                .transactionType(transactionType)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .build();

        walletTransaction = walletTransactionRepository.save(walletTransaction);

        // 6. Map to response
        WalletTransactionResponse response = walletTransactionMapper.toResponse(walletTransaction);
        // Set balance info
        response.setBalanceBefore(currentBalance);
        response.setBalanceAfter(newBalance);

        return response;
    }

}
