package com.github.ligangty.droolstest.cep.bank.model;

import java.util.UUID;

public class SuspiciousTransaction {

    public enum SuspiciousTransactionSeverity {
        MINOR, MAJOR
    }

    @SuppressWarnings("unused")
    private final UUID transactionUuid;
    @SuppressWarnings("unused")
    private final SuspiciousTransactionSeverity severity;

    public SuspiciousTransaction(UUID transactionUuid, SuspiciousTransactionSeverity severity) {
        this.transactionUuid = transactionUuid;
        this.severity = severity;
    }

}
