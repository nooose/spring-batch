package com.example.batch.chunk;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.batch.repeat.exception.SimpleLimitExceptionHandler;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;

public class CustomItemProcessor implements ItemProcessor<String, String> {

    private final RepeatTemplate repeatTemplate = new RepeatTemplate();

    @Override
    public String process(String item) throws Exception {
        SimpleCompletionPolicy policyA = new SimpleCompletionPolicy(3);
        TimeoutTerminationPolicy policyB = new TimeoutTerminationPolicy(500);
        ExceptionHandler exceptionHandler = new SimpleLimitExceptionHandler(3);

        CompositeCompletionPolicy compositePolicy = new CompositeCompletionPolicy();
        CompletionPolicy[] policies = {policyA, policyB};
        compositePolicy.setPolicies(policies);

        repeatTemplate.setCompletionPolicy(compositePolicy);
        repeatTemplate.setExceptionHandler(exceptionHandler);
        repeatTemplate.iterate(context -> {
            System.out.println("repeatTemplate is testing");
//            throw new RuntimeException();
            return RepeatStatus.CONTINUABLE;
        });
        return item.toUpperCase();
    }
}
