package com.test.service;

import com.test.model.Account;

import java.math.BigDecimal;

public class InsufficientFundException extends Exception {
    public InsufficientFundException(Account from, BigDecimal amount) {
        super(String.format("Cannot transfer amount %s from account %d. Not enough money on account balance.", amount.toString(), from.getId()));
    }
}
