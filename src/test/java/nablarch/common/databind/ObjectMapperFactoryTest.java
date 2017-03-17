package nablarch.common.databind;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import nablarch.common.databind.csv.BeanCsvMapper;
import nablarch.common.databind.csv.Csv;
import nablarch.common.databind.csv.CsvBeanMapper;
import nablarch.common.databind.csv.CsvDataBindConfig;
import nablarch.common.databind.csv.CsvMapMapper;
import nablarch.common.databind.csv.MapCsvMapper;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;

import org.junit.After;
import org.junit.Test;

import mockit.Expectations;

/**
 * {@link ObjectMapperFactory}のテストクラス。
 */
public class ObjectMapperFactoryTest {

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
     * CSV用アノテーションを持つBeanを指定しているが{@link DataBindConfig}を指定しているので、例外が発生すること
     */
    @Test
    public void createCsvBeanMapper_preference() throws Exception {

        try {
            ObjectMapperFactory.create(CsvBean.class, new ByteArrayInputStream(new byte[0]), CsvDataBindConfig.DEFAULT);
            fail("preferenceが指定されているため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:InputStream", e.getMessage(),
                    is("this class should not be set config. class = [nablarch.common.databind.ObjectMapperFactoryTest$CsvBean]"));
        }

        try {
            ObjectMapperFactory.create(CsvBean.class, new StringReader("1,2"), CsvDataBindConfig.EXCEL);
            fail("preferenceが指定されているため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:Reader", e.getMessage(),
                    is("this class should not be set config. class = [nablarch.common.databind.ObjectMapperFactoryTest$CsvBean]"));
        }

        try {
            ObjectMapperFactory.create(CsvBean.class, "12345,12345", CsvDataBindConfig.RFC4180);
            fail("preferenceが指定されているため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:String", e.getMessage(),
                    is("this class should not be set config. class = [nablarch.common.databind.ObjectMapperFactoryTest$CsvBean]"));
        }

        try {
            ObjectMapperFactory.create(CsvBean.class, new ByteArrayOutputStream(), CsvDataBindConfig.TSV);
            fail("preferenceが指定されているため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:OutputStream", e.getMessage(),
                    is("this class should not be set config. class = [nablarch.common.databind.ObjectMapperFactoryTest$CsvBean]"));
        }

        try {
            ObjectMapperFactory.create(CsvBean.class, new StringWriter(), CsvDataBindConfig.DEFAULT);
            fail("preferenceが指定されているため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:Writer", e.getMessage(),
                    is("this class should not be set config. class = [nablarch.common.databind.ObjectMapperFactoryTest$CsvBean]"));
        }
    }

    /**
     * Mapインタフェースを指定しているので、{@link CsvMapMapper}が生成されること。
     */
    @Test
    public void createCsvMapMapper() throws Exception {
        assertThat("input:InputStream",
                ObjectMapperFactory.create(Map.class, new ByteArrayInputStream(new byte[0]),
                        CsvDataBindConfig.EXCEL.withRequiredHeader(true)
                                .withHeaderTitles("test")),
                is(instanceOf(CsvMapMapper.class)));
        assertThat("input:Reader",
                ObjectMapperFactory.create(Map.class, new StringReader("1,2"),
                        CsvDataBindConfig.EXCEL.withRequiredHeader(true)
                                .withHeaderTitles("col1", "col2")),
                is(instanceOf(CsvMapMapper.class)));
        assertThat("input:String",
                ObjectMapperFactory.create(Map.class, "12345,12345",
                        CsvDataBindConfig.RFC4180.withRequiredHeader(true)
                                .withHeaderTitles("test", "test2")),
                is(instanceOf(CsvMapMapper.class)));
    }


    /**
     * Mapインタフェースを指定しているので、{@link nablarch.common.databind.csv.MapCsvMapper}が生成されること。
     */
    @Test
    public void testMapCsvMapper() throws Exception {
        assertThat("input:OutputStream",
                ObjectMapperFactory.create(Map.class, new ByteArrayOutputStream(),
                        CsvDataBindConfig.TSV.withRequiredHeader(true)
                                .withHeaderTitles("test")),
                is(instanceOf(MapCsvMapper.class)));
        assertThat("input:Writer",
                ObjectMapperFactory.create(Map.class, new StringWriter(),
                        CsvDataBindConfig.RFC4180.withRequiredHeader(true)
                                .withHeaderTitles("test")),
                is(instanceOf(MapCsvMapper.class)));

    }

