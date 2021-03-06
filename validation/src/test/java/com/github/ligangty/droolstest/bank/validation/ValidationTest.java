package com.github.ligangty.droolstest.bank.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateMidnight;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.conf.KieBaseOption;
import org.kie.api.runtime.StatelessKieSession;
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
import com.github.ligangty.droolstest.bank.utils.KieHelper;
import com.github.ligangty.droolstest.bank.utils.TrackingAgendaEventListener;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class ValidationTest {
    protected static StatelessKieSession session;
    protected static ReportFactory reportFactory;
    protected static KieHelper kieHelper = KieHelper.newHelper();

    @BeforeClass
    public static void setUpClass() throws Exception {
        KieBaseOption[] options = null;
        KieBase kieBase = kieHelper.addFromClassPath("rules/validation.drl", ValidationTest.class.getClassLoader()).build(
                options);

        session = kieBase.newStatelessKieSession();
        session.addEventListener(new TrackingAgendaEventListener());

        BankingInquiryService inquiryService = new BankingInquiryServiceImpl();
        reportFactory = new DefaultReportFactory();

        session.setGlobal("reportFactory", reportFactory);
        session.setGlobal("inquiryService", inquiryService);
    }

    @Test
    public void addressRequired() {
        Customer customer = createCustomerBasic();
        assertNull(customer.getAddress());
        assertReportContains(Message.Type.WARNING, "addressRequired", customer);

        customer = createCustomerBasic();
        customer.setAddress(new Address());
        assertNotReportContains(Message.Type.WARNING, "addressRequired", customer);
    }

    @Test
    public void phoneNumberRequired() {
        Customer customer = createCustomerBasic();
        assertNull(customer.getPhoneNumber());
        assertReportContains(Message.Type.ERROR, "phoneNumberRequired", customer);

        customer = createCustomerBasic();
        customer.setPhoneNumber("1234567");
        assertNotReportContains(Message.Type.ERROR, "phoneNumberRequired", customer);
    }

    @Test
    public void accountOwnerRequired() {
        Customer customer = createCustomerBasic();
        Account account = new Account();
        customer.setAccounts(Sets.newHashSet(account));
        assertReportContains(Message.Type.ERROR, "accountOwnerRequired", customer, account);

        customer = createCustomerBasic();
        account = customer.getAccounts().iterator().next();
        assertNotNull(account.getOwner());
        assertNotReportContains(Message.Type.ERROR, "accountOwnerRequired", customer);

    }

    @Test
    public void accountBalanceAtLeast() {
        Customer customer = createCustomerBasic();
        Account account = customer.getAccounts().iterator().next();
        assertNotNull(account.getOwner());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertReportContains(Message.Type.WARNING, "accountBalanceAtLeast", customer, account);

        customer = createCustomerBasic();
        account = customer.getAccounts().iterator().next();
        account.setBalance(BigDecimal.valueOf(54.00));
        assertReportContains(Message.Type.WARNING, "accountBalanceAtLeast", customer, account);

        customer = createCustomerBasic();
        account = customer.getAccounts().iterator().next();
        account.setBalance(BigDecimal.valueOf(100.00));
        assertNotReportContains(Message.Type.WARNING, "accountBalanceAtLeast", customer, account);
    }

    @Test
    public void studentAccountCustomerAgeLessThan1() {
        final DateMidnight NOW = new DateMidnight();
        Customer customer = createCustomerBasic();
        Account account = customer.getAccounts().iterator().next();
        customer.setDateOfBirth(NOW.minusYears(40).toDate());
        assertEquals(Account.Type.TRANSACTIONAL, account.getType());
        assertNotReportContains(Message.Type.ERROR, "studentAccountCustomerAgeLessThan", customer, account);
    }

    @Test
    public void studentAccountCustomerAgeLessThan2() {
        final DateMidnight NOW = new DateMidnight();
        Customer customer = createCustomerBasic();
        customer.setDateOfBirth(NOW.minusYears(20).toDate());
        Account account = customer.getAccounts().iterator().next();
        account.setType(Account.Type.STUDENT);
        assertNotReportContains(Message.Type.ERROR, "studentAccountCustomerAgeLessThan", customer, account);
    }

    @Test
    public void studentAccountCustomerAgeLessThan3() {
        final DateMidnight NOW = new DateMidnight();
        Customer customer = createCustomerBasic();
        Account account = customer.getAccounts().iterator().next();
        customer.setDateOfBirth(NOW.minusYears(20).toDate());
        account.setType(Account.Type.TRANSACTIONAL);
        assertNotReportContains(Message.Type.ERROR, "studentAccountCustomerAgeLessThan", customer);
    }

    @Test
    public void studentAccountCustomerAgeLessThan4() {
        final DateMidnight NOW = new DateMidnight();
        Customer customer = createCustomerBasic();
        Account account = customer.getAccounts().iterator().next();
        customer.setDateOfBirth(NOW.minusYears(40).toDate());
        account.setType(Account.Type.STUDENT);
        assertReportContains(Message.Type.ERROR, "studentAccountCustomerAgeLessThan", customer, account);
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
        assertReportContains(Message.Type.ERROR, "accountNumberUnique", customer, account);

        session.setGlobal("inquiryService", new BankingInquiryService() {
            @Override
            public boolean isAccountNumberUnique(Account account) {
                return true;
            }
        });

        assertNotReportContains(Message.Type.ERROR, "accountNumberUnique", customer, account);
    }

    protected void assertReportContains(Message.Type type, String messageKey, Customer customer, Object... context) {
        ValidationReport report = executeRules(type, messageKey, customer, context);
        assertTrue("Report doesn't contain message [" + messageKey + "]", report.contains(messageKey));
        Message message = getMessage(report, messageKey);
        assertEquals(Arrays.asList(context), message.getContextOrdered());
    }

    protected void assertNotReportContains(Message.Type type, String messageKey, Customer customer, Object... context) {
        ValidationReport report = executeRules(type, messageKey, customer, context);
        assertFalse("Report contains message [" + messageKey + "]", report.contains(messageKey));
        assertNull(getMessage(report, messageKey));
    }

    @SuppressWarnings("unchecked")
    private ValidationReport executeRules(Message.Type type, String messageKey, Customer customer, Object... context) {
        ValidationReport report = reportFactory.createValidationReport();
        List<Command<Object>> commands = newArrayList();
        commands.add(CommandFactory.newSetGlobal("validationReport", report));
        commands.add(CommandFactory.newInsertElements(getFacts(customer)));
        session.execute(CommandFactory.newBatchExecution(commands));
        return report;
    }

    private Collection<Object> getFacts(Customer customer) {
        List<Object> facts = newArrayList();
        facts.add(customer);
        facts.add(customer.getAddress());
        facts.addAll(customer.getAccounts());
        return facts;
    }

    protected Message getMessage(ValidationReport report, String messageKey) {
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
