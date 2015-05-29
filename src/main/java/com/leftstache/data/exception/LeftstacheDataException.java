package com.leftstache.data.exception;

import java.lang.reflect.*;

/**
 * @author Joel Johnson
 */
public class LeftstacheDataException extends RuntimeException {
	public LeftstacheDataException(String message) {
		super(message);
	}

	public LeftstacheDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class InitializationException extends LeftstacheDataException {
		public InitializationException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class MissingQueryAnnotationException extends LeftstacheDataException {
		public MissingQueryAnnotationException(Method method) {
			super("Method: " + method.getName() + " is missing annotation for type: " + method.getDeclaringClass());
		}
	}

	public static class TypeMismatchException extends LeftstacheDataException {
		public TypeMismatchException(Class<?> expected, Class<?> actual) {
			super("Unexpected return type. Expected '" + expected.getName() + "' but was '" + actual.getName() + "'");
		}
	}
}
