package com.transactionriskengine.alertservice.alert.application;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class AlertReferenceGenerator {

    public String generate() {

        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return "ALT-" + date + "-" + suffix;
    }
}
