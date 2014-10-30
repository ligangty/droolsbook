package com.github.ligangty.droolstest.bank.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.command.Command;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.command.CommandFactory;

import com.github.ligangty.droolstest.bank.model.Account;
import com.github.ligangty.droolstest.bank.model.Address;
import com.github.ligangty.droolstest.bank.model.Customer;
import com.github.ligangty.droolstest.bank.service.BankingInquiryService;
import com.github.ligangty.droolstest.bank.service.BankingInquiryServiceImpl;
import com.github.ligangty.droolstest.bank.service.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.service.Message;
import com.github.ligangty.droolstest.bank.service.ReportFactory;
import com.github.ligangty.droolstest.bank.service.ValidationReport;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class ValidationTest {
    KieSession session;
    ReportFactory reportFactory;

    @Before
    public void setUpClass() throws Exception {
        KieServices kieServices = KieServices.Factory.get();
        KieResources kieResources = kieServices.getResources();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        KieRepository kieRepository = kieServices.getRepository();

        Resource resource = kieResources.newClassPathResource("rules/validation.drl");
        // path has to start with src/main/resources
        // append it with the package from the rule
        kieFileSystem.write("src/main/resources/rules/validation.drl", resource);

        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }
        KieContainer kContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
        
        session = kContainer.newKieSession();

        BankingInquiryService inquiryService = new BankingInquiryServiceImpl();
        reportFactory = new DefaultReportFactory();
        
        session.setGlobal("reportFactory", reportFactory);
        session.setGlobal("inquiryService", inquiryService);
    }

    @Test
    public void addressRequired() {
        Customer customer = createCustomerBasic();
        assertNull(customer.getAddress());
        assertReportContains(Message.Type.WARNING, "address required rule", customer);

        customer = createCustomerBasic();
        customer.setAddress(new Address());
        assertNotReportContains(Message.Type.WARNING, "address required rule", customer);
    }

    @Test
    public void phoneNumberRequired() {
        Customer customer = createCustomerBasic();
        assertNull(customer.getPhoneNumber());
        assertReportContains(Message.Type.ERROR, "phone number required", customer);

        customer = createCustomerBasic();
        customer.setPhoneNumber("1234567");
        assertNotReportContains(Message.Type.ERROR, "phone number required", customer);
    }

    @Test
    public void accountOwnerRequired() {
        Customer customer = createCustomerBasic();
        Account account = new Account();
        customer.setAccounts(Sets.newHashSet(account));
        assertReportContains(Message.Type.ERROR, "account owner required", customer, account);

        customer = createCustomerBasic();
        account = customer.getAccounts().iterator().next();
        assertNotNull(account.getOwner());
        assertNotReportContains(Message.Type.ERROR, "account owner required", customer);

    }

    @Test
    public void accountBalanceAtLeast() {
        Customer customer = createCustomerBasic();
        Account account = customer.getAccounts().iterator().next();
        assertNotNull(account.getOwner());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertReportContains(Message.Type.WARNING, "account balance at least", customer, account);

        customer = createCustomerBasic();
        account = customer.getAccounts().iterator().next();
        account.setBalance(BigDecimal.valueOf(54.00));
        assertReportContains(Message.Type.WARNING, "account balance at least", customer, account);

        customer = createCustomerBasic();
        account = customer.getAccounts().iterator().next();
        account.setBalance(BigDecimal.valueOf(100.00));
        assertNotReportContains(Message.Type.WARNING, "account balance at least", customer, account);
    }

    @Test
    public void studentAccountCustomerAgeLessThan1() {
        final DateMidnight NOW = new DateMidnight();
        Customer customer = createCustomerBasic();
        Account account = customer.getAccounts().iterator().next();
        customer.setDateOfBirth(NOW.minusYears(40).toDate());
        assertEquals(Account.Type.TRANSACTIONAL, account.getType());
        assertNotReportContains(Message.Type.ERROR, "student account customer age less than rule", customer, account);
    }

    @Test
    public void studentAccountCustomerAgeLessThan2() {
        final DateMidnight NOW = new DateMidnight();
        Customer customer = createCustomerBasic();
        customer.setDateOfBirth(NOW.minusYears(20).toDate());
        Account account = customer.getAccounts().iterator().next();
        account.setType(Account.Type.STUDENT);
        assertNotReportContains(Message.Type.ERROR, "student account customer age less than rule", customer, account);
    }

    @Test
    public void studentAccountCustomerAgeLessThan3() {
        final DateMidnight NOW = new DateMidnight();
        Customer customer = createCustomerBasic();
        Account account = customer.getAccounts().iterator().next();
        customer.setDateOfBirth(NOW.minusYears(20).toDate());
        account.setType(Account.Type.TRANSACTIONAL);
        assertNotReportContains(Message.Type.ERROR, "student account customer age less than rule", customer);
    }

    @Test
    public void studentAccountCustomerAgeLessThan4() {
        final DateMidnight NOW = new DateMidnight();
        Customer customer = createCustomerBasic();
        Account account = customer.getAccounts().iterator().next();
        customer.setDateOfBirth(NOW.minusYears(40).toDate());
        account.setType(Account.Type.STUDENT);
        assertReportContains(Message.Type.ERROR, "student account customer age less than rule", customer, account);
    }

    @Test
    public void accountNumberUnique() {
        Customer customer = createCustomerBasic();
        Account account = customer.getAccounts().iterator().next();
        session.setGlobal("inquiryService", new BankingInquiryService() {
            @Override
            public boolean isAccountNumberUnique(Account account) {
                return false;
            }
        });
        assertReportContains(Message.Type.ERROR, "account number unique", customer, account);

        session.setGlobal("inquiryService", new BankingInquiryService() {
            @Override
            public boolean isAccountNumberUnique(Account account) {
                return true;
            }
        });

        assertNotReportContains(Message.Type.ERROR, "account number unique", customer, account);
    }

    private void assertReportContains(Message.Type type, String messageKey, Customer customer, Object... context) {
        ValidationReport report = executeRules(type, messageKey, customer, context);
        assertTrue("Report doesn't contain message [" + messageKey + "]", report.contains(messageKey));
        Message message = getMessage(report, messageKey);
        // assertEquals(Arrays.asList(context), message.getContextOrdered());
    }

    private void assertNotReportContains(Message.Type type, String messageKey, Customer customer, Object... context) {
        ValidationReport report = executeRules(type, messageKey, customer, context);
        assertFalse("Report contains message [" + messageKey + "]", report.contains(messageKey));
        assertNull(getMessage(report, messageKey));
    }

    private ValidationReport executeRules(Message.Type type, String messageKey, Customer customer, Object... context) {
        ValidationReport report = reportFactory.createValidationReport();
        List<Command<Object>> commands = newArrayList();
        commands.add(CommandFactory.newSetGlobal("validationReport", report));
        commands.add(CommandFactory.newInsertElements(getFacts(customer)));
        session.execute(CommandFactory.newBatchExecution(commands));
        session.fireAllRules();
        return report;
    }

    private Collection<Object> getFacts(Customer customer) {
        List<Object> facts = newArrayList();
        facts.add(customer);
        facts.add(customer.getAddress());
        facts.addAll(customer.getAccounts());
        return facts;
    }

    private Message getMessage(ValidationReport report, String messageKey) {
        final String inMessageKey = messageKey;
        return Iterables.tryFind(report.getMessages(), new Predicate<Message>() {
            @Override
            public boolean apply(Message arg0) {
                return inMessageKey.equals(arg0.getMessageKey());
            }
        }).orNull();
    }

    Customer createCustomerBasic() {
        Customer customer = new Customer();
        Account account = new Account();
        customer.addAccount(account);
        account.setOwner(customer);
        return customer;
    }

}
