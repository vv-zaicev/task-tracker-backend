package com.zaicev.task_tracker_backend.services;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.zaicev.task_tracker_backend.dto.EmailVerificationMessage;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerTest {
	@Mock
	private KafkaTemplate<String, EmailVerificationMessage> kafkaTemplate;
	@InjectMocks
	private KafkaProducer kafkaProducer;

	@BeforeEach
	void setUp() {
		kafkaProducer.setTopic("topic");
	}

	@Test
	void sendMessage_shouldSendMessageToKafka() {
		EmailVerificationMessage message = new EmailVerificationMessage("user@example.com", "username", "123456", 1000);

		kafkaProducer.sendMessage(message);

		verify(kafkaTemplate, times(1)).send("topic", message);
	}
}
