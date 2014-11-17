package com.github.ligangty.droolstest.bank.utils;

import org.drools.compiler.compiler.DecisionTableFactory;
import org.kie.api.io.Resource;
import org.kie.internal.builder.DecisionTableConfiguration;

public class DroolsTestUtil {
    /*
     * A util method used to check decision table translation to drl content. .xls is behind the scenes converted into a drl; it
     * is done by the DecisiontableFactory.loadFromInputStream method; you can use this drl and examine its contents if it looks
     * as you're expecting
     */
    public static void seeDecisionDrlContent(DecisionTableConfiguration dtconf, Resource dtResource) throws Exception {
        // @extract-start 04 75
        String drlString = DecisionTableFactory.loadFromInputStream(dtResource.getInputStream(), dtconf);
        // @extract-end
        System.out.println(drlString);

    }
}
