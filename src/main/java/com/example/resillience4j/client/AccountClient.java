package com.example.resillience4j.client;

import com.example.resillience4j.domain.InsuranceCard;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "account")
public interface AccountClient {
    InsuranceCard getPatientFamilyInsuranceCard(long patientId, long familyId);
}