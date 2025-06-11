package com.zaicev.task_tracker_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.zaicev.task_tracker_backend.dto.VereficationDTO;

@Service
public class KafkaProducer {
	private final KafkaTemplate<String, VereficationDTO> kafkaTemplate;
	
	@Value("${VEREFICATION_TOPIC}")
	private String topic;

	public KafkaProducer(KafkaTemplate<String, VereficationDTO> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendMessage(VereficationDTO vereficationDTO) {
		kafkaTemplate.send(topic, vereficationDTO);
	}
}
