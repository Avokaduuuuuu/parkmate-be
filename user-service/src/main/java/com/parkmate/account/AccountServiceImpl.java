package com.parkmate.account;

import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;


    @Override
    public Account getAccountByEmail(String email) {
        return accountRepository.findAccountByEmail(email).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
    }
}
