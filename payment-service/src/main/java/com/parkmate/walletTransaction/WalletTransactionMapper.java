package com.parkmate.walletTransaction;

import com.parkmate.config.MapStructConfig;
import com.parkmate.walletTransaction.dto.WalletTransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface WalletTransactionMapper {

    @Mapping(target = "balanceBefore", ignore = true)
    @Mapping(target = "balanceAfter", ignore = true)
    WalletTransactionResponse toResponse(WalletTransaction walletTransaction);

}
