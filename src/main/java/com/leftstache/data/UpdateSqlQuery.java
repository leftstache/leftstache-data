package com.leftstache.data;

import java.lang.annotation.*;

/**
 * @author Joel Johnson
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdateSqlQuery {
	String value();
}
