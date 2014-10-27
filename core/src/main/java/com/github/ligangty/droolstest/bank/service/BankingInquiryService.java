package com.github.ligangty.droolstest.bank.service;

import com.github.ligangty.droolstest.bank.model.Account;

public interface BankingInquiryService {
    public boolean isAccountNumberUnique(Account account);
}
