package com.github.ligangty.droolstest.bank.utils;

import static org.kie.api.io.ResourceType.determineResourceType;

import java.io.InputStream;

import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.KieBaseOption;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;

public class KieHelper {

    public final KieServices ks = KieServices.Factory.get();

    public final KieFileSystem kfs = ks.newKieFileSystem();

    private int counter = 0;

    public static final KieHelper newHelper() {
        return new KieHelper();
    }

    public KieBase build(KieBaseOption... options) {
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException(results.getMessages().toString());
        }
        KieContainer kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        if (options == null || options.length == 0) {
            return kieContainer.getKieBase();
        }
        KieBaseConfiguration kieBaseConf = ks.newKieBaseConfiguration();
        for (KieBaseOption option : options) {
            kieBaseConf.setOption(option);
        }
        return kieContainer.newKieBase(kieBaseConf);
    }

    public KieHelper addContent(String content, ResourceType type) {
        kfs.write(generateResourceName(type), content);
        return this;
    }

    public KieHelper addFromClassPath(String name) {
        return addFromClassPath(name, null);
    }

    public KieHelper addFromClassPath(String name, ClassLoader loader) {
        return addFromClassPath(name, null, loader);
    }

    public KieHelper addFromClassPath(String name, String encoding, ClassLoader loader) {
        InputStream input = null;
        if (loader != null) {
            input = loader.getResourceAsStream(name);
        } else {
            input = getClass().getResourceAsStream(name);
        }
        if (input == null) {
            throw new IllegalArgumentException("The file (" + name + ") does not exist as a classpath resource.");
        }
        ResourceType type = determineResourceType(name);
        String resourceExt = determineResourceExtenstion(name);
        String resourceName = null;
        if (resourceExt != null && resourceExt.equals(type.getDefaultExtension())) {
            resourceName = generateResourceName(type);
        } else {
            resourceName = generateResourceName(type, resourceExt);
        }
        kfs.write(resourceName, ks.getResources().newInputStreamResource(input, encoding));
        return this;
    }

    public KieHelper addResource(Resource resource) {
        kfs.write(resource);
        return this;
    }

    public KieHelper addResource(Resource resource, ResourceType type) {
        if (resource.getSourcePath() == null && resource.getTargetPath() == null) {
            resource.setSourcePath(generateResourceName(type));
        }
        return addResource(resource);
    }

    private String determineResourceExtenstion(String resourceName) {
        String[] resourceToken = resourceName.split("\\.");
        return resourceToken[resourceToken.length - 1];
    }

    private String generateResourceName(ResourceType type) {
        return generateResourceName(type, null);
    }

    private String generateResourceName(ResourceType type, String otherExtension) {
        if (otherExtension == null || otherExtension.trim().equals("")) {
            return "src/main/resources/file" + counter++ + "." + type.getDefaultExtension();
        } else {
            return "src/main/resources/file" + counter++ + "." + otherExtension;
        }
    }
}
