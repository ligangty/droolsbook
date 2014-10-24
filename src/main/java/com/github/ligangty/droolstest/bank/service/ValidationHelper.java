package com.github.ligangty.droolstest.bank.service;

import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.rule.RuleContext;

import com.github.ligangty.droolstest.bank.util.Message;
import com.github.ligangty.droolstest.bank.util.ReportFactory;
import com.github.ligangty.droolstest.bank.util.ValidationReport;

public class ValidationHelper {

    /**
     * adds an error message to the global validation report @param kontext RuleContext that is accessible from rule condition
     */
    public static void error(RuleContext kcontext, Object... context) {
        KieRuntime runtime = kcontext.getKieRuntime();
        ValidationReport validationReport = (ValidationReport) runtime.getGlobal("validationReport");
        ReportFactory reportFactory = (ReportFactory) runtime.getGlobal("reportFactory");
        validationReport.addMessage(reportFactory.createMessage(Message.Type.ERROR, kcontext.getRule().getName(), context));
    }

    public static void warning(RuleContext kcontext, Object... context) {
        KieRuntime runtime = kcontext.getKieRuntime();
        ValidationReport validationReport = (ValidationReport) runtime.getGlobal("validationReport");
        ReportFactory reportFactory = (ReportFactory) runtime.getGlobal("reportFactory");
        validationReport.addMessage(reportFactory.createMessage(Message.Type.WARNING, kcontext.getRule().getName(), context));
    }
}
