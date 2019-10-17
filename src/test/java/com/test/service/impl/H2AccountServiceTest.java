package com.test.service.impl;

import com.test.model.Account;
import com.test.service.AccountService;
import com.test.service.AccountServiceException;
import com.test.service.InsufficientFundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class H2AccountServiceTest {
    private AccountService accountService = null;

    @Before
    public void init() {
        accountService = new H2AccountService();
    }

    @After
    public void tearDown() {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:accounts", "sa", "sa");
             PreparedStatement st = conn.prepareStatement("DROP TABLE ACCOUNT");
        ) {
            st.execute();
        } catch (SQLException e) {
            throw new AccountServiceException(e);
        }
    }

    @Test
    public void list() {

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
        Account account = new Account();
        account.setAmount(new BigDecimal("100.04"));
        accountService.add(account);

        assertNotNull(accountService.get(1));
        assertEquals(new BigDecimal("100.04"), accountService.get(1).getAmount());
    }

    @Test
    public void update() {
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
        Account account = new Account();
        account.setAmount(new BigDecimal("100.04"));
        accountService.add(account);

        assertEquals(1, accountService.list().size());

        accountService.delete(1);

        assertEquals(0, accountService.list().size());
    }

    @Test
    public void transfer() throws InsufficientFundException {
        Account account1 = new Account();
        account1.setAmount(new BigDecimal("100.04"));
        accountService.add(account1);
        Account account2 = new Account();
        account2.setAmount(new BigDecimal("200.05"));
        accountService.add(account2);

        account1.setId(1);
        account2.setId(2);
        accountService.transfer(account1, account2, new BigDecimal("50.04"));

        assertEquals(new BigDecimal("50.00"), accountService.get(account1.getId()).getAmount());
        assertEquals(new BigDecimal("250.09"), accountService.get(account2.getId()).getAmount());

    }

    @Test(expected = InsufficientFundException.class)
    public void transferInsufficient() throws InsufficientFundException {
        Account account1 = new Account();
        account1.setAmount(new BigDecimal("100.04"));
        accountService.add(account1);
        Account account2 = new Account();
        account2.setAmount(new BigDecimal("200.05"));
        accountService.add(account2);
        account1.setId(1);
        account2.setId(2);
        accountService.transfer(account1, account2, new BigDecimal("200"));
    }
}