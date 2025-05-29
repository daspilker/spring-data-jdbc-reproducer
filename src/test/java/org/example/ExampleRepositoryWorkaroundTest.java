package org.example;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.jdbc.core.convert.DefaultJdbcTypeFactory;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.convert.JdbcTypeFactory;
import org.springframework.data.jdbc.core.convert.MappingJdbcConverter;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.jdbc.core.dialect.JdbcArrayColumns;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

@DataJdbcTest
public class ExampleRepositoryWorkaroundTest {
    static final Logger LOGGER = LoggerFactory.getLogger(ExampleRepositoryWorkaroundTest.class);

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

    @TestConfiguration
    @Configuration
    public static class JdbcConfiguration extends AbstractJdbcConfiguration {
        @Override
        public JdbcConverter jdbcConverter(JdbcMappingContext mappingContext, NamedParameterJdbcOperations operations,
                                           @Lazy RelationResolver relationResolver, JdbcCustomConversions conversions, Dialect dialect) {
            org.springframework.data.jdbc.core.dialect.JdbcArrayColumns arrayColumns = dialect instanceof JdbcDialect jd
                    ? jd.getArraySupport()
                    : JdbcArrayColumns.DefaultSupport.INSTANCE;
            DefaultJdbcTypeFactory jdbcTypeFactory = new DefaultJdbcTypeFactory(operations.getJdbcOperations(), arrayColumns);

            return new CustomMappingJdbcConverter(mappingContext, relationResolver, conversions, jdbcTypeFactory);
        }
    }

    public static class CustomMappingJdbcConverter extends MappingJdbcConverter {
        public CustomMappingJdbcConverter(RelationalMappingContext context, RelationResolver relationResolver,
                                          CustomConversions conversions, JdbcTypeFactory typeFactory) {
            super(context, relationResolver, conversions, typeFactory);
        }

        @Override
        public Class<?> getColumnType(RelationalPersistentProperty property) {
            if (property.getRawType().equals(byte[].class)) {
                return property.getRawType();
            }
            return super.getColumnType(property);
        }
    }
}
