package com.github.ligangty.droolstest.stateful.bank.service.impl;

import com.github.ligangty.droolstest.bank.service.DefaultValidationReport;

public class StatefulDefaultValidationReport extends DefaultValidationReport {

    // @extract-start 05 17
    /**
     * clears this report
     */
    public void reset() {
        messagesMap.clear();
    }
    // @extract-end

}
