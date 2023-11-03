package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcAccountDao implements AccountDao {

    private final JdbcTemplate jdbcTemplate;
    private static int PENDING = 1;
    private static int APPROVED =2;
    private static int REJECTED = 3;

    private static int REQUEST = 1;
    private static int SEND = 2;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal getBalance(int accountId){
        Account newAccount = null;
        // create account
        String sql = "SELECT * FROM account WHERE account_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
            if(results.next()){
                 newAccount = mapRowToAccount(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        System.out.println(newAccount.getBalance());
        return newAccount.getBalance();
    }
    // Add a method that gets accountId based on userId
    @Override
    public int getAccountIdFromUserId(int userId){
        Account newAccount = null;
        // create account
        String sql = "SELECT * FROM account WHERE user_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            if(results.next()){
                newAccount = mapRowToAccount(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return newAccount.getAccountId();

    }

    @Override
    public boolean sendTEBucks(BigDecimal amountToAdd, int recepientId, int senderId) {
        boolean didItWork = false;
        if (recepientId == senderId) {
            throw new DaoException("Cannot send money to own account");
        }
        if (amountToAdd.equals(new BigDecimal(0))) {
            throw new DaoException("Cannot send an amount of 0");
        }
        if (amountToAdd.compareTo(BigDecimal.valueOf(0)) < 0) {
            throw new DaoException("Cannot send a negative amount");
        }
        BigDecimal balanceAvailable = getBalance(getAccountIdFromUserId(senderId));
        if (amountToAdd.compareTo(balanceAvailable) > 0) {
            throw new DaoException("Cannot send more money than is in your account");
        }
        String insertTransferSql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?)";
        String sql = "UPDATE account SET balance = balance + ? " +
                "WHERE user_id = ?";
        String sql1 = "UPDATE account SET balance = balance - ? " + "" +
                "WHERE user_id =?";
        //create a record in the transfers table

        //TODO:create a record in the transfers table
        int transferTypeId = SEND;
        int transferStatusId = APPROVED;

        try {
            int insertResults = jdbcTemplate.update(insertTransferSql, transferTypeId, transferStatusId, getAccountIdFromUserId(senderId), getAccountIdFromUserId(recepientId), amountToAdd);
            int results = jdbcTemplate.update(sql, amountToAdd, recepientId);
            int results1 = jdbcTemplate.update(sql1, amountToAdd, senderId);
            if (insertResults == 1 && results == 1 && results1 == 1) {
                didItWork = true;
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return didItWork;
    }

    public boolean acceptRequest(int transferId) {
        boolean didItWork = false;
        String updateStatus = "UPDATE transfer SET transfer_status_id = " + APPROVED +
                " WHERE transfer_id = ?";
        String sqlAdd = "UPDATE account SET balance = balance + (SELECT amount FROM transfer WHERE transfer_id = ?) " +
                "WHERE account_id = (SELECT account_to FROM transfer WHERE transfer_id = ?)";
        String sqlSubtract = "UPDATE account SET balance = balance - (SELECT amount FROM transfer WHERE transfer_id = ?) " +
                "WHERE account_id = (SELECT account_from FROM transfer WHERE transfer_id = ?)";
        //create a record in the transfers table

        try {
            int updateStatusResults = jdbcTemplate.update(updateStatus, transferId);
            int addResult = jdbcTemplate.update(sqlAdd, transferId, transferId);
            int subtractResult = jdbcTemplate.update(sqlSubtract, transferId, transferId);
            if (updateStatusResults == 1 && addResult == 1 && subtractResult == 1) {
                didItWork = true;
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return didItWork;
    }




    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));
        return account;
    }
}
