package com.leftstache.data.internal;

import com.leftstache.data.*;
import com.leftstache.data.exception.*;

import javax.sql.*;
import java.lang.reflect.*;
import java.sql.*;

/**
 * @author Joel Johnson
 */
public class SqlRepositoryProxyHandler implements InvocationHandler {
	private final DataSource dataSource;

	public SqlRepositoryProxyHandler(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result;

		UpdateSqlQuery updateAnnotation = method.getAnnotation(UpdateSqlQuery.class);
		if(updateAnnotation != null) {
			try(Connection connection = dataSource.getConnection()) {
				result = executeUpdate(connection, updateAnnotation, method);
			}
		} else {
			throw new LeftstacheDataException.MissingQueryAnnotationException(method);
		}

		if(method.getReturnType() == void.class || method.getReturnType() == Void.class) {
			return null;
		} else if(method.getReturnType().isInstance(result)) {
			return result;
		} else {
			throw new LeftstacheDataException.TypeMismatchException(result.getClass(), method.getReturnType());
		}
	}

	private int executeUpdate(Connection connection, UpdateSqlQuery query, Method method) throws SQLException {
		try(PreparedStatement statement = connection.prepareStatement(query.value())) {
			return statement.executeUpdate();
		}
	}
}
