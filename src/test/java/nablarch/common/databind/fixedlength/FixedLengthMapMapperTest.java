package nablarch.common.databind.fixedlength;

import nablarch.common.databind.DataBindConfig;
import nablarch.common.databind.InvalidDataFormatException;
import nablarch.common.databind.ObjectMapper;
import nablarch.common.databind.ObjectMapperFactory;
import nablarch.common.databind.fixedlength.converter.Lpad;
import nablarch.common.databind.fixedlength.converter.Rpad;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * {@link FixedLengthMapMapper}のテストクラス
 */
public class FixedLengthMapMapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void シンプルな固定長をMapに変換できること() throws UnsupportedEncodingException {
        final InputStream inputStream = createInputStream("ab  あい003\r\nefg か　000", "MS932");

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .addRecord(
                                new RecordConfig(
                                        Arrays.asList(
                                                new FieldConfig("name", 1, 4,
                                                        new Rpad.RpadConverter(' ')),
                                                new FieldConfig("text", 5, 4,
                                                        new Rpad.RpadConverter('　')),
                                                new FieldConfig("age", 9, 3,
                                                        new Lpad.LpadConverter('0'))
                                        )
                                )
                        )
                        .build();

        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        Map<String, ?> map = sut.read();
        assertThat(map.get("name").toString(), is("ab"));
        assertThat(map.get("text").toString(), is("あい"));
        assertThat(Integer.valueOf(map.get("age").toString()), is(3));

