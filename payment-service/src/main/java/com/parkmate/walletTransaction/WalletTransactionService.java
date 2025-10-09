package com.parkmate.walletTransaction;

import com.parkmate.walletTransaction.dto.CreateTransactionRequest;
import com.parkmate.walletTransaction.dto.WalletTransactionResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface WalletTransactionService {


    WalletTransactionResponse createWalletTransaction(@RequestBody CreateTransactionRequest walletTransaction);


}
