package com.parkmate.wallet.dto;

import com.parkmate.wallet.WalletOwner;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CreateWalletRequest {
    Long userId;
    WalletOwner walletOwnerType;
}
