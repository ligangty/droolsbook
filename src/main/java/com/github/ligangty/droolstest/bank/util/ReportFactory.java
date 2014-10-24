package com.github.ligangty.droolstest.bank.util;

public interface ReportFactory {
    ValidationReport createValidationReport();

    Message createMessage(Message.Type type, String messageKey, Object... context);
}
