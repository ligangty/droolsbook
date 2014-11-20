package com.github.ligangty.droolstest.stateful.bank.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.BeforeClass;
import org.kie.api.KieBase;
import org.kie.api.conf.KieBaseOption;

import com.github.ligangty.droolstest.bank.model.Customer;
import com.github.ligangty.droolstest.bank.service.BankingInquiryService;
import com.github.ligangty.droolstest.bank.service.BankingInquiryServiceImpl;
import com.github.ligangty.droolstest.bank.service.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.service.Message;
import com.github.ligangty.droolstest.bank.service.ValidationReport;
import com.github.ligangty.droolstest.bank.validation.ValidationTest;

/**
 * for testing logical assertions that they don't break the functionality it uses stateless session,
 * 
 * @author miba
 * 
 */
public class StatefulValidationTest extends ValidationTest {

    static KieBase kieBase;
    static BankingInquiryService inquiryService;
    static ValidationReport validationReport;

    @BeforeClass
    public static void setUpClass() throws Exception {
        KieBaseOption[] options = null;
        kieBase = kieHelper.addFromClassPath("validation-stateful.drl", StatefulValidationTest.class.getClassLoader()).build(
                options);
        inquiryService = new BankingInquiryServiceImpl();
        reportFactory = new DefaultReportFactory();

        validationReport = new StatefulDefaultValidationReport();

        session = kieBase.newStatelessKieSession();
        session.setGlobal("validationReport", validationReport);
        session.setGlobal("reportFactory", reportFactory);
        session.setGlobal("inquiryService", inquiryService);
    }

    @After
    public void terminate() {
    }

    ValidationReport validate(Customer customer) {

        session.execute(getFacts(customer));

        /*
         * //create new jsut to be sure validationReport = new StatefulDefaultValidationReport();
         * 
         * StatelessSessionResult result = session.executeWithResults(getFacts(customer)); QueryResults queryResults =
         * result.getQueryResults("getAllMessages"); for (Iterator queryResultsIter = queryResults.iterator(); queryResultsIter
         * .hasNext();) { QueryResult queryResult = (QueryResult) queryResultsIter.next(); Message message = (Message)
         * queryResult.get("$message"); validationReport.addMessage(message); }
         */

        return validationReport;
    }

    Collection<Object> getFacts(Customer customer) {
        ArrayList<Object> facts = new ArrayList<Object>();
        facts.add(customer);
        facts.add(customer.getAddress());
        facts.addAll(customer.getAccounts());
        return facts;
    }

    @Override
    protected void assertReportContains(Message.Type type, String messageKey, Customer customer, Object... context) {
        ValidationReport validationReport = validate(customer);

        assertTrue("Report doesn't contain message [" + messageKey + "]", validationReport.contains(messageKey));
        Message message = getMessage(validationReport, messageKey);
        assertEquals(Arrays.asList(context), message.getContextOrdered());
    }

    @Override
    protected void assertNotReportContains(Message.Type type, String messageKey, Customer customer, Object... context) {
        ValidationReport validationReport = validate(customer);

        assertFalse("Report contains message [" + messageKey + "]", validationReport.contains(messageKey));
    }

}
