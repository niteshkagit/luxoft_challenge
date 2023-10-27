package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountBalanceServiceImpl;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountOperationControllerTest {


    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    void transferBWAccounts() throws Exception {

        accountsService.createAccount(new Account("source_acc_1",new BigDecimal(4000)));
        accountsService.createAccount(new Account("source_acc_2",new BigDecimal(5000)));
        accountsService.createAccount(new Account("dest_acc",new BigDecimal(2000)));


        this.mockMvc.perform(post("/v1/accounts/operation")
                        .param("source","source_acc_1")
                        .param("destination","dest_acc")
                        .param("amount","100"))
                .andExpect(status().isOk());


    }
}