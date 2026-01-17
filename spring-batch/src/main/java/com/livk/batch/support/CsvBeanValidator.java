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

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.validator.ValidationException;

/**
 * @author livk
 */
public class CsvBeanValidator<T> implements org.springframework.batch.infrastructure.item.validator.Validator<T> {

	private final Validator validator;

	public CsvBeanValidator() {
		try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
			this.validator = validatorFactory.usingContext().getValidator();
		}
	}

	@Override
	public void validate(@NonNull T value) throws ValidationException {
		var constraintViolations = validator.validate(value);
		if (!constraintViolations.isEmpty()) {
			var message = new StringBuilder();
			constraintViolations
				.forEach(constraintViolation -> message.append(constraintViolation.getMessage()).append("\n"));
			throw new ValidationException(message.toString());
		}
	}

}
