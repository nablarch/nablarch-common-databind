package nablarch.common.databind.fixedlength;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.nio.charset.Charset;

import nablarch.common.databind.fixedlength.converter.DefaultConverter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FixedLengthDataBindConfigBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void 設定した情報をもとにコンフィグを生成できること() throws Exception {
        final FixedLengthDataBindConfig config = FixedLengthDataBindConfigBuilder
                .newBuilder()
                .lineSeparator("\r\n")
                .length(128)
                .charset(Charset.forName("MS932"))
                .addRecord(new RecordBuilder().addField("test", 1, 128).build())
                .build();

        assertThat(config.getLineSeparator(), is("\r\n"));
        assertThat(config.getLength(), is(128));
        assertThat(config.getCharset(), is(Charset.forName("MS932")));
        assertThat(config.getRecordConfig().getFieldConfigList(), contains(
                allOf(
                        hasProperty("name", is("test")),
                        hasProperty("offset", is(1)),
                        hasProperty("length", is(128)),
                        hasProperty("fieldConverter", instanceOf(DefaultConverter.class))
                )
        ));
    }

    @Test
    public void 固定長の桁数が定義されていない場合に例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("length is invalid. must set greater than 0.");
        FixedLengthDataBindConfigBuilder
                .newBuilder()
                .lineSeparator("\r\n")
                .charset(Charset.forName("MS932"))
                .addRecord(new RecordBuilder().addField("test", 1, 128).build())
                .build();
    }

    @Test
    public void フィールドのオフセットの定義が不正な場合に例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("field offset is invalid. record_name:single, field_name:text, expected offset:9 but was 2");
        FixedLengthDataBindConfigBuilder
                .newBuilder()
                .lineSeparator("\r\n")
                .length(128)
                .charset(Charset.forName("MS932"))
                .addRecord(new RecordBuilder().addField("test", 1, 8).addField("text", 2, 120).build())
                .build();
    }

    @Test
    public void フィールドの桁数の定義が不正な場合に例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("field length is invalid. record_name:single, field_name:text, expected length:120 but was 119");
        FixedLengthDataBindConfigBuilder
                .newBuilder()
                .lineSeparator("\r\n")
                .length(128)
                .charset(Charset.forName("MS932"))
                .addRecord(new RecordBuilder().addField("test", 1, 8).addField("text", 9, 119).build())
                .build();
    }

    @Test
    public void レコード定義が未定義の場合に例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("record config is undefined.");
        FixedLengthDataBindConfigBuilder
                .newBuilder()
                .lineSeparator("\r\n")
                .length(128)
                .charset(Charset.forName("MS932"))
                .build();
    }

    @Test
    public void レコード定義にフィールド情報が未定義の場合に例外が送出されること() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("field was not found. record_name:single");
        FixedLengthDataBindConfigBuilder
                .newBuilder()
                .lineSeparator("\r\n")
                .length(128)
                .charset(Charset.forName("MS932"))
                .addRecord(new RecordBuilder().build())
                .build();
    }
}