package com.parkmate.wallet;

import com.parkmate.client.UserServiceClient;
import com.parkmate.common.PaginationUtil;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.wallet.dto.CreateWalletRequest;
import com.parkmate.wallet.dto.WalletResponse;
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
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserServiceClient userServiceClient;
    private final WalletMapper walletMapper;

    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest createWalletRequest) {

        // Check if user exists in user-service
        validateUserId(createWalletRequest.getUserId());

        // Check if wallet already exists for this user
        if (walletRepository.existsByUserId(createWalletRequest.getUserId())) {
            throw new AppException(ErrorCode.WALLET_ALREADY_EXISTS, createWalletRequest.getUserId());
        }

        Wallet wallet = Wallet.builder()
                .userId(createWalletRequest.getUserId())
                .balance(BigDecimal.valueOf(10000000)) // Initial balance of 10,000,000 VND for testing
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
    public WalletResponse getByUserId(String userHeaderId) {
        Long accountId = Long.parseLong(userHeaderId);
        // Convert accountId to actual userId
        Long userId = getUserIdFromAccountId(accountId);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND, userId));
        return walletMapper.toResponse(wallet);
    }

    @Override
    public Page<WalletResponse> getAll(int page, int size, String sortBy, String sortOrder, String userHeaderId) {

        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);
        if (userHeaderId != null && !userHeaderId.isEmpty()) {
            Long userId = Long.parseLong(userHeaderId);
            return walletRepository.findAllByUserId(userId, pageable)
                    .map(walletMapper::toResponse);
        }

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

    Long getUserIdFromAccountId(Long accountId) {
        try {
            return userServiceClient.getUserIdByAccountId(accountId);
        } catch (FeignException.NotFound e) {
            log.error("User not found for account ID: {}", accountId);
            throw new AppException(ErrorCode.USER_NOT_FOUND, accountId);
        } catch (FeignException e) {
            log.error("Error calling user-service: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

}
