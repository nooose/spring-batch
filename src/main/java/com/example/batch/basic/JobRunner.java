package com.example.batch.basic;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job helloJob;
    private final Job flowJob;
    private final Job chunkJob;

    public JobRunner(JobLauncher jobLauncher, Job helloJob, @Qualifier("flowJob") Job flowJob, @Qualifier("chunkJob") Job chunkJob) {
        this.jobLauncher = jobLauncher;
        this.helloJob = helloJob;
        this.flowJob = flowJob;
        this.chunkJob = chunkJob;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JobParameters jobParametersA = new JobParametersBuilder().addString("name", "userA")
                .addDate("date", new Date())
                .toJobParameters();
        JobParameters jobParametersB = new JobParametersBuilder().addString("name", "userB")
                .addDate("date", new Date())
                .toJobParameters();
        JobParameters jobParametersC = new JobParametersBuilder().addString("name", "userC")
                .addDate("date", new Date())
                .toJobParameters();
//        jobLauncher.run(helloJob, jobParametersA);
//        jobLauncher.run(helloJob, jobParametersB);
//        jobLauncher.run(flowJob, jobParametersC);
        jobLauncher.run(chunkJob, new JobParameters());
    }
}
