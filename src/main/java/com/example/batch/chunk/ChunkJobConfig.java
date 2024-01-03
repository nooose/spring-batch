package com.example.batch.chunk;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class ChunkJobConfig {

    @Bean
    public Job chunkJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("batchJob", jobRepository)
                .start(chunkStep1(jobRepository, transactionManager))
                .next(chunkStep2(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step chunkStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .<String, String>chunk(3, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .faultTolerant()
                .skip(IllegalArgumentException.class)
                .skipLimit(2)
                .retry(IllegalArgumentException.class)
                .retryLimit(2)
                .build();
    }

    @Bean
    public ItemReader<String> itemReader() {
        return new CustomItemReader(List.of("user1", "user2", "user3", "user4"));
    }

    @Bean
    public ItemProcessor<String, String> itemProcessor() {
        return new CustomItemProcessor();
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return new CustomItemWriter();
    }

    @Bean
    public Step chunkStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
