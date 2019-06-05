package com.github.mikeldpl.hw.money.transfer.service;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

import com.github.mikeldpl.hw.money.transfer.exception.NotFoundApiException;
import com.github.mikeldpl.hw.money.transfer.exception.ValidationApiException;
import com.github.mikeldpl.hw.money.transfer.model.Account;
import com.github.mikeldpl.hw.money.transfer.repository.AccountRepository;

@Singleton
public class AccountService {

    private final TransactionExecutorService transactionExecutorService;
    private final AccountRepository accountRepository;

    @Inject
    public AccountService(TransactionExecutorService transactionExecutorService,
                          AccountRepository accountRepository) {
        this.transactionExecutorService = transactionExecutorService;
        this.accountRepository = accountRepository;
    }


    @Nonnull
    public Account createAccount(Account account) {
        validateEntity(account);

        return transactionExecutorService.executeTransaction(false, () -> {
            Long id = accountRepository.add(account);
            return accountRepository.getById(id);
        });
    }

    private void validateEntity(Account account) {
        String name = account.getName();
        if (name == null || name.isEmpty()) {
            throw new ValidationApiException("Account name is required.");
        }
        BigDecimal money = account.getMoney();
        if (money == null) {
            throw new ValidationApiException("Account money is required.");
        }
        if (money.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationApiException("Account money can not be less the zero.");
        }
    }

    @Nonnull
    public List<Account> getAll() {
        return transactionExecutorService.executeTransaction(true, accountRepository::getAll);
    }

    @Nonnull
    public Account getAccount(Long id) {
        Account account = transactionExecutorService.executeTransaction(true, () -> accountRepository.getById(id));
        checkExistence(id, account);
        return account;
    }

    private void checkExistence(Long id, Account account) {
        if (account == null) {
            throw new NotFoundApiException("Account is not found. Id: " + id);
        }
    }

    public void checkIfAccountExists(Long accountId) {
        //todo: use count
        getAccount(accountId);
    }

    public void addAccountMoney(Long accountId, BigDecimal amount) {
        transactionExecutorService.executeTransaction(false, () -> {
            Account account = accountRepository.getWithXLock(accountId);
            checkExistence(accountId, account);
            account.setMoney(account.getMoney().add(amount));
            validateEntity(account);
            accountRepository.updateMoney(account);
            return Void.class;
        });
    }
}
