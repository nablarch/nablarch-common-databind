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
    public void フィールドのオフセットが前のフィールドの終了位置より大きい場合例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
                "field offset is invalid. record_name:single, field_name:other, expected offset:11 but was 12");
        sut.convert(InvalidOffsetField.class);
    }
    
    @Test
    public void フィールドのオフセットが前のフィールドの終了位置より小さい場合例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
                "field offset is invalid. record_name:single, field_name:other, expected offset:11 but was 5");
        sut.convert(InvalidOffsetField2.class);
    }

    @Test
    public void 最後のレコードの長さがレコード長に満たない場合は例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
                "field length is invalid. record_name:single, field_name:type, expected length:1024 but was 10");
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
    public void マルチレイアウトでMultiLayoutを継承していない場合に例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("bean class must inherit nablarch.common.databind.fixedlength.MultiLayout. " +
                "bean_class:nablarch.common.databind.fixedlength.FixedLengthDataBindConfigConverterTest$InValidMultiLayoutBean");
        sut.convert(InValidMultiLayoutBean.class);
    }

    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public static class FixedLengthBean {

        private String type;

        private String other;
        private String notField;

        @Field(offset = 1, length = 10)
        @Rpad
        public String getType() {
            return type;
        }

        @Field(offset = 11, length = 1014)
        @Lpad
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

        private String type;

        private String other;

        @Field(offset = 1, length = 10)
        public String getType() {
            return type;
        }

        @Field(offset = 12, length = 1014)
        public String getOther() {
            return other;
        }
    }
    
    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public static class InvalidOffsetField2 {

        private String type;

        private String other;

        @Field(offset = 1, length = 10)
        public String getType() {
            return type;
        }

        @Field(offset = 5, length = 1014)
        public String getOther() {
            return other;
        }
    }

    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public static class InvalidLengthField {

        private String type;

        private String other;

        @Field(offset = 1, length = 10)
        public String getType() {
            return type;
        }
    }

    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public class MultipleConverter {
        private String name;

        @Field(offset = 1, length = 1024)
        @Rpad
        @Lpad
        public String getName() {
            return name;
        }
    }

    @FixedLength(length = 1024, charset = "MS932", lineSeparator = "\n")
    public class NoConstructorConverter {
        private String name;

        @Field(offset = 1, length = 1024)
        @NoConstructor
        public String getName() {
            return name;
        }
    }

    @FieldConvert(NoConstructor.NoConstructorConverter.class)
    @Target(ElementType.METHOD)
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
        private String name;

        @Field(offset = 1, length = 1024)
        @NewInstanceFail
        public String getName() {
            return name;
        }
    }

    @FieldConvert(NewInstanceFail.NewInstanceFailConverter.class)
    @Target(ElementType.METHOD)
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

        private Header header;

        private Data data;

        @Record
        public Header getHeader() {
            return header;
        }

        public void setHeader(Header header) {
            this.header = header;
        }

        @Record
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

        private Long id;

        private String field;

        @Field(offset = 1, length = 1)
        public Long getId() {
            return id;
        }


        public void setId(Long id) {
            this.id = id;
        }

        @Rpad
        @Field(offset = 2, length = 7)
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }

    public static class Data {

        private Long id;

        private String name;

        private Long age;

        @Field(offset = 1, length = 1)
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        @Rpad
        @Field(offset = 2, length = 4)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Lpad
        @Field(offset = 6, length = 3)
        public Long getAge() {
            return age;
        }

        public void setAge(Long age) {
            this.age = age;
        }
    }

    @FixedLength(length = 8, charset = "MS932", lineSeparator = "\r\n", multiLayout = true)
    public static class InValidMultiLayoutBean {

        private Header header;

        private Data data;

        @Record
        public Header getHeader() {
            return header;
        }

        public void setHeader(Header header) {
            this.header = header;
        }

        @Record
        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }
    }

}
