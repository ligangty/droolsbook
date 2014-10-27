package com.github.ligangty.droolstest.bank.service;

import java.util.ArrayList;
import java.util.Collection;

import org.kie.api.KieBase;
import org.kie.api.runtime.StatelessKieSession;

import com.github.ligangty.droolstest.bank.model.Customer;

// @extract-start 03 66
public class BankingValidationServiceImpl implements
    BankingValidationService {

  private KieBase kieBase;
  private ReportFactory reportFactory;
  private BankingInquiryService bankingInquiryService;
  
  /**
   * validates provided customer and returns validation report
   */
  public ValidationReport validate(Customer customer) {
    ValidationReport report = reportFactory
        .createValidationReport();
    StatelessKieSession session = kieBase
        .newStatelessKieSession();
    session.setGlobal("validationReport", report);
    session.setGlobal("reportFactory", reportFactory);
    session
        .setGlobal("inquiryService", bankingInquiryService);
    session.execute(getFacts(customer));
    return report;
  }

  /**
   * @return facts that the rules will reason upon
   */
  private Collection<Object> getFacts(Customer customer) {
    ArrayList<Object> facts = new ArrayList<Object>();
    facts.add(customer);
    facts.add(customer.getAddress());
    facts.addAll(customer.getAccounts());
    return facts;
  }
  // @extract-end
  
  public void setKiBase(KieBase knowledgeBase) {
    this.kieBase = knowledgeBase;
  }
  
  public void setReportFactory(ReportFactory reportFactory) {
    this.reportFactory = reportFactory;
  }
  
  public void setBankingInquiryService(
      BankingInquiryService bankingInquiryService) {
    this.bankingInquiryService = bankingInquiryService;
  }
  
}
