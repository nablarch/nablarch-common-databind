package nablarch.common.databind;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import nablarch.common.databind.csv.BeanCsvMapper;
import nablarch.common.databind.csv.Csv;
import nablarch.common.databind.csv.CsvBeanMapper;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ObjectMapperFactory}のテストクラス。
 */
public class ObjectMapperFactoryTest {

    @Before
    public void setUp() throws Exception {
        SystemRepository.clear();
    }

    @After
    public void tearDown() throws Exception {
        SystemRepository.clear();
    }

    /**
     * CSV用アノテーションを持つBeanを指定しているので、{@link CsvBeanMapper}が生成されること。
     */
    @Test
    public void createCsvBeanMapper() throws Exception {
        assertThat("input:InputStream",
                ObjectMapperFactory.create(CsvBean.class, new ByteArrayInputStream(new byte[0])),
                is(instanceOf(CsvBeanMapper.class)));
        assertThat("input:Reader",
                ObjectMapperFactory.create(CsvBean.class, new StringReader("1,2")),
                is(instanceOf(CsvBeanMapper.class)));
        assertThat("input:String",
                ObjectMapperFactory.create(CsvBean.class, "12345,12345"), is(instanceOf(CsvBeanMapper.class)));
    }

    /**
     * CSV用アノテーションを持つBeanを指定しているので、{@link BeanCsvMapper}が生成されること。
     */
    @Test
    public void createBeanCsvMapper() throws Exception {
        assertThat("input:OutputStream",
                ObjectMapperFactory.create(CsvBean.class, new ByteArrayOutputStream()),
                is(instanceOf(BeanCsvMapper.class)));
        assertThat("input:Writer",
                ObjectMapperFactory.create(CsvBean.class, new StringWriter()),
                is(instanceOf(BeanCsvMapper.class)));
    }

    /**
     *
     */
    @Test
    public void crateFactory() throws Exception {

        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                final HashMap<String, Object> objects = new HashMap<String, Object>();
                objects.put("objectMapperFactory", new BasicObjectMapperFactory() {
                    @Override
                    public <T> ObjectMapper<T> createMapper(Class<T> clazz, InputStream stream) {
                        return new DummyMapper<T>();
                    }
                });
                return objects;
            }
        });

        assertThat("input:InputStream",
                ObjectMapperFactory.create(CsvBean.class, new ByteArrayInputStream(new byte[0])),
                is(instanceOf(DummyMapper.class)));

    }

    class DummyMapper<T> implements ObjectMapper<T> {

        @Override
        public void write(T object) {

        }

        @Override
        public T read() {
            return null;
        }

        @Override
        public void close() {

        }
    }

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"field1", "field2"}, headers = {"フィールド1", "フィールド2"})
    public static class CsvBean {

        private String field1;

        private String field2;

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public void setField2(String field2) {
            this.field2 = field2;
        }
    }

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"field1", "field2"}, headers = {"フィールド1"})
    public static class CsvBeanPropertySize extends CsvBean {

    }

    @Csv(type = Csv.CsvType.RFC4180, properties = {})
    public static class CsvBeanPropertyEmpty extends CsvBean {

    }
}
