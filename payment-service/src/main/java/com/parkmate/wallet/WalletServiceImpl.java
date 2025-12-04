package com.parkmate.wallet;

import com.parkmate.client.UserServiceClient;
import com.parkmate.common.PaginationUtil;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.payos.PayOSService;
import com.parkmate.wallet.dto.CreateWalletRequest;
import com.parkmate.wallet.dto.WalletResponse;
import com.parkmate.wallet.dto.WithdrawalRequest;
import com.parkmate.wallet.dto.WithdrawalResponse;
import com.parkmate.walletTransaction.TransactionStatus;
import com.parkmate.walletTransaction.TransactionType;
import com.parkmate.walletTransaction.WalletTransaction;
import com.parkmate.walletTransaction.WalletTransactionRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.payos.model.v1.payouts.Payout;
import vn.payos.model.v1.payouts.PayoutRequests;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserServiceClient userServiceClient;
    private final WalletMapper walletMapper;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PayOSService payOSService;

    private static final BigDecimal MIN_WITHDRAWAL_AMOUNT = BigDecimal.valueOf(10000); // 10,000 VND
    private static final BigDecimal MAX_WITHDRAWAL_AMOUNT = BigDecimal.valueOf(50000000); // 50,000,000 VND

    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest createWalletRequest) {

        validateUserId(createWalletRequest.getHolderId());

        WalletOwner walletOwner = createWalletRequest.getWalletOwnerType().equals("PARTNER")
                ? WalletOwner.PARTNER
                : WalletOwner.MEMBER;

        if (walletRepository.findByHolderIdAndWalletOwner(createWalletRequest.getHolderId(), walletOwner).isPresent()) {
            throw new AppException(ErrorCode.WALLET_ALREADY_EXISTS,
                    String.format("userId: %d, type: %s", createWalletRequest.getHolderId(), walletOwner));
        }

        Wallet wallet = Wallet.builder()
                .holderId(createWalletRequest.getHolderId())
                .balance(BigDecimal.valueOf(0))
                .walletOwner(walletOwner)
                .currency("VND")
                .isActive(true)
                .build();

        return walletMapper.toResponse(walletRepository.save(wallet));
    }

    @Override
    public WalletResponse getById(Long id) {

        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND, id));

        return walletMapper.toResponse(wallet);
    }

    @Override
    public WalletResponse getByUserId(String userHeaderId, String role) {
        Long userId = Long.parseLong(userHeaderId);

        // Determine wallet owner type based on role
        WalletOwner walletOwner = determineWalletOwnerFromRole(role);

        Wallet wallet = walletRepository.findByHolderIdAndWalletOwner(userId, walletOwner)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND, userId));
        return walletMapper.toResponse(wallet);
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

    @Override
    public Page<WalletResponse> getAll(int page, int size, String sortBy, String sortOrder) {

        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);
        Page<Wallet> wallets = walletRepository.findAll(pageable);
        return wallets.map(walletMapper::toResponse);

    }


    @Override
    public WalletResponse updateWallet(Long id, WalletResponse walletResponse) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public Map<Long, BigDecimal> getUserWallets(List<Long> userIds) {

        Set<Long> requestedUserIds = new HashSet<>(userIds);

        List<Wallet> wallets = walletRepository.findByHolderIdIn(userIds);

        Set<Long> foundUserIds = wallets.stream().map(Wallet::getHolderId).collect(Collectors.toSet());


        Set<Long> missingUserIds = new HashSet<>(requestedUserIds);
        missingUserIds.removeAll(foundUserIds);

        if (!missingUserIds.isEmpty()) {
            log.info("Found {} users without wallet: {}", missingUserIds.size(), missingUserIds);
        }


        return wallets.stream()
                .collect(Collectors.toMap(
                        Wallet::getHolderId,
                        Wallet::getBalance
                ));
    }

    void validateUserId(Long userId) {
        try {
            userServiceClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            log.error("User not found with ID: {}", userId);
            throw new AppException(ErrorCode.USER_NOT_FOUND, userId);
        } catch (FeignException e) {
            log.error("Error calling user-service: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public WithdrawalResponse requestWithdrawal(String userHeaderId, WithdrawalRequest request) {
        log.info("Processing withdrawal request for user: {}, amount: {}", userHeaderId, request.getAmount());

        // 1. Parse and validate user ID
        Long userId;
        try {
            userId = Long.parseLong(userHeaderId);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", userHeaderId);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 2. Find PARTNER wallet (withdrawal is for partners only)
        Wallet wallet = walletRepository.findByHolderIdAndWalletOwner(userId, WalletOwner.PARTNER)
                .orElseThrow(() -> {
                    log.error("PARTNER wallet not found for userId: {}. User may not be a partner or wallet not created.", userId);
                    return new AppException(ErrorCode.WALLET_NOT_FOUND, userId);
                });

        // 3. Validate wallet is active
        if (!wallet.isActive()) {
            log.error("PARTNER wallet found but INACTIVE - userId: {}, walletId: {}, isActive: {}",
                    userId, wallet.getId(), false);
            throw new AppException(ErrorCode.WALLET_IS_INACTIVE);
        }

        log.debug("PARTNER wallet validated - userId: {}, walletId: {}, balance: {}, isActive: {}",
                userId, wallet.getId(), wallet.getBalance(), true);

        BigDecimal amount = request.getAmount();
        BigDecimal oldBalance = wallet.getBalance();

        // 4. Validate amount constraints
        if (amount.compareTo(MIN_WITHDRAWAL_AMOUNT) < 0) {
            throw new AppException(ErrorCode.WITHDRAWAL_AMOUNT_TOO_LOW);
        }

        if (amount.compareTo(MAX_WITHDRAWAL_AMOUNT) > 0) {
            throw new AppException(ErrorCode.WITHDRAWAL_AMOUNT_TOO_HIGH, MAX_WITHDRAWAL_AMOUNT);
        }

        // 5. Validate sufficient balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance for withdrawal - userId: {}, balance: {}, requested: {}",
                    userId, wallet.getBalance(), amount);
            throw new AppException(ErrorCode.INSUFFICIENT_WALLET_BALANCE);
        }

        // 6. Validate bank account information
        if (request.getBankAccount() == null ||
                request.getBankAccount().getAccountNumber() == null ||
                request.getBankAccount().getAccountName() == null ||
                request.getBankAccount().getBankCode() == null) {
            throw new AppException(ErrorCode.INVALID_BANK_ACCOUNT);
        }

        // 7. Generate unique reference ID
        String referenceId = "payout_" + userId + "_" + System.currentTimeMillis();

        // 8. DEDUCT from wallet IMMEDIATELY (critical step!)
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        log.info("Wallet balance deducted - userId: {}, balance: {} -> {}", userId, oldBalance, wallet.getBalance());

        // 9. Create CASH_OUT transaction with PENDING status
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(TransactionType.CASH_OUT)
                .amount(amount)
                .externalTransactionId(referenceId)
                .status(TransactionStatus.PENDING)
                .description("Withdrawal request to " + request.getBankAccount().getBankCode())
                .build();

        walletTransactionRepository.save(transaction);
        log.info("Withdrawal transaction created - transactionId: {}, referenceId: {}", transaction.getId(), referenceId);

        // 10. Call PayOS to create payout
        Payout payout;
        try {
            PayoutRequests payoutRequest = PayoutRequests.builder()
                    .referenceId(referenceId)
                    .amount(amount.longValue())
                    .toAccountNumber(request.getBankAccount().getAccountNumber())
                    .toBin(request.getBankAccount().getBankCode())
                    .description(request.getBankAccount().getDescription() != null ?
                            request.getBankAccount().getDescription() : "Partner withdrawal")
                    .build();

            payout = payOSService.retrievePayout(payoutRequest);

            if (payout == null) {
                log.error("PayOS returned null payout response for referenceId: {}", referenceId);
                throw new AppException(ErrorCode.PAYOUT_CREATION_FAILED, "PayOS service unavailable");
            }

            log.info("✓ PayOS payout created successfully - referenceId: {}, payoutId: {}", referenceId, payout.getId());

            // Store PayOS batch ID in metadata for status checking
            String metadata = String.format("{\"payoutId\":\"%s\",\"referenceId\":\"%s\"}",
                    payout.getId(), referenceId);

            // Update transaction status to PROCESSING
            transaction.setStatus(TransactionStatus.PROCESSING);
            transaction.setMetadata(metadata);
            walletTransactionRepository.save(transaction);

            return WithdrawalResponse.builder()
                    .referenceId(referenceId)
                    .amount(amount)
                    .status(TransactionStatus.PROCESSING)
                    .balanceBefore(oldBalance)
                    .balanceAfter(wallet.getBalance())
                    .requestedAt(LocalDateTime.now().plusHours(7))
                    .message("Withdrawal request submitted successfully. You will be notified when the payout is processed.")
                    .build();

        } catch (Exception e) {
            // CRITICAL: Rollback wallet deduction if PayOS call fails
            log.error("Failed to create PayOS payout for referenceId: {}. Rolling back wallet deduction.", referenceId, e);

            wallet.setBalance(oldBalance);
            walletRepository.save(wallet);

            transaction.setStatus(TransactionStatus.FAILED);
            walletTransactionRepository.save(transaction);

            throw new AppException(ErrorCode.PAYOUT_CREATION_FAILED, e.getMessage());
        }
    }

}
