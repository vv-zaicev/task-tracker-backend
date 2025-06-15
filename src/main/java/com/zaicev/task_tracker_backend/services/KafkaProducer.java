package com.zaicev.task_tracker_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.zaicev.task_tracker_backend.dto.EmailVerificationMessage;

@Service
public class KafkaProducer {
	private final KafkaTemplate<String, EmailVerificationMessage> kafkaTemplate;
	
	@Value("${VEREFICATION_TOPIC}")
	private String topic;

	public KafkaProducer(KafkaTemplate<String, EmailVerificationMessage> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendMessage(EmailVerificationMessage vereficationDTO) {
		kafkaTemplate.send(topic, vereficationDTO);
	}
}
