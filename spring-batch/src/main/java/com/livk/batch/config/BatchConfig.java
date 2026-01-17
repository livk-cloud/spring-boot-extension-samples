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

package com.livk.batch.config;

import com.livk.batch.domain.User;
import com.livk.batch.support.CsvBeanValidator;
import com.livk.batch.support.CsvItemProcessor;
import com.livk.batch.support.CsvLineMapper;
import com.livk.batch.support.JobCompletionListener;
import com.livk.batch.support.JobReadListener;
import com.livk.batch.support.JobWriteListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.LimitCheckingExceptionHierarchySkipPolicy;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.util.Set;

/**
 * @author livk
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Bean
	public ItemReader<User> reader() {
		CsvLineMapper<User> lineMapper = CsvLineMapper.builder(User.class)
			.delimiter("\t")
			.fields("userName", "sex", "age", "address")
			.build();
		var reader = new FlatFileItemReader<>(lineMapper);
		reader.setLinesToSkip(1);
		reader.setResource(new ClassPathResource("data.csv"));
		return reader;
	}

	@Bean
	public ItemProcessor<User, User> processor() {
		return new CsvItemProcessor(new CsvBeanValidator<>());
	}

	@Bean
	public ItemWriter<User> writer(DataSource dataSource) {
		var writer = new JdbcBatchItemWriter<User>();
		writer.setDataSource(dataSource);
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
		writer.setSql("insert into sys_user(user_name, sex, age, address, status, create_time ,update_time) "
				+ "values (:userName, :sex, :age, :address, :status, :createTime, :updateTime)");
		return writer;
	}

	@Bean
	public Step csvStep(JobRepository jobRepository, DataSource dataSource,
			DataSourceTransactionManager dataSourceTransactionManager) {
		return new StepBuilder("csvStep", jobRepository).<User, User>chunk(5)
			.transactionManager(dataSourceTransactionManager)
			.reader(reader())
			.listener(new JobReadListener())
			.processor(processor())
			.writer(writer(dataSource))
			.listener(new JobWriteListener())
			.faultTolerant()
			// 设定一个我们允许的这个step可以跳过的异常数量，假如我们设定为3，则当这个step运行时，只要出现的异常数目不超过3，整个step都不会fail。注意，若不设定skipLimit，则其默认值是0
			.skipLimit(3)
			// 指定我们可以跳过的异常，因为有些异常的出现，我们是可以忽略的
			.skip(Exception.class)
			// 出现这个异常我们不想跳过，因此这种异常出现一次时，计数器就会加一，直到达到上限
			.skipPolicy(new LimitCheckingExceptionHierarchySkipPolicy(Set.of(FileNotFoundException.class), 3))
			.build();
	}

	@Bean
	public Job csvJob(JobRepository jobRepository, Step step, JobCompletionListener listener) {
		return new JobBuilder("csvJob", jobRepository).start(step).listener(listener).build();
	}

	@Bean
	public MapJobRegistry mapJobRegistry() {
		return new MapJobRegistry();
	}

	@Bean
	public TaskExecutorJobOperator taskExecutorJobOperator(JobRepository jobRepository, MapJobRegistry mapJobRegistry) {
		var jobOperator = new TaskExecutorJobOperator();
		// 设置jobRepository
		jobOperator.setJobRepository(jobRepository);
		jobOperator.setJobRegistry(mapJobRegistry);
		return jobOperator;
	}

}
