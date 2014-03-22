package br.com.http.queue;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/http") })
public class HttpClientMDB implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(HttpClientMDB.class);

	public void onMessage(Message message) {
		try {
			ObjectMessage msg = (ObjectMessage) message;
			HttpRequestMessage httpRequestMessage = (HttpRequestMessage) msg.getObject();
			logger.info(httpRequestMessage.getUrl());
		} catch (JMSException e) {
			throw new EJBException(e);
		}
	}
}
