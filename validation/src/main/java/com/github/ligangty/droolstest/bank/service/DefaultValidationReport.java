package com.github.ligangty.droolstest.bank.service;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.github.ligangty.droolstest.bank.service.Message;
import com.github.ligangty.droolstest.bank.service.ValidationReport;
import com.github.ligangty.droolstest.bank.service.Message.Type;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DefaultValidationReport implements ValidationReport {
    protected Map<Message.Type, Set<Message>> messagesMap = newHashMap();

    @Override
    public Set<Message> getMessages() {
        Set<Message> messagesAll = newHashSet();
        for (Collection<Message> messages : messagesMap.values()) {
            messagesAll.addAll(messages);
        }
        return messagesAll;
    }

    @Override
    public Set<Message> getMessagesByType(Type type) {
        if (type == null) {
            return emptySet();
        }
        Set<Message> messages = messagesMap.get(type);
        if (messages == null) {
            return emptySet();
        } else {
            return messages;
        }
    }

    @Override
    public Boolean contains(String messageKey) {
        final String inMessageKey = messageKey;
        Message message = Iterables.tryFind(getMessages(), new Predicate<Message>() {
            @Override
            public boolean apply(Message arg0) {
                return inMessageKey.equals(arg0.getMessageKey());
            }
        }).orNull();
        return message != null;
    }

    @Override
    public Boolean addMessage(Message message) {
        if (message == null) {
            return false;
        }
        Set<Message> messages = messagesMap.get(message.getType());
        if (messages == null) {
            messages = newHashSet();
            messagesMap.put(message.getType(), messages);
        }
        return messages.add(message);
    }

}
