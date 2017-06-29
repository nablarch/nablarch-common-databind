package nablarch.common.databind.fixedlength;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.nio.charset.Charset;

import kotlin.reflect.jvm.internal.impl.javax.inject.Named;

import nablarch.common.databind.DataBindConfig;
import nablarch.common.databind.fixedlength.converter.Lpad;
import nablarch.common.databind.fixedlength.converter.Rpad;

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
                hasProperty("lineSeparator", is("\n"))
        ));

        // assert record
        final RecordConfig recordConfig = ((FixedLengthDataBindConfig) actual).getRecordConfig();
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
}
