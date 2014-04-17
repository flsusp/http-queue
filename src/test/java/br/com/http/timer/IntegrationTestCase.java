package br.com.http.timer;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.junit.BeforeClass;

import com.googlecode.mycontainer.kernel.boot.ContainerBuilder;
import com.googlecode.mycontainer.kernel.deploy.MyTransactionManagerDeployer;
import com.googlecode.mycontainer.kernel.deploy.ScannerDeployer;

public class IntegrationTestCase {

	private static ContainerBuilder builder;
	private static InitialContext ctx;
	private static TransactionManager tm;

	@BeforeClass
	public static void bootMyContainer() throws NamingException {
		builder = new ContainerBuilder();

		builder.createDeployer(MyTransactionManagerDeployer.class).setName("TransactionManager").deploy();

		DataSourceDeployer ds = builder.createDeployer(DataSourceDeployer.class);
		ds.setName("TestDS");
		ds.setDriver("org.hsqldb.jdbcDriver");
		ds.setUrl("jdbc:hsqldb:mem:.");
		ds.setUser("sa");
		ds.deploy();

		JPADeployer jpa = builder.createDeployer(HibernateJPADeployer.class);
		JPAInfoBuilder info = (JPAInfoBuilder) jpa.getInfo();
		info.setPersistenceUnitName("test-pu");
		info.setJtaDataSourceName("TestDS");
		info.addJarFileUrl(CustomerBean.class);
		info.setPersistenceUnitRootUrl(CustomerBean.class);
		Properties props = info.getProperties();
		props.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		props.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		props.setProperty("hibernate.show_sql", "true");
		jpa.deploy();

		ScannerDeployer scanner = builder.createDeployer(ScannerDeployer.class);
		scanner.add(new StatelessScannableDeployer());
		scanner.scan(EntityManagerWrapperBean.class);
		scanner.deploy();

		ctx = builder.getContext();
		tm = (TransactionManager) ctx.lookup("TransactionManager");
	}

}
