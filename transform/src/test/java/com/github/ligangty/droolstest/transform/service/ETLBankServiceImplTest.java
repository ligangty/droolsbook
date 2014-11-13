package com.github.ligangty.droolstest.transform.service;

import java.io.Reader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;

import com.github.ligangty.droolstest.bank.service.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.utils.DroolsHelper;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

public class ETLBankServiceImplTest {

    static DataTransformationServiceImpl etlBankService;

    @BeforeClass
    public static void setUp() throws Exception {
        Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
        SqlMapClient sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
        reader.close();

        LegacyBankService legacyBankService = new IBatisLegacyBankService(sqlMapClient);

        KieBase kieBase = DroolsHelper.createKieBase("rules/transform.drl", "src/main/resources/rules/transform.drl");

        etlBankService = new DataTransformationServiceImpl();
        // etlBankService.setBankingService(new BankingServiceImpl());
        etlBankService.setLegacyBankService(legacyBankService);
        etlBankService.setReportFactory(new DefaultReportFactory());
        etlBankService.setKnowledgeBase(kieBase);
    }

    @Test
    public void testEtl() {
        // TODO: setup your database before enabling this test
         etlBankService.etl();
    }

}