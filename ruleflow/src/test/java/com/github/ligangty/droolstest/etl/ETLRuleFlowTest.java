package com.github.ligangty.droolstest.etl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.conf.KieBaseOption;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.runtime.KieSession;

import com.github.ligangty.droolstest.bank.service.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.service.ReportFactory;
import com.github.ligangty.droolstest.bank.service.ValidationReport;
import com.github.ligangty.droolstest.bank.utils.KieHelper;
import com.github.ligangty.droolstest.bank.utils.MyDebugProcessEventListener;
import com.github.ligangty.droolstest.transform.service.LegacyBankService;
import com.google.common.collect.Maps;

public class ETLRuleFlowTest {

    LegacyBankService legacyBankService;
    ReportFactory reportFactory;
    ValidationReport validationReport;

    // @extract-start 04 76
    static KieBase kieBase;
    KieSession session;

    @BeforeClass
    public static void setUpClass() throws Exception {
        KieBaseOption[] options = null;
        kieBase = KieHelper.newHelper()
                .addFromClassPath("dataTransformation-ruleflow.drl", ETLRuleFlowTest.class.getClassLoader())
                .addFromClassPath("dataTransformation.bpmn", ETLRuleFlowTest.class.getClassLoader()).build(options);
    }

    @Before
    public void initialize() throws Exception {
        session = kieBase.newKieSession();
        // @extract-end
        session.setGlobal("legacyService", new MockLegacyBankService());

        reportFactory = new DefaultReportFactory();
        validationReport = reportFactory.createValidationReport();

        session.setGlobal("reportFactory", reportFactory);
        session.setGlobal("validationReport", validationReport);

        session.addEventListener(new MyDebugProcessEventListener());
        session.addEventListener(new DebugAgendaEventListener());
    }

    // @extract-start 04 74
    @After
    public void terminate() {
        session.dispose();
    }

    // FIXME refactor this, it is duplicated
    /*
     * void reportContextContains(Object object) { assertEquals(1, validationReport.getMessages().size()); List<Object>
     * messageContext = validationReport .getMessages().iterator().next().getContextOrdered(); assertEquals(1,
     * messageContext.size()); assertSame(object, messageContext.iterator().next()); }
     */

    // @extract-start 04 72
    @Test
    public void unknownCountryUnknown() throws Exception {
        Map<String,String> addressMap = Maps.newHashMap();
        addressMap.put("_type_", "Address");
        addressMap.put("country", "no country");

        session.insert(addressMap);
        session.startProcess("dataTransformation");
        session.fireAllRules();

        assertTrue(validationReport.contains("unknownCountry"));
    }

    // @extract-end

    // @extract-start 04 73
    @Test
    public void unknownCountryKnown() throws Exception {
        Map<String,String> addressMap = Maps.newHashMap();
        addressMap.put("_type_", "Address");
        addressMap.put("country", "Ireland");

        session.startProcess("dataTransformation");
        session.insert(addressMap);
        session.fireAllRules();

        assertFalse(validationReport.contains("unknownCountry"));
    }
    // @extract-end

}
