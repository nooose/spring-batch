package com.example.batch.basic;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@RequiredArgsConstructor
@Component
public class JobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job job;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JobParameters jobParametersA = new JobParametersBuilder().addString("name", "userA")
                .addDate("date", new Date())
                .toJobParameters();
        JobParameters jobParametersB = new JobParametersBuilder().addString("name", "userB")
                .addDate("date", new Date())
                .toJobParameters();
        jobLauncher.run(job, jobParametersA);
        jobLauncher.run(job, jobParametersB);
    }
}
