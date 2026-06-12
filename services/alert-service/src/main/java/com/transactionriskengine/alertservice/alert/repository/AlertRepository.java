package com.transactionriskengine.alertservice.alert.repository;

import com.transactionriskengine.alertservice.alert.domain.Alert;
import com.transactionriskengine.alertservice.alert.domain.AlertPriority;
import com.transactionriskengine.alertservice.alert.domain.AlertStatus;
import com.transactionriskengine.alertservice.alert.domain.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    boolean existsByTransactionId(UUID transactionId);

    Optional<Alert> findByAlertReference(String alertReference);

    Optional<Alert> findByTransactionId(UUID transactionId);

    List<Alert> findByAlertStatusOrderByCreatedAtDesc(AlertStatus alertStatus);

    List<Alert> findByAlertPriorityOrderByCreatedAtDesc(AlertPriority alertPriority);

    List<Alert> findByRiskLevelOrderByCreatedAtDesc(RiskLevel riskLevel);

    List<Alert> findTop50ByOrderByCreatedAtDesc();
}
