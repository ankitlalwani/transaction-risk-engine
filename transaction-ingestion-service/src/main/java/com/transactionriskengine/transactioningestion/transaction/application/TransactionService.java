package com.transactionriskengine.transactioningestion.transaction.application;

import com.transactionriskengine.transactioningestion.account.domain.Account;
import com.transactionriskengine.transactioningestion.account.repository.AccountRepository;
import com.transactionriskengine.transactioningestion.common.exception.ResourceNotFoundException;
import com.transactionriskengine.transactioningestion.customer.domain.Customer;
import com.transactionriskengine.transactioningestion.customer.repository.CustomerRepository;
import com.transactionriskengine.transactioningestion.messaging.event.TransactionEventPayload;
import com.transactionriskengine.transactioningestion.messaging.outbox.OutboxEvent;
import com.transactionriskengine.transactioningestion.messaging.outbox.OutboxEventRepository;
import com.transactionriskengine.transactioningestion.transaction.api.TransactionRequest;
import com.transactionriskengine.transactioningestion.transaction.api.TransactionResponse;
import com.transactionriskengine.transactioningestion.transaction.domain.Transaction;
import com.transactionriskengine.transactioningestion.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
public class TransactionService {

    private static final String TRANSACTION_RECEIVED_EVENT = "TransactionReceived";

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final TransactionValidator transactionValidator;
    private final String transactionCreatedTopic;
    private final ObjectMapper objectMapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            OutboxEventRepository outboxEventRepository,
            TransactionValidator transactionValidator,
            @Value("${transaction-risk-engine.kafka.topics.transaction-created}") String transactionCreatedTopic,
            ObjectMapper objectMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.transactionValidator = transactionValidator;
        this.transactionCreatedTopic = transactionCreatedTopic;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        return transactionRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(this::toIdempotentResponse)
                .orElseGet(() -> createNewTransaction(request));
    }

    private TransactionResponse createNewTransaction(TransactionRequest request) {
        Customer customer = customerRepository.findByExternalCustomerId(request.externalCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Account account = accountRepository.findByExternalAccountId(request.externalAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        transactionValidator.validateCustomerIsActive(customer);
        transactionValidator.validateAccountIsActive(account);
        transactionValidator.validateAccountBelongsToCustomer(account, customer);
        transactionValidator.validateAmount(request.amount());
        transactionValidator.validateCurrency(account, request.currency());

        Transaction transaction = new Transaction(
                request.idempotencyKey(),
                customer.getId(),
                account.getId(),
                request.amount(),
                request.currency().toUpperCase(),
                request.transactionType(),
                request.paymentChannel(),
                request.merchantName(),
                request.merchantCategory(),
                request.merchantCountry(),
                request.sourceIp(),
                request.deviceId(),
                request.transactionTime()
        );

        Transaction savedTransaction = transactionRepository.save(transaction);
        UUID eventId = UUID.randomUUID();

        outboxEventRepository.save(new OutboxEvent(
                eventId,
                savedTransaction.getId(),
                TRANSACTION_RECEIVED_EVENT,
                transactionCreatedTopic,
                transactionPayload(eventId, savedTransaction)
        ));

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getTransactionReference(),
                savedTransaction.getTransactionStatus().name(),
                "Transaction received"
        );
    }

    private TransactionResponse toIdempotentResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getTransactionStatus().name(),
                "Transaction already received"
        );
    }

    private String transactionPayload(UUID eventId, Transaction transaction) {
        TransactionEventPayload payload = new TransactionEventPayload(
                eventId,
                transaction.getId(),
                transaction.getTransactionReference(),
                TRANSACTION_RECEIVED_EVENT,
                transaction.getTransactionStatus().name()
        );

        return objectMapper.writeValueAsString(payload);
    }
}
