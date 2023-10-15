package com.example.batch.basic;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@EnableConfigurationProperties(BatchProperties.class)
@Configuration
public class HelloJobConfig extends DefaultBatchConfiguration {

    private final JobExecutionListener jobListener;

    @Primary
    @Bean
    public Job helloJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("helloJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(helloStep1(jobRepository, transactionManager))
                .next(helloStep2(jobRepository, transactionManager))
                .next(helloStep3(jobRepository, transactionManager))
                .next(helloStep4(jobRepository, transactionManager))
//                .validator(new CustomJobParametersValidator())
                .validator(new DefaultJobParametersValidator(new String[]{"name", "date"}, new String[]{"optional"}))
                .listener(jobListener)
                .build();
    }

    @Bean
    public Step helloStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("helloStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
                    String name = jobParameters.getString("name");
                    System.out.printf("[%s] Step1 was executed!!\n", name);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step helloStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("helloStep2", jobRepository)
                .<String, String>chunk(3, transactionManager)
                .reader(() -> null)
                .processor(item -> null)
                .writer(chunk -> {

                }).build();
    }

    @Bean
    public Step helloStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("helloStep3", jobRepository)
                .tasklet(new CustomTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step helloStep4(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("helloStep4", jobRepository)
                .partitioner(helloStep1(jobRepository, transactionManager))
                .gridSize(2)
                .build();
    }
}
