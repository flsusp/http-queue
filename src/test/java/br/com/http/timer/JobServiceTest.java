package br.com.http.timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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

		unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
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

	private SimplifiedResponse post(String path, Map<String, String> formParameters) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (Entry<String, String> entry : formParameters.entrySet()) {
			nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		SimplifiedResponse responseToReturn = null;
		HttpClientBuilder clientBuilder = HttpClients.custom();
		// clientBuilder.setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(30000)
		// .setConnectTimeout(30000).build());
		try (CloseableHttpClient httpClient = clientBuilder.build()) {
			HttpPost request = new HttpPost("http://localhost:4204" + path);
			HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs);
			request.setEntity(entity);
			try (CloseableHttpResponse response = httpClient.execute(request)) {
				responseToReturn = new SimplifiedResponse(response.getStatusLine().getStatusCode(),
						EntityUtils.toString(response.getEntity()));
			}
		} catch (IOException e) {
			new RuntimeException(e);
		}

		return responseToReturn;
	}
}

class SimplifiedResponse {
	private int statusCode;
	private String content;

	SimplifiedResponse(int statusCode, String content) {
		this.statusCode = statusCode;
		this.content = content;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getContent() {
		return content;
	}
}
