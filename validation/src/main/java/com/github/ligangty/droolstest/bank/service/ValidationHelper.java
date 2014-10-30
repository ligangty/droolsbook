package com.github.ligangty.droolstest.bank.service;

import java.util.Date;

import org.joda.time.DateMidnight;
import org.joda.time.Years;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.rule.RuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationHelper {
    private static final Logger logger = LoggerFactory.getLogger(ValidationHelper.class);

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

    public static int yearsPassedSince(Date date) {
        return Years.yearsBetween(new DateMidnight(date), new DateMidnight()).getYears();
    }

    public static void log(String logMessage) {
        logger.debug(logMessage);
    }
}
