package com.github.ligangty.droolstest.bank.service.impl;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.kie.api.KieBase;
import org.kie.internal.builder.DecisionTableConfiguration;
import org.kie.internal.builder.DecisionTableInputType;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

import com.github.ligangty.droolstest.bank.service.BankingInquiryService;
import com.github.ligangty.droolstest.bank.service.BankingInquiryServiceImpl;
import com.github.ligangty.droolstest.bank.service.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.utils.DroolsTestUtil;
import com.github.ligangty.droolstest.bank.validation.ValidationTest;

/**
 * @author miba
 * 
 */
public class DecisionTablesValidationTest extends ValidationTest {

    static KieBase kieBase;
    static BankingInquiryService inquiryService;

    @BeforeClass
    public static void setUpClass() throws Exception {
//        method();
        ClassLoader currentLoader = DecisionTablesValidationTest.class.getClassLoader();
        KieBase kieBase = kieHelper.addFromClassPath("validation-full.xls", currentLoader).build();
        inquiryService = new BankingInquiryServiceImpl();
        reportFactory = new DefaultReportFactory();

        session = kieBase.newStatelessKieSession();
        session.setGlobal("reportFactory", reportFactory);
        session.setGlobal("inquiryService", inquiryService);
    }

    @SuppressWarnings("unused")
    private static void method() throws Exception {
        DecisionTableConfiguration dtconf = KnowledgeBuilderFactory.newDecisionTableConfiguration();
        dtconf.setInputType(DecisionTableInputType.XLS);
        DroolsTestUtil.seeDecisionDrlContent(dtconf, ResourceFactory.newClassPathResource("validation-full.xls"));
    }

//    @Ignore("Not implemented yet")
//    @Override
//    public void accountBalanceAtLeast() {
//    }

    @Ignore("Not implemented yet")
    @Override
    public void studentAccountCustomerAgeLessThan1() {
    }

    @Ignore("Not implemented yet")
    @Override
    public void studentAccountCustomerAgeLessThan2() {
    }

    @Ignore("Not implemented yet")
    @Override
    public void studentAccountCustomerAgeLessThan3() {
    }

    @Ignore("Not implemented yet")
    @Override
    public void studentAccountCustomerAgeLessThan4() {
    }

//    @Ignore("Not implemented yet")
//    @Override
//    public void accountNumberUnique() {
//    }

//    @Ignore("Not implemented yet")
//    @Override
//    public void accountOwnerRequired() {
//    }
}
