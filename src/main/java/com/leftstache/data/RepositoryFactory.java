package com.leftstache.data;

import com.leftstache.data.exception.*;
import com.leftstache.data.internal.*;

import javax.sql.*;
import java.lang.reflect.*;

/**
 * @author Joel Johnson
 */
public class RepositoryFactory {
	private final DataSource dataSource;

	public RepositoryFactory(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public <REPO> REPO getRepository(Class<REPO> type) {
		InvocationHandler handler = new SqlRepositoryProxyHandler(dataSource);
		Class proxyClass = Proxy.getProxyClass(type.getClassLoader(), type);

		REPO repo;
		try {
			repo = (REPO) proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(handler);
		} catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new LeftstacheDataException.InitializationException("Unable to initialize type: " + type.getName(), e);
		}

		return repo;
	}
}
