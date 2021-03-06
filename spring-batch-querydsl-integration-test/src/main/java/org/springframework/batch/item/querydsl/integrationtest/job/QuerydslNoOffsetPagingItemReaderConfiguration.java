package org.springframework.batch.item.querydsl.integrationtest.job;

import org.springframework.batch.item.querydsl.integrationtest.entity.Manufacture;
import org.springframework.batch.item.querydsl.integrationtest.entity.ManufactureBackup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.querydsl.reader.QuerydslNoOffsetPagingItemReader;
import org.springframework.batch.item.querydsl.reader.expression.Expression;
import org.springframework.batch.item.querydsl.reader.options.QuerydslNoOffsetNumberOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

import static org.springframework.batch.item.querydsl.integrationtest.entity.QManufacture.manufacture;

/**
 * Created by jojoldu@gmail.com on 06/10/2019
 * Blog : http://jojoldu.tistory.com
 * Github : http://github.com/jojoldu
 */

@Slf4j // log 사용을 위한 lombok 어노테이션
@RequiredArgsConstructor // 생성자 DI를 위한 lombok 어노테이션
@Configuration
public class QuerydslNoOffsetPagingItemReaderConfiguration {
    public static final String JOB_NAME = "querydslNoOffsetPagingReaderJob";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;
    private final QuerydslNoOffsetPagingItemReaderJobParameter jobParameter;

    private int chunkSize;

    @Value("${chunkSize:1000}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean
    @JobScope
    public QuerydslNoOffsetPagingItemReaderJobParameter jobParameter() {
        return new QuerydslNoOffsetPagingItemReaderJobParameter();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(step())
                .build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("querydslNoOffsetPagingReaderStep")
                .<Manufacture, ManufactureBackup>chunk(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public QuerydslNoOffsetPagingItemReader<Manufacture> reader() {
        // 1. No Offset 옵션
        QuerydslNoOffsetNumberOptions<Manufacture, Long> options =
                new QuerydslNoOffsetNumberOptions<>(manufacture.id, Expression.ASC);

        // 2. Querydsl
        return new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(manufacture)
                .where(manufacture.createDate.eq(jobParameter.getTxDate())));
    }

    private ItemProcessor<Manufacture, ManufactureBackup> processor() {
        return ManufactureBackup::new;
    }

    @Bean
    public JpaItemWriter<ManufactureBackup> writer() {
        return new JpaItemWriterBuilder<ManufactureBackup>()
                .entityManagerFactory(emf)
                .build();
    }
}
