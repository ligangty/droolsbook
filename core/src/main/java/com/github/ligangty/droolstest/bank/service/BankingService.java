package com.github.ligangty.droolstest.bank.service;

import java.math.BigDecimal;
import java.util.List;

import com.github.ligangty.droolstest.bank.model.Account;
import com.github.ligangty.droolstest.bank.model.Customer;
import com.github.ligangty.droolstest.bank.model.Loan;
import com.github.ligangty.droolstest.bank.model.LoanApprovalHolder;

public interface BankingService {
    void add(Customer customer);

    void save(Customer customer);

    void requestLoan(Loan loan, Customer customer);

    void approveLoan(LoanApprovalHolder holder);

    List<Customer> findAllCustomers();

    void transfer(Account sourceAccount, Account destinationAccount, BigDecimal sum);

    void claim(long taskId, String userId);

    void start(long taskId, String userId);

    void complete(long taskId, String userId);
}
