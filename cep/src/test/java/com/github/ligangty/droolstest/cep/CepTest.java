package com.github.ligangty.droolstest.cep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.drools.core.base.RuleNameEqualsAgendaFilter;
import org.drools.core.time.SessionPseudoClock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.logger.KnowledgeRuntimeLogger;

import com.github.ligangty.droolstest.bank.utils.KieHelper;
import com.github.ligangty.droolstest.cep.bank.model.Account;
import com.github.ligangty.droolstest.cep.bank.model.AccountUpdatedEvent;
import com.github.ligangty.droolstest.cep.bank.model.LostCardEvent;
import com.github.ligangty.droolstest.cep.bank.model.TransactionCompletedEvent;
import com.github.ligangty.droolstest.cep.bank.model.TransactionCreatedEvent;

// @extract-start 06 03
public class CepTest {
    static KieBase kieBase;
    KieSession session;
    Account account;
    FactHandle accountHandle;
    SessionPseudoClock clock;
    TrackingAgendaEventListener trackingAgendaEventListener;
    EntryPoint entry;
    static KieHelper kieHelper = KieHelper.newHelper();

    @Before
    public void initialize() throws Exception {
        KieSessionConfiguration conf = kieHelper.newKieSessionConfiguration();
        conf.setOption(ClockTypeOption.get("pseudo"));
        session = kieBase.newKieSession(conf, null);
        clock = (SessionPseudoClock) session.getSessionClock();

        trackingAgendaEventListener = new TrackingAgendaEventListener();
        session.addEventListener(trackingAgendaEventListener);

        account = new Account();
        account.setNumber(123456l);
        account.setBalance(BigDecimal.valueOf(1000.00));
        accountHandle = session.insert(account);
        // @extract-end

        // logger = KnowledgeRuntimeLoggerFactory.newFileLogger(session, "mylogfile.log");

        /*
         * // @extract-start 06 24 accountInfoFactType = knowledgeBase.getFactType( "com.github.ligangty.droolstest.cep",
         * "AccountInfo"); accountInfo = accountInfoFactType.newInstance(); accountInfoFactType.set(accountInfo, "number",
         * account.getNumber()); accountInfoFactType.set(accountInfo, "averageBalance", BigDecimal.ZERO);
         * accountInfoFactType.set(accountInfo, "averageAmount", BigDecimal.ZERO); accountInfoHandle =
         * session.insert(accountInfo); // @extract-end
         */

        accountInfoFactType = kieBase.getFactType("com.github.ligangty.droolstest.cep", "AccountInfo");
        accountInfo = accountInfoFactType.newInstance();
        accountInfoFactType.set(accountInfo, "number", account.getNumber());
        accountInfoFactType.set(accountInfo, "averageBalance", BigDecimal.ZERO);
        accountInfoFactType.set(accountInfo, "averageAmount", BigDecimal.ZERO);
        accountInfoFactType.set(accountInfo, "averageNumberOfTransactions", BigDecimal.ZERO);
        accountInfoFactType.set(accountInfo, "numberOfTransactions1Day", Long.valueOf(0l));
        accountInfoHandle = session.insert(accountInfo);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // @extract-start 06 27
        // KnowledgeBuilderConfiguration builderConf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        // builderConf.setOption(AccumulateFunctionOption.get("bigDecimalAverage", new BigDecimalAverageAccumulateFunction()));
        // @extract-end

        // @extract-start 06 36
        KieBaseConfiguration config = kieHelper.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);
        
        // @extract-end