    /**
     * Mapインタフェースを指定しているが{@link DataBindConfig}を指定していないので、例外が発生すること。
     */
    @Test
    public void createCsvMapMapperWithoutDataBinding() throws Exception {

        try {
            ObjectMapperFactory.create(Map.class, new ByteArrayInputStream(new byte[0]));
            fail("preferenceが指定されていないため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:InputStream", e.getMessage(), is("can not find config. class = [java.util.Map]"));
        }

        try {
            ObjectMapperFactory.create(Map.class, new StringReader("1,2"));
            fail("preferenceが指定されていないため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:Reader", e.getMessage(), is("can not find config. class = [java.util.Map]"));
        }

        try {
            ObjectMapperFactory.create(Map.class, "12345,12345");
            fail("preferenceが指定されていないため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:String", e.getMessage(), is("can not find config. class = [java.util.Map]"));
        }

        try {
            ObjectMapperFactory.create(Map.class, new ByteArrayOutputStream());
            fail("preferenceが指定されていないため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:OutputStream", e.getMessage(), is("can not find config. class = [java.util.Map]"));
        }

        try {
            ObjectMapperFactory.create(Map.class, new StringWriter());
            fail("preferenceが指定されていないため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:Writer", e.getMessage(), is("can not find config. class = [java.util.Map]"));
        }
    }

    /**
     * パラメータにnullを指定した場合に例外を送出すること
     */
    @Test
    public void create_class_null() throws Exception {

        try {
            ObjectMapperFactory.create(null, new ByteArrayInputStream(new byte[0]), CsvDataBindConfig.DEFAULT);
            fail("classがNULLのため、例外が発生する");
        } catch (Exception e) {
            assertThat("class", e, instanceOf(NullPointerException.class));
        }

        try {
            ObjectMapperFactory.create(Map.class, (Reader) null, CsvDataBindConfig.DEFAULT);
            fail("readerがNULLのため、例外が発生する");
        } catch (Exception e) {
            assertThat("reader", e, instanceOf(NullPointerException.class));
        }

        try {
            ObjectMapperFactory.create(CsvBean.class, (Writer) null);
            fail("writerがNULLのため、例外が発生する");
        } catch (Exception e) {
            assertThat("writer", e, instanceOf(NullPointerException.class));
        }

        try {
            ObjectMapperFactory.create(Map.class, new ByteArrayInputStream(new byte[0]), null);
            fail("preferenceがNULLのため、例外が発生する");
        } catch (Exception e) {
            assertThat("config", e, instanceOf(IllegalArgumentException.class));
            assertThat("config", e.getMessage(),
                    is("Unsupported config or class. class = [java.util.Map], config = [null]"));
        }
    }

    /**
     * CSV用アノテーションを持たず、かつMap以外のクラスを指定しているので、例外が発生すること
     */
    @Test
    public void createCsvBeanMapper_object() throws Exception {

        try {
            ObjectMapperFactory.create(Object.class, new ByteArrayInputStream(new byte[0]), CsvDataBindConfig.DEFAULT);
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:InputStream", e.getMessage(),
                    is("this class should not be set config. class = [java.lang.Object]"));
        }

