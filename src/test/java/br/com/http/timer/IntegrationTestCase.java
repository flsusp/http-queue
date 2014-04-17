package br.com.http.timer;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.junit.BeforeClass;

import com.googlecode.mycontainer.datasource.DataSourceDeployer;
import com.googlecode.mycontainer.ejb.SessionInterceptorDeployer;
import com.googlecode.mycontainer.ejb.StatelessScannableDeployer;
import com.googlecode.mycontainer.jpa.HibernateJPADeployer;
import com.googlecode.mycontainer.jpa.JPADeployer;
import com.googlecode.mycontainer.jpa.JPAInfoBuilder;
import com.googlecode.mycontainer.jta.MyTransactionManagerDeployer;
import com.googlecode.mycontainer.kernel.ShutdownCommand;
import com.googlecode.mycontainer.kernel.boot.ContainerBuilder;
import com.googlecode.mycontainer.kernel.deploy.ScannerDeployer;
import com.googlecode.mycontainer.web.ContextWebServer;
import com.googlecode.mycontainer.web.FilterDesc;
import com.googlecode.mycontainer.web.LogFilter;
import com.googlecode.mycontainer.web.Realm;
import com.googlecode.mycontainer.web.ServletDesc;
import com.googlecode.mycontainer.web.jetty.JettyServerDeployer;

public class IntegrationTestCase {

	private static ContainerBuilder builder;
	private static InitialContext ctx;
	private static TransactionManager tm;

	@BeforeClass
	public static void bootMyContainer() throws NamingException {
		builder = new ContainerBuilder();

		SessionInterceptorDeployer sessionInterceptorDeployer = builder
				.createDeployer(SessionInterceptorDeployer.class);
		sessionInterceptorDeployer.deploy();

		builder.createDeployer(MyTransactionManagerDeployer.class).setName("TransactionManager").deploy();

		DataSourceDeployer ds = builder.createDeployer(DataSourceDeployer.class);
		ds.setName("EBSDS");
		ds.setDriver("org.hsqldb.jdbcDriver");
		ds.setUrl("jdbc:hsqldb:mem:.");
		ds.setUser("sa");
		ds.deploy();

		JPADeployer jpa = builder.createDeployer(HibernateJPADeployer.class);
		JPAInfoBuilder info = (JPAInfoBuilder) jpa.getInfo();
		info.setPersistenceUnitName("primary");
		info.setJtaDataSourceName("EBSDS");
		info.addJarFileUrl(JobManager.class);
		info.setPersistenceUnitRootUrl(JobManager.class);
		Properties props = info.getProperties();

		props.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		props.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		props.setProperty("hibernate.show_sql", "true");
		jpa.deploy();

		ScannerDeployer scanner = builder.createDeployer(ScannerDeployer.class);
		scanner.add(new StatelessScannableDeployer());
		scanner.scan(JobManager.class);
		scanner.deploy();

		ctx = builder.getContext();
		tm = (TransactionManager) ctx.lookup("TransactionManager");
	}

}
