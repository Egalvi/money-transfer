package com.test.service;

import com.test.model.Account;

import java.math.BigDecimal;
import java.util.Collection;

public interface AccountService {

    Collection<Account> list();

    Account get(int accountId);

    void add(Account account);

    void update(Account account);

    void delete(Account account);

    void transfer(Account from, Account to, BigDecimal amount) throws InsufficientFundException;

}
