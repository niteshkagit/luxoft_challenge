package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InvalidTransactionException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This is an implementation for Account's balance related operations
 *
 */
@Service
public class AccountBalanceServiceImpl implements AccountBalanceService{

    @Getter
    private final AccountsRepository accountsRepository;

    private final Map<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();

    @Autowired
    private NotificationService notificationService;


    @Autowired
    public AccountBalanceServiceImpl(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }


    /**
     * This method transfers balance between the accounts.
     * It also checks for some base condition which needs to meets and facilitate transfer
     *
     * First take lock on source account and then verify the transfer condition(s)
     * then take lock on destination account and do the transfer
     *
     * @param srcAccountId
     * @param destAccountId
     * @param transferAmount
     * @return returns true if transfer successful - else throw the exception
     */
    @Override
    public boolean transferBalanceBWAccounts(String srcAccountId, String destAccountId, BigDecimal transferAmount) {

        ReentrantLock srcLock = accountLocks.computeIfAbsent(srcAccountId, k -> new ReentrantLock()); // may consider using cache to have locks
        ReentrantLock destLock = accountLocks.computeIfAbsent(destAccountId, k -> new ReentrantLock());

        try {
            srcLock.lock();
            Account srcAccount = accountsRepository.getAccount(srcAccountId);
            checkIfBalAvailable(srcAccount, transferAmount); // We can have multiple such pre validations
            try {
                destLock.lock();
                Account destAccount = accountsRepository.getAccount(destAccountId);
                destAccount.setBalance(destAccount.getBalance().add(transferAmount));
                srcAccount.setBalance(srcAccount.getBalance().subtract(transferAmount));
                notificationService.notifyAboutTransfer(srcAccount,"Dear account holder ! $"+ transferAmount +" is debited from account :"+srcAccountId);
                notificationService.notifyAboutTransfer(destAccount,"Dear account holder ! $"+ transferAmount +" is credited from account :"+destAccountId+"\n");

            } catch (Exception e) {
                throw e;
            } finally {
                destLock.unlock();
            }
        } finally {
            srcLock.unlock();
        }



        /*
        try {
          // Basic synchronization
           synchronized (srcAccountId){
                synchronized (destAccountId){
                    checkIfBalAvailable(srcAccount,transferAmount); // We can have multiple such pre validations -
                    // such as 1] account status, 2] destination account status 3] transaction amount size etc etc but here it can be considered as out of scope

                    destAccount.setBalance(destAccount.getBalance().add(transferAmount));
                    srcAccount.setBalance(srcAccount.getBalance().subtract(transferAmount));
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
          //TODO
        }

         */



        return false;
    }

    private void checkIfBalAvailable(Account srcAccount, BigDecimal transferAmount) {
        if (srcAccount.getBalance().subtract(transferAmount).compareTo(BigDecimal.ZERO) == -1){
            throw new InvalidTransactionException("Not sufficient balance in account <"+srcAccount.getAccountId()+">");
        }
    }
}
