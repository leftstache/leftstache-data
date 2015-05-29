package com.leftstache.data.internal;

import java.math.*;
import java.sql.*;
import java.util.*;

/**
 * @author Joel Johnson
 */
public class NamedPreparedStatement implements AutoCloseable {
	private final List<String> fields;
	private final PreparedStatement preparedStatement;
	private final String query;

	private NamedPreparedStatement(PreparedStatement preparedStatement, String query, List<String> fields) {
		this.preparedStatement = preparedStatement;
		this.query = query;
		this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
	}

	public static NamedPreparedStatement prepareStatement(Connection connection, String query) throws SQLException {
		List<String> fields = new ArrayList<>();
		String newQuery = replaceFields(query, fields);

		PreparedStatement preparedStatement = connection.prepareStatement(newQuery);

		return new NamedPreparedStatement(preparedStatement, newQuery, fields);
	}

	/* scoped for testing */
	static String replaceFields(String query, List<String> fields) {
		int pos;
		while((pos = query.indexOf(":")) != -1) {
			int end = indexOfNonAlphaNumeric(query, pos);
			if (end == -1)
				end = query.length();
			else
				end += pos;
			fields.add(query.substring(pos+1,end));
			query = query.substring(0, pos) + "?" + query.substring(end);
		}
		return query;
	}

	private static int indexOfNonAlphaNumeric(String query, int pos) {
		String substring = query.substring(pos);
		char[] charArray = substring.toCharArray();
		for (int i = 1; i < charArray.length; i++) {
			char c = charArray[i];
			if(!Character.isLetterOrDigit(c)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void close() throws SQLException {
		preparedStatement.close();
	}

	public int executeUpdate() throws SQLException {
		return preparedStatement.executeUpdate();
	}

	public ResultSet executeQuery() throws SQLException {
		return preparedStatement.executeQuery();
	}

	public void setParameter(String name, Object parameter) throws SQLException {
		int index = getIndex(name);

		if(parameter == null) {
			throw new UnsupportedOperationException("Null parameters unsupported. name: " + name);
		}

		if(parameter instanceof String) {
			preparedStatement.setString(index, (String) parameter);
		} else if(parameter instanceof Boolean) {
			preparedStatement.setBoolean(index, (Boolean) parameter);
		} else if(parameter instanceof Byte) {
			preparedStatement.setByte(index, (Byte) parameter);
		} else if(parameter instanceof Character) {
			preparedStatement.setString(index, String.valueOf(parameter));
		} else if(parameter instanceof Short) {
			preparedStatement.setShort(index, (Short) parameter);
		} else if(parameter instanceof Integer) {
			preparedStatement.setInt(index, (Integer) parameter);
		} else if(parameter instanceof Long) {
			preparedStatement.setLong(index, (Long) parameter);
		} else if(parameter instanceof Float) {
			preparedStatement.setFloat(index, (Float) parameter);
		} else if(parameter instanceof Double) {
			preparedStatement.setDouble(index, (Double) parameter);
		} else if(parameter instanceof BigDecimal) {
			preparedStatement.setBigDecimal(index, (BigDecimal) parameter);
		} else {
			throw new UnsupportedOperationException(parameter.getClass().getName());
		}
	}

	private int getIndex(String name) {
		return fields.indexOf(name) + 1;
	}

}
