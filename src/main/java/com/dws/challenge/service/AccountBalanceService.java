package com.dws.challenge.service;

import java.math.BigDecimal;

public interface AccountBalanceService {
    boolean transferBalanceBWAccounts(String sourceAccount, String destinationAccount, BigDecimal transferAmount);
}
