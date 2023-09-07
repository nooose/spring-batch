package com.example.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@EnableConfigurationProperties(BatchProperties.class)
@Configuration
public class HelloJobConfig extends DefaultBatchConfiguration {

    @Bean
    public Job helloJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("helloJob", jobRepository)
                .start(helloStep1(jobRepository, transactionManager))
                .next(helloStep2(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step helloStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("helloStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Hello, Spring Batch!!");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step helloStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("helloStep2", jobRepository)
                .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
                    System.out.println("Stpe2 executed!!");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
