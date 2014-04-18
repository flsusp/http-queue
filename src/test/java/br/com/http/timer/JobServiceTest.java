package br.com.http.timer;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

@EnableServices(value = "jaxrs", httpDebug = true)
@RunWith(ApplicationComposer.class)
public class JobServiceTest {

	@Module
	@Classes(value = { JobService.class, JobManager.class, JobExecutor.class }, cdi = true)
	public WebApp app() {
		return new WebApp().contextRoot("");
	}

	@Module
	public PersistenceUnit persistence() {
		PersistenceUnit unit = new PersistenceUnit("primary");
		unit.setJtaDataSource("openejb/Resource/ESBDS");

		unit.setProperty("hibernate.hbm2ddl.auto", "update");
		unit.setProperty("hibernate.show_sql", "true");
		return unit;
	}

	@SuppressWarnings("serial")
	@Configuration
	public Properties configuration() {
		return new Properties() {
			{
				setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
				setProperty("ESBDS", "new://Resource?type=DataSource");
				setProperty("ESBDS.JdbcDriver", "org.hsqldb.jdbcDriver");
				setProperty("ESBDS.JdbcUrl", "jdbc:hsqldb:mem:.");
			}
		};
	}

	@Test
	public void testCreateJob() {
		String string = WebClient.create("http://localhost:4204").path("/job/greeting")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
		assertEquals("{\"ai\":\"sim\"}", string);
		// JobService service = (JobService)
		// getContext().lookup("JobService/local");
		//
		// service.createJob(null, "GET", "http://www.dextra.com.br", null,
		// null, null, null, "0 0 12 1/1 * ? *");
	}
}
