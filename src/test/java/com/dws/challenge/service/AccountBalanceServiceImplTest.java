package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InvalidTransactionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountBalanceServiceImplTest {


    @Autowired
    private AccountBalanceServiceImpl accountBalanceService;

    @Autowired
    private AccountsService accountsService;

    @Test
    void transferBalanceBWAccounts() {
        accountsService.getAccountsRepository().clearAccounts();
        Account account = new Account("source");
        account.setBalance(new BigDecimal(9000));
        this.accountsService.createAccount(account);

        account = new Account("dest");
        account.setBalance(new BigDecimal(3000));
        this.accountsService.createAccount(account);


        accountBalanceService.transferBalanceBWAccounts("source","dest",new BigDecimal(2000));

        assertThat(accountsService.getAccount("source").getBalance()).isEqualByComparingTo("7000");
        assertThat(accountsService.getAccount("dest").getBalance()).isEqualByComparingTo("5000");

        accountBalanceService.transferBalanceBWAccounts("source","dest",new BigDecimal(500));

        assertThat(accountsService.getAccount("source").getBalance()).isEqualByComparingTo("6500");
        assertThat(accountsService.getAccount("dest").getBalance()).isEqualByComparingTo("5500");
    }

    @Test
    void transfer_multiple_transfers() throws InterruptedException {
        accountsService.getAccountsRepository().clearAccounts();
        Account account = new Account("source");
        account.setBalance(new BigDecimal(90000));
        this.accountsService.createAccount(account);

        account = new Account("dest");
        account.setBalance(new BigDecimal(20000));
        this.accountsService.createAccount(account);

        //Each runner job is doing 100 transaction
        Runnable runner = () -> {
            for (int i = 0; i < 100 ; i++){
                System.out.println(Thread.currentThread().getName() + ": transferring for : count "+i);
                accountBalanceService.transferBalanceBWAccounts("source","dest",new BigDecimal(100));
            }
        };

        ExecutorService executorService = Executors.newCachedThreadPool();
        for(int i=0;i<2;i++){ // 2 threads forked
            executorService.execute(runner);
        }
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(1, TimeUnit.MINUTES);

        assertThat(accountsService.getAccount("source").getBalance()).isEqualByComparingTo("70000");
        assertThat(accountsService.getAccount("dest").getBalance()).isEqualByComparingTo("40000");
    }

    @Test
    void transferBalanceBWAccounts_source_not_have_fund() throws InvalidTransactionException {
        accountsService.getAccountsRepository().clearAccounts();
        Account account = new Account("source_exceptional");
        account.setBalance(new BigDecimal(3000));
        this.accountsService.createAccount(account);

        account = new Account("dest_exceptional");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        try {
            accountBalanceService.transferBalanceBWAccounts("source","dest",new BigDecimal(4000));
        }catch (Exception e){
            assertThat(e instanceof InvalidTransactionException);
            assertThat("Not sufficient balance in account <source>").isEqualTo(e.getMessage());
        }
    }
}