package com.github.ligangty.droolstest.cep;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;

// @extract-start 06 16
public class TrackingAgendaEventListener extends DefaultAgendaEventListener {
    List<String> rulesFiredList = new ArrayList<String>();

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        rulesFiredList.add(event.getMatch().getRule().getName());
    }

    public boolean isRuleFired(String ruleName) {
        for (String firedRuleName : rulesFiredList) {
            if (firedRuleName.equals(ruleName)) {
                return true;
            }
        }
        return false;
    }

    public void reset() {
        rulesFiredList.clear();
    }
}
// @extract-end