        kieBase = kieHelper.addFromClassPath("cep.drl", CepTest.class.getClassLoader()).build(config);
    }

    // @extract-end

    KnowledgeRuntimeLogger logger;

    // @extract-start 06 30
    FactType accountInfoFactType;
    Object accountInfo;
    FactHandle accountInfoHandle;

    // @extract-end

    @After
    public void terminate() {
        if (logger != null)
            logger.close();
        session.dispose();
    }

    // @extract-start 06 02
    @Test
    public void notification() throws Exception {
        session.fireAllRules();
        assertNotSame(Account.Status.BLOCKED, account.getStatus());
        entry = session.getEntryPoint("LostCardStream");
        entry.insert(new LostCardEvent(account.getNumber()));
        session.fireAllRules();
        assertSame(Account.Status.BLOCKED, account.getStatus());
    }

    // @extract-end

    // @extract-start 06 05
    @Test
    public void averageBalanceQuery() throws Exception {
        session.fireAllRules();
        assertEquals(account.getBalance(), getAverageBalance());

        Account account2 = new Account();
        account2.setBalance(BigDecimal.valueOf(1400));
        session.insert(account2);
        session.fireAllRules();
        
        Account account3 = new Account();
        account3.setBalance(BigDecimal.valueOf(2100));
        session.insert(account3);
        session.fireAllRules();
        assertEquals(BigDecimal.valueOf(1500.00), getAverageBalance());
    }

    BigDecimal getAverageBalance() {
        QueryResults queryResults = session.getQueryResults("averageBalanceQuery");
        return BigDecimal.valueOf((Double) queryResults.iterator().next().get("$averageBalance"));
    }

    // @extract-end

    // @extract-start 06 10
    @Test
    public void sequenceOfIncreasingWithdrawals() throws Exception {
        entry = session.getEntryPoint("TransactionStream");
        accountInfoFactType.set(accountInfo, "averageBalance", BigDecimal.valueOf(1000));
        session.update(accountInfoHandle, accountInfo);

        transactionCreatedEvent(290);
        clock.advanceTime(10, TimeUnit.SECONDS);
        transactionCreatedEvent(50);
        clock.advanceTime(10, TimeUnit.SECONDS);
        transactionCreatedEvent(300);
        assertNotFired("sequenceOfIncreasingWithdrawals");

        clock.advanceTime(10, TimeUnit.SECONDS);
        transactionCreatedEvent(350);
        assertNotFired("sequenceOfIncreasingWithdrawals");

        clock.advanceTime(10, TimeUnit.SECONDS);
        transactionCreatedEvent(400);
        clock.advanceTime(1, TimeUnit.MICROSECONDS);
        assertFired("sequenceOfIncreasingWithdrawals");
    }

    // @extract-end

    private void assertNotFired(String ruleName) {
        session.fireAllRules(new RuleNameEqualsAgendaFilter(ruleName));
        assertFalse(trackingAgendaEventListener.isRuleFired(ruleName));
    }

    // @extract-start 06 35
    private void assertFired(String ruleName) {
        session.fireAllRules(new RuleNameEqualsAgendaFilter(ruleName));
        assertTrue(trackingAgendaEventListener.isRuleFired(ruleName));
    }

    // @extract-end

    private void transactionCreatedEvent(double amountToTransfer) {
        entry.insert(new TransactionCreatedEvent(BigDecimal.valueOf(amountToTransfer).setScale(2), account.getNumber(), null,
                null));
    }

    // @extract-start 06 17
    private void transactionCompletedEvent(double amountTransferred) {
        entry.insert(new TransactionCompletedEvent(BigDecimal.valueOf(amountTransferred), account.getNumber()));
    }

    // @extract-end

    // @extract-start 06 08
    @Test
    public void twoLargeWithdrawals() throws Exception {
        entry = session.getEntryPoint("TransactionStream");
        transactionCompletedEvent(400);
        clock.advanceTime(5, TimeUnit.DAYS);
        transactionCompletedEvent(600);
        clock.advanceTime(11, TimeUnit.DAYS);

        transactionCreatedEvent(100);
        clock.advanceTime(30, TimeUnit.SECONDS);
        transactionCreatedEvent(1600);
        assertNotFired("twoLargeWithdrawals");

        clock.advanceTime(91, TimeUnit.SECONDS);
        transactionCreatedEvent(2100);
        assertNotFired("twoLargeWithdrawals");

        clock.advanceTime(30, TimeUnit.SECONDS);
        transactionCreatedEvent(1700);
        assertFired("twoLargeWithdrawals");
    }

    // @extract-end

    // @extract-start 06 19
    @Test
    public void averageBalanceOver30Days() throws Exception {
        entry = session.getEntryPoint("AccountStream");

        accountUpdatedEvent(account.getNumber(), 1000.50, 1000.50);
        accountUpdatedEvent(account.getNumber(), -700.40, 300.10);
        accountUpdatedEvent(account.getNumber(), 500, 800);
        accountUpdatedEvent(11223344l, 700, 1300);

        assertFired("averageBalanceOver30Days");
        assertEquals(BigDecimal.valueOf(700.20).setScale(2), accountInfoFactType.get(accountInfo, "averageBalance"));
    }

    // @extract-end

    private void accountUpdatedEvent(Long accountNumber, double amount, double balance) {
        entry.insert(new AccountUpdatedEvent(accountNumber, BigDecimal.valueOf(amount).setScale(2), BigDecimal.valueOf(balance)
                .setScale(2), UUID.randomUUID()));
    }

    @Test
    public void averageNumberOfTransactions() throws Exception {
        // set accountInfo to 0

        // add transaction
        // advance 20 days
        // add transaction
        // add transaction
        // verify average is 0.1

        // advance 15 days
        // add transaction
        // verify average is 0.1

        // add 3 transaction
        // verify average is 0.2

        session.fireAllRules();

    }

    @Test
    public void numberOfTransactions1Day() throws Exception {

        // session.fireAllRules();

    }

    // @extract-start 06 14
    @Test
    public void highActivity() throws Exception {
        accountInfoFactType.set(accountInfo, "averageNumberOfTransactions", BigDecimal.valueOf(10));
        accountInfoFactType.set(accountInfo, "numberOfTransactions1Day", 40l);
        accountInfoFactType.set(accountInfo, "averageBalance", BigDecimal.valueOf(9000));
        session.update(accountInfoHandle, accountInfo);
        assertNotFired("highActivity");

        accountInfoFactType.set(accountInfo, "averageBalance", BigDecimal.valueOf(11000));
        session.update(accountInfoHandle, accountInfo);
        assertNotFired("highActivity");

        accountInfoFactType.set(accountInfo, "numberOfTransactions1Day", 60l);
        accountInfoFactType.set(accountInfo, "averageBalance", BigDecimal.valueOf(6000));
        session.update(accountInfoHandle, accountInfo);
        assertNotFired("highActivity");

        accountInfoFactType.set(accountInfo, "averageBalance", BigDecimal.valueOf(11000));
        session.update(accountInfoHandle, accountInfo);
        assertFired("highActivity");
    }

    // @extract-end

    /*
     * @Test public void highActivity_withQuery() throws Exception { entry = session
     * .getWorkingMemoryEntryPoint("TransactionStream"); for (int i = 0; i < 30; i++) { for (int j = 0; j < 10; j++) {
     * transactionCompletedEvent(400+i); clock.advanceTime(1, TimeUnit.DAYS); } } for (int i = 0; i < 70; i++) {
     * transactionCompletedEvent(400+i); }
     * 
     * QueryResults queryResults = session.getQueryResults("numberOfTransactions1DayQuery", account.getNumber(), Variable.v);
     * assertEquals(1, queryResults.size()); assertEquals(account.getNumber(),
     * queryResults.iterator().next().get("accountNumber")); //assertEquals(70, queryResults.iterator().next().get("sum"));
     * 
     * QueryResults queryResults2 = session.getQueryResults("numberOfTransactions1DayQuery", Variable.v, 99); assertEquals(1,
     * queryResults2.size()); assertEquals(account.getNumber(), queryResults2.iterator().next().get("accountNumber"));
     * assertEquals(70, queryResults2.iterator().next().get("sum"));
     * 
     * accountInfoFactType.set(accountInfo, "averageNumberOfTransactions",BigDecimal.valueOf(10));
     * accountInfoFactType.set(accountInfo, "numberOfTransactions1Day", 40l); accountInfoFactType.set(accountInfo,
     * "averageBalance", BigDecimal.valueOf(9000)); session.update(accountInfoHandle, accountInfo);
     * assertNotFired("highActivity");
     * 
     * // accountInfoFactType.set(accountInfo, "averageBalance", // BigDecimal.valueOf(11000)); //
     * session.update(accountInfoHandle, accountInfo); // assertNotFired("highActivity");
     * 
     * accountInfoFactType.set(accountInfo, "numberOfTransactions1Day", 60l); accountInfoFactType.set(accountInfo,
     * "averageBalance", BigDecimal.valueOf(6000)); session.update(accountInfoHandle, accountInfo);
     * assertNotFired("highActivity");
     * 
     * accountInfoFactType.set(accountInfo, "averageBalance", BigDecimal.valueOf(11000)); session.update(accountInfoHandle,
     * accountInfo); assertFired("highActivity"); }
     */

    private BigDecimal getAverageBalanceOverMonth() {
        QueryResults queryResults = session.getQueryResults("averageBalanceOverMonthQuery",
                new Object[] { account.getNumber() });
        assertEquals(1, queryResults.size());
        QueryResultsRow queryResult = (QueryResultsRow) queryResults.iterator().next();
        return (BigDecimal) queryResult.get("$averageBalance");
    }

    @Test
    public void fireUntilHalt() throws Exception {
        // @extract-start 06 37
        new Thread(new Runnable() {
            public void run() {
                session.fireUntilHalt();
            }
        }).start();
        // @extract-end

        entry = session.getEntryPoint("AccountStream");

        accountUpdatedEvent(account.getNumber(), 1000.50, 1000.50);
        accountUpdatedEvent(account.getNumber(), -700.40, 300.10);
        accountUpdatedEvent(account.getNumber(), 500, 800);
        accountUpdatedEvent(11223344l, 700, 1300);

        // need to wait here
        // assertTrue(trackingAgendaEventListener
        // .isRuleFired("averageBalanceOver30Days"));
        // assertEquals(BigDecimal.valueOf(700.20).setScale(2),
        // accountInfoFactType.get(accountInfo,"averageBalance"));

        session.halt();
    }

}
