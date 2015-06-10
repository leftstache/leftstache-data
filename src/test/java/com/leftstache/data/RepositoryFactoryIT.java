package com.leftstache.data;

import com.mchange.v2.c3p0.*;
import org.junit.*;

import java.sql.*;
import java.util.*;

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
		dataSource.setJdbcUrl("jdbc:hsqldb:mem:leftstachedata" + UUID.randomUUID().toString());
		dataSource.setUser("sa");
		dataSource.setPassword("");
		dataSource.setMaxPoolSize(1);

		try(Connection connection = dataSource.getConnection()) {
			try(java.sql.Statement statement = connection.createStatement()) {
				statement.executeUpdate(
					"create table TestInterface (" +
						"blah INTEGER," +
						"blah2 VARCHAR(10)," +
						"blah3 BOOLEAN" +
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
	public void testSimpleUpdate() throws SQLException {
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

	@Test
	public void testParameterizedUpdate() throws SQLException {
		TestInterface testInterface = repositoryFactory.getRepository(TestInterface.class);
		testInterface.createSomethingWithParameter(10);

		try(Connection connection = dataSource.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				ResultSet resultSet = statement.executeQuery("select * from TestInterface");
				Assert.assertTrue(resultSet.next());
				Assert.assertEquals("result value should be what was inserted", 10, resultSet.getInt("blah"));
				Assert.assertFalse(resultSet.next());
			}
		}
	}

	@Test
	public void testParameterizedUpdate_3() throws SQLException {
		TestInterface testInterface = repositoryFactory.getRepository(TestInterface.class);
		testInterface.createSomethingWithParameters(10, "someValue", true);

		try(Connection connection = dataSource.getConnection()) {
			try(Statement statement = connection.createStatement()) {
				ResultSet resultSet = statement.executeQuery("select * from TestInterface");
				Assert.assertTrue(resultSet.next());
				Assert.assertEquals("result value should be what was inserted", 10, resultSet.getInt("blah"));
				Assert.assertEquals("result value should be what was inserted", "someValue", resultSet.getString("blah2"));
				Assert.assertEquals("result value should be what was inserted", true, resultSet.getBoolean("blah3"));
				Assert.assertFalse(resultSet.next());
			}
		}
	}

	@Test
	public void testSimpleSelect() throws SQLException {
		try(Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate("delete from TestInterface");
			}
			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate("insert into TestInterface (blah) VALUES (120) ");
			}
		}

		TestInterface testInterface = repositoryFactory.getRepository(TestInterface.class);
		List<TestPojo> all = testInterface.findAll();
		Assert.assertEquals(all.get(0).blah, 120);
		Assert.assertEquals(1, all.size());
	}

	@Test
	public void testSimpleSelectLots() throws SQLException {
		try(Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate("delete from TestInterface");
			}
			for (int i = 0; i < 10; i++) {
				try (Statement statement = connection.createStatement()) {
					statement.executeUpdate("insert into TestInterface (blah) VALUES ("+i+") ");
				}
			}
		}

		TestInterface testInterface = repositoryFactory.getRepository(TestInterface.class);
		List<TestPojo> all = testInterface.findAll();
		for (int i = 0; i < all.size(); i++) {
			TestPojo testPojo = all.get(i);
			Assert.assertEquals(testPojo.blah, i);
		}
		Assert.assertEquals(10, all.size());
	}

	@Test
	public void testSimpleSelectOne() throws SQLException {
		try(Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate("delete from TestInterface");
			}
			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate("insert into TestInterface (blah) VALUES (120) ");
			}
		}

		TestInterface testInterface = repositoryFactory.getRepository(TestInterface.class);
		TestPojo one = testInterface.findOne();
		Assert.assertEquals(one.blah, 120);
	}

	private interface TestInterface {
		@UpdateSqlQuery("insert into TestInterface (blah) VALUES (1)")
		void createSomething();

		@UpdateSqlQuery("insert into TestInterface (blah) VALUES (:value)")
		void createSomethingWithParameter(int value);

		@UpdateSqlQuery("insert into TestInterface (blah, blah2, blah3) VALUES (:value,:value2, :value3)")
		void createSomethingWithParameters(int value, String value2, boolean value3);

		@SelectSqlQuery("select * from TestInterface")
		List<TestPojo> findAll();

		@SelectSqlQuery("select * from TestInterface limit 1")
		TestPojo findOne();
	}

	public static class TestPojo {
		private int blah;
		private String blah2;
		private boolean blah3;

		public int getBlah() {
			return blah;
		}

		public void setBlah(int blah) {
			this.blah = blah;
		}

		public String getBlah2() {
			return blah2;
		}

		public void setBlah2(String blah2) {
			this.blah2 = blah2;
		}

		public boolean isBlah3() {
			return blah3;
		}

		public void setBlah3(boolean blah3) {
			this.blah3 = blah3;
		}
	}
}
