package com.github.ligangty.droolstest.bank.service;

import java.util.Arrays;

import com.github.ligangty.droolstest.bank.service.Message;
import com.github.ligangty.droolstest.bank.service.ReportFactory;
import com.github.ligangty.droolstest.bank.service.ValidationReport;
import com.github.ligangty.droolstest.bank.service.Message.Type;

public class DefaultReportFactory implements ReportFactory {

    @Override
    public ValidationReport createValidationReport() {
        return new DefaultValidationReport();
    }

    @Override
    public Message createMessage(Type type, String messageKey, Object... context) {
        return new DefaultMessage(type, messageKey, Arrays.asList(context));
    }
}
