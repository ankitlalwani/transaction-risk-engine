package com.riskpulse.transactioningestion.transaction.application;

import com.riskpulse.transactioningestion.account.domain.Account;
import com.riskpulse.transactioningestion.account.domain.AccountStatus;
import com.riskpulse.transactioningestion.common.exception.BusinessException;
import com.riskpulse.transactioningestion.customer.domain.Customer;
import com.riskpulse.transactioningestion.customer.domain.CustomerStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionValidator {

    public void validateCustomerIsActive(Customer customer) {
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException("Customer is not active");
        }
    }

    public void validateAccountIsActive(Account account) {
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active");
        }
    }

    public void validateAccountBelongsToCustomer(Account account, Customer customer) {
        if (!account.getCustomerId().equals(customer.getId())) {
            throw new BusinessException("Account does not belong to customer");
        }
    }

    public void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transaction amount must be positive");
        }
    }

    public void validateCurrency(Account account, String currency) {
        if (!account.getCurrency().equalsIgnoreCase(currency)) {
            throw new BusinessException("Transaction currency must match account currency");
        }
    }
}
