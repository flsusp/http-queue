package br.com.http.queue;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class JMSSenderUtil {

	public static void sendJMSMessage(ConnectionFactory connectionFactory, Destination destination, Serializable message) {
		Connection conn = null;
		Session session = null;
		MessageProducer publisher = null;

		try {
			conn = connectionFactory.createConnection();
			session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			publisher = session.createProducer(destination);

			ObjectMessage msg = session.createObjectMessage(message);
			publisher.send(msg);
		} catch (JMSException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (publisher != null)
					publisher.close();
				if (session != null)
					session.close();
				if (conn != null)
					conn.close();
			} catch (JMSException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
