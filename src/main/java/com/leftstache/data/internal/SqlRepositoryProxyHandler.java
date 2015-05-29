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
				result = executeUpdate(connection, updateAnnotation, method, args);
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

	private int executeUpdate(Connection connection, UpdateSqlQuery query, Method method, Object[] args) throws SQLException {
		try(NamedPreparedStatement statement = NamedPreparedStatement.prepareStatement(connection, query.value())) {
			setParameters(statement, method.getParameters(), args);

			return statement.executeUpdate();
		}
	}

	private void setParameters(NamedPreparedStatement statement, Parameter[] parameters, Object[] args) throws SQLException {
		if(parameters != null) {
			assert parameters.length == (args == null ? 0 : args.length) : "Something screwy is going on with reflection or proxies: args ("+args.length+") and parameters ("+parameters.length+") have different lengths";

			for (int i = 0; i < parameters.length; i++) {
				Parameter parameter = parameters[i];
				String name = parameter.getName();
				Object value = args[i];

				statement.setParameter(name, value);
			}
		}
	}
}
