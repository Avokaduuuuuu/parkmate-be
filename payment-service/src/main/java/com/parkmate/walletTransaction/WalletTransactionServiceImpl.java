package com.parkmate.walletTransaction;

import com.github.f4b6a3.uuid.UuidCreator;
import com.parkmate.client.UserServiceClient;
import com.parkmate.common.PaginationUtil;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.wallet.Wallet;
import com.parkmate.wallet.WalletRepository;
import com.parkmate.walletTransaction.dto.CreateTransactionRequest;
import com.parkmate.walletTransaction.dto.TransactionSearchCriteria;
import com.parkmate.walletTransaction.dto.WalletTransactionResponse;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletTransactionServiceImpl implements WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionMapper walletTransactionMapper;
    private final UserServiceClient userClient;

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

    @Override
    public Page<WalletTransactionResponse> getTransactions(int page, int size, String sortBy, String sortOrder, TransactionSearchCriteria criteria, String userHeaderId) {
        // Parse userId from header
        Long userId = null;
        if (userHeaderId != null) {
            try {
                userId = getUserIdFromAccountId(Long.parseLong(userHeaderId));
                log.info("Parsed user ID from header: {}", userId);
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in header: {}", userHeaderId);
            }
        }

        // Create pageable
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);

        // Build predicate from criteria and userId
        com.querydsl.core.types.Predicate predicate = TransactionSpecification.buildPredicate(criteria, userId);

        // Query with predicate
        Page<WalletTransaction> transactions = walletTransactionRepository.findAll(predicate, pageable);

        // Map to response
        return transactions.map(walletTransactionMapper::toResponse);
    }

    Long getUserIdFromAccountId(Long accountId) {
        try {
            return userClient.getUserIdByAccountId(accountId);
        } catch (FeignException.NotFound e) {
            log.error("User not found for account ID: {}", accountId);
            throw new AppException(ErrorCode.USER_NOT_FOUND, accountId);
        } catch (FeignException e) {
            log.error("Error calling user-service: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

}
