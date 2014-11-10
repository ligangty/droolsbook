package com.github.ligangty.droolstest.dsl.simple;

import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.io.KieResources;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.conf.SequentialOption;
import org.kie.internal.io.ResourceFactory;

import com.github.ligangty.droolstest.bank.model.Customer;
import com.github.ligangty.droolstest.bank.service.BankingInquiryService;
import com.github.ligangty.droolstest.bank.service.BankingInquiryServiceImpl;
import com.github.ligangty.droolstest.bank.service.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.utils.TrackingAgendaEventListener;

public class SimpleDSLTest {

    KieSession session;

    @Before
    public void setUp() throws Exception {

        KieServices kieServices = KieServices.Factory.get();
        KieResources kieResources = kieServices.getResources();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        KieRepository kieRepository = kieServices.getRepository();

        // path has to start with src/main/resources
        // append it with the package from the rule
        kieFileSystem.write("src/main/resources/simple.dsl", kieResources.newClassPathResource("simple.dsl"));
        kieFileSystem.write("src/main/resources/simple.dslr", kieResources.newClassPathResource("simple.dslr"));

        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }
        KieContainer kContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());

        session = kContainer.newKieSession();
        session.addEventListener(new TrackingAgendaEventListener());
    }

    // @extract-end

    @SuppressWarnings("unchecked")
    @Test
    public void simple1() throws Exception {
        // verify that there is no message
        Customer customer = new Customer();
        customer.setFirstName("Daaa");
        session.execute(CommandFactory.newInsert(customer));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void simple2() throws Exception {
        // verify that there is message
        Customer customer = new Customer();
        customer.setFirstName("David");
        session.execute(CommandFactory.newInsert(customer));
    }

}
