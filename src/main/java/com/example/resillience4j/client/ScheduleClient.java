package com.example.resillience4j.client;

import com.example.resillience4j.domain.Appointment;

public interface ScheduleClient {
    Appointment getAppointments(long id);
}
