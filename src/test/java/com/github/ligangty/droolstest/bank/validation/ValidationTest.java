package com.github.ligangty.droolstest.bank.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
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
import com.github.ligangty.droolstest.bank.service.BankingINquiryServiceImpl;
import com.github.ligangty.droolstest.bank.service.BankingInquiryService;
import com.github.ligangty.droolstest.bank.util.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.util.Message;
import com.github.ligangty.droolstest.bank.util.ReportFactory;
import com.github.ligangty.droolstest.bank.util.ValidationReport;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class ValidationTest {
    static KieSession session;
    static ReportFactory reportFactory;

    @BeforeClass
    public static void setUpClass() throws Exception {
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

        BankingInquiryService inquiryService = new BankingINquiryServiceImpl();
        reportFactory = new DefaultReportFactory();

        session = kContainer.newKieSession();
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
        Account account = customer.getAccounts().iterator().next();
        assertNotNull(account.getOwner());
        assertNotReportContains(Message.Type.ERROR, "account owner required", customer);
        customer = createCustomerBasic();
        account = new Account();
        customer.setAccounts(Sets.newHashSet(account));
        assertReportContains(Message.Type.ERROR, "account owner required", customer, account);
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

    private void assertReportContains(Message.Type type, String messageKey, Customer customer, Object... context) {
        ValidationReport report = executeRules(type, messageKey, customer, context);
        assertTrue("Report doesn't contain message [" + messageKey + "]", report.contains(messageKey));
        Message message = getMessage(report, messageKey);
        assertEquals(Arrays.asList(context), message.getContextOrdered());
    }

    private void assertNotReportContains(Message.Type type, String messageKey, Customer customer, Object... context) {
        ValidationReport report = executeRules(type, messageKey, customer, context);
        assertFalse("Report doesn't contain message [" + messageKey + "]", report.contains(messageKey));
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
