package com.github.ligangty.droolstest.bank.service;

import com.github.ligangty.droolstest.bank.model.Customer;

public interface BankingValidationService {
    ValidationReport validate(Customer customer);
    
}
