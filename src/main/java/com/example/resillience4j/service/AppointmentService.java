package com.example.resillience4j.service;

import com.example.resillience4j.client.ScheduleClient;
import com.example.resillience4j.domain.Appointment;
import com.example.resillience4j.domain.StaffAuthInfo;
import com.example.resillience4j.dto.AppointmentDetailDTO;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.List;

import static com.example.resillience4j.util.ReactorUtils.list;
import static com.example.resillience4j.util.ReactorUtils.object;

@RequiredArgsConstructor
public class AppointmentService {
    private final ScheduleClient scheduleClient;

    public AppointmentDetailDTO getAppointmentDetail(StaffAuthInfo staffAuthInfo, long id) {
        Appointment appointment = scheduleClient.getAppointments(id);
        return Mono.zipDelayError(
                           Mono.just(appointmentDTO),
                           object("get treatment with info", () -> treatmentService.getTreatmentWithInfo(treatmentId, patientId), TreatmentDTO.ERROR, reactorExecutor),
                           list("get paymentHistories", () -> paymentHistoryService.getHistoryList(null, null, List.of(treatmentId), new Pageable(), SortType.LATEST), reactorExecutor)
                                   .flatMapMany(Flux::fromIterable)
                                   .singleOrEmpty()
                                   .defaultIfEmpty(PaymentHistoryDTO.ERROR),
                           object("get patient insuranceCard", () -> patientService.getFamilyInsuranceCard(patientId, familyId, obsAuthValue), InsuranceCardDTO.ERROR, reactorExecutor),
                           object("get video call status", () -> videoCallService.getVideoCall(treatmentId, staffAuthInfo), VideoCallDTO.ERROR, reactorExecutor),
                           object("get test patient info", () -> patientService.isTestPatient(patientId, staffAuthInfo.getClinicId()), false, reactorExecutor),
                           Mono.just(videoUrlOpenLimitMinute),
                           object("get patient medical document", () -> patientService.getFamilyMedicalDocument(patientId, familyId, obsAuthValue), MedicalDocumentDTO.ERROR, reactorExecutor))
                   .map(TupleUtils.function(AppointmentDetailDTO::assemble))
                   .block();
    }
}
