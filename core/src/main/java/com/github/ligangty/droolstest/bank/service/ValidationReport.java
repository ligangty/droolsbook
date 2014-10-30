package com.github.ligangty.droolstest.bank.service;

import java.util.Set;

/**
 * represents the result of the validation process 
 *
 */
public interface ValidationReport {
    /**
     * @return all messages in this report
     */
    Set<Message> getMessages();

    /**
     * @return all messages of specified type in this report
     */
    Set<Message> getMessagesByType(Message.Type type);
    
    /**
     * @return true if this report contains message with specified key, false otherwise
     */
    Boolean contains(String messageKey);
    
    /**
     * adds specified message to this report 
     */
    Boolean addMessage(Message messate);

}
