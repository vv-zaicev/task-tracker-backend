package com.zaicev.task_tracker_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.zaicev.task_tracker_backend.dto.MailVerificationMessage;

@Service
public class KafkaProducer {
	private final KafkaTemplate<String, MailVerificationMessage> kafkaTemplate;
	
	@Value("${VEREFICATION_TOPIC}")
	private String topic;

	public KafkaProducer(KafkaTemplate<String, MailVerificationMessage> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendMessage(MailVerificationMessage vereficationDTO) {
		kafkaTemplate.send(topic, vereficationDTO);
	}
}
