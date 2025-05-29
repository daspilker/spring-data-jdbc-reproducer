package org.example;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

import static org.slf4j.LoggerFactory.getLogger;

@DataJdbcTest
public class ExampleRepositoryTest {
    static final Logger LOGGER = getLogger(ExampleRepositoryTest.class);

    @Autowired
    ExampleRepository exampleRepository;

    @Test
    void test() {
        Example entity = new Example();
        entity.setData(new byte[20 * 1024 * 1024]);

        long start = System.currentTimeMillis();
        exampleRepository.save(entity);
        LOGGER.info(">>>> Time: {}ms",  System.currentTimeMillis() - start);
    }
}
