package com.dws.challenge.web;


import com.dws.challenge.exception.InvalidTransactionException;
import com.dws.challenge.service.AccountBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * This controller class is created to rest apis for Account operations
 * currently exposing transferBWAccounts to do balance transfer
 */
@RestController
@RequestMapping("/v1/accounts/operation")
@Slf4j
public class AccountOperationController {
    private final AccountBalanceService accountBalanceService;

    @Autowired
    public AccountOperationController(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    @RequestMapping
    public ResponseEntity<Object> transferBWAccounts(@RequestParam("source") String source,
                                                     @RequestParam("destination") String destination ,
                                                     @RequestParam("amount")  BigDecimal amount) {
        log.info("Transferring from account {} to account {} and transfer amount is {}", source,destination,amount);

        try {
            this.accountBalanceService.transferBalanceBWAccounts(source,destination,amount);
            log.info("Transfer done!");

        } catch (InvalidTransactionException ite) {
            return new ResponseEntity<>(ite.getMessage(), HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity<>(HttpStatus.OK);
    }
}
