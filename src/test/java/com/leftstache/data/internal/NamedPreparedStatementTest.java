package com.leftstache.data.internal;

import org.junit.*;

import java.util.*;

/**
 * @author Joel Johnson
 */
public class NamedPreparedStatementTest {
	@Test
	public void testReplaceFields() {
		List<String> resultFields = new ArrayList<>();
		String result = NamedPreparedStatement.replaceFields("select * from t where f1 = :something and f2=:somethingelse and f3 not in :something", resultFields);

		Assert.assertEquals("select * from t where f1 = ? and f2=? and f3 not in ?", result);
		Assert.assertEquals(resultFields.size(), 3);
		Assert.assertEquals(resultFields.get(0), "something");
		Assert.assertEquals(resultFields.get(1), "somethingelse");
		Assert.assertEquals(resultFields.get(2), "something");
	}

	@Test
	public void testReplaceWithParens_1() {
		List<String> resultFields = new ArrayList<>();
		String result = NamedPreparedStatement.replaceFields("insert into TestInterface (blah) VALUES (:value)", resultFields);

		Assert.assertEquals("insert into TestInterface (blah) VALUES (?)", result);
	}

	@Test
	public void testReplaceWithParens_2() {
		List<String> resultFields = new ArrayList<>();
		String result = NamedPreparedStatement.replaceFields("insert into TestInterface (blah) VALUES (:value, :value2)", resultFields);

		Assert.assertEquals("insert into TestInterface (blah) VALUES (?, ?)", result);
	}

	@Test
	public void testReplaceWithParens_3() {
		List<String> resultFields = new ArrayList<>();
		String result = NamedPreparedStatement.replaceFields("insert into TestInterface (blah) VALUES (:value1, :value2, :value3)", resultFields);

		Assert.assertEquals("insert into TestInterface (blah) VALUES (?, ?, ?)", result);
	}
}
