package br.com.http.queue;

import java.util.ArrayList;
import java.util.List;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.journal.impl.AIOSequentialFileFactory;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.server.embedded.EmbeddedHornetQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HornetQServiceManager {

	private static final Logger logger = LoggerFactory.getLogger(HornetQServiceManager.class);

	private EmbeddedHornetQ hornetq;
	private ClientSession session;
	private ClientProducer producer;
	private ReceiverGroup receivers;
	private ClientSessionFactory factory;

	public void start() {
		logger.info("Starting HornetQ...");

		hornetq = new EmbeddedHornetQ();

		Configuration configuration = new ConfigurationImpl();
		configuration.getAcceptorConfigurations().add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
		configureJournalType(configuration);
		hornetq.setConfiguration(configuration);

		try {
			hornetq.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			ServerLocator serverLocator = HornetQClient.createServerLocator(false, new TransportConfiguration(
					InVMConnectorFactory.class.getName()));
			factory = serverLocator.createSessionFactory();
			session = factory.createSession();
			session.createQueue("http-client", "http-client", true);
			producer = session.createProducer("http-client");

			session.start();

			createReceivers();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createReceivers() {
		receivers = new ReceiverGroup(session, 100);
		receivers.start();
	}

	private void configureJournalType(Configuration configuration) {
		boolean supportsAIO = AIOSequentialFileFactory.isSupported();

		if (supportsAIO) {
			configuration.setJournalType(JournalType.ASYNCIO);
		} else {
			configuration.setJournalType(JournalType.NIO);
		}
		configuration.setSecurityEnabled(false);
	}

	public void shutdown() {
		try {
			producer.close();
			receivers.shutdown();
			session.stop();
			hornetq.stop();
		} catch (Exception e) {
			logger.warn("Exception thrown while shuttingdown HornetQ.", e);
		}
	}

	public void send(String url) {
		logger.info("Registering message : " + url);

		try {
			ClientMessage message = session.createMessage(true);
			message.getBodyBuffer().writeString(url);

			producer.send("http-client", message);
		} catch (HornetQException e) {
			throw new RuntimeException(e);
		}
	}
}

class ReceiverGroup {

	private List<Receiver> receivers;

	public ReceiverGroup(ClientSession session, int n) {
		super();
		this.receivers = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			this.receivers.add(new Receiver(session));
		}
	}

	public void start() {
		for (Receiver receiver : receivers) {
			receiver.start();
		}
	}

	public void shutdown() {
		for (Receiver receiver : receivers) {
			receiver.shutdown();
		}
	}
}

class Receiver implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

	private final Thread thread;
	private boolean shutdown = false;
	private ClientConsumer consumer;

	public Receiver(ClientSession session) {
		this.thread = new Thread(this, "queue-receiver");
		try {
			this.consumer = session.createConsumer("http-client");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void start() {
		thread.start();
	}

	public void shutdown() {
		shutdown = true;
	}

	@Override
	public void run() {
		while (!shutdown) {
			try {
				ClientMessage message = consumer.receive();

				if (message != null) {
					String content = message.getBodyBuffer().readString();
					logger.info("Message received and processing : " + content);

					message.acknowledge();
				}
			} catch (Exception e) {
				logger.error("Exception thrown while processing message.", e);
			}
		}
	}
}
