package nablarch.common.databind.fixedlength;

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
        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                new FixedLengthDataBindConfigConverter().convert(TestBean.class)
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
        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                new FixedLengthDataBindConfigConverter().convert(TestBeanWithoutLineSeparator.class)
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
        final ObjectMapper<Map> sut =
                ObjectMapperFactory.create(
                        Map.class,
                        inputStream,
                        new FixedLengthDataBindConfigConverter().convert(TestBean.class));
        assertThat(sut.read(), is(nullValue()));
    }

    @Test
    public void 末尾に改行コードがあっても読みこめてMapに変換できること() throws UnsupportedEncodingException {
        final InputStream inputStream = createInputStream("ab  あい003\r\nefg か　000\r\n", "MS932");
        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                new FixedLengthDataBindConfigConverter().convert(TestBean.class)
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
        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                new FixedLengthDataBindConfigConverter().convert(TestBean.class)
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
        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                new FixedLengthDataBindConfigConverter().convert(TestBean.class)
        );
        expectedException.expect(InvalidDataFormatException.class);
        expectedException.expectMessage("data format is invalid. line separator is invalid. line number = [1]");
        sut.read();
    }

    @Test
    public void 最終レコードの改行が実際より短い場合は例外が送出されること() throws Exception {
        final InputStream inputStream = createInputStream("ab  あい003\r\nefg か　000\n", "MS932");
        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                new FixedLengthDataBindConfigConverter().convert(TestBean.class)
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
        final ObjectMapper<Map> sut = ObjectMapperFactory.create(
                Map.class,
                inputStream,
                new FixedLengthDataBindConfigConverter().convert(TestBean.class)
        );
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("unsupported write method.");
        sut.write(null);
    }

    private InputStream createInputStream(String text, String charset) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(text.getBytes(charset));
    }
}

@FixedLength(length = 11, charset = "MS932", lineSeparator = "\r\n")
class TestBean {

    private String name;
    private String text;
    private int age;

    @Field(offset = 1, length = 4)
    @Rpad
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Field(offset = 5, length = 4)
    @Rpad('　')
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Field(offset = 9, length = 3)
    @Lpad
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

@FixedLength(length = 11, charset = "MS932", lineSeparator = "")
class TestBeanWithoutLineSeparator {

    private String name;
    private String text;
    private int age;

    @Field(offset = 1, length = 4)
    @Rpad
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Field(offset = 5, length = 4)
    @Rpad('　')
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Field(offset = 9, length = 3)
    @Lpad
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
