package br.com.http.queue;

public class ServiceManager {

	private HornetQServiceManager hornetqManager;

	public ServiceManager start() {
		hornetqManager = new HornetQServiceManager();
		hornetqManager.start();
		return this;
	}

	public void shutdown() {
		hornetqManager.shutdown();
	}
}
