/*
 * Copyright 2021-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.livk.batch.support;

import com.livk.commons.util.BeanUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.batch.item.file.LineMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author livk
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CsvLineMapper<T> implements LineMapper<T> {

	private final Class<T> targetClass;

	private final String[] fields;

	private final String delimiter;

	public static <T> Builder<T> builder(Class<T> targetClass) {
		return new Builder<>(targetClass);
	}

	@NonNull
	@Override
	public T mapLine(@NonNull String line, int lineNumber) {
		var instance = BeanUtils.instantiateClass(targetClass);
		var fieldArray = line.split(delimiter);
		if (fieldArray.length != fields.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		for (var i = 0; i < fields.length; i++) {
			set(instance, fields[i], fieldArray[i]);
		}
		return instance;
	}

	@SneakyThrows
	private void set(T t, String field, String valueStr) {
		Object value;
		var targetClass = t.getClass();
		var declaredField = targetClass.getDeclaredField(field);
		var type = declaredField.getType();
		if (Integer.class.equals(type)) {
			value = Integer.parseInt(valueStr);
		}
		else if (Long.class.equals(type)) {
			value = Long.parseLong(valueStr);
		}
		else if (Float.class.equals(type)) {
			value = Float.parseFloat(valueStr);
		}
		else if (Double.class.equals(type)) {
			value = Double.parseDouble(valueStr);
		}
		else if (Boolean.class.equals(type)) {
			value = Boolean.parseBoolean(valueStr);
		}
		else {
			value = valueStr;
		}
		field = StringUtils.capitalize(field);
		var method = targetClass.getMethod("set" + field, type);
		method.invoke(t, value);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Builder<T> {

		private final Class<T> targetClass;

		private String[] fields;

		private String delimiter;

		public Builder<T> fields(String... fields) {
			this.fields = fields;
			return this;
		}

		public Builder<T> delimiter(String delimiter) {
			this.delimiter = delimiter;
			return this;
		}

		public CsvLineMapper<T> build() {
			Assert.notNull(targetClass, "targetClass not null");
			Assert.notNull(fields, "fields not null");
			Assert.notNull(delimiter, "delimiter not null");
			return new CsvLineMapper<>(targetClass, fields, delimiter);
		}

	}

}
