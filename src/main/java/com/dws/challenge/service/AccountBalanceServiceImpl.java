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
     * First take lock on source account id string and then verify the transfer condition(s)
     * then take lock on destination account id and do the transfer
     *
     * @param srcAccountId
     * @param destAccountId
     * @param transferAmount
     * @return returns true if transfer successful - else throw the exception
     */
    @Override
    public boolean transferBalanceBWAccounts(String srcAccountId, String destAccountId, BigDecimal transferAmount) {
        accountLocks.putIfAbsent(srcAccountId, new ReentrantLock()); // may consider using cache to have locks
        accountLocks.putIfAbsent(destAccountId, new ReentrantLock());
        ReentrantLock srcLock = accountLocks.get(srcAccountId);
        ReentrantLock destLock = accountLocks.get(srcAccountId);

        try {
            srcLock.lock();
            Account srcAccount = accountsRepository.getAccount(srcAccountId);
            validateAccount(srcAccount, transferAmount,"SOURCE"); // We can have multiple such pre validations
            try {
                destLock.lock();
                Account destAccount = accountsRepository.getAccount(destAccountId);
                validateAccount(destAccount,transferAmount,"DEST");
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

        return false;
    }

    private void validateAccount(Account account, BigDecimal transferAmount, String accountType) {
        if (account ==null ){ // or any other account invalidity related thing
            throw new RuntimeException(String.format("invalid account {} for account type {}",account,accountType));
        }
        if ("SOURCE".equals(accountType)) {
            if (account.getBalance().subtract(transferAmount).compareTo(BigDecimal.ZERO) == -1){
                throw new InvalidTransactionException("Not sufficient balance in account <"+account.getAccountId()+">");
            }
        }else if ("DEST".equals(accountType)){
            //TODO have implementation
        }
    }

    private void checkIfBalAvailable(Account srcAccount, BigDecimal transferAmount) {

    }
}
