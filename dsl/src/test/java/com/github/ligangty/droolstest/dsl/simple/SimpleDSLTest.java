package com.github.ligangty.droolstest.dsl.simple;

import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.conf.KieBaseOption;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.conf.SequentialOption;

import com.github.ligangty.droolstest.bank.model.Customer;
import com.github.ligangty.droolstest.bank.utils.KieHelper;
import com.github.ligangty.droolstest.bank.utils.TrackingAgendaEventListener;

public class SimpleDSLTest {

    StatelessKieSession session;
    KieHelper kieHelper = KieHelper.newHelper();

    @Before
    public void setUp() throws Exception {
        ClassLoader currentLoader = SimpleDSLTest.class.getClassLoader();
        // set to sequential mode
        KieBaseOption[] options = { SequentialOption.YES };
        KieBase kieBase = kieHelper.addFromClassPath("simple.dsl", currentLoader)
                .addFromClassPath("simple.dslr", currentLoader).build(options);
        
        session = kieBase.newStatelessKieSession();
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
