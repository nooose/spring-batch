package com.example.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RequiredArgsConstructor
@RestController
public class JobLauncherController {
    private final JobLauncher jobLauncher;
    private final Job job;

    @PostMapping("/members")
    public ResponseEntity<Void> launch(@RequestBody MemberRequest memberRequest) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("id", memberRequest.id())
                .addDate("date", new Date())
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/members:async")
    public ResponseEntity<Void> asyncLaunch(@RequestBody MemberRequest memberRequest) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("id", memberRequest.id())
                .addDate("date", new Date())
                .toJobParameters();

        TaskExecutorJobLauncher taskExecutorJobLauncher = (TaskExecutorJobLauncher) jobLauncher;
        taskExecutorJobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        taskExecutorJobLauncher.run(job, jobParameters);
        return ResponseEntity.accepted().build();
    }
}
