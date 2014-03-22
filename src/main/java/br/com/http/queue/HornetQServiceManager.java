package br.com.http.queue;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.journal.impl.AIOSequentialFileFactory;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.server.embedded.EmbeddedHornetQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HornetQServiceManager {

	private static final Logger logger = LoggerFactory.getLogger(HornetQServiceManager.class);

	private EmbeddedHornetQ hornetq;

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
			hornetq.stop();
		} catch (Exception e) {
			logger.warn("Exception thrown while shuttingdown HornetQ.", e);
		}
	}
}
