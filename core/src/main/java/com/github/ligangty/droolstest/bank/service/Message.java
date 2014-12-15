package com.github.ligangty.droolstest.bank.service;

import java.util.List;

public interface Message {
    public enum Type {
        ERROR, WARNING,
    }

    Type getType();

    String getMessageKey();

    List<Object> getContextOrdered();

}
