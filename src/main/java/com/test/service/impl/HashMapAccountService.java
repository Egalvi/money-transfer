package com.test.service.impl;

import com.test.model.Account;
import com.test.service.AccountService;
import com.test.service.InsufficientFundException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class HashMapAccountService implements AccountService {

    public static HashMapAccountService INSTANCE = new HashMapAccountService();

    private Object equalsLog = new Object();

    private HashMap<Integer, Account> accountStorage = new HashMap<>();

    @Override
    public Collection<Account> list() {
        return accountStorage.values();
    }

    @Override
    public Account get(int accountId) {
        return accountStorage.get(accountId);
    }

    @Override
    public void add(Account account) {
        int newId = accountStorage.size() == 0 ? 1 : Collections.<Integer>max(accountStorage.keySet()) + 1;
        account.setId(newId);
        accountStorage.put(newId, account);
    }

    @Override
    public void update(Account account) {
        accountStorage.put(account.getId(), account);
    }

    @Override
    public void delete(int id) {
        accountStorage.remove(id);
    }

    @Override
    public void transfer(Account from, Account to, BigDecimal amount) throws InsufficientFundException {
        if (from.hashCode() < to.hashCode()) {
            synchronized (from) {
                synchronized (to) {
                    innerTransfer(from, to, amount);
                }
            }
        } else if (from.hashCode() > to.hashCode()) {
            synchronized (to) {
                synchronized (from) {
                    innerTransfer(from, to, amount);
                }
            }
        } else {
            synchronized (equalsLog) {
                synchronized (to) {
                    synchronized (from) {
                        innerTransfer(from, to, amount);
                    }
                }
            }
        }
    }

    //TODO should be transactional
    private void innerTransfer(Account from, Account to, BigDecimal amount) throws InsufficientFundException {
        if (from.getAmount().compareTo(amount) < 0) {
            throw new InsufficientFundException(from, amount);
        } else {
            from.setAmount(from.getAmount().subtract(amount));
            to.setAmount(to.getAmount().add(amount));
        }
    }
}
