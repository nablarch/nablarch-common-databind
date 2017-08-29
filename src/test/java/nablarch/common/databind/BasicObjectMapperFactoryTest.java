package nablarch.common.databind;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
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
import java.nio.charset.Charset;
import java.util.Map;

import nablarch.common.databind.csv.Csv;
import nablarch.common.databind.csv.CsvDataBindConfig;
import nablarch.common.databind.csv.CsvMapMapper;
import nablarch.common.databind.csv.MapCsvMapper;
import nablarch.common.databind.fixedlength.Field;
import nablarch.common.databind.fixedlength.FixedLength;
import nablarch.common.databind.fixedlength.FixedLengthDataBindConfigBuilder;
import nablarch.common.databind.fixedlength.converter.Rpad;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@link BasicObjectMapperFactory}のテスト。
 */
public class BasicObjectMapperFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    /** テスト対象 */
    private final BasicObjectMapperFactory sut = new BasicObjectMapperFactory();

    /**
     * CSV用アノテーションを持つBeanを指定しているが{@link DataBindConfig}を指定しているので、例外が発生すること
     */
    @Test
    public void createCsvBeanMapper_preference() throws Exception {

        try {
            sut.createMapper(CsvBean.class, new ByteArrayInputStream(new byte[0]), CsvDataBindConfig.DEFAULT);
            fail("configが指定されているため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:InputStream", e.getMessage(),
                    is("this class should not be set config. class = [nablarch.common.databind.BasicObjectMapperFactoryTest$CsvBean]"));
        }

        try {
            sut.createMapper(CsvBean.class, new StringReader("1,2"), CsvDataBindConfig.EXCEL);
            fail("configが指定されているため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:Reader", e.getMessage(),
                    is("this class should not be set config. class = [nablarch.common.databind.BasicObjectMapperFactoryTest$CsvBean]"));
        }

        try {
            sut.createMapper(CsvBean.class, new ByteArrayOutputStream(), CsvDataBindConfig.TSV);
            fail("configが指定されているため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:OutputStream", e.getMessage(),
                    is("this class should not be set config. class = [nablarch.common.databind.BasicObjectMapperFactoryTest$CsvBean]"));
        }

        try {
            sut.createMapper(CsvBean.class, new StringWriter(), CsvDataBindConfig.DEFAULT);
            fail("configが指定されているため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:Writer", e.getMessage(),
                    is("this class should not be set config. class = [nablarch.common.databind.BasicObjectMapperFactoryTest$CsvBean]"));
        }
    }

    /**
     * Mapインタフェースを指定しているので、{@link CsvMapMapper}が生成されること。
     */
    @Test
    public void createCsvMapMapper() throws Exception {
        assertThat("input:InputStream",
                sut.createMapper(Map.class, new ByteArrayInputStream(new byte[0]),
                        CsvDataBindConfig.EXCEL.withRequiredHeader(true)
                                               .withHeaderTitles("test")),
                is(instanceOf(CsvMapMapper.class)));
        
        assertThat("input:Reader",
                sut.createMapper(Map.class, new StringReader("1,2"),
                        CsvDataBindConfig.EXCEL.withRequiredHeader(true)
                                               .withHeaderTitles("col1", "col2")),
                is(instanceOf(CsvMapMapper.class)));
    }


    /**
     * Mapインタフェースを指定しているので、{@link nablarch.common.databind.csv.MapCsvMapper}が生成されること。
     */
    @Test
    public void testMapCsvMapper() throws Exception {
        assertThat("input:OutputStream",
                sut.createMapper(Map.class, new ByteArrayOutputStream(),
                        CsvDataBindConfig.TSV.withRequiredHeader(true)
                                             .withHeaderTitles("test")),
                is(instanceOf(MapCsvMapper.class)));
        
        assertThat("input:Writer",
                sut.createMapper(Map.class, new StringWriter(),
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
            sut.createMapper(Map.class, new ByteArrayInputStream(new byte[0]));
            fail("configが指定されていないため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:InputStream", e.getMessage(), is("can not find config. class = [java.util.Map]"));
        }

        try {
            sut.createMapper(Map.class, new StringReader("1,2"));
            fail("configが指定されていないため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:Reader", e.getMessage(), is("can not find config. class = [java.util.Map]"));
        }

        try {
            sut.createMapper(Map.class, new ByteArrayOutputStream());
            fail("configが指定されていないため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:OutputStream", e.getMessage(), is("can not find config. class = [java.util.Map]"));
        }

        try {
            sut.createMapper(Map.class, new StringWriter());
            fail("configが指定されていないため、例外が発生する");
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
            sut.createMapper(null, new ByteArrayInputStream(new byte[0]), CsvDataBindConfig.DEFAULT.withHeaderTitles("test"));
            fail("classがNULLのため、例外が発生する");
        } catch (Exception e) {
            assertThat("class", e, instanceOf(NullPointerException.class));
        }

        try {
            sut.createMapper(Map.class, (Reader) null, CsvDataBindConfig.DEFAULT.withHeaderTitles("test"));
            fail("readerがNULLのため、例外が発生する");
        } catch (Exception e) {
            assertThat("reader", e, instanceOf(NullPointerException.class));
        }

        try {
            sut.createMapper(CsvBean.class, (Writer) null);
            fail("writerがNULLのため、例外が発生する");
        } catch (Exception e) {
            assertThat("writer", e, instanceOf(NullPointerException.class));
        }

        try {
            sut.createMapper(Map.class, new ByteArrayInputStream(new byte[0]), null);
            fail("configがNULLのため、例外が発生する");
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
            sut.createMapper(Object.class, new ByteArrayInputStream(new byte[0]), CsvDataBindConfig.DEFAULT);
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:InputStream", e.getMessage(),
                    is("this class should not be set config. class = [java.lang.Object]"));
        }

        try {
            sut.createMapper(Object.class, new StringReader("1,2"), CsvDataBindConfig.EXCEL);
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:Reader", e.getMessage(),
                    is("this class should not be set config. class = [java.lang.Object]"));
        }

        try {
            sut.createMapper(Object.class, new ByteArrayOutputStream(), CsvDataBindConfig.TSV);
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:OutputStream", e.getMessage(),
                    is("this class should not be set config. class = [java.lang.Object]"));
        }

        try {
            sut.createMapper(Object.class, new StringWriter(), CsvDataBindConfig.DEFAULT);
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalArgumentException e) {
            assertThat("input:Writer", e.getMessage(),
                    is("this class should not be set config. class = [java.lang.Object]"));
        }

        try {
            sut.createMapper(Object.class, new ByteArrayInputStream(new byte[0]));
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:InputStream", e.getMessage(),
                    is("can not find config. class = [java.lang.Object]"));
        }

        try {
            sut.createMapper(Object.class, new StringReader("1,2"));
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:Reader", e.getMessage(),
                    is("can not find config. class = [java.lang.Object]"));
        }

        try {
            sut.createMapper(Object.class, new ByteArrayOutputStream());
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:OutputStream", e.getMessage(),
                    is("can not find config. class = [java.lang.Object]"));
        }

        try {
            sut.createMapper(Object.class, new StringWriter());
            fail("Object.classを渡したため、例外が発生する");
        } catch (IllegalStateException e) {
            assertThat("input:Writer", e.getMessage(),
                    is("can not find config. class = [java.lang.Object]"));
        }
    }

    /**
     * {@link BasicObjectMapperFactory#create(Class, String)}の例外ケース
     */
    @Test
    public void create_class_input_failed() throws Exception {
        final BasicObjectMapperFactory sut = new BasicObjectMapperFactory() {
            @Override
            protected MapperType toMapperType(final Class<?> clazz, final DataBindConfig dataBindConfig) {
                return null;
            }
        };

        try {
            sut.createMapper(CsvBean.class, new ByteArrayInputStream(new byte[0]));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link BasicObjectMapperFactory#createMapper(Class, Reader)}の例外ケース
     */
    @Test
    public void create_class_reader_failed() throws Exception {
        final BasicObjectMapperFactory sut = new BasicObjectMapperFactory() {
            @Override
            protected MapperType toMapperType(final Class<?> clazz, final DataBindConfig dataBindConfig) {
                return null;
            }
        };

        try {
            sut.createMapper(CsvBean.class, new StringReader(""));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link BasicObjectMapperFactory#createMapper(Class, InputStream, DataBindConfig)}の例外ケース
     */
    @Test
    public void create_class_input_config_failed() throws Exception {
        final BasicObjectMapperFactory sut = new BasicObjectMapperFactory() {
            @Override
            protected MapperType toMapperType(final Class<?> clazz, final DataBindConfig dataBindConfig) {
                return null;
            }
        };

        try {
            sut.createMapper(Map.class, new ByteArrayInputStream(new byte[0]), CsvDataBindConfig.DEFAULT);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link BasicObjectMapperFactory#createMapper(Class, Reader, DataBindConfig)}の例外ケース
     */
    @Test
    public void create_class_reader_config_failed() throws Exception {
        final BasicObjectMapperFactory sut = new BasicObjectMapperFactory() {
            @Override
            protected MapperType toMapperType(final Class<?> clazz, final DataBindConfig dataBindConfig) {
                return null;
            }
        };

        try {
            sut.createMapper(Object.class, new StringReader(""), CsvDataBindConfig.DEFAULT);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link BasicObjectMapperFactory#createMapper(Class, OutputStream)}の例外ケース
     */
    @Test
    public void crate_class_output_failed() throws Exception {
        final BasicObjectMapperFactory sut = new BasicObjectMapperFactory() {
            @Override
            protected MapperType toMapperType(final Class<?> clazz, final DataBindConfig dataBindConfig) {
                return null;
            }
        };

        try {
            sut.createMapper(CsvBean.class, new ByteArrayOutputStream());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link BasicObjectMapperFactory#createMapper(Class, Writer)}の例外ケース
     */
    @Test
    public void crate_class_writer() throws Exception {
        final BasicObjectMapperFactory sut = new BasicObjectMapperFactory() {
            @Override
            protected MapperType toMapperType(final Class<?> clazz, final DataBindConfig dataBindConfig) {
                return null;
            }
        };

        try {
            sut.createMapper(CsvBean.class, new StringWriter());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link BasicObjectMapperFactory#createMapper(Class, OutputStream, DataBindConfig)}の例外ケース
     */
    @Test
    public void crate_class_output_config_failed() throws Exception {
        final BasicObjectMapperFactory sut = new BasicObjectMapperFactory() {
            @Override
            protected MapperType toMapperType(final Class<?> clazz, final DataBindConfig dataBindConfig) {
                return null;
            }
        };

        try {
            sut.createMapper(CsvBean.class, new ByteArrayOutputStream(), CsvDataBindConfig.DEFAULT);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
        }
    }

    /**
     * {@link BasicObjectMapperFactory#createMapper(Class, Writer, DataBindConfig)}の例外ケース
     */
    @Test
    public void crate_class_writer_config_failed() throws Exception {
        final BasicObjectMapperFactory sut = new BasicObjectMapperFactory() {
            @Override
            protected MapperType toMapperType(final Class<?> clazz, final DataBindConfig dataBindConfig) {
                return null;
            }
        };

        try {
            sut.createMapper(CsvBean.class, new StringWriter(), CsvDataBindConfig.DEFAULT);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unsupported config or class"));
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

    @Test
    public void 固定長からBeanでReaderが指定された場合に例外が送出されること() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("fixed length type does not support reader.");
        sut.createMapper(FixedLengthBean.class, new StringReader("test"));
    }

    @Test
    public void Beanから固定長でWriterが指定された場合に例外が送出されること() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("fixed length type does not support writer.");
        sut.createMapper(FixedLengthBean.class, new StringWriter());
    }

    @Test
    public void 固定長からMapでReaderが指定された場合に例外が送出されること() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("fixed length type does not support reader.");
        sut.createMapper(Map.class, new StringReader("test"),
                FixedLengthDataBindConfigBuilder.newBuilder()
                                                .charset(Charset.forName("ms932"))
                                                .length(10)
                                                .singleLayout()
                                                .field("hoge", 1, 10)
                                                .build()
        );
    }

    @Test
    public void Mapから固定長でWriterが指定された場合に例外が送出されること() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("fixed length type does not support writer.");
        sut.createMapper(Map.class, new StringWriter(),
                FixedLengthDataBindConfigBuilder.newBuilder()
                                                .charset(Charset.forName("ms932"))
                                                .length(10)
                                                .singleLayout()
                                                .field("hoge", 1, 10)
                                                .build()
        );
    }
    
    @Test
    public void 固定長からBean_streamでConfigを明示的に指定した場合例外が送出されること() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("this class should not be set config. class");
        sut.createMapper(FixedLengthBean.class, new ByteArrayInputStream(new byte[0]),
                FixedLengthDataBindConfigBuilder.newBuilder()
                                                .charset(Charset.forName("ms932"))
                                                .length(10)
                                                .singleLayout()
                                                .field("hoge", 1, 10)
                                                .build()
        );
    }
    
    @Test
    public void 固定長からBean_readerでConfigを明示的に指定した場合例外が送出されること() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("this class should not be set config. class");
        sut.createMapper(FixedLengthBean.class, new StringReader(""),
                FixedLengthDataBindConfigBuilder.newBuilder()
                                                .charset(Charset.forName("ms932"))
                                                .length(10)
                                                .singleLayout()
                                                .field("hoge", 1, 10)
                                                .build()
        );
    }

    @Test
    public void Beanから固定長_streamでConfigを明示的に指定した場合例外が送出されること() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("this class should not be set config. class");
        sut.createMapper(FixedLengthBean.class, new ByteArrayOutputStream(),
                FixedLengthDataBindConfigBuilder.newBuilder()
                                                .charset(Charset.forName("ms932"))
                                                .length(10)
                                                .singleLayout()
                                                .field("hoge", 1, 10)
                                                .build()
        );
    }
    
    @Test
    public void Beanから固定長_writerでConfigを明示的に指定した場合例外が送出されること() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("this class should not be set config. class");
        sut.createMapper(FixedLengthBean.class, new StringWriter(),
                FixedLengthDataBindConfigBuilder.newBuilder()
                                                .charset(Charset.forName("ms932"))
                                                .length(10)
                                                .singleLayout()
                                                .field("hoge", 1, 10)
                                                .build()
        );
    }

    @FixedLength(length = 4, charset = "MS932", lineSeparator = "\r\n")
    public static class FixedLengthBean {

        @Field(offset = 1, length = 4)
        @Rpad
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}