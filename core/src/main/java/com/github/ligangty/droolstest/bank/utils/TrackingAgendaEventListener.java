package com.github.ligangty.droolstest.bank.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener that will track all rule firings in a session.
 * 
 * @author Stephen Masters
 */
public class TrackingAgendaEventListener extends DefaultAgendaEventListener {

    private static Logger log = LoggerFactory.getLogger(TrackingAgendaEventListener.class);

    private List<Activation> activationList = new ArrayList<Activation>();

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        Rule rule = event.getMatch().getRule();

        String ruleName = rule.getName();
        Map<String, Object> ruleMetaDataMap = rule.getMetaData();

        activationList.add(new Activation(ruleName));
        StringBuilder sb = new StringBuilder("Rule fired: " + ruleName);

        if (ruleMetaDataMap.size() > 0) {
            sb.append("\n  With [" + ruleMetaDataMap.size() + "] meta-data:");
            for (String key : ruleMetaDataMap.keySet()) {
                sb.append("\n    key=" + key + ", value=" + ruleMetaDataMap.get(key));
            }
        }

        log.debug(sb.toString());
    }

    public boolean isRuleFired(String ruleName) {
        for (Activation a : activationList) {
            if (a.getRuleName().equals(ruleName)) {
                return true;
            }
        }
        return false;
    }

    public void reset() {
        activationList.clear();
    }

    public final List<Activation> getActivationList() {
        return activationList;
    }

    public String activationsToString() {
        if (activationList.size() == 0) {
            return "No activations occurred.";
        } else {
            StringBuilder sb = new StringBuilder("Activations: ");
            for (Activation activation : activationList) {
                sb.append("\n  rule: ").append(activation.getRuleName());
            }
            return sb.toString();
        }
    }

}

class Activation {
    /**
     * The name of the rule which was activated.
     */
    private String ruleName;

    /**
     * Default no-args constructor.
     */
    public Activation() {
    }

    /**
     * Simple constructor for just noting a rule name for an activation.
     * 
     * @param ruleName The name of the rule which was activated.
     */
    public Activation(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * @return The name of the rule which was activated.
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * @param ruleName The name of the rule which was activated.
     */
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Activation[ruleName=" + ruleName);
        sb.append("]");
        return sb.toString();
    }
}
