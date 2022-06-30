package com.example.resillience4j.dto;

import com.example.resillience4j.domain.InsuranceCard;

public class InsuranceCardDTO {
    public static final InsuranceCardDTO ERROR = new InsuranceCardDTO();

    public static InsuranceCardDTO toDTO(InsuranceCard patientInsuranceCard) {
        return new InsuranceCardDTO();
    }
}
