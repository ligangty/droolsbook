package com.github.ligangty.droolstest.bank.service;

import com.github.ligangty.droolstest.bank.model.Account;

public class BankingInquiryServiceImpl implements BankingInquiryService {
    @Override
    public boolean isAccountNumberUnique(Account account) {
        return false;
    }
}
