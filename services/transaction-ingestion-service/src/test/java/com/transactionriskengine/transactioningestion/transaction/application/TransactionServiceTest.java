package com.transactionriskengine.transactioningestion.transaction.application;

import com.transactionriskengine.transactioningestion.account.domain.Account;
import com.transactionriskengine.transactioningestion.account.domain.AccountStatus;
import com.transactionriskengine.transactioningestion.account.repository.AccountRepository;
import com.transactionriskengine.transactioningestion.common.exception.BusinessException;
import com.transactionriskengine.transactioningestion.customer.domain.Customer;
import com.transactionriskengine.transactioningestion.customer.domain.CustomerStatus;
import com.transactionriskengine.transactioningestion.customer.repository.CustomerRepository;
import com.transactionriskengine.transactioningestion.messaging.outbox.OutboxEvent;
import com.transactionriskengine.transactioningestion.messaging.outbox.OutboxEventRepository;
import com.transactionriskengine.transactioningestion.transaction.api.LatestTransactionResponse;
import com.transactionriskengine.transactioningestion.transaction.api.TransactionRequest;
import com.transactionriskengine.transactioningestion.transaction.api.TransactionResponse;
import com.transactionriskengine.transactioningestion.transaction.domain.PaymentChannel;
import com.transactionriskengine.transactioningestion.transaction.domain.Transaction;
import com.transactionriskengine.transactioningestion.transaction.domain.TransactionStatus;
import com.transactionriskengine.transactioningestion.transaction.domain.TransactionType;
import com.transactionriskengine.transactioningestion.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final UUID CUST_1001_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CUST_1002_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CUST_1003_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ACC_9001_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
    private static final UUID ACC_9003_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1");
    private static final UUID ACC_9005_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc1");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                customerRepository,
                accountRepository,
                outboxEventRepository,
                new TransactionValidator(),
                "transaction.created.v1",
                new ObjectMapper()
        );
    }

    @Test
    void createTransactionSavesMobileTransferAndReturnsReceivedResponse() {
        TransactionRequest request = new TransactionRequest(
                "REQ-20260603-000001",
                "CUST-1001",
                "ACC-9001",
                new BigDecimal("2750.00"),
                "USD",
                TransactionType.TRANSFER,
                PaymentChannel.MOBILE_APP,
                null,
                null,
                "USA",
                "192.168.1.20",
                "iphone-test-device-001",
                Instant.parse("2026-06-03T14:29:50Z")
        );

        assertTransactionCreated(request, CUST_1001_ID, ACC_9001_ID);
    }

    @Test
    void createTransactionSavesCardPurchaseAndReturnsReceivedResponse() {
        TransactionRequest request = new TransactionRequest(
                "REQ-20260603-000002",
                "CUST-1002",
                "ACC-9003",
                new BigDecimal("189.99"),
                "USD",
                TransactionType.PURCHASE,
                PaymentChannel.CARD,
                "Amazon",
                "ECOMMERCE",
                "USA",
                "172.16.10.45",
                "web-browser-device-002",
                Instant.parse("2026-06-03T15:10:00Z")
        );

        assertTransactionCreated(request, CUST_1002_ID, ACC_9003_ID);
    }

    @Test
    void createTransactionSavesAtmWithdrawalAndReturnsReceivedResponse() {
        TransactionRequest request = new TransactionRequest(
                "REQ-20260603-000003",
                "CUST-1003",
                "ACC-9005",
                new BigDecimal("500.00"),
                "USD",
                TransactionType.WITHDRAWAL,
                PaymentChannel.ATM,
                "Bank ATM Charlotte",
                "ATM",
                "USA",
                null,
                "atm-terminal-088",
                Instant.parse("2026-06-03T15:20:00Z")
        );

        assertTransactionCreated(request, CUST_1003_ID, ACC_9005_ID);
    }

    @Test
    void createTransactionSavesHighValueWireTransferAndReturnsReceivedResponse() {
        TransactionRequest request = new TransactionRequest(
                "REQ-20260603-000004",
                "CUST-1001",
                "ACC-9001",
                new BigDecimal("15000.00"),
                "USD",
                TransactionType.TRANSFER,
                PaymentChannel.WIRE,
                null,
                "WIRE_TRANSFER",
                "USA",
                "10.10.20.30",
                "desktop-device-004",
                Instant.parse("2026-06-03T15:30:00Z")
        );

        assertTransactionCreated(request, CUST_1001_ID, ACC_9001_ID);
    }

    @Test
    void createTransactionRejectsFrozenAccount() {
        TransactionRequest request = new TransactionRequest(
                "REQ-20260603-000005",
                "CUST-1004",
                "ACC-9007",
                new BigDecimal("250.00"),
                "USD",
                TransactionType.PURCHASE,
                PaymentChannel.CARD,
                "Target",
                "RETAIL",
                "USA",
                "172.16.10.46",
                "web-browser-device-005",
                Instant.parse("2026-06-03T15:40:00Z")
        );

        Customer customer = mock(Customer.class);
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);

        Account account = mock(Account.class);
        when(account.getAccountStatus()).thenReturn(AccountStatus.FROZEN);

        when(transactionRepository.findByIdempotencyKey(request.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(customerRepository.findByExternalCustomerId(request.externalCustomerId()))
                .thenReturn(Optional.of(customer));
        when(accountRepository.findByExternalAccountId(request.externalAccountId()))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Account is not active");

        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
    }

    @Test
    void createTransactionRejectsBlockedCustomer() {
        TransactionRequest request = new TransactionRequest(
                "REQ-20260603-000006",
                "CUST-1005",
                "ACC-9008",
                new BigDecimal("100.00"),
                "USD",
                TransactionType.TRANSFER,
                PaymentChannel.MOBILE_APP,
                null,
                null,
                "USA",
                "192.168.1.21",
                "iphone-test-device-006",
                Instant.parse("2026-06-03T15:45:00Z")
        );

        Customer customer = mock(Customer.class);
        when(customer.getStatus()).thenReturn(CustomerStatus.BLOCKED);

        Account account = mock(Account.class);

        when(transactionRepository.findByIdempotencyKey(request.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(customerRepository.findByExternalCustomerId(request.externalCustomerId()))
                .thenReturn(Optional.of(customer));
        when(accountRepository.findByExternalAccountId(request.externalAccountId()))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Customer is not active");

        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
    }

    @Test
    void getLatestTransactionReturnsLatestRepositoryRecord() {
        Transaction transaction = mock(Transaction.class);
        UUID transactionId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-06-15T12:00:00Z");

        when(transaction.getId()).thenReturn(transactionId);
        when(transaction.getTransactionReference()).thenReturn("TXN-" + transactionId);
        when(transaction.getTransactionStatus()).thenReturn(TransactionStatus.RECEIVED);
        when(transaction.getCreatedAt()).thenReturn(createdAt);
        when(transactionRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(transaction));

        Optional<LatestTransactionResponse> response =
                transactionService.getLatestTransaction();

        assertThat(response).contains(new LatestTransactionResponse(
                transactionId,
                "TXN-" + transactionId,
                "RECEIVED",
                createdAt
        ));
    }

    private void assertTransactionCreated(TransactionRequest request, UUID customerId, UUID accountId) {
        Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(customerId);
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);

        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getCustomerId()).thenReturn(customerId);
        when(account.getAccountStatus()).thenReturn(AccountStatus.ACTIVE);
        when(account.getCurrency()).thenReturn("USD");

        when(transactionRepository.findByIdempotencyKey(request.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(customerRepository.findByExternalCustomerId(request.externalCustomerId()))
                .thenReturn(Optional.of(customer));
        when(accountRepository.findByExternalAccountId(request.externalAccountId()))
                .thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxEventRepository.save(any(OutboxEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(request);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        verify(outboxEventRepository).save(any(OutboxEvent.class));

        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getIdempotencyKey()).isEqualTo(request.idempotencyKey());
        assertThat(savedTransaction.getCustomerId()).isEqualTo(customerId);
        assertThat(savedTransaction.getAccountId()).isEqualTo(accountId);
        assertThat(savedTransaction.getTransactionType()).isEqualTo(request.transactionType());
        assertThat(savedTransaction.getPaymentChannel()).isEqualTo(request.paymentChannel());
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo(request.amount());
        assertThat(savedTransaction.getCurrency()).isEqualTo(request.currency());
        assertThat(savedTransaction.getMerchantName()).isEqualTo(request.merchantName());
        assertThat(savedTransaction.getMerchantCategory()).isEqualTo(request.merchantCategory());
        assertThat(savedTransaction.getMerchantCountry()).isEqualTo(request.merchantCountry());
        assertThat(savedTransaction.getSourceIp()).isEqualTo(request.sourceIp());
        assertThat(savedTransaction.getDeviceId()).isEqualTo(request.deviceId());
        assertThat(savedTransaction.getTransactionTime()).isEqualTo(request.transactionTime());
        assertThat(savedTransaction.getTransactionStatus()).isEqualTo(TransactionStatus.RECEIVED);

        assertThat(response.transactionId()).isEqualTo(savedTransaction.getId());
        assertThat(response.transactionReference()).isEqualTo(savedTransaction.getTransactionReference());
        assertThat(response.status()).isEqualTo("RECEIVED");
        assertThat(response.message()).isEqualTo("Transaction received");
    }
}
