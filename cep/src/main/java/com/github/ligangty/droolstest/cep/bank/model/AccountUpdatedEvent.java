package com.github.ligangty.droolstest.cep.bank.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AccountUpdatedEvent implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public final Long accountNumber;
    public final BigDecimal balance;
    public final UUID tranasctionUuid;

    public AccountUpdatedEvent(Long accountNumber, BigDecimal amount, BigDecimal balance, UUID tranasctionUuid) {
        super();
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.tranasctionUuid = tranasctionUuid;
    }

    public Long getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public UUID getTranasctionUuid() {
        return tranasctionUuid;
    }

    // do not override equals and hashCode, this class represents an active entity
    // e.g. each instance is unique

    private transient String toString;

    @Override
    public String toString() {
        if (toString == null) {
            toString = new ToStringBuilder(this).appendSuper(super.toString()).append("accountNumber", accountNumber)
                    .append("balance", balance).toString();
        }
        return toString;
    }

}
