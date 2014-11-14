/**
 * 
 */
package com.github.ligangty.droolstest.bank.utils;

import java.io.IOException;

import org.drools.compiler.compiler.DroolsParserException;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;

/**
 * @deprecated use {@link com.github.ligangty.droolstest.bank.utils.KieHelper} instead
 */
@Deprecated
public class DroolsHelper {

    public static KieBase createKieBase(String ruleFileClassPath, String ruleFilePath) throws DroolsParserException,
            IOException {
        return createKieBase(null, ruleFileClassPath, ruleFilePath);
    }

    public static KieBase createKieBase(KieBaseConfiguration config, String ruleFileClassPath, String ruleFilePath)
            throws DroolsParserException, IOException {
        return createKieBase(config, null, ruleFileClassPath, ruleFilePath);
    }

    public static KieBase createKieBase(KieBaseConfiguration config, KnowledgeBuilderConfiguration knowledgeBuilderConfig,
            String ruleFileClassPath, String ruleFilePath) throws DroolsParserException, IOException {
        KieServices kieServices = KieServices.Factory.get();
        KieResources kieResources = kieServices.getResources();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        KieRepository kieRepository = kieServices.getRepository();

        Resource resource = kieResources.newClassPathResource(ruleFileClassPath);
        // path has to start with src/main/resources
        // append it with the package from the rule
        kieFileSystem.write(ruleFilePath, resource);

        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }
        KieContainer kContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());

        return config == null ? kContainer.getKieBase() : kContainer.newKieBase(config);
    }

}
