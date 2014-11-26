/**
 * 
 */
package com.github.ligangty.droolstest.cep.bank.model;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionCompletedEvent extends TransactionEvent {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TransactionCompletedEvent(BigDecimal amount, Long fromAccountNumber, Long toAccountNumber, UUID tranasctionUuid) {
        super(amount, fromAccountNumber, toAccountNumber, tranasctionUuid);
    }

    public TransactionCompletedEvent(BigDecimal amount, Long fromAccountNumber) {
        super(amount, fromAccountNumber, null, null);
    }
}