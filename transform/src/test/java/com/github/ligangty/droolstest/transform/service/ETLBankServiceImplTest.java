package com.github.ligangty.droolstest.transform.service;

import java.io.Reader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.conf.KieBaseOption;

import com.github.ligangty.droolstest.bank.service.DefaultReportFactory;
import com.github.ligangty.droolstest.bank.utils.KieHelper;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

public class ETLBankServiceImplTest {

    static DataTransformationServiceImpl etlBankService;
    static KieHelper kieHelper = new KieHelper();

    @BeforeClass
    public static void setUp() throws Exception {
        Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
        SqlMapClient sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
        reader.close();

        LegacyBankService legacyBankService = new IBatisLegacyBankService(sqlMapClient);

        KieBaseOption[] options = null;
        KieBase kieBase = kieHelper.addFromClassPath("rules/transform.drl", ETLBankServiceImplTest.class.getClassLoader())
                .build(options);

        etlBankService = new DataTransformationServiceImpl();
        // etlBankService.setBankingService(new BankingServiceImpl());
        etlBankService.setLegacyBankService(legacyBankService);
        etlBankService.setReportFactory(new DefaultReportFactory());
        etlBankService.setKieBase(kieBase);
    }

    @Test
    public void testEtl() {
        // TODO: setup your database before enabling this test
        etlBankService.etl();
    }

}