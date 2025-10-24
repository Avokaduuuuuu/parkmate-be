package com.parkmate.wallet;

import com.parkmate.wallet.dto.CreateWalletRequest;
import com.parkmate.wallet.dto.WalletResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WalletService {

    WalletResponse createWallet(CreateWalletRequest createWalletRequest);

    WalletResponse getById(Long id);

    WalletResponse getByUserId(String userHeaderId);

    Page<WalletResponse> getAll(int page, int size, String sortBy, String sortOrder, String userHeaderId);

    WalletResponse updateWallet(Long id, WalletResponse walletResponse);

    void deleteById(Long id);

    Map<Long, BigDecimal> getUserWallets(List<Long> userIds);


}
