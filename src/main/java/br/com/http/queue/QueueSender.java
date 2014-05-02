package br.com.http.queue;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class QueueSender {

	private static final Logger logger = LoggerFactory.getLogger(QueueSender.class);

	@Resource
	private SessionContext ctx;

	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName = "java:/queue/http")
	private Destination destination;

	public void send(HttpRequestMessage message) {
		logger.info("Message received : {}", message.getUrl());
		JMSSenderUtil.sendJMSMessage(connectionFactory, destination, message);
	}
}
