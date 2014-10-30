package com.github.ligangty.droolstest.transform.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;

import com.github.ligangty.droolstest.bank.model.Account;
import com.github.ligangty.droolstest.bank.model.Address;
import com.github.ligangty.droolstest.bank.model.Customer;
import com.github.ligangty.droolstest.bank.service.BankingService;
import com.github.ligangty.droolstest.bank.service.Message;
import com.github.ligangty.droolstest.bank.service.Message.Type;
import com.github.ligangty.droolstest.bank.service.ReportFactory;
import com.github.ligangty.droolstest.bank.service.ValidationReport;

public class DataTransformationServiceImpl implements DataTransformationService {

  private LegacyBankService legacyBankService;
  private KieBase kieBase;
  private ReportFactory reportFactory;
  private BankingService bankingService;
  private KieSession session;

  public void etl() {
    session = kieBase.newKieSession();
    session.setGlobal("legacyService", legacyBankService);
    session.setGlobal("reportFactory", reportFactory);

    for (Object customerMap : legacyBankService
        .findAllCustomers()) {
      processCustomer((Map) customerMap);
    }
  }

  // @extract-start 03 12
  /**
   * transforms customerMap, creates and stores new customer
   */
  protected void processCustomer(Map customerMap) {
    ValidationReport validationReport = reportFactory
        .createValidationReport();

    List<Command<?>> commands = new ArrayList<Command<?>>();
    commands.add(CommandFactory.newSetGlobal(
        "validationReport", validationReport));
    commands.add(CommandFactory.newInsert(customerMap));
    commands.add(new FireAllRulesCommand());
    commands.add(CommandFactory.newQuery(
        "address", "getAddressByCustomerId",
        new Object[] { customerMap }));
    commands.add(CommandFactory.newQuery(
        "accounts", "getAccountByCustomerId",
        new Object[] { customerMap }));
    ExecutionResults results = session.execute(
        CommandFactory.newBatchExecution(commands));
    
    if (!validationReport.getMessagesByType(Type.ERROR)
        .isEmpty()) {
      logError(validationReport
          .getMessagesByType(Type.ERROR));
      logWarning(validationReport
          .getMessagesByType(Type.WARNING));
    } else {
      logWarning(validationReport
          .getMessagesByType(Type.WARNING));
      Customer customer = buildCustomer(customerMap,
          results);
      bankingService.add(customer); // runs validation
    }
  }
  // @extract-end

  protected Customer buildCustomer(Map customerMap,
      ExecutionResults results) {
    Customer customer = new Customer();
    customer.setFirstName((String) customerMap
        .get("first_name"));
    customer.setLastName((String) customerMap
        .get("last_name"));
    // ..

    QueryResults addressQueryResults = (QueryResults) 
      results.getValue("address"); 
    if (addressQueryResults.size() > 0) {
      QueryResultsRow addressQueryResult = addressQueryResults
          .iterator().next();
      Map addressMap = (Map) addressQueryResult
          .get("$addressMap");

      Address address = new Address();
      address.setAddressLine1((String) addressMap
          .get("street"));
      address.setCountry((Address.Country) addressMap
          .get("country"));
      customer.setAddress(address);
    }

    // @extract-start 03 13
    QueryResults accountQueryResults = (QueryResults) 
      results.getValue("accounts");
    for (QueryResultsRow accountQueryResult : 
      accountQueryResults) {
      Map accountMap = (Map) accountQueryResult
          .get("$accountMap");

      Account account = new Account();
      account.setNumber((Long) accountMap.get("number"));
      account.setBalance((BigDecimal) accountMap
          .get("balance"));
      //..
      customer.addAccount(account);
      // @extract-end
    }
    return customer;
  }

  protected void logError(Set<Message> errors) {

  }

  protected void logWarning(Set<Message> warnings) {

  }

  public void setLegacyBankService(
      LegacyBankService legacyBankService) {
    this.legacyBankService = legacyBankService;
  }

  public void setKnowledgeBase(KieBase knowledgeBase) {
    this.kieBase = knowledgeBase;
  }

  public void setReportFactory(ReportFactory reportFactory) {
    this.reportFactory = reportFactory;
  }

  public void setBankingService(BankingService bankingService) {
    this.bankingService = bankingService;
  }

}