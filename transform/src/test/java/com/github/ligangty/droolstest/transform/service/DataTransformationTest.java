package com.github.ligangty.droolstest.transform.service;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.core.base.RuleNameEqualsAgendaFilter;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.GetObjectsCommand;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.conf.KieBaseOption;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;

import com.github.ligangty.droolstest.bank.model.Address;
import com.github.ligangty.droolstest.bank.service.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.service.Message;
import com.github.ligangty.droolstest.bank.service.ReportFactory;
import com.github.ligangty.droolstest.bank.service.ValidationReport;
import com.github.ligangty.droolstest.bank.utils.KieHelper;
import com.github.ligangty.droolstest.bank.utils.TrackingAgendaEventListener;

@SuppressWarnings("unchecked")
public class DataTransformationTest {
    protected static StatelessKieSession session;
    protected static ReportFactory reportFactory;
    protected static KieHelper kieHelper = KieHelper.newHelper();

    protected static KieBase kieBase;

    @BeforeClass
    public static void setUpClass() throws Exception {
        KieBaseOption[] options = null;
        KieBase kieBase = kieHelper.addFromClassPath("rules/transform.drl", DataTransformationTest.class.getClassLoader())
                .build(options);

        // @extract-start 03 09
        session = kieBase.newStatelessKieSession();
        session.setGlobal("legacyService", new MockLegacyBankService());
        // @extract-end

        reportFactory = new DefaultReportFactory();

        session.setGlobal("reportFactory", reportFactory);

        session.addEventListener(new DebugRuleRuntimeEventListener());
        session.addEventListener(new TrackingAgendaEventListener());

    }

    @Before
    public void initialize() throws Exception {
    }

    /*
     * @Ignore("mvel has problems with accessing anonymous classes?")
     * 
     * @Test public void findCustomer() throws Exception { final Map customerMap = new HashMap(); LegacyBankService service =
     * new MockLegacyBankService() {
     * 
     * @Override public List findAllCustomers() { return Arrays.asList(new Object[] { customerMap }); } };
     * session.setGlobal("legacyService", service); List<Command<?>> commands = new ArrayList<Command<?>>(); commands.add(new
     * FireAllRulesCommand( new RuleNameEqualsAgendaFilter("findAllCustomers"))); commands.add(new
     * GetObjectsCommandResults("customers", new ObjectFilter() { public boolean accept(Object object) { return object
     * instanceof Map && ((Map) object).get("_type_").equals( "Customer"); } })); BatchExecutionResults results =
     * session.execute( CommandFactory.newBatchExecution(commands));
     * 
     * assertEquals("Customer", customerMap.get("_type_")); Iterator<?> customerIterator = (Iterator<?>)
     * results.getValue("customers");
     * 
     * assertEquals(customerMap, customerIterator.next()); assertFalse(customerIterator.hasNext()); }
     */

    /*
     * @Ignore("mvel has problems with accessing anonymous classes?")
     * 
     * @Test public void findAddress() throws Exception { final Map customerMap = new HashMap(); customerMap.put("_type_",
     * "Customer"); customerMap.put("customer_id", new Long(111));
     * 
     * final Map addressMap = new HashMap(); LegacyBankService service = new MockLegacyBankService() {
     * 
     * @Override public List findAddressByCustomerId(Long customerId) { assertEquals(customerMap.get("customer_id"),
     * customerId); return Arrays.asList(new Object[] { addressMap }); } }; session.setGlobal("legacyService", service);
     * List<Command<?>> commands = new ArrayList<Command<?>>(); commands.add(CommandFactory.newInsertObject(customerMap));
     * commands.add(new FireAllRulesCommand( new RuleNameEqualsAgendaFilter("findAddress"))); commands.add(new
     * GetObjectsCommandResults("addresses", new ObjectFilter() { public boolean accept(Object object) { return object
     * instanceof Map && ((Map) object).get("_type_").equals( "Address"); } })); BatchExecutionResults results =
     * session.execute( CommandFactory.newBatchExecution(commands));
     * 
     * assertEquals("Address", addressMap.get("_type_")); Iterator<?> addressIterator = (Iterator<?>)
     * results.getValue("addresses");
     * 
     * assertEquals(addressMap, addressIterator.next()); assertFalse(addressIterator.hasNext()); }
     */

    // @extract-start 03 08
    @Test
    public void twoEqualAddressesDifferentInstance() throws Exception {
        Map<String, String> addressMap1 = newHashMap();
        addressMap1.put("_type_", "Address");
        addressMap1.put("street", "Barrack Street");

        Map<String, String> addressMap2 = newHashMap();
        addressMap2.put("_type_", "Address");
        addressMap2.put("street", "Barrack Street");
        assertEquals(addressMap1, addressMap2);

        ExecutionResults results = execute(Arrays.asList(addressMap1, addressMap2), "two equal addresses remove duplication",
                "Address", "addresses");

        Iterator<?> addressIterator = ((List<?>) results.getValue("addresses")).iterator();
        Map<String, String> addressMapWinner = (Map<String, String>) addressIterator.next();
        assertEquals(addressMap1, addressMapWinner);
        assertFalse(addressIterator.hasNext());
        reportContextContains(results, "two equal addresses remove duplication", addressMapWinner == addressMap1 ? addressMap2
                : addressMap1);
    }

    // @extract-end

    // @extract-start 03 72
    /**
     * creates multiple commands, calls session.execute and returns results back
     */
    protected ExecutionResults execute(Collection<?> objects, String ruleName, final String filterType, String filterOut) {
        ValidationReport validationReport = reportFactory.createValidationReport();
        List<Command<?>> commands = new ArrayList<Command<?>>();
        commands.add(CommandFactory.newSetGlobal("validationReport", validationReport, true));
        commands.add(CommandFactory.newInsertElements(objects));
        commands.add(new FireAllRulesCommand(new RuleNameEqualsAgendaFilter(ruleName)));
        if (filterType != null && filterOut != null) {
            GetObjectsCommand getObjectsCommand = new GetObjectsCommand(new ObjectFilter() {
                @SuppressWarnings("rawtypes")
                public boolean accept(Object object) {
                    return object instanceof Map && ((Map) object).get("_type_").equals(filterType);
                }
            });
            getObjectsCommand.setOutIdentifier(filterOut);
            commands.add(getObjectsCommand);
        }
        ExecutionResults results = session.execute(CommandFactory.newBatchExecution(commands));

        return results;
    }

    // @extract-end

    @Ignore("TODO implement")
    @Test
    public void twoOrMoreAddresses() throws Exception {
        // ..
    }

    // @extract-start 03 10
    @Test
    public void addressNormalizationUSA() throws Exception {
        Map<String, String> addressMap = newHashMap();
        addressMap.put("_type_", "Address");
        addressMap.put("country", "U.S.A");

        execute(Arrays.asList(addressMap), "address normalization USA", null, null);

        assertEquals(Address.Country.USA, addressMap.get("country"));
    }

    // @extract-end

    // @Test
    public void addressNormalizationIreland() throws Exception {
        Map<String, String> addressMap = newHashMap();
        addressMap.put("_type_", "Address");
        addressMap.put("country", "Republic of Ireland");

        execute(Arrays.asList(addressMap), "addressNormalizationIreland", null, null);

        assertEquals(Address.Country.Ireland, addressMap.get("country"));
    }

    // @extract-start 03 25
    @Test
    public void unknownCountry() throws Exception {
        Map<String, String> addressMap = newHashMap();
        addressMap.put("_type_", "Address");
        addressMap.put("country", "no country");

        ExecutionResults results = execute(Arrays.asList(addressMap), "unknown country", null, null);

        // ValidationReport report = (ValidationReport) results.getValue("validationReport");
        reportContextContains(results, "unknown country", addressMap);
    }

    // @extract-end

    // @extract-start 03 16
    @Test
    public void currencyConversionToUSD() throws Exception {
        Map<String, String> accountMap = newHashMap();
        accountMap.put("_type_", "Account");
        accountMap.put("currency", "EUR");
        accountMap.put("balance", "1000");

        execute(Arrays.asList(accountMap), "currency conversion to USD", null, null);

        assertEquals("USD", accountMap.get("currency"));
        assertEquals(new BigDecimal("1282.000"), accountMap.get("balance"));
    }

    @Test
    public void unknownCurrencyToUSD() throws Exception {
        Map<String, String> accountMap = newHashMap();
        accountMap.put("_type_", "Account");
        accountMap.put("currency", "unknown");
        accountMap.put("balance", "2345");

        ExecutionResults results = execute(Arrays.asList(accountMap), "unknown currency if no USD", null, null);
        assertEquals("unknown", accountMap.get("currency"));
        assertEquals("2345", accountMap.get("balance"));
        reportContextContains(results, "unknown currency if no USD", accountMap);

    }

    @Test
    public void nocurrencyToUSD() throws Exception {
        Map<String, String> accountMap = newHashMap();
        accountMap.put("_type_", "Account");

        execute(Arrays.asList(accountMap), "no currency to USD", null, null);

        assertEquals("USD", accountMap.get("currency"));
    }

    // @Test
    // public void nocurrencyToEUR() throws Exception {
    // Map<String, String> accountMap = newHashMap();
    // accountMap.put("_type_", "Account");
    //
    // execute(Arrays.asList(accountMap), "noCurrencyToEUR", null, null);
    //
    // assertEquals("EUR", accountMap.get("currency"));
    // }

    // @extract-start 03 16
    // @Test
    // public void currencyConversionToEUR() throws Exception {
    // Map<String, String> accountMap = newHashMap();
    // accountMap.put("_type_", "Account");
    // accountMap.put("currency", "USD");
    // accountMap.put("balance", "1000");
    //
    // execute(Arrays.asList(accountMap), "currencyConversionToEUR", null, null);
    //
    // assertEquals("EUR", accountMap.get("currency"));
    // assertEquals(new BigDecimal("780.000"), accountMap.get("balance"));
    // }

    // @extract-end

    // @Test
    // public void unknownCurrency() throws Exception {
    // Map<String, String> accountMap = newHashMap();
    // accountMap.put("_type_", "Account");
    // accountMap.put("currency", "unknown");
    // accountMap.put("balance", "1000");
    //
    // ExecutionResults results = execute(Arrays.asList(accountMap), "unknownCurrency", null, null);
    //
    // assertEquals("unknown", accountMap.get("currency"));
    // assertEquals("1000", accountMap.get("balance"));
    // reportContextContains(results, "unknownCurrency", accountMap);
    // }

    // @extract-start 03 20
    /**
     * asserts that the report contains one message with expected context (input parameter)
     */
    void reportContextContains(ExecutionResults results, String messgeKey, Object object) {
        ValidationReport validationReport = (ValidationReport) results.getValue("validationReport");
        assertEquals(1, validationReport.getMessages().size());
        Message message = validationReport.getMessages().iterator().next();
        List<Object> messageContext = message.getContextOrdered();
        assertEquals(1, messageContext.size());
        assertSame(object, messageContext.iterator().next());
    }

    // @extract-end

    // @extract-start 03 19
    @Test
    public void reduceLegacyAccounts() throws Exception {
        Map<String, Object> accountMap1 = newHashMap();
        accountMap1.put("_type_", "Account");
        accountMap1.put("customer_id", "00123");
        accountMap1.put("balance", new BigDecimal("100.00"));

        Map<String, Object> accountMap2 = newHashMap();
        accountMap2.put("_type_", "Account");
        accountMap2.put("customer_id", "00123");
        accountMap2.put("balance", new BigDecimal("300.00"));

        ExecutionResults results = execute(Arrays.asList(accountMap1, accountMap2), "reduce legacy accounts", "Account",
                "accounts");

        Iterator<?> accountIterator = ((List<?>) results.getValue("accounts")).iterator();
        Map<String, Object> accountMap = (Map<String, Object>) accountIterator.next();
        assertEquals(new BigDecimal("400.00"), accountMap.get("balance"));
        assertFalse(accountIterator.hasNext());
    }

    // @extract-end

    // @extract-start 03 21
    public class StaticMockLegacyBankService extends MockLegacyBankService {
        private Map<String, Object> addressMap;

        public StaticMockLegacyBankService(Map<String, Object> addressMap) {
            this.addressMap = addressMap;
        }

        public List<Map<String, Object>> findAddressByCustomerId(Long customerId) {
            return Arrays.asList(addressMap);
        }
    }

    // @extract-end

    // @extract-start 03 22
    // @Test
    public void findAddress() throws Exception {
        final Map<String, Object> customerMap = newHashMap();
        customerMap.put("_type_", "Customer");
        customerMap.put("customer_id", new Long(111));

        final Map<String, Object> addressMap = newHashMap();
        LegacyBankService service = new StaticMockLegacyBankService(addressMap);
        session.setGlobal("legacyService", service);

        ExecutionResults results = execute(Arrays.asList(customerMap), "findAddress", "Address", "objects");

        assertEquals("Address", addressMap.get("_type_"));
        Iterator<?> addressIterator = ((List<?>) results.getValue("objects")).iterator();
        assertEquals(addressMap, addressIterator.next());
        assertFalse(addressIterator.hasNext());

        // clean-up
        session.setGlobal("legacyService", new MockLegacyBankService());
    }
    // @extract-end
}
