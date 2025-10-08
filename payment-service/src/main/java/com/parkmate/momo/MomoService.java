package com.parkmate.momo;

import com.parkmate.momo.dto.MoMoIPNRequest;
import com.parkmate.momo.dto.MoMoPaymentRequest;
import com.parkmate.momo.dto.MoMoPaymentResponse;

public interface MomoService {

    MoMoPaymentResponse create(Long userId, Long amount, String orderInfo);

    boolean processIPNCallback(MoMoIPNRequest request);

    boolean verifyIPNSignature(MoMoIPNRequest ipnRequest);

    String createSignature(MoMoPaymentRequest request);

}
