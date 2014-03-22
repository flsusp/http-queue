package br.com.http.queue;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class HttpQueueApplication extends Application {

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<>();
		singletons.add(new QueueService());
		return singletons;
	}
}
