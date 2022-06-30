package com.example.resillience4j.service;

import com.example.resillience4j.client.AccountClient;
import com.example.resillience4j.domain.InsuranceCard;
import com.example.resillience4j.dto.InsuranceCardDTO;
import com.example.resillience4j.exceptions.InternalServerErrorException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccountService {
    private final AccountClient accountClient;

    @CircuitBreaker(name = "account", fallbackMethod = "insuranceCardFallback")
    public InsuranceCard getPatientFamilyInsuranceCard(Long patientId, Long familyId) {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new InternalServerErrorException("request failed");
    }

    public InsuranceCardDTO insuranceCardFallback() {
        return InsuranceCardDTO.ERROR;
    }
}
