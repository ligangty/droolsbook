package com.github.ligangty.droolstest.bank.service;

import com.github.ligangty.droolstest.bank.model.Customer;
import com.github.ligangty.droolstest.bank.util.ValidationReport;

public interface BankingValidationService {
    ValidationReport validate(Customer customer);
    
}
