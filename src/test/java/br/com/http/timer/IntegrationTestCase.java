package br.com.http.timer;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.junit.AfterClass;
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
		scanner.scan(JobService.class);
		scanner.deploy();

		ctx = builder.getContext();
		tm = (TransactionManager) ctx.lookup("TransactionManager");
	}

	@AfterClass
	public static void shutdown() {
		try {
			ShutdownCommand shutdown = new ShutdownCommand();
			shutdown.setContext(new InitialContext());
			shutdown.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ContainerBuilder getBuilder() {
		return builder;
	}

	public InitialContext getContext() {
		return ctx;
	}

	public TransactionManager getTm() {
		return tm;
	}

	@SuppressWarnings("unchecked")
	protected <T> T lookup(Class<T> clazz) throws NamingException {
		return (T) ctx.lookup("ejb/" + clazz.getPackage().getName().replace('.', '/') + "/" + clazz.getSimpleName());
	}

}