        map = sut.read();
        assertThat(map.get("name").toString(), is("efg"));
        assertThat(map.get("text").toString(), is("か"));
        assertThat(map.get("age").toString(), isEmptyString());
        sut.close();
    }

    @Test
    public void 改行コードの存在しないデータでもMapに変換できること() throws Exception {
        final InputStream inputStream = createInputStream("ab  あい003efg か　000", "MS932");

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("")
                        .addRecord(
                                new RecordConfig(
                                        Arrays.asList(
                                                new FieldConfig("name", 1, 4,
                                                        new Rpad.RpadConverter(' ')),
                                                new FieldConfig("text", 5, 4,
                                                        new Rpad.RpadConverter('　')),
                                                new FieldConfig("age", 9, 3,
                                                        new Lpad.LpadConverter('0'))
                                        )
                                )
                        )
                        .build();

        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );

        Map<String, ?> map = sut.read();
        assertThat(map.get("name").toString(), is("ab"));
        assertThat(map.get("text").toString(), is("あい"));
        assertThat(Integer.valueOf(map.get("age").toString()), is(3));

        map = sut.read();
        assertThat(map.get("name").toString(), is("efg"));
        assertThat(map.get("text").toString(), is("か"));
        assertThat(map.get("age").toString(), isEmptyString());
        sut.close();
    }

    @Test
    public void 空のInputStreamでも読みこめること() throws Exception {
        final InputStream inputStream = createInputStream("", "MS932");

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .addRecord(
                                new RecordConfig(
                                        Arrays.asList(
                                                new FieldConfig("name", 1, 4,
                                                        new Rpad.RpadConverter(' ')),
                                                new FieldConfig("text", 5, 4,
                                                        new Rpad.RpadConverter('　')),
                                                new FieldConfig("age", 9, 3,
                                                        new Lpad.LpadConverter('0'))
                                        )
                                )
                        )
                        .build();
        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig);
        assertThat(sut.read(), is(nullValue()));
    }

    @Test
    public void 末尾に改行コードがあっても読みこめてMapに変換できること() throws UnsupportedEncodingException {
        final InputStream inputStream = createInputStream("ab  あい003\r\nefg か　000\r\n", "MS932");

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .addRecord(
                                new RecordConfig(
                                        Arrays.asList(
                                                new FieldConfig("name", 1, 4,
                                                        new Rpad.RpadConverter(' ')),
                                                new FieldConfig("text", 5, 4,
                                                        new Rpad.RpadConverter('　')),
                                                new FieldConfig("age", 9, 3,
                                                        new Lpad.LpadConverter('0'))
                                        )
                                )
                        )
                        .build();

        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        Map<String, ?> map = sut.read();
        assertThat(map.get("name").toString(), is("ab"));
        assertThat(map.get("text").toString(), is("あい"));
        assertThat(Integer.valueOf(map.get("age").toString()), is(3));

        map = sut.read();
        assertThat(map.get("name").toString(), is("efg"));
        assertThat(map.get("text").toString(), is("か"));
        assertThat(map.get("age").toString(), isEmptyString());
        sut.close();
    }

    @Test
    public void レコードの途中でEOFになる場合は例外が送出されること() throws Exception {
        final InputStream inputStream = createInputStream("ab  あい003\r\ninvalid", "MS932");

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .addRecord(
                                new RecordConfig(
                                        Arrays.asList(
                                                new FieldConfig("name", 1, 4,
                                                        new Rpad.RpadConverter(' ')),
                                                new FieldConfig("text", 5, 4,
                                                        new Rpad.RpadConverter('　')),
                                                new FieldConfig("age", 9, 3,
                                                        new Lpad.LpadConverter('0'))
                                        )
                                )
                        )
                        .build();

        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        sut.read();
        expectedException.expect(InvalidDataFormatException.class);
        expectedException.expectMessage("data format is invalid. last record is short. line number = [2]");
        sut.read();
    }

    @Test
    public void 実際の改行コードが設定と異なる場合は例外が送出されること() throws Exception {
        // 改行コード部分に\r\nではなく、「--」を設定
        final InputStream inputStream = createInputStream("ab  あい003--efg か　000", "MS932");
        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .addRecord(
                                new RecordConfig(
                                        Arrays.asList(
                                                new FieldConfig("name", 1, 4,
                                                        new Rpad.RpadConverter(' ')),
                                                new FieldConfig("text", 5, 4,
                                                        new Rpad.RpadConverter('　')),
                                                new FieldConfig("age", 9, 3,
                                                        new Lpad.LpadConverter('0'))
                                        )
                                )
                        )
                        .build();
        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        expectedException.expect(InvalidDataFormatException.class);
        expectedException.expectMessage("data format is invalid. line separator is invalid. line number = [1]");
        sut.read();
    }

    @Test
    public void 最終レコードの改行が実際より短い場合は例外が送出されること() throws Exception {
        final InputStream inputStream = createInputStream("ab  あい003\r\nefg か　000\n", "MS932");

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .addRecord(
                                new RecordConfig(
                                        Arrays.asList(
                                                new FieldConfig("name", 1, 4,
                                                        new Rpad.RpadConverter(' ')),
                                                new FieldConfig("text", 5, 4,
                                                        new Rpad.RpadConverter('　')),
                                                new FieldConfig("age", 9, 3,
                                                        new Lpad.LpadConverter('0'))
                                        )
                                )
                        )
                        .build();

        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        Map<String, ?> map = sut.read();
        assertThat(map.get("name").toString(), is("ab"));
        assertThat(map.get("text").toString(), is("あい"));
        assertThat(Integer.valueOf(map.get("age").toString()), is(3));
        expectedException.expect(InvalidDataFormatException.class);
        expectedException.expectMessage("data format is invalid. line separator is invalid. line number = [2]");
        sut.read();
    }

    @Test
    public void writerメソッドはサポートしない例外が送出されること() throws Exception {
        final InputStream inputStream = createInputStream("ab  あい003\r\nefg か　000\r\n", "MS932");

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .addRecord(
                                new RecordConfig(
                                        Arrays.asList(
                                                new FieldConfig("name", 1, 4,
                                                        new Rpad.RpadConverter(' ')),
                                                new FieldConfig("text", 5, 4,
                                                        new Rpad.RpadConverter('　')),
                                                new FieldConfig("age", 9, 3,
                                                        new Lpad.LpadConverter('0'))
                                        )
                                )
                        )
                        .build();

        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("unsupported write method.");
        sut.write(null);
    }

    private InputStream createInputStream(String text, String charset) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(text.getBytes(charset));
    }
}