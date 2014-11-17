package com.github.ligangty.droolstest.decisiontables.bank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.builder.DecisionTableConfiguration;
import org.kie.internal.builder.DecisionTableInputType;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

import com.github.ligangty.droolstest.bank.utils.DroolsTestUtil;
import com.github.ligangty.droolstest.bank.utils.KieHelper;
import com.github.ligangty.droolstest.decisiontables.bank.model.Account;

public class InterestRateDecisionTableTest {

    // @extract-start 04 36
    static StatelessKieSession session;
    Account account;
    static DateMidnight DATE;
    static KieHelper kieHelper = KieHelper.newHelper();

    @BeforeClass
    public static void setUpClass() throws Exception {
        ClassLoader currentLoader = InterestRateDecisionTableTest.class.getClassLoader();
        KieBase kieBase = kieHelper.addFromClassPath("interest calculation.xls", currentLoader).build();
        session = kieBase.newStatelessKieSession();
        DATE = new DateMidnight(2008, 1, 1);

//        method();
    }

    @SuppressWarnings("unused")
    private static void method() throws Exception {
        DecisionTableConfiguration dtconf = KnowledgeBuilderFactory.newDecisionTableConfiguration();
        dtconf.setInputType(DecisionTableInputType.XLS);
        DroolsTestUtil.seeDecisionDrlContent(dtconf, ResourceFactory.newClassPathResource("interest calculation.xls"));
    }

    @Before
    public void setUp() throws Exception {
        account = new Account();
    }

    // @extract-end

    // @extract-start 04 37
    @Test
    public void deposit125EURfor40Days() throws Exception {
        account.setType(Account.Type.SAVINGS);
        account.setBalance(new BigDecimal("125.00"));
        account.setCurrency("EUR");
        account.setStartDate(DATE.minusDays(40));
        account.setEndDate(DATE);

        session.execute(account);

        assertEquals(new BigDecimal("3.00"), account.getInterestRate());
    }

    // @extract-end

    @Test
    public void deposit20USDfor10Days() throws Exception {
        account.setType(Account.Type.SAVINGS);
        account.setBalance(new BigDecimal("20.00"));
        account.setCurrency("USD");
        account.setStartDate(DATE);
        account.setEndDate(DATE.plusDays(10));

        session.execute(account);

        assertEquals(BigDecimal.ZERO.setScale(2), account.getInterestRate());
    }

    @Test
    public void deposit9000EURfor11months() throws Exception {
        account.setType(Account.Type.SAVINGS);
        account.setBalance(new BigDecimal("9000.00"));
        account.setCurrency("EUR");
        account.setStartDate(DATE.minusMonths(11));
        account.setEndDate(DATE);

        session.execute(account);

        assertEquals(new BigDecimal("3.75"), account.getInterestRate());
    }

    @Test
    public void noInterestRate() throws Exception {
        account.setType(Account.Type.SAVINGS);
        account.setBalance(new BigDecimal("1000000.00"));
        account.setCurrency("EUR");
        account.setStartDate(DATE.minusMonths(12));
        account.setEndDate(DATE);

        session.execute(account);

        assertNull(account.getInterestRate());
    }

    // @extract-start 04 38
    @Test
    public void defaultTransactionalRate() throws Exception {
        account.setType(Account.Type.TRANSACTIONAL);
        account.setCurrency("EUR");

        session.execute(account);

        assertEquals(new BigDecimal("0.01"), account.getInterestRate());
    }

    // @extract-end

    @Test
    public void defaultStudentRate() throws Exception {
        account.setType(Account.Type.STUDENT);
        account.setBalance(new BigDecimal("150.00"));
        account.setCurrency("EUR");

        session.execute(account);

        assertEquals(new BigDecimal("1.00"), account.getInterestRate());
    }

}
