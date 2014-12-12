package com.github.ligangty.droolstest.bank.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.core.audit.WorkingMemoryFileLogger;
import org.drools.task.MockUserInfo;
import org.jbpm.bpmn2.JbpmBpmn2TestCase;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.utils.OnErrorAction;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.SystemEventListenerFactory;

import com.github.ligangty.droolstest.bank.model.Account;
import com.github.ligangty.droolstest.bank.model.Customer;
import com.github.ligangty.droolstest.bank.model.Loan;
import com.github.ligangty.droolstest.bank.service.BankingService;
import com.github.ligangty.droolstest.bank.utils.KieHelper;
import com.github.ligangty.droolstest.droolsflow.model.DefaultMessage;
import com.github.ligangty.droolstest.droolsflow.model.Rating;
import com.github.ligangty.droolstest.droolsflow.workitem.TransferWorkItemHandler;

@RunWith(JMock.class)
public class DefaultLoanApprovalServiceTest extends JbpmBpmn2TestCase {

    private static final String PROCESS_RATING_CALCULATION = "ratingCalculation";
    private static final String PROCESS_LOAN_APPROVAL = "loanApproval";
    static KieBase kieBase;
    KieSession session;
    Loan loan;
    Customer customer;
    ProcessInstance processInstance;
    BankingService bankingService;

    Mockery mockery;
    Account loanSourceAccount;

    static final long NODE_FAULT_NOT_VALID = 21;
    static final long NODE_SPLIT_VALIDATED = 20;
    final long NODE_SPLIT_AMOUNT_TO_BORROW = 4;
    // final long NODE_WORK_ITEM_EMAIL = 25;
    final long NODE_FAULT_LOW_RATING = 19;
    // @extract-start 07 03
    final long NODE_SUBFLOW_RATING_CALCULATION = 7;
    final long NODE_WORK_ITEM_TRANSFER = 26;
    // @extract-end
    final long NODE_SPLIT_RATING = 8;
    final long NODE_JOIN_RATING = 5;
    final long NODE_JOIN_PROCESS_LOAN = 23;
    final long NODE_GROUP_VALIDATE_LOAN = 14;
    final long NODE_HUMAN_TASK_PROCESS_LOAN = 12;

    final long NODE_GROUP_CALCULATE_RATING = 4;
    final long NODE_GROUP_CALCULATE_INCOMES = 2;
    final long NODE_GROUP_CALCULATE_EXPENSES = 3;
    final long NODE_GROUP_CALCULATE_REPAYMENTS = 8;

    TrackingProcessEventListener trackingProcessEventListener;

    WorkingMemoryFileLogger fileLogger;

    @BeforeClass
    public static void setUpClass() throws Exception {

        ClassLoader currentLoader = DefaultLoanApprovalServiceTest.class.getClassLoader();
        KieHelper helper = KieHelper.newHelper();
        helper.addFromClassPath("loanApproval.drl", currentLoader).addFromClassPath("loanApproval.bpmn", currentLoader)
                .addFromClassPath("ratingCalculation.drl", currentLoader)
                .addFromClassPath("ratingCalculation.bpmn", currentLoader);

        // it actually doesn't matter for runtime if we set this porperty or not
        // it is better to set it through a configuration file because the Eclipse ruleflow editor can pick it up
        KieBaseConfiguration configuration = helper.newKieBaseConfiguration();
        // configuration.setProperty("drools.workDefinitions", "WorkDefinitions.conf BankingWorkDefinitions.conf");

        kieBase = helper.build(configuration);
    }

    // @extract-start 07 01
    @Before
    public void setUp() throws Exception {
        session = createKnowledgeSession(kieBase);

        trackingProcessEventListener = new TrackingProcessEventListener();
        session.addEventListener(trackingProcessEventListener);
        session.getWorkItemManager().registerWorkItemHandler("Email", new SystemOutWorkItemHandler());

        loanSourceAccount = new Account();

        customer = new Customer();
        customer.setFirstName("Bob");
        customer.setLastName("Green");
        customer.setEmail("bob.green@mail.com");
        Account account = new Account();
        account.setNumber(123456789l);
        customer.addAccount(account);
        account.setOwner(customer);

        loan = new Loan();
        loan.setDestinationAccount(account);
        loan.setAmount(BigDecimal.valueOf(4000.0));
        loan.setDurationYears(2);
        // @extract-end

        mockery = new JUnit4Mockery();
        bankingService = mockery.mock(BankingService.class);

        WorkItemHandler handler = new SystemOutWorkItemHandler();
        session.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        WorkItemHandler transferFundsHandler = new SystemOutWorkItemHandler();
        session.getWorkItemManager().registerWorkItemHandler("Transfer Funds", transferFundsHandler);

        // fileLogger = new WorkingMemoryFileLogger(session);
    }

    // @extract-start 07 15
    private void startProcess() {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("loanSourceAccount", loanSourceAccount);
        parameterMap.put("customer", customer);
        parameterMap.put("loan", loan);
        processInstance = session.startProcess(PROCESS_LOAN_APPROVAL, parameterMap);
        session.insert(processInstance);
        session.fireAllRules();
    }

    // @extract-end
    // important note: Be careful when starting a process instance and then
    // inserting the process instance into the session, if there are rules
    // that react to a process instance then the 'startProcess' may not
    // trigger them; what seems to be happening is that when the process is
    // started it runs until it comes to a wait state or a ruleflow group
    // that has some activated rules, then it waits for the fireAllRules
    // call.

    @After
    public void onShutDown() {
        session.dispose();
        if (fileLogger != null) {
            fileLogger.writeToDisk();
        }
    }

    private void setUpLowAmount() {
        assertTrue(loan.getAmount().doubleValue() < 5000);
        session.insert(loan);
    }

    private void setUpHighAmount() {
        loan.setAmount(BigDecimal.valueOf(19000));
        session.insert(loan);
    }

    private void setUpHighRating() {
    }

    private void setUpLowRating() {
        Rating rating = new Rating();
        rating.setRating(10);
        session.insert(rating);
    }

    private void approveLoan() {
        processInstance.signalEvent("LoanApprovedEvent", null);
    }

    @Ignore("for now")
    @Test
    public void workItemSetup() {
        // RuleBaseConfiguration ruleBaseConfiguration = ((AbstractRuleBase)kieBase).getConfiguration();
        // System.out.println(ruleBaseConfiguration
        // .getWorkItemHandlers());
        // System.out.println(ruleBaseConfiguration
        // .getProcessWorkDefinitions());
    }

    @Test
    public void testTest() throws Exception {
        session.getWorkItemManager().registerWorkItemHandler("Email", new WorkItemHandler() {
            public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
                System.out.println("Sending email:");
                System.out.println("From: " + workItem.getParameter("From"));
                System.out.println("To: " + workItem.getParameter("To"));
                System.out.println("Subject: " + workItem.getParameter("Subject"));
                System.out.println("Body: ");
                System.out.println(workItem.getParameter("Body"));
                manager.completeWorkItem(workItem.getId(), null);
            }

            public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
            }
        });
        // ReportFactory factory = new DefaultReportFactory();
        // ValidationReport report = factory.createValidationReport();
        // session.setGlobal("validationReport", report);
        session.insert(new DefaultMessage());
        startProcess();
    }

    @Test
    public void validateLoan() {
        startProcess();
        assertTrue(trackingProcessEventListener.isNodeTriggered(PROCESS_LOAN_APPROVAL, NODE_GROUP_VALIDATE_LOAN));
        assertNodeTriggered(processInstance.getId(), "Validate Loan");
    }

    @Test
    public void validatedSplit() {
        startProcess();
        assertTrue(trackingProcessEventListener.isNodeTriggered(PROCESS_LOAN_APPROVAL, NODE_SPLIT_VALIDATED));
        assertNodeTriggered(processInstance.getId(), "Validated?");
    }

    // @extract-start 07 02
    @Test
    public void notValid() {
        session.insert(new DefaultMessage());
        startProcess();

        assertNodeTriggered(processInstance.getId(), "Not Valid");
        assertProcessInstanceAborted(processInstance.getId(), session);
    }

    // @extract-end

    @Test
    public void amountToBorrow() {
        startProcess();
        assertNodeTriggered(processInstance.getId(), "Amount to borrow?");
    }

    // @extract-start 07 07
    @Test
    public void amountToBorrowLow() {
        setUpLowAmount();
        startProcess();

        assertNodeTriggered(processInstance.getId(), "Join Rating");
        assertFalse(trackingProcessEventListener.isNodeTriggered(PROCESS_LOAN_APPROVAL, NODE_SUBFLOW_RATING_CALCULATION));
    }

    // @extract-end

    // @extract-start 07 05
    @Test
    public void amountToBorrowHighRatingCalculation() {
        setUpHighAmount();
        startProcess();
        assertNodeTriggered(processInstance.getId(), "Rating Calculation");
        assertTrue(trackingProcessEventListener.isNodeTriggered(PROCESS_RATING_CALCULATION, NODE_GROUP_CALCULATE_RATING));
        assertProcessVarExists(processInstance, "customerLoanRating");
        WorkflowProcessInstance process = (WorkflowProcessInstance) processInstance;
        assertEquals(1500, process.getVariable("customerLoanRating"));
    }

    // @extract-end

    // assertNodeTriggered(findProcessInstance("ratingCalculation").getId(),
    // "Calculate Rating");
    // private ProcessInstance findProcessInstance(String processId) {
    // for (ProcessInstance pi : session.getProcessInstances()) {
    // if (processId.equals(pi.getProcessId())) {
    // return pi;
    // }
    // }
    // return null;
    // }

    @Test
    public void ratingCalculation() {
        processInstance = session.startProcess(PROCESS_RATING_CALCULATION); // no parameters
        session.fireAllRules();
        assertTrue(trackingProcessEventListener.isNodeTriggered(PROCESS_RATING_CALCULATION, NODE_GROUP_CALCULATE_INCOMES));
        assertTrue(trackingProcessEventListener.isNodeTriggered(PROCESS_RATING_CALCULATION, NODE_GROUP_CALCULATE_REPAYMENTS));
        assertTrue(trackingProcessEventListener.isNodeTriggered(PROCESS_RATING_CALCULATION, NODE_GROUP_CALCULATE_EXPENSES));
        assertTrue(trackingProcessEventListener.isNodeTriggered(PROCESS_RATING_CALCULATION, NODE_GROUP_CALCULATE_RATING));
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertNotNull(session.getObjects(new ClassObjectFilter(Rating.class)).iterator().next());
    }

    @Test
    public void ratingSplitNodeOther() {
        setUpLowRating();
        setUpHighAmount();
        startProcess();

        assertFalse(trackingProcessEventListener.isNodeTriggered(PROCESS_LOAN_APPROVAL, NODE_JOIN_RATING));
        assertTrue(trackingProcessEventListener.isNodeTriggered(PROCESS_LOAN_APPROVAL, NODE_FAULT_LOW_RATING));
    }

    // @extract-start 07 10
    @Test
    public void ratingSplitNodeAccept() {
        setUpHighAmount();
        setUpHighRating();
        startProcess();

        assertNodeTriggered(processInstance.getId(), "Rating?");
        assertNodeTriggered(processInstance.getId(), "Join Rating");
    }

    // @extract-end

    // @extract-start 07 11
    @Test
    public void processLoan() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("droolsbook.jbpm");

        TaskService taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        LocalTaskService localTaskService = new LocalTaskService(taskService);

        MockUserInfo userInfo = new MockUserInfo();
        taskService.setUserinfo(userInfo);

        TaskServiceSession taskSession = taskService.createSession();
        taskSession.addUser(new UserImpl("Administrator"));
        taskSession.addUser(new UserImpl("123"));
        taskSession.addUser(new UserImpl("456"));
        taskSession.addUser(new UserImpl("789"));

        LocalHTWorkItemHandler htHandler = new LocalHTWorkItemHandler(localTaskService, session, OnErrorAction.RETHROW);
        htHandler.connect();
        session.getWorkItemManager().registerWorkItemHandler("Human Task", htHandler);
        setUpLowAmount();
        startProcess();
        // @extract-end

        // @extract-start 07 12
        List<TaskSummary> tasks = localTaskService.getTasksAssignedAsPotentialOwner("123", "en-UK");
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals("Process Loan", task.getName());
        assertEquals(3, task.getPriority());
        assertEquals(Status.Ready, task.getStatus());
        // @extract-end

        // @extract-start 07 13
        localTaskService.claim(task.getId(), "123");
        localTaskService.start(task.getId(), "123");
        localTaskService.complete(task.getId(), "123", null);

        assertNodeTriggered(processInstance.getId(), "Join Process");
    }

    // @extract-end

    // @extract-start 07 14
    @Test
    public void approveEventJoin() {
        setUpLowAmount();
        startProcess();
        assertProcessInstanceActive(processInstance.getId(), session);
        assertFalse(trackingProcessEventListener.isNodeTriggered(PROCESS_LOAN_APPROVAL, NODE_WORK_ITEM_TRANSFER));
        approveLoan();
        assertTrue(trackingProcessEventListener.isNodeTriggered(PROCESS_LOAN_APPROVAL, NODE_WORK_ITEM_TRANSFER));

        assertProcessInstanceCompleted(processInstance.getId(), session);
    }

    // @extract-end

    // @extract-start 07 09
    @Test
    public void transferFunds() {
        mockery.checking(new Expectations() {
            {
                one(bankingService).transfer(loanSourceAccount, loan.getDestinationAccount(), loan.getAmount());
            }
        });

        setUpTransferWorkItem();
        setUpLowAmount();
        startProcess();
        approveLoan();

        assertNodeTriggered(processInstance.getId(), "Transfer Funds");
    }

    // @extract-end

    private void setUpTransferWorkItem() {
        // @extract-start 07 08
        TransferWorkItemHandler transferHandler = new TransferWorkItemHandler();
        transferHandler.setBankingService(bankingService);
        session.getWorkItemManager().registerWorkItemHandler("Transfer Funds", transferHandler);
        // @extract-end
    }

    // integration test
    @Ignore("this is a abstract example")
    @Test
    public void defaultBankingServiceTest() throws Exception {
        bankingService.requestLoan(loan, customer);
    }

}
