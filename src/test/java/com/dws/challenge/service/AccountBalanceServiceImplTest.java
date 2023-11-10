package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InvalidTransactionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.concurrent.*;

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

    /**
     * Test to transfer $100 from source account to dest account using 2 threads , each thread having 100 iterations each
     * @throws InterruptedException
     */
    @Test
    void transfer_multiple_transfers() throws InterruptedException, ExecutionException {
        accountsService.getAccountsRepository().clearAccounts();
        Account account = new Account("source");
        account.setBalance(new BigDecimal(90000));
        this.accountsService.createAccount(account);

        account = new Account("dest");
        account.setBalance(new BigDecimal(20000));
        this.accountsService.createAccount(account);
        CountDownLatch latch = new CountDownLatch(2);
        //Each runner job is doing 100 transaction
        Callable runner = () -> {
            for (int i = 0; i < 100 ; i++){
                accountBalanceService.transferBalanceBWAccounts("source","dest",new BigDecimal(100));
            }
            latch.countDown();
            return true;
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future task1 = executorService.submit(runner);
        Future task2 = executorService.submit(runner);
        latch.await();
        assertThat(accountsService.getAccount("source").getBalance()).isEqualByComparingTo("70000");
        assertThat(accountsService.getAccount("dest").getBalance()).isEqualByComparingTo("40000");
    }


    /**
     * This test has 2 threads
     * thread 1 transfers $100 from source to dest account [ 100 times ]
     * thread2 transfers $100 from dest to source account [ 50 times ]
     * @throws InterruptedException
     */
    @Test
    void multiple_transfers_reverse_transfer() throws InterruptedException, ExecutionException {
        accountsService.getAccountsRepository().clearAccounts();
        Account account = new Account("source");
        account.setBalance(new BigDecimal(90000));
        this.accountsService.createAccount(account);

        account = new Account("dest");
        account.setBalance(new BigDecimal(20000));
        this.accountsService.createAccount(account);

        //Each runner job is doing 100 transactions where it is transferring $100 from source account to dest account
        Callable runner = () -> {
            for (int i = 0; i < 100 ; i++){
                accountBalanceService.transferBalanceBWAccounts("source","dest",new BigDecimal(100));
            }
            return true;
        };
        //Each runner job is doing 50 transactions where it is transferring $100 from dest account to source account
        Callable reverseRunner = () -> {
            for (int i = 0; i < 50 ; i++){
                accountBalanceService.transferBalanceBWAccounts("dest","source",new BigDecimal(100));
            }
            return true;
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future task1 = executorService.submit(runner);
        Future task2 = executorService.submit(reverseRunner);

        task2.get();
        task1.get();

        assertThat(accountsService.getAccount("source").getBalance()).isEqualByComparingTo("85000");
        assertThat(accountsService.getAccount("dest").getBalance()).isEqualByComparingTo("25000");
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
            accountBalanceService.transferBalanceBWAccounts("source_exceptional","dest_exceptional",new BigDecimal(4000));
        }catch (Exception e){
            assertThat(e instanceof InvalidTransactionException);
            assertThat(e.getMessage()).contains("Not sufficient balance in account <source_exceptional>");
        }
    }

    @Test
    void transferBalanceBWAccounts_invalid_Destination_account() throws InvalidTransactionException {
        accountsService.getAccountsRepository().clearAccounts();
        Account account = new Account("source_exceptional");
        account.setBalance(new BigDecimal(3000));
        this.accountsService.createAccount(account);

        account = new Account("dest_exceptional");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        try {
            accountBalanceService.transferBalanceBWAccounts("source_exceptional","dest_exceptional",new BigDecimal(500));
        }catch (RuntimeException e){
            assertThat(e.getMessage()).isEqualTo("invalid account !");
        }
    }
}