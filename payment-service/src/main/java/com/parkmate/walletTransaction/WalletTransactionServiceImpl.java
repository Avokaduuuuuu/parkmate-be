package com.parkmate.walletTransaction;

import com.parkmate.client.UserServiceClient;
import com.parkmate.common.PaginationUtil;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.wallet.Wallet;
import com.parkmate.wallet.WalletOwner;
import com.parkmate.wallet.WalletRepository;
import com.parkmate.walletTransaction.dto.CreateTransactionRequest;
import com.parkmate.walletTransaction.dto.TransactionSearchCriteria;
import com.parkmate.walletTransaction.dto.WalletTransactionResponse;
import com.querydsl.core.types.Predicate;
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

        log.info("Creating wallet transaction for user {}: type={}, amount={}, partnerId={}",
                request.getUserId(), request.getTransactionType(), request.getAmount(), request.getPartnerId());
        Wallet wallet = walletRepository.findByHolderId(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        // Only fetch partner wallet if partnerId is provided
        Wallet partnerWallet = null;
        BigDecimal partnerCurrentBalance = null;
        if (request.getPartnerId() != null) {
            partnerWallet = walletRepository.findByHolderId(request.getPartnerId())
                    .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
            partnerCurrentBalance = partnerWallet.getBalance();
        }

        BigDecimal memberCurrentBalance = wallet.getBalance();
        BigDecimal amount = request.getAmount();

        // 2. Parse transaction type
        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(request.getTransactionType());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
        BigDecimal newBalance;
        BigDecimal partnerNewBalance = null;
        switch (transactionType) {
            case DEDUCTION, SUBSCRIPTION -> {
                // Validate sufficient balance
                if (memberCurrentBalance.compareTo(amount) < 0) {
                    log.warn("Insufficient balance for user {}. Current: {}, Required: {}",
                            request.getUserId(), memberCurrentBalance, amount);
                    throw new AppException(ErrorCode.INSUFFICIENT_WALLET_BALANCE);
                }
                // Deduct from member
                newBalance = memberCurrentBalance.subtract(amount);

                // Credit partner if partnerId is provided
                if (partnerCurrentBalance != null) {
                    partnerNewBalance = partnerCurrentBalance.add(amount);
                    log.info("Crediting partner {}: {} + {} = {}",
                            request.getPartnerId(), partnerCurrentBalance, amount, partnerNewBalance);
                } else {
                    log.warn("No partnerId provided for {} transaction. Partner will not be credited.",
                            transactionType);
                }
            }
            case TOP_UP, REFUND, REVERSAL -> {
                newBalance = memberCurrentBalance.add(amount);
                log.info("Adding to member wallet: {} + {} = {}", memberCurrentBalance, amount, newBalance);
            }
            default -> throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }

        // Update member wallet
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        // Update partner wallet if applicable
        if (partnerNewBalance != null) {
            partnerWallet.setBalance(partnerNewBalance);
            walletRepository.save(partnerWallet);
            log.info("Partner wallet updated for partner {}: {} -> {}",
                    request.getPartnerId(), partnerCurrentBalance, partnerNewBalance);
        }

        log.debug("Wallet balance updated for user {}: {} -> {}",
                request.getUserId(), memberCurrentBalance, newBalance);

        // Create member transaction record
        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .amount(amount)
                .transactionType(transactionType)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .build();

        walletTransaction = walletTransactionRepository.save(walletTransaction);

        // Create partner transaction record only if partner wallet was credited
        if (partnerNewBalance != null) {
            WalletTransaction partnerWalletTransaction = WalletTransaction.builder()
                    .walletId(partnerWallet.getId())
                    .amount(amount)
                    .transactionType(TransactionType.TOP_UP)  // Partner receives money (credit)
                    .status(TransactionStatus.COMPLETED)
                    .description("Received from reservation/subscription: " +
                            (request.getDescription() != null ? request.getDescription() : ""))
                    .build();

            walletTransactionRepository.save(partnerWalletTransaction);
            log.info("Partner transaction record created for partner {}", request.getPartnerId());
        }

        WalletTransactionResponse response = walletTransactionMapper.toResponse(walletTransaction);

        response.setBalanceBefore(memberCurrentBalance);
        response.setBalanceAfter(newBalance);

        return response;
    }

    @Override
    public Page<WalletTransactionResponse> getTransactions(int page, int size, String sortBy, String sortOrder, TransactionSearchCriteria criteria, String userHeaderId, String role) {
        Long userId = null;
        Long walletId = null;

        if (userHeaderId != null) {
            try {
                userId = Long.parseLong(userHeaderId);
                log.info("Parsed user ID from header: {}", userId);

                // Get the correct wallet based on user role
                if (role != null) {
                    WalletOwner walletOwner = determineWalletOwnerFromRole(role);
                    Long finalUserId = userId;
                    Wallet wallet = walletRepository.findByHolderIdAndWalletOwner(userId, walletOwner)
                            .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND, finalUserId));
                    walletId = wallet.getId();
                    log.info("Found wallet ID {} for user {} with role {}", walletId, userId, role);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in header: {}", userHeaderId);
            }
        }

        // Create pageable
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);

        // Build predicate from criteria and walletId (not userId)
        Predicate predicate = TransactionSpecification.buildPredicate(criteria, walletId);

        // Query with predicate
        Page<WalletTransaction> transactions = walletTransactionRepository.findAll(predicate, pageable);

        // Map to response
        return transactions.map(walletTransactionMapper::toResponse);
    }

    private WalletOwner determineWalletOwnerFromRole(String role) {
        if (role == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "User role is required");
        }

        // Role from gateway is typically "ROLE_PARTNER" or "ROLE_MEMBER" or "ROLE_DRIVER"
        if (role.contains("PARTNER")) {
            return WalletOwner.PARTNER;
        } else if (role.contains("MEMBER") || role.contains("DRIVER")) {
            return WalletOwner.MEMBER;
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid role: " + role);
        }
    }


}
