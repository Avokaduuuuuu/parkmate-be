package com.parkmate.walletTransaction;

import com.parkmate.walletTransaction.dto.CreateTransactionRequest;
import com.parkmate.walletTransaction.dto.SessionPaymentCreateRequest;
import com.parkmate.walletTransaction.dto.TransactionSearchCriteria;
import com.parkmate.walletTransaction.dto.WalletTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;

public interface WalletTransactionService {


    WalletTransactionResponse createWalletTransaction(@RequestBody CreateTransactionRequest walletTransaction);

    Page<WalletTransactionResponse> getTransactions(int page,
                                                    int size,
                                                    String sortBy,
                                                    String sortOrder,
                                                    TransactionSearchCriteria criteria,
                                                    String userHeaderId,
                                                    String role);

    void createSessionCharge(SessionPaymentCreateRequest sessionPaymentCreateRequest);

}
