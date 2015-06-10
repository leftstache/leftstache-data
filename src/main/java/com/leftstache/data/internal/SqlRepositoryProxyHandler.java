package com.leftstache.data.internal;

import com.leftstache.data.*;
import com.leftstache.data.exception.*;

import javax.sql.*;
import java.beans.*;
import java.lang.reflect.*;
import java.math.*;
import java.sql.*;
import java.util.*;

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
			SelectSqlQuery selectAnnotation = method.getAnnotation(SelectSqlQuery.class);
			if(selectAnnotation != null) {
				try(Connection connection = dataSource.getConnection()) {
					try(NamedPreparedStatement statement = NamedPreparedStatement.prepareStatement(connection, selectAnnotation.value())) {
						setParameters(statement, method.getParameters(), args);

						try(ResultSet resultSet = statement.executeQuery()) {
							result = convertResultSet(resultSet, method.getGenericReturnType());
						}
					}
				}
			} else {
				throw new LeftstacheDataException.MissingQueryAnnotationException(method);
			}
		}

		if(method.getReturnType() == void.class || method.getReturnType() == Void.class) {
			return null;
		} else if(method.getReturnType().isInstance(result)) {
			return result;
		} else {
			throw new LeftstacheDataException.TypeMismatchException(result.getClass(), method.getReturnType());
		}
	}

	private Object convertResultSet(ResultSet resultSet, Type genericReturnType) throws SQLException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Type rawType = null;
		Class collectionOf = null;
		if(genericReturnType instanceof Class) {
			rawType = genericReturnType;
			collectionOf = null;
		} else if(genericReturnType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;


			rawType = parameterizedType.getRawType();
			Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

			if(actualTypeArguments != null && actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class && rawType instanceof Class && List.class.isAssignableFrom((Class)rawType)) {
				collectionOf = (Class)actualTypeArguments[0];
			} else {
				rawType = null;
			}
		}

		if(rawType == null && collectionOf == null) {
			throw new LeftstacheDataException.InvalidPropertyType("Expected simple bean type or List of a simple bean type");
		}

		List<Object> result = new ArrayList<>();

		Class type;
		if(collectionOf != null) {
			type = collectionOf;
		} else {
			type = (Class) rawType;
		}

		BeanInfo beanInfo = Introspector.getBeanInfo(type);
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

		while(resultSet.next()) {
			Object instance = type.newInstance();

			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				Method writeMethod = propertyDescriptor.getWriteMethod();
				if(writeMethod != null) {
					writeMethod.setAccessible(true);

					Class<?> propertyType = propertyDescriptor.getPropertyType();
					String name = propertyDescriptor.getName();
					Object value = getResultSetValueByType(resultSet, propertyType, name);
					writeMethod.invoke(instance, value);
				}
			}

			result.add(instance);
		}

		if(collectionOf == null) {
			if(result.size() <= 0) {
				return null;
			} else {
				return result.get(0);
			}
		} else {
			return result;
		}
	}

	private Object getResultSetValueByType(ResultSet resultSet, Class<?> propertyType, String name) throws SQLException {
		Object result;
		if(String.class.isAssignableFrom(propertyType)) {
			result = resultSet.getString(name);
		} else if (boolean.class.isAssignableFrom(propertyType) || Boolean.class.isAssignableFrom(propertyType)) {
			result = resultSet.getBoolean(name);
		} else if (byte.class.isAssignableFrom(propertyType) || Byte.class.isAssignableFrom(propertyType)) {
			result = resultSet.getByte(name);
		} else if (char.class.isAssignableFrom(propertyType) || Character.class.isAssignableFrom(propertyType)) {
			result = resultSet.getString(name);
		} else if (short.class.isAssignableFrom(propertyType) || Short.class.isAssignableFrom(propertyType)) {
			result = resultSet.getShort(name);
		} else if (int.class.isAssignableFrom(propertyType) || Integer.class.isAssignableFrom(propertyType)) {
			result = resultSet.getInt(name);
		} else if (long.class.isAssignableFrom(propertyType) || Long.class.isAssignableFrom(propertyType)) {
			result = resultSet.getLong(name);
		} else if (float.class.isAssignableFrom(propertyType) || Float.class.isAssignableFrom(propertyType)) {
			result = resultSet.getFloat(name);
		} else if (double.class.isAssignableFrom(propertyType) || Double.class.isAssignableFrom(propertyType)) {
			result = resultSet.getDouble(name);
		} else if (BigDecimal.class.isAssignableFrom(propertyType)) {
			result = resultSet.getBigDecimal(name);
		} else {
			throw new UnsupportedOperationException(propertyType.getName() + " " + name);
		}
		return result;
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
