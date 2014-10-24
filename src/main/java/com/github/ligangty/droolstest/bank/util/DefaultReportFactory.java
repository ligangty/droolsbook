package com.github.ligangty.droolstest.bank.util;

import java.util.Arrays;

import com.github.ligangty.droolstest.bank.util.Message.Type;

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
