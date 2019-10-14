package com.test.service.impl;

import com.test.model.Account;
import com.test.service.AccountService;
import com.test.service.InsufficientFundException;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class HashMapAccountServiceTest {

    @Test
    public void list() {
        AccountService accountService = new HashMapAccountService();
        Account account = new Account();
        account.setAmount(new BigDecimal("100.04"));
        accountService.add(account);
        account = new Account();
        account.setAmount(new BigDecimal("200.05"));
        accountService.add(account);
        account = new Account();
        account.setAmount(new BigDecimal("300.06"));
        accountService.add(account);

        assertEquals(3, accountService.list().size());
    }

    @Test
    public void get() {
        AccountService accountService = new HashMapAccountService();
        Account account = new Account();
        account.setAmount(new BigDecimal("100.04"));
        accountService.add(account);

        assertNotNull(accountService.get(1));
        assertEquals(new BigDecimal("100.04"), accountService.get(1).getAmount());
    }

    @Test
    public void update() {
        AccountService accountService = new HashMapAccountService();
        Account account = new Account();
        account.setAmount(new BigDecimal("100.04"));
        accountService.add(account);

        Account account1 = new Account();
        account1.setId(1);
        account1.setAmount(new BigDecimal("50.00"));
        accountService.update(account1);

        assertEquals(1, accountService.list().size());
        assertNotNull(accountService.get(1));
        assertEquals(new BigDecimal("50.00"), accountService.get(1).getAmount());
    }

    @Test
    public void delete() {
        AccountService accountService = new HashMapAccountService();
        Account account = new Account();
        account.setAmount(new BigDecimal("100.04"));
        accountService.add(account);

        assertEquals(1, accountService.list().size());

        accountService.delete(account);

        assertEquals(0, accountService.list().size());
    }

    @Test
    public void transfer() throws InsufficientFundException {
        AccountService accountService = new HashMapAccountService();
        Account account1 = new Account();
        account1.setAmount(new BigDecimal("100.04"));
        accountService.add(account1);
        Account account2 = new Account();
        account2.setAmount(new BigDecimal("200.05"));
        accountService.add(account2);

        accountService.transfer(account1, account2, new BigDecimal("50.04"));

        assertEquals(new BigDecimal("50.00"), accountService.get(account1.getId()).getAmount());
        assertEquals(new BigDecimal("250.09"), accountService.get(account2.getId()).getAmount());

    }

    @Test(expected = InsufficientFundException.class)
    public void transferInsufficient() throws InsufficientFundException {
        AccountService accountService = new HashMapAccountService();
        Account account1 = new Account();
        account1.setAmount(new BigDecimal("100.04"));
        accountService.add(account1);
        Account account2 = new Account();
        account2.setAmount(new BigDecimal("200.05"));
        accountService.add(account2);

        accountService.transfer(account1, account2, new BigDecimal("200"));

    }
}