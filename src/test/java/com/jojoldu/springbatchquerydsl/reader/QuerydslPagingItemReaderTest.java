package com.jojoldu.springbatchquerydsl.reader;

import com.jojoldu.springbatchquerydsl.TestBatchConfig;
import com.jojoldu.springbatchquerydsl.entity.Product;
import com.jojoldu.springbatchquerydsl.entity.ProductRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;

import static com.jojoldu.springbatchquerydsl.entity.QProduct.product;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jojoldu@gmail.com on 15/01/2020
 * Blog : http://jojoldu.tistory.com
 * Github : http://github.com/jojoldu
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestBatchConfig.class})
public class QuerydslPagingItemReaderTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManagerFactory emf;

    @After
    public void tearDown() throws Exception {
        productRepository.deleteAll();
    }

    @Test
    public void reader가_정상적으로_값을반환한다() throws Exception {
        //given
        LocalDate txDate = LocalDate.of(2020,10,12);
        String name = "a";
        int expected1 = 1000;
        int expected2 = 2000;
        productRepository.save(new Product(name, expected1, txDate));
        productRepository.save(new Product(name, expected2, txDate));

        int chunkSize = 1;

        QuerydslPagingItemReader<Product> reader = new QuerydslPagingItemReader<>(emf, chunkSize, queryFactory -> queryFactory
                .selectFrom(product)
                .where(product.createDate.eq(txDate)));

        reader.open(new ExecutionContext());

        //when
        Product read1 = reader.read();
        Product read2 = reader.read();
        Product read3 = reader.read();

        //then
        assertThat(read1.getPrice()).isEqualTo(1000L);
        assertThat(read2.getPrice()).isEqualTo(2000L);
        assertThat(read3).isNull();
    }
}
