package br.com.http.queue;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServiceStarter implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServiceManager manager = new ServiceManager().start();
		sce.getServletContext().setAttribute(ServiceManager.class.getName(), manager);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServiceManager manager = (ServiceManager) sce.getServletContext().getAttribute(ServiceManager.class.getName());
		manager.shutdown();
	}
}
