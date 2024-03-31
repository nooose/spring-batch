package com.example.app.batch.job.file;

import com.example.app.batch.chunk.processor.FileItemProcessor;
import com.example.app.batch.domain.Product;
import com.example.app.batch.domain.ProductDto;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class FileJobConfiguration {
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job fileJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("fileJob", jobRepository)
                .start(fileStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step fileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("fileStep", jobRepository)
                .<ProductDto, Product>chunk(10, transactionManager)
                .reader(fileItemReader(null))
                .processor(fileItemProcessor())
                .writer(fileItemWriter())
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<ProductDto> fileItemReader(@Value("#{jobParameters['requestDate']}") String requestDate) {
        return new FlatFileItemReaderBuilder<ProductDto>()
                .name("flatFIle")
                .resource(new ClassPathResource("product_" + requestDate + ".csv"))
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
                .targetType(ProductDto.class)
                .linesToSkip(1)
                .delimited().delimiter(",")
                .names("id", "name", "price", "type")
                .build();
    }

    @Bean
    public ItemProcessor<ProductDto, Product> fileItemProcessor() {
        return new FileItemProcessor();
    }

    @Bean
    public ItemWriter<Product> fileItemWriter() {
        return new JpaItemWriterBuilder<Product>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }

}

