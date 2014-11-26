/**
 * 
 */
package com.github.ligangty.droolstest.cep.bank.model;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionCreatedEvent extends TransactionEvent {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TransactionCreatedEvent(BigDecimal amount, Long fromAccountNumber, Long toAccountNumber, UUID tranasctionUuid) {
        super(amount, fromAccountNumber, toAccountNumber, tranasctionUuid);
    }
}