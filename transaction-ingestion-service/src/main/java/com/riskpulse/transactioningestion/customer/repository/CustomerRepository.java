package com.riskpulse.transactioningestion.customer.repository;

import com.riskpulse.transactioningestion.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByExternalCustomerId(String externalCustomerId);
}
