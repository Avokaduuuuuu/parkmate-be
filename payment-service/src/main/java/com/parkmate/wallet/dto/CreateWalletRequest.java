package com.parkmate.wallet.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CreateWalletRequest {
    Long userId;
}
