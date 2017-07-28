package nablarch.common.databind.fixedlength;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.Charset;

import nablarch.common.databind.DataBindConfig;
import nablarch.common.databind.fixedlength.converter.Lpad;
import nablarch.common.databind.fixedlength.converter.Rpad;
import nablarch.core.beans.BeansException;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@link FixedLengthDataBindConfigConverter}のテスト。
 */
public class FixedLengthDataBindConfigConverterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /** テスト対象 */
    private final FixedLengthDataBindConfigConverter sut = new FixedLengthDataBindConfigConverter();

    @Test
    public void FixedLengthアノテーションからConfigへの変換ができること() throws Exception {
        final DataBindConfig actual = sut.convert(FixedLengthBean.class);

        // assert file
        assertThat(actual, instanceOf(FixedLengthDataBindConfig.class));
        assertThat(actual, allOf(
                hasProperty("length", is(1024)),
                hasProperty("charset", is(Charset.forName("MS932"))),
                hasProperty("lineSeparator", is("\n")),
                hasProperty("multiLayoutConfig", is(nullValue()))
        ));

        // assert record
        final RecordConfig recordConfig = ((FixedLengthDataBindConfig) actual).getRecordConfig(RecordConfig.SINGLE_LAYOUT_RECORD_NAME);
        assertThat("フィールド数は2", recordConfig.getFieldConfigList(), hasSize(2));
        assertThat("field:1", recordConfig.getFieldConfigList()
                                          .get(0),
                allOf(
                        hasProperty("name", is("type")),
                        hasProperty("offset", is(1)),
                        hasProperty("length", is(10))
                ));
        assertThat("field:2", recordConfig.getFieldConfigList()
                                          .get(1),
                allOf(
                        hasProperty("name", is("other")),
                        hasProperty("offset", is(11)),
                        hasProperty("length", is(1014))
                ));
    }

    @Test
    public void レコード長が0以下の場合は例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("length is invalid. must set greater than 0.");
        sut.convert(InvalidRecordLength.class);
    }

    @Test
    public void レコードにフィールドが存在しない場合例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("field was not found. record_name:single");
        sut.convert(EmptyField.class);
    }
    
    @Test
    public void フィールドのオフセットが前のフィールドの終了位置より小さい場合例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
                "field offset is invalid. record_name:single, field_name:other, expected offset:11 but was 5");
        sut.convert(InvalidOffsetField2.class);
    }

    @Test
    public void 最後のレコードの長さがレコード長を超えている場合は例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
                "field length is invalid. record_name:single, field_name:type, expected length:10 but was 11");
        sut.convert(InvalidLengthField.class);
    }

    @Test
    public void 複数のフィールドコンバータを設定した場合例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("multiple field converters can not be set. field_name:name");
        sut.convert(MultipleConverter.class);
    }

    @Test
    public void フィールドコンバータのインスタンス生成に失敗した場合例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("instance creation failed. " +
                "class:nablarch.common.databind.fixedlength.FixedLengthDataBindConfigConverterTest$NewInstanceFail$NewInstanceFailConverter");
        expectedException.expectCause(Matchers.<Throwable>instanceOf(BeansException.class));
        sut.convert(NewInstanceFailConverter.class);
    }

    @Test
    public void マルチレイアウトの場合にマルチレイアウトコンフィグが生成されること() throws Exception {
        final DataBindConfig actual = sut.convert(MultiLayoutBean.class);

        assertThat(actual, instanceOf(FixedLengthDataBindConfig.class));
        assertThat(actual, allOf(
                hasProperty("length", is(8)),
                hasProperty("charset", is(Charset.forName("MS932"))),
                hasProperty("lineSeparator", is("\r\n")),
                hasProperty("multiLayoutConfig", is(notNullValue()))
        ));

        final FixedLengthDataBindConfig config = (FixedLengthDataBindConfig) actual;
        final RecordConfig header = config.getRecordConfig("header");
        assertThat(header.getFieldConfigList(), contains(
                allOf(
                        hasProperty("name", is("id")),
                        hasProperty("offset", is(1)),
                        hasProperty("length", is(1))
                ),
                allOf(
                        hasProperty("name", is("field")),
                        hasProperty("offset", is(2)),
                        hasProperty("length", is(7))
                )
        ));
        final RecordConfig data = config.getRecordConfig("data");
        assertThat(data.getFieldConfigList(), contains(
                allOf(
                        hasProperty("name", is("id")),
                        hasProperty("offset", is(1)),
                        hasProperty("length", is(1))
                ),
                allOf(
                        hasProperty("name", is("name")),
                        hasProperty("offset", is(2)),
                        hasProperty("length", is(4))
                ),
                allOf(
                        hasProperty("name", is("age")),
                        hasProperty("offset", is(6)),
                        hasProperty("length", is(3))
                )
        ));
    }

    @Test
    public void fillCharが設定された場合にコンフィグが生成できること() throws Exception {
        final DataBindConfig actual = sut.convert(FillBean.class);

        // assert file
        assertThat(actual, instanceOf(FixedLengthDataBindConfig.class));
        assertThat(actual, allOf(
                hasProperty("length", is(24)),
                hasProperty("charset", is(Charset.forName("MS932"))),
                hasProperty("lineSeparator", is("\n")),
                hasProperty("fillChar", is('0')),
                hasProperty("multiLayoutConfig", is(nullValue()))
        ));

        // assert record
        final RecordConfig recordConfig = ((FixedLengthDataBindConfig) actual).getRecordConfig(RecordConfig.SINGLE_LAYOUT_RECORD_NAME);
        assertThat("フィールド数は2", recordConfig.getFieldConfigList(), hasSize(2));
        assertThat("field:1", recordConfig.getFieldConfigList()
                        .get(0),
                allOf(
                        hasProperty("name", is("name")),
                        hasProperty("offset", is(5)),
                        hasProperty("length", is(10))
                ));
        assertThat("field:2", recordConfig.getFieldConfigList()
                        .get(1),
                allOf(
                        hasProperty("name", is("age")),
                        hasProperty("offset", is(18)),
                        hasProperty("length", is(3))
                ));
    }

    @Test
    public void マルチレイアウトでMultiLayoutを継承していない場合に例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("bean class must inherit nablarch.common.databind.fixedlength.MultiLayout. " +
                "bean_class:nablarch.common.databind.fixedlength.FixedLengthDataBindConfigConverterTest$InValidMultiLayoutBean");
        sut.convert(InValidMultiLayoutBean.class);
    }

    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public static class FixedLengthBean {

        @Field(offset = 1, length = 10)
        @Rpad
        private String type;

        @Field(offset = 11, length = 1014)
        @Lpad
        private String other;
        private String notField;

        public String getType() {
            return type;
        }

        public String getOther() {
            return other;
        }

        public String getNotField() {
            return notField;
        }
    }
    
    @FixedLength(length = 0, charset = "MS932", lineSeparator = "")
    public static class InvalidRecordLength {}
    
    @FixedLength(length = 1, charset = "invalid", lineSeparator = "")
    public static class InvalidCharset {}

    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public static class EmptyField {
    }

    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public static class InvalidOffsetField {

        @Field(offset = 1, length = 10)
        private String type;

        @Field(offset = 12, length = 1014)
        private String other;

        public String getType() {
            return type;
        }

        public String getOther() {
            return other;
        }
    }
    
    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public static class InvalidOffsetField2 {

        @Field(offset = 1, length = 10)
        private String type;

        @Field(offset = 5, length = 1014)
        private String other;

        public String getType() {
            return type;
        }

        public String getOther() {
            return other;
        }
    }

    @FixedLength(length = 10, charset = "MS932", lineSeparator = "\n")
    public static class InvalidLengthField {

        @Field(offset = 1, length = 11)
        private String type;

        private String other;

        public String getType() {
            return type;
        }
    }

    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public class MultipleConverter {
        @Field(offset = 1, length = 1024)
        @Rpad
        @Lpad
        private String name;

        public String getName() {
            return name;
        }
    }

    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public class NoConstructorConverter {
        @Field(offset = 1, length = 1024)
        @NoConstructor
        private String name;

        public String getName() {
            return name;
        }
    }

    @FieldConvert(NoConstructor.NoConstructorConverter.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NoConstructor {

        class NoConstructorConverter implements FieldConvert.FieldConverter<NoConstructor> {


            @Override
            public void initialize(NoConstructor annotation) {}

            @Override
            public Object convertOfRead(final FixedLengthDataBindConfig fixedLengthDataBindConfig, final FieldConfig fieldConfig, final byte[] input) {
                return input;
            }

            @Override
            public byte[] convertOfWrite(final FixedLengthDataBindConfig fixedLengthDataBindConfig, final FieldConfig fieldConfig, final Object output) {
                return (byte[]) output;
            }
        }
    }

    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public class NewInstanceFailConverter {
        @Field(offset = 1, length = 1024)
        @NewInstanceFail
        private String name;

        public String getName() {
            return name;
        }
    }

    @FieldConvert(NewInstanceFail.NewInstanceFailConverter.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NewInstanceFail {

        class NewInstanceFailConverter implements FieldConvert.FieldConverter<NewInstanceFail> {

            public NewInstanceFailConverter() {
                throw new RuntimeException("fail");
            }

            @Override
            public void initialize(NewInstanceFail annotation) {

            }

            @Override
            public Object convertOfRead(final FixedLengthDataBindConfig fixedLengthDataBindConfig, final FieldConfig fieldConfig, final byte[] input) {
                return input;
            }

            @Override
            public byte[] convertOfWrite(final FixedLengthDataBindConfig fixedLengthDataBindConfig, final FieldConfig fieldConfig, final Object output) {
                return (byte[]) output;
            }
        }
    }

    @FixedLength(length = 8, charset = "MS932", lineSeparator = "\r\n", multiLayout = true)
    public static class MultiLayoutBean extends MultiLayout {

        @Record
        private Header header;

        @Record
        private Data data;

        public Header getHeader() {
            return header;
        }

        public void setHeader(Header header) {
            this.header = header;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        @Override
        public MultiLayoutConfig.RecordIdentifier getRecordIdentifier() {
            return null;
        }
    }

    public static class Header {

        @Field(offset = 1, length = 1)
        private Long id;

        @Rpad
        @Field(offset = 2, length = 7)
        private String field;

        public Long getId() {
            return id;
        }


        public void setId(Long id) {
            this.id = id;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }

    public static class Data {

        @Field(offset = 1, length = 1)
        private Long id;

        @Rpad
        @Field(offset = 2, length = 4)
        private String name;

        @Lpad
        @Field(offset = 6, length = 3)
        private Long age;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getAge() {
            return age;
        }

        public void setAge(Long age) {
            this.age = age;
        }
    }

    @FixedLength(length = 8, charset = "MS932", lineSeparator = "\r\n", multiLayout = true)
    public static class InValidMultiLayoutBean {

        @Record
        private Header header;

        @Record
        private Data data;

        public Header getHeader() {
            return header;
        }

        public void setHeader(Header header) {
            this.header = header;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }
    }

    @FixedLength(length = 24, charset = "MS932", lineSeparator = "\n", fillChar = '0')
    public static class FillBean {

        @Field(offset = 5, length = 10)
        private String name;

        @Field(offset = 18, length = 3)
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

}
