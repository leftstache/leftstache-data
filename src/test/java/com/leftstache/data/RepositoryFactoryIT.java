package com.leftstache.data;

import com.mchange.v2.c3p0.*;
import org.junit.*;

import java.sql.*;

/**
 * @author Joel Johnson
 */
public class RepositoryFactoryIT {

	private RepositoryFactory repositoryFactory;
	private ComboPooledDataSource dataSource;

	@Before
	public void setUp() throws Exception {
		dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("org.hsqldb.jdbc.JDBCDriver"); //loads the jdbc driver
		dataSource.setJdbcUrl("jdbc:hsqldb:mem:leftstachedata");
		dataSource.setUser("sa");
		dataSource.setPassword("");
		dataSource.setMaxPoolSize(1);

		try(Connection connection = dataSource.getConnection()) {
			try(java.sql.Statement statement = connection.createStatement()) {
				statement.executeUpdate(
					"create table TestInterface (" +
						"blah INTEGER" +
					")"
				);
			}
		}

		repositoryFactory = new RepositoryFactory(dataSource);
	}

	@After
	public void tearDown() throws Exception {
		dataSource.close();
	}

	@Test
	public void testGetRepository() throws SQLException {
		TestInterface testInterface = repositoryFactory.getRepository(TestInterface.class);
		testInterface.createSomething();

		try(Connection connection = dataSource.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				ResultSet resultSet = statement.executeQuery("select * from TestInterface");
				Assert.assertTrue(resultSet.next());
				Assert.assertEquals("result value should be what was inserted", 1, resultSet.getInt("blah"));
				Assert.assertFalse(resultSet.next());
			}
		}
	}

	private interface TestInterface {
		@UpdateSqlQuery("insert into TestInterface (blah) VALUES (1)")
		void createSomething();
	}
}
