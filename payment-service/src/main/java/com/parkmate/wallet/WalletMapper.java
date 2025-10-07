package com.parkmate.wallet;

import com.parkmate.config.MapStructConfig;
import com.parkmate.wallet.dto.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface WalletMapper {

    @Mapping(target = "isActive", source = "active")
    WalletResponse toResponse(Wallet wallet);

}
