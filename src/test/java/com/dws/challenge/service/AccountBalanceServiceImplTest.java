package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InvalidTransactionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountBalanceServiceImplTest {


    @Autowired
    private AccountBalanceServiceImpl accountBalanceService;

    @Autowired
    private AccountsService accountsService;

    @Test
    void transferBalanceBWAccounts() {
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
    void transferBalanceBWAccounts_source_not_have_fund() throws InvalidTransactionException {
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