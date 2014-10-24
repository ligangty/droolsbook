package com.github.ligangty.droolstest.bank.model;


public interface BankingFactory {

  Customer createCustomer();
  Loan createLoan();
  Account createAccount(Customer customer);
  Address createAddress(Customer customer);
  
}
