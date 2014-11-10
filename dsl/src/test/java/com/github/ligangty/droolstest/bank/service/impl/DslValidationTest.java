package com.github.ligangty.droolstest.bank.service.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.conf.SequentialOption;

import com.github.ligangty.droolstest.bank.service.BankingInquiryService;
import com.github.ligangty.droolstest.bank.service.BankingInquiryServiceImpl;
import com.github.ligangty.droolstest.bank.service.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.service.ReportFactory;
import com.github.ligangty.droolstest.bank.utils.TrackingAgendaEventListener;

public class DslValidationTest {
    protected KieSession session;
    protected ReportFactory reportFactory;

    @Before
    public void setUp() throws Exception {

        KieServices kieServices = KieServices.Factory.get();
        KieResources kieResources = kieServices.getResources();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        KieRepository kieRepository = kieServices.getRepository();

        // path has to start with src/main/resources
        // append it with the package from the rule
        kieFileSystem.write("src/main/resources/rules/validation.dslr",
                kieResources.newClassPathResource("rules/validation.dslr"));
        kieFileSystem.write("src/main/resources/dsl/validation.dsl", kieResources.newClassPathResource("dsl/validation.dsl"));

        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }
        KieContainer kContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
        
        // set to sequential mode
        KieBaseConfiguration configuration = kieServices.newKieBaseConfiguration();
        configuration.setOption(SequentialOption.YES);
        session = kContainer.newKieBase(configuration).newKieSession();
        session.addEventListener(new TrackingAgendaEventListener());

        BankingInquiryService inquiryService = new BankingInquiryServiceImpl();
        reportFactory = new DefaultReportFactory();

        session.setGlobal("reportFactory", reportFactory);
        session.setGlobal("inquiryService", inquiryService);
    }


    @Ignore("Not implemented yet")
    public void accountBalanceAtLeast() throws Exception {
    }

    @Ignore("Not implemented yet")
    public void studentAccountCustomerAgeLessThan() throws Exception {
    }

    @Ignore("Not implemented yet")
    public void accountNumberUnique() throws Exception {
    }

}
