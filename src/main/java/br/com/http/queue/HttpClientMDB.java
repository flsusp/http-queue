package br.com.http.queue;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/http") })
public class HttpClientMDB implements MessageListener {

	public void onMessage(Message message) {
		try {
			ObjectMessage msg = (ObjectMessage) message;
			HttpRequestMessage httpRequestMessage = (HttpRequestMessage) msg.getObject();
			httpRequestMessage.send();
			if (!httpRequestMessage.success()) {
				throw new RuntimeException("Message not processed - HTTP error code : " + httpRequestMessage.getResponseStatus());
			}
		} catch (JMSException e) {
			throw new EJBException(e);
		}
	}
}
