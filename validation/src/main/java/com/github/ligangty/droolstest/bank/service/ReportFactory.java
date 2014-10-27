package com.github.ligangty.droolstest.bank.service;

import com.github.ligangty.droolstest.bank.service.Message;
import com.github.ligangty.droolstest.bank.service.ValidationReport;

public interface ReportFactory {
    ValidationReport createValidationReport();

    Message createMessage(Message.Type type, String messageKey, Object... context);
}
