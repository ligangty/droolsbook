package com.github.ligangty.droolstest.transform.service;

import java.sql.SQLException;
import java.util.List;

import com.ibatis.sqlmap.client.SqlMapClient;

public class IBatisLegacyBankService implements LegacyBankService {

    private SqlMapClient sqlMapClient;

    public IBatisLegacyBankService(SqlMapClient sqlMapClient) {
        this.sqlMapClient = sqlMapClient;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List findAccountByCustomerId(Long customerId) {
        try {
            System.out.println("Executing findAccountByCustomerId with customerId=" + customerId);
            return sqlMapClient.queryForList("findAccountByCustomerId", customerId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List findAddressByCustomerId(Long customerId) {
        try {
            System.out.println("Executing findAddressByCustomerId with customerId=" + customerId);
            return sqlMapClient.queryForList("findAddressByCustomerId", customerId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List findAllCustomers() {
        try {
            System.out.println("Executing findAllCustomers");
            return sqlMapClient.queryForList("findAllCustomers");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}