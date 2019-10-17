package com.test.service.impl;

import com.test.model.Account;
import com.test.service.AccountService;
import com.test.service.AccountServiceException;
import com.test.service.InsufficientFundException;
import org.h2.jdbcx.JdbcConnectionPool;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class H2AccountService implements AccountService {
    private static final class SQL {
        static final String LIST = "SELECT * FROM ACCOUNT";
        static final String GET = "SELECT * FROM ACCOUNT WHERE id=?";
        static final String ADD = "INSERT INTO account(amount) VALUES(?);";
        static final String DELETE = "DELETE FROM ACCOUNT WHERE id=?";
        static final String UPDATE = "UPDATE account SET amount=? WHERE id=?";
    }

    private static final class FIELD {
        static final String ID = "id";
        static final String AMOUNT = "amount";
    }

    public static final H2AccountService INSTANCE = new H2AccountService();

    private static final String connectionString = "jdbc:h2:mem:accounts;INIT=RUNSCRIPT FROM 'classpath:init.sql'";

    private JdbcConnectionPool pool;

    private Object equalsLock = new Object();

    public H2AccountService() {
        pool = JdbcConnectionPool.create(connectionString, "sa", "sa");
    }

    @Override
    public Collection<Account> list() {
        List<Account> result = new ArrayList<>();
        try (Connection conn = pool.getConnection();
             PreparedStatement st = conn.prepareStatement(SQL.LIST);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                result.add(new Account(rs.getInt(FIELD.ID), rs.getBigDecimal(FIELD.AMOUNT)));
            }
        } catch (SQLException e) {
            throw new AccountServiceException(e);
        }
        return result;
    }

    @Override
    public Account get(int accountId) {
        Account account = null;
        try (Connection conn = pool.getConnection();
             PreparedStatement st = conn.prepareStatement(SQL.GET);
        ) {
            st.setInt(1, accountId);
            try (ResultSet rs = st.executeQuery();) {
                while (rs.next()) {
                    account = new Account(rs.getInt(FIELD.ID), rs.getBigDecimal(FIELD.AMOUNT));
                }
            }
        } catch (SQLException e) {
            throw new AccountServiceException(e);
        }
        return account;
    }

    @Override
    public void add(Account account) {
        try (Connection conn = pool.getConnection();
             PreparedStatement st = conn.prepareStatement(SQL.ADD);
        ) {
            st.setBigDecimal(1, account.getAmount());
            st.execute();
        } catch (SQLException e) {
            throw new AccountServiceException(e);
        }
    }

    @Override
    public void update(Account account) {
        synchronized (account) {
            try (Connection conn = pool.getConnection();
                 PreparedStatement st = conn.prepareStatement(SQL.UPDATE);
            ) {
                st.setBigDecimal(1, account.getAmount());
                st.setInt(2, account.getId());
                st.execute();
            } catch (SQLException e) {
                throw new AccountServiceException(e);
            }
        }
    }

    @Override
    public void delete(int accountId) {
        try (Connection conn = pool.getConnection();
             PreparedStatement st = conn.prepareStatement(SQL.DELETE);
        ) {
            st.setInt(1, accountId);
            st.execute();
        } catch (SQLException e) {
            throw new AccountServiceException(e);
        }
    }

    @Override
    public void transfer(Account from, Account to, BigDecimal amount) throws InsufficientFundException {
        if (from.getId() == to.getId()) return;
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
            synchronized (equalsLock) {
                synchronized (to) {
                    synchronized (from) {
                        innerTransfer(from, to, amount);
                    }
                }
            }
        }
    }

    private void innerTransfer(Account from, Account to, BigDecimal amount) throws InsufficientFundException {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            if (from.getAmount().compareTo(amount) < 0) {
                throw new InsufficientFundException(from, amount);
            } else {
                from.setAmount(from.getAmount().subtract(amount));
                to.setAmount(to.getAmount().add(amount));
            }
            conn.setAutoCommit(false); //Will commit two updates in one transaction
            try (PreparedStatement st = conn.prepareStatement(SQL.UPDATE);
            ) {
                st.setBigDecimal(1, from.getAmount());
                st.setInt(2, from.getId());
                st.execute();
            }
            try (PreparedStatement st = conn.prepareStatement(SQL.UPDATE);
            ) {
                st.setBigDecimal(1, to.getAmount());
                st.setInt(2, to.getId());
                st.execute();
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                //Ignore
            }
            throw new AccountServiceException(e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                //Ignore
            }
        }
    }
}
