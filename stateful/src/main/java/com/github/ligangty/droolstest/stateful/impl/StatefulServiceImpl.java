package com.github.ligangty.droolstest.stateful.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.drools.core.common.DroolsObjectInputStream;
import org.drools.core.common.DroolsObjectOutputStream;
import org.kie.api.KieBase;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategyAcceptor;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.marshalling.MarshallerFactory;

import com.github.ligangty.droolstest.bank.model.Account;
import com.github.ligangty.droolstest.bank.model.Customer;
import com.github.ligangty.droolstest.bank.service.BankingInquiryService;
import com.github.ligangty.droolstest.bank.service.BankingInquiryServiceImpl;
import com.github.ligangty.droolstest.bank.service.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.service.ReportFactory;
import com.github.ligangty.droolstest.bank.service.ValidationReport;
import com.github.ligangty.droolstest.stateful.StatefulService;
import com.github.ligangty.droolstest.stateful.bank.service.impl.StatefulDefaultValidationReport;

/**
 * can be stored in HTTP session -> is Serializable
 * 
 * @author miba
 * 
 */
// @extract-start 05 02
public class StatefulServiceImpl implements StatefulService, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private transient KieBase knowledgeBase;
    private transient KieSession kieSession;
    private transient ReportFactory reportFactory;

    public StatefulServiceImpl(KieBase knowledgeBase, ReportFactory reportFactory, BankingInquiryService inquiryService) {
        this.reportFactory = reportFactory;
        this.knowledgeBase = knowledgeBase;
        kieSession = createKnowledgeSession(inquiryService);
    }

    // @extract-end

    // @extract-start 05 22
    private KieSession createKnowledgeSession(BankingInquiryService inquiryService) {
        KieSession session = knowledgeBase.newKieSession();
        session.setGlobal("reportFactory", reportFactory);
        session.setGlobal("inquiryService", inquiryService);
        return session;
    }

    // @extract-end

    // @extract-start 05 03
    public void insertOrUpdate(Object fact) {
        if (fact == null) {
            return;
        }

        FactHandle factHandle = kieSession.getFactHandle(fact);

        if (factHandle == null) {
            kieSession.insert(fact);
        } else {
            kieSession.update(factHandle, fact);
        }
    }

    public void insertOrUpdateRecursive(Customer customer) {
        insertOrUpdate(customer);
        insertOrUpdate(customer.getAddress());
        if (customer.getAccounts() != null) {
            for (Account account : customer.getAccounts()) {
                insertOrUpdate(account);
            }
        }
    }

    // @extract-end

    /*
     * // @extract-start 05 04 public ValidationReport executeRules() { ValidationReport validationReport =
     * reportFactory.createValidationReport(); statefulSession.setGlobal("validationReport", validationReport);
     * statefulSession.fireAllRules(); return validationReport; } // @extract-end
     */

    public ValidationReport executeRules() {
        // /*
        ValidationReport validationReport = new StatefulDefaultValidationReport();
        kieSession.setGlobal("validationReport", validationReport);
        // */

        kieSession.fireAllRules();

        /*
         * ValidationReport validationReport = reportFactory .createValidationReport(); QueryResults queryResults =
         * statefulSession.getQueryResults("getAllMessages"); for (QueryResultsRow queryResultsRow : queryResults) { Message
         * message = (Message) queryResultsRow.get( "$message"); validationReport.addMessage(message); }
         */

        return validationReport;
    }

    // @extract-start 05 05
    public void terminate() {
        kieSession.dispose();
    }

    // @extract-end

    // @extract-start 05 33
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        DroolsObjectOutputStream droolsOut = new DroolsObjectOutputStream(out);
        droolsOut.writeObject(knowledgeBase);

        Marshaller marshaller = createSerializableMarshaller(knowledgeBase);
        marshaller.marshall(droolsOut, kieSession);
    }

    // @extract-end

    // @extract-start 05 35
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        DroolsObjectInputStream droolsIn = new DroolsObjectInputStream(in);
        this.knowledgeBase = (KieBase) droolsIn.readObject();

        Marshaller marshaller = createSerializableMarshaller(knowledgeBase);
        kieSession = marshaller.unmarshall(droolsIn);

        this.reportFactory = new DefaultReportFactory();
        kieSession.setGlobal("reportFactory", reportFactory);
        kieSession.setGlobal("inquiryService", new BankingInquiryServiceImpl());
    }

    // @extract-end

    // @extract-start 05 34
    private Marshaller createSerializableMarshaller(KieBase knowledgeBase) {
        ObjectMarshallingStrategyAcceptor acceptor = MarshallerFactory.newClassFilterAcceptor(new String[] { "*.*" });
        ObjectMarshallingStrategy strategy = MarshallerFactory.newSerializeMarshallingStrategy(acceptor);
        Marshaller marshaller = MarshallerFactory.newMarshaller(knowledgeBase, new ObjectMarshallingStrategy[] { strategy });
        return marshaller;
    }
    // @extract-end

    /*
     * // @extract-start 05 23 private void writeObject(ObjectOutputStream out) throws IOException { out.defaultWriteObject();
     * 
     * DroolsObjectOutputStream droolsOut = new DroolsObjectOutputStream( out); droolsOut.writeObject(knowledgeBase); } //
     * @extract-end
     * 
     * // @extract-start 05 24 private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
     * in.defaultReadObject();
     * 
     * DroolsObjectInputStream droolsIn = new DroolsObjectInputStream(in); this.knowledgeBase = (KieBase)droolsIn.readObject();
     * 
     * this.reportFactory = new DefaultReportFactory(); statefulSession = createKnowledgeSession( new
     * BankingInquiryServiceImpl()); } // @extract-end
     */
}
