package nablarch.common.databind.fixedlength;

import nablarch.common.databind.DataBindConfig;
import nablarch.common.databind.InvalidDataFormatException;
import nablarch.common.databind.ObjectMapper;
import nablarch.common.databind.ObjectMapperFactory;
import nablarch.common.databind.fixedlength.converter.Lpad;
import nablarch.common.databind.fixedlength.converter.Rpad;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

    private InputStream inputStream;

    private ObjectMapper<Map> sut;

    @After
    public void tearDown() throws Exception {
        inputStream.close();
        sut.close();
    }

    @Test
    public void シンプルな固定長をMapに変換できること() throws UnsupportedEncodingException {
        inputStream = new ByteArrayInputStream("ab  あい003\r\nefg か　000".getBytes("MS932"));

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .singleLayout()
                        .field("name", 1, 4, new Rpad.RpadConverter(' '))
                        .field("text", 5, 4, new Rpad.RpadConverter('　'))
                        .field("age", 9, 3, new Lpad.LpadConverter('0'))
                        .build();

        sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        Map<String, ?> map = sut.read();
        assertThat(map.get("name").toString(), is("ab"));
        assertThat(map.get("text").toString(), is("あい"));
        assertThat(map.get("age").toString(), is("3"));

        map = sut.read();
        assertThat(map.get("name").toString(), is("efg"));
        assertThat(map.get("text").toString(), is("か"));
        assertThat(map.get("age").toString(), isEmptyString());

        map = sut.read();
        assertThat(map, nullValue());
        sut.close();
    }

    @Test
    public void マルチレイアウトの固定長をMapに変換できること() throws Exception {
        inputStream = new ByteArrayInputStream("1test   \r\n2aaa 012\r\n2bb  345".getBytes("MS932"));

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(8)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .multiLayout()
                        .record("header")
                        .field("id", 1, 1)
                        .field("field", 2, 7, new Rpad.RpadConverter(' '))
                        .record("data")
                        .field("id", 1, 1)
                        .field("name", 2, 4, new Rpad.RpadConverter(' '))
                        .field("age", 6, 3, new Lpad.LpadConverter('0'))
                        .recordIdentifier(new MultiLayoutConfig.RecordIdentifier() {
                            @Override
                            public MultiLayoutConfig.RecordName identifyRecordName(byte[] record) {
                                return record[0] == 0x31 ? RecordType.HEADER : RecordType.DATA;
                            }
                        })
                        .build();

        sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        Map<String, ?> map = sut.read();
        assertThat(map.get("recordName"), Matchers.<Object>is(RecordType.HEADER));
        final Map<String, ?> header = (Map<String, ?>) map.get("header");
        assertThat(header.get("id").toString(), is("1"));
        assertThat(header.get("field").toString(), is("test"));

        map = sut.read();
        assertThat(map.get("recordName"), Matchers.<Object>is(RecordType.DATA));
        Map<String, ?> data = (Map<String, ?>) map.get("data");
        assertThat(data.get("id").toString(), is("2"));
        assertThat(data.get("name").toString(), is("aaa"));
        assertThat(data.get("age").toString(), is("12"));

        map = sut.read();
        assertThat(map.get("recordName"), Matchers.<Object>is(RecordType.DATA));
        data = (Map<String, ?>) map.get("data");
        assertThat(data.get("id").toString(), is("2"));
        assertThat(data.get("name").toString(), is("bb"));
        assertThat(data.get("age").toString(), is("345"));

        map = sut.read();
        assertThat(map, nullValue());
        sut.close();
    }

    @Test
    public void 改行コードの存在しないデータでもMapに変換できること() throws Exception {
        inputStream = new ByteArrayInputStream("ab  あい003efg か　000".getBytes("MS932"));

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("")
                        .singleLayout()
                        .field("name", 1, 4, new Rpad.RpadConverter(' '))
                        .field("text", 5, 4, new Rpad.RpadConverter('　'))
                        .field("age", 9, 3, new Lpad.LpadConverter('0'))
                        .build();

        sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );

        Map<String, ?> map = sut.read();
        assertThat(map.get("name").toString(), is("ab"));
        assertThat(map.get("text").toString(), is("あい"));
        assertThat(map.get("age").toString(), is("3"));

        map = sut.read();
        assertThat(map.get("name").toString(), is("efg"));
        assertThat(map.get("text").toString(), is("か"));
        assertThat(map.get("age").toString(), isEmptyString());
        sut.close();
    }

    @Test
    public void 空のInputStreamでも読みこめること() throws Exception {
        inputStream = new ByteArrayInputStream("".getBytes("MS932"));

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .singleLayout()
                        .field("name", 1, 4, new Rpad.RpadConverter(' '))
                        .field("text", 5, 4, new Rpad.RpadConverter('　'))
                        .field("age", 9, 3, new Lpad.LpadConverter('0'))
                        .build();
        sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig);
        assertThat(sut.read(), is(nullValue()));
    }

    @Test
    public void 末尾に改行コードがあっても読みこめてMapに変換できること() throws UnsupportedEncodingException {
        inputStream = new ByteArrayInputStream("ab  あい003\r\nefg か　000\r\n".getBytes("MS932"));

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .singleLayout()
                        .field("name", 1, 4, new Rpad.RpadConverter(' '))
                        .field("text", 5, 4, new Rpad.RpadConverter('　'))
                        .field("age", 9, 3, new Lpad.LpadConverter('0'))
                        .build();

        sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        Map<String, ?> map = sut.read();
        assertThat(map.get("name").toString(), is("ab"));
        assertThat(map.get("text").toString(), is("あい"));
        assertThat(map.get("age").toString(), is("3"));

        map = sut.read();
        assertThat(map.get("name").toString(), is("efg"));
        assertThat(map.get("text").toString(), is("か"));
        assertThat(map.get("age").toString(), isEmptyString());
        sut.close();
    }

    @Test
    public void レコードの途中でEOFになる場合は例外が送出されること() throws Exception {
        inputStream = new ByteArrayInputStream("ab  あい003\r\ninvalid".getBytes("MS932"));

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .singleLayout()
                        .field("name", 1, 4, new Rpad.RpadConverter(' '))
                        .field("text", 5, 4, new Rpad.RpadConverter('　'))
                        .field("age", 9, 3, new Lpad.LpadConverter('0'))
                        .build();

        sut = ObjectMapperFactory.create(
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
        inputStream = new ByteArrayInputStream("ab  あい003--efg か　000".getBytes("MS932"));
        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .singleLayout()
                        .field("name", 1, 4, new Rpad.RpadConverter(' '))
                        .field("text", 5, 4, new Rpad.RpadConverter('　'))
                        .field("age", 9, 3, new Lpad.LpadConverter('0'))
                        .build();

        sut = ObjectMapperFactory.create(
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
        inputStream = new ByteArrayInputStream("ab  あい003\r\nefg か　000\n".getBytes("MS932"));

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .singleLayout()
                        .field("name", 1, 4, new Rpad.RpadConverter(' '))
                        .field("text", 5, 4, new Rpad.RpadConverter('　'))
                        .field("age", 9, 3, new Lpad.LpadConverter('0'))
                        .build();

        sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        Map<String, ?> map = sut.read();
        assertThat(map.get("name").toString(), is("ab"));
        assertThat(map.get("text").toString(), is("あい"));
        assertThat(map.get("age").toString(), is("3"));
        expectedException.expect(InvalidDataFormatException.class);
        expectedException.expectMessage("data format is invalid. line separator is invalid. line number = [2]");
        sut.read();
    }

    @Test
    public void writerメソッドはサポートしない例外が送出されること() throws Exception {
        inputStream = new ByteArrayInputStream(("ab  あい003\r\nefg か　000\r\n").getBytes("MS932"));

        final DataBindConfig dataBindConfig =
                FixedLengthDataBindConfigBuilder
                        .newBuilder()
                        .length(11)
                        .charset(Charset.forName("MS932"))
                        .lineSeparator("\r\n")
                        .singleLayout()
                        .field("name", 1, 4, new Rpad.RpadConverter(' '))
                        .field("text", 5, 4, new Rpad.RpadConverter('　'))
                        .field("age", 9, 3, new Lpad.LpadConverter('0'))
                        .build();

        sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                dataBindConfig
        );
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("unsupported write method.");
        sut.write(null);
    }

    enum RecordType implements MultiLayoutConfig.RecordName {
        HEADER {
            @Override
            public String getRecordName() {
                return "header";
            }
        },
        DATA {
            @Override
            public String getRecordName() {
                return "data";
            }
        }
    }
}