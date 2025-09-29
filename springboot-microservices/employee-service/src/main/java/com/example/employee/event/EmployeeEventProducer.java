package com.example.employee.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Employee Event Producer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "employee-events";

    public void sendEmployeeCreatedEvent(Long employeeId, String email) {
        String message = String.format("{\"event\":\"EMPLOYEE_CREATED\",\"id\":%d,\"email\":\"%s\"}",
                employeeId, email);
        kafkaTemplate.send(TOPIC, message);
        log.info("Sent employee created event: {}", message);
    }

    public void sendEmployeeUpdatedEvent(Long employeeId) {
        String message = String.format("{\"event\":\"EMPLOYEE_UPDATED\",\"id\":%d}", employeeId);
        kafkaTemplate.send(TOPIC, message);
        log.info("Sent employee updated event: {}", message);
    }

    public void sendEmployeeDeletedEvent(Long employeeId) {
        String message = String.format("{\"event\":\"EMPLOYEE_DELETED\",\"id\":%d}", employeeId);
        kafkaTemplate.send(TOPIC, message);
        log.info("Sent employee deleted event: {}", message);
    }
}