package com.github.ligangty.droolstest.cep.bank.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class TransactionEvent implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public final Long fromAccountNumber;
    public final Long toAccountNumber;
    public final BigDecimal amount;
    public final UUID transactionUuid;

    public TransactionEvent(BigDecimal amount, Long fromAccountNumber, Long toAccountNumber, UUID tranasctionUuid) {
        this.amount = amount;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.transactionUuid = tranasctionUuid;
    }

    public Long getFromAccountNumber() {
        return fromAccountNumber;
    }

    public Long getToAccountNumber() {
        return toAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getTransactionUuid() {
        return transactionUuid;
    }

    // do not override equals and hashCode, this class represents an active entity
    // e.g. each instance is unique

    private transient String toString;

    @Override
    public String toString() {
        if (toString == null) {
            toString = new ToStringBuilder(this).appendSuper(super.toString()).append("fromAccountNumber", fromAccountNumber)
                    .append("toAccountNumber", toAccountNumber).append("amount", amount)
                    .append("tranasctionUuid", transactionUuid).toString();
        }
        return toString;
    }

}