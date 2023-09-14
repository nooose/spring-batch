package com.example.batch.basic;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class FlowJobConfig {

    @Bean
    public Job flowJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("flowJob", jobRepository)
                .start(flow(jobRepository, transactionManager))
                .next(step3(jobRepository, transactionManager))
                .end()
                .build();
    }

    @Bean
    public Flow flow(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new FlowBuilder<Flow>("flow")
                .start(step1(jobRepository, transactionManager))
                .next(step2(jobRepository, transactionManager))
                .end();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("Step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
                    String name = jobParameters.getString("name");
                    System.out.printf("[%s] Step1 was executed!!\n", name);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("Step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
                    String name = jobParameters.getString("name");
                    System.out.printf("[%s] Step2 was executed!!\n", name);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step3", jobRepository)
                .tasklet(new CustomTasklet(), transactionManager)
                .build();
    }
}
