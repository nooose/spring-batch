package com.example.batch.basic;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class CustomTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Thread.sleep(2500);
        JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
        String id = jobParameters.getString("id");
        System.out.println("Hello " + id);
        return RepeatStatus.FINISHED;
    }
}
