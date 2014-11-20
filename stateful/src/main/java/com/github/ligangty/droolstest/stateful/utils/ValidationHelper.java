package com.github.ligangty.droolstest.stateful.utils;

import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.rule.RuleContext;

import com.github.ligangty.droolstest.bank.service.Message;
import com.github.ligangty.droolstest.bank.service.ReportFactory;

/**
 * @author miba
 * 
 */
// @extract-start 05 11
public class ValidationHelper {
    /**
     * inserts new logical assertion - a message
     * 
     * @param kcontext RuleContext that is accessible from rule condition
     * @param context for the message
     */
    public static void error(RuleContext kcontext, Object... context) {
        KieRuntime kieRuntime = kcontext.getKieRuntime();
        ReportFactory reportFactory = (ReportFactory) kieRuntime.getGlobal("reportFactory");

        kcontext.insertLogical(reportFactory.createMessage(Message.Type.ERROR, kcontext.getRule().getName(), context));
    }

    // @extract-end

    public static void warning(RuleContext kcontext, Object... context) {
        KieRuntime kieRuntime = kcontext.getKieRuntime();
        ReportFactory reportFactory = (ReportFactory) kieRuntime.getGlobal("reportFactory");

        kcontext.insertLogical(reportFactory.createMessage(Message.Type.WARNING, kcontext.getRule().getName(), context));
    }

}