        try {
            ObjectMapperFactory.create(Object.class, new StringReader("1,2"), CsvDataBindConfig.EXCEL);
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:Reader", e.getMessage(),
                    is("this class should not be set config. class = [java.lang.Object]"));
        }

        try {
            ObjectMapperFactory.create(Object.class, "12345,12345", CsvDataBindConfig.RFC4180);
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:String", e.getMessage(),
                    is("this class should not be set config. class = [java.lang.Object]"));
        }

        try {
            ObjectMapperFactory.create(Object.class, new ByteArrayOutputStream(), CsvDataBindConfig.TSV);
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:OutputStream", e.getMessage(),
                    is("this class should not be set config. class = [java.lang.Object]"));
        }

        try {
            ObjectMapperFactory.create(Object.class, new StringWriter(), CsvDataBindConfig.DEFAULT);
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:Writer", e.getMessage(),
                    is("this class should not be set config. class = [java.lang.Object]"));
        }

        try {
            ObjectMapperFactory.create(Object.class, new ByteArrayInputStream(new byte[0]));
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:InputStream", e.getMessage(),
                    is("can not find config. class = [java.lang.Object]"));
        }

        try {
            ObjectMapperFactory.create(Object.class, new StringReader("1,2"));
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:Reader", e.getMessage(),
                    is("can not find config. class = [java.lang.Object]"));
        }

        try {
            ObjectMapperFactory.create(Object.class, "12345,12345");
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:String", e.getMessage(),
                    is("can not find config. class = [java.lang.Object]"));
        }

        try {
            ObjectMapperFactory.create(Object.class, new ByteArrayOutputStream());
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:OutputStream", e.getMessage(),
                    is("can not find config. class = [java.lang.Object]"));
        }

        try {
            ObjectMapperFactory.create(Object.class, new StringWriter());
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:Writer", e.getMessage(),
                    is("can not find config. class = [java.lang.Object]"));
        }
    }

    /**
     * {@link ObjectMapperFactory#create(Class, InputStream)}の例外ケース
     */
    @Test
    public void create_class_input_failed() throws Exception {
        final ObjectMapperFactory sut = new ObjectMapperFactory();

        new Expectations(sut, DataBindUtil.class) {{
            DataBindUtil.createDataBindConfig(Object.class);
            result = CsvDataBindConfig.DEFAULT;
            sut.toMapperType(Object.class, CsvDataBindConfig.DEFAULT);
            result = null;
        }};

        try {
            sut.createMapper(Object.class, new ByteArrayInputStream(new byte[0]));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link ObjectMapperFactory#create(Class, Reader)}の例外ケース
     */
    @Test
    public void create_class_reader_failed() throws Exception {
        final ObjectMapperFactory sut = new ObjectMapperFactory();

        new Expectations(sut, DataBindUtil.class) {{
            DataBindUtil.createDataBindConfig(Object.class);
            result = CsvDataBindConfig.DEFAULT;
            sut.toMapperType(Object.class, CsvDataBindConfig.DEFAULT);
            result = null;
        }};

        try {
            sut.createMapper(Object.class, new StringReader(""));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link ObjectMapperFactory#create(Class, InputStream, DataBindConfig)}の例外ケース
     */
    @Test
    public void create_class_input_config_failed() throws Exception {
        final ObjectMapperFactory sut = new ObjectMapperFactory();

        new Expectations(sut) {{
            sut.toMapperType(Map.class, CsvDataBindConfig.DEFAULT);
            result = null;
        }};

        try {
            sut.createMapper(Map.class, new ByteArrayInputStream(new byte[0]), CsvDataBindConfig.DEFAULT);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link ObjectMapperFactory#create(Class, Reader, DataBindConfig)}の例外ケース
     */
    @Test
    public void create_class_reader_config_failed() throws Exception {
        final ObjectMapperFactory sut = new ObjectMapperFactory();

        new Expectations(sut) {{
            sut.toMapperType(Object.class, CsvDataBindConfig.DEFAULT);
            result = null;
        }};

        try {
            sut.createMapper(Object.class, new StringReader(""), CsvDataBindConfig.DEFAULT);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link ObjectMapperFactory#create(Class, OutputStream)}の例外ケース
     */
    @Test
    public void crate_class_output_failed() throws Exception {
        final ObjectMapperFactory sut = new ObjectMapperFactory();

        new Expectations(sut, DataBindUtil.class) {{
            DataBindUtil.createDataBindConfig(Object.class);
            result = CsvDataBindConfig.DEFAULT;
            sut.toMapperType(Object.class, CsvDataBindConfig.DEFAULT);
            result = null;
        }};

        try {
            sut.createMapper(Object.class, new ByteArrayOutputStream());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link ObjectMapperFactory#create(Class, Writer)}の例外ケース
     */
    @Test
    public void crate_class_writer() throws Exception {
        final ObjectMapperFactory sut = new ObjectMapperFactory();

        new Expectations(sut, DataBindUtil.class) {{
            DataBindUtil.createDataBindConfig(Object.class);
            result = CsvDataBindConfig.DEFAULT;
            sut.toMapperType(Object.class, CsvDataBindConfig.DEFAULT);
            result = null;
        }};

        try {
            sut.createMapper(Object.class, new StringWriter());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link ObjectMapperFactory#create(Class, OutputStream, DataBindConfig)}の例外ケース
     */
    @Test
    public void crate_class_output_config_failed() throws Exception {
        final ObjectMapperFactory sut = new ObjectMapperFactory();

        new Expectations(sut) {{
            sut.toMapperType(Object.class, CsvDataBindConfig.DEFAULT);
            result = null;
        }};

        try {
            sut.createMapper(Object.class, new ByteArrayOutputStream(), CsvDataBindConfig.DEFAULT);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link ObjectMapperFactory#create(Class, Writer, DataBindConfig)}の例外ケース
     */
    @Test
    public void crate_class_writer_config_failed() throws Exception {
        final ObjectMapperFactory sut = new ObjectMapperFactory();

        new Expectations(sut) {{
            sut.toMapperType(Object.class, CsvDataBindConfig.DEFAULT);
            result = null;
        }};

        try {
            sut.createMapper(Object.class, new StringWriter(), CsvDataBindConfig.DEFAULT);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
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
                objects.put("objectMapperFactory", new ObjectMapperFactory() {
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
