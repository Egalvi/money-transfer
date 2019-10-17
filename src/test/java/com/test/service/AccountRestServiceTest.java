package com.test.service;

import com.test.model.Account;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class AccountRestServiceTest {
    private AccountRestService restService = new AccountRestService();

    @After
    public void tearDown() {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:accounts", "sa", "sa");
             PreparedStatement st = conn.prepareStatement("DELETE FROM ACCOUNT");
        ) {
            st.execute();
        } catch (SQLException e) {
            throw new AccountServiceException(e);
        }
    }

    @AfterClass
    public static void dropTable() {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:accounts", "sa", "sa");
             PreparedStatement st = conn.prepareStatement("DROP TABLE ACCOUNT");
        ) {
            st.execute();
        } catch (SQLException e) {
            throw new AccountServiceException(e);
        }
    }

    @Test
    public void testAddListGet() {
        assertEquals(0, restService.list().size());

        restService.add(new BigDecimal("100.00"));
        restService.add(new BigDecimal("200.00"));
        restService.add(new BigDecimal("300.00"));

        Collection<Account> accounts = restService.list();
        assertEquals(3, accounts.size());

        for (Account account : accounts) {
            assertEquals(account.getAmount(), restService.get(account.getId()).getAmount());
        }
    }

    @Test
    public void testUpdate() {
        restService.add(new BigDecimal("100.00"));

        Account account = new ArrayList<>(restService.list()).get(0);

        restService.update(account.getId(), new BigDecimal("200.00"));

        assertEquals(new BigDecimal("200.00"), restService.get(account.getId()).getAmount());
    }

    @Test
    public void testDelete() {
        restService.add(new BigDecimal("100.00"));

        Collection<Account> list = restService.list();
        Account account = new ArrayList<>(list).get(0);

        assertEquals(1, list.size());

        restService.delete(account.getId());

        assertEquals(0, restService.list().size());
    }

    @Test
    public void testTransfer() throws InsufficientFundException {
        restService.add(new BigDecimal("100.00"));
        restService.add(new BigDecimal("200.00"));

        Collection<Account> list = restService.list();
        Account account1 = new ArrayList<>(list).get(0);
        Account account2 = new ArrayList<>(list).get(1);

        restService.transfer(account1.getId(), account2.getId(), new BigDecimal("50"));

        assertEquals(new BigDecimal("50.00"), restService.get(account1.getId()).getAmount());
        assertEquals(new BigDecimal("250.00"), restService.get(account2.getId()).getAmount());
    }

    @Test
    public void testTransferToSelf() throws InsufficientFundException {
        restService.add(new BigDecimal("100.00"));

        Collection<Account> list = restService.list();
        Account account1 = new ArrayList<>(list).get(0);

        restService.transfer(account1.getId(), account1.getId(), new BigDecimal("50"));

        assertEquals(new BigDecimal("100.00"), restService.get(account1.getId()).getAmount());
    }
}