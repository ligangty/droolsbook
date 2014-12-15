package com.github.ligangty.droolstest.bank.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;

import com.github.ligangty.droolstest.bank.model.Account;
import com.github.ligangty.droolstest.bank.model.Customer;
import com.github.ligangty.droolstest.bank.model.Loan;

public class DefaulLoanApprovalService /* implements LoanApprovalService */{

    // @extract-start 07 06
    KieBase kieBase;
    Account loanSourceAccount;

    /**
     * runs the loan approval process for a specified customer's loan
     */
    public void approveLoan(Loan loan, Customer customer) {
        KieSession session = kieBase.newKieSession();
        try {
            registerWorkItemHandlers(session);
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("loanSourceAccount", loanSourceAccount);
            parameterMap.put("customer", customer);
            parameterMap.put("loan", loan);
            session.insert(loan);
            session.insert(customer);
            ProcessInstance processInstance = session.startProcess("loanApproval", parameterMap);
            session.insert(processInstance);
            session.fireAllRules();
        } finally {
            session.dispose();
        }
    }

    // @extract-end

    private void registerWorkItemHandlers(KieSession session) {
        // TODO Auto-generated method stub

    }

    public void setKieBase(KieBase knowledgeBase) {
        this.kieBase = knowledgeBase;
    }

    public void setLoanSourceAccount(Account loanSourceAccount) {
        this.loanSourceAccount = loanSourceAccount;
    }

}
