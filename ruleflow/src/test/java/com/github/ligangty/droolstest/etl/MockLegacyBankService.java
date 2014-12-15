package com.github.ligangty.droolstest.etl;

import java.util.List;

import com.github.ligangty.droolstest.transform.service.LegacyBankService;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class MockLegacyBankService implements LegacyBankService {

    public List findAccountByCustomerId(Long customerId) {
        return null;
    }

    public List findAddressByCustomerId(Long customerId) {
        return null;
    }

    public List findAllCustomers() {
        return null;
    }

}
