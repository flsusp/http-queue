package br.com.http.timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Properties;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.com.base.test.IntegrationTestCase;
import br.com.base.test.SimplifiedResponse;

@EnableServices(value = "jaxrs", httpDebug = true)
@RunWith(ApplicationComposer.class)
public class JobServiceTest extends IntegrationTestCase {

	@Module
	@Classes(value = { JobService.class, JobManager.class, JobExecutor.class }, cdi = true)
	public WebApp app() {
		return setupWebApp();
	}

	@Module
	public PersistenceUnit persistence() {
		return setupPersistenceUnit();
	}

	@Configuration
	public Properties configuration() {
		return setupConfigurationProperties();
	}

	@SuppressWarnings("serial")
	@Test
	public void testCreateJobWithId() {
		SimplifiedResponse response = post("/job", new HashMap<String, String>() {
			{
				put("id", "2");
				put("method", "GET");
				put("url", "http://www.dextra.com.br/");
				put("cron", "* * * * * * *");
			}
		});
		assertEquals(200, response.getStatusCode());
		assertNotNull(response.getContent());
	}

	@SuppressWarnings("serial")
	@Test
	public void testCreateJobWithSameIdTwice() {
		HashMap<String, String> formParameters = new HashMap<String, String>() {
			{
				put("id", "1");
				put("method", "GET");
				put("url", "http://www.dextra.com.br/");
				put("cron", "* * * * * * *");
			}
		};

		SimplifiedResponse response = post("/job", formParameters);
		assertEquals(200, response.getStatusCode());
		assertNotNull(response.getContent());

		response = post("/job", formParameters);
		assertEquals(400, response.getStatusCode());
		assertNotNull(response.getContent());
	}
}
