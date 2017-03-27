package nablarch.common.databind.csv;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;

import org.junit.Test;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

/**
 * {@link CsvTokenizer}のテスト。
 *
 * ※パース処理の網羅的なテストは{@link CsvDataReaderLineTest}で行っている。
 */
public class CsvTokenizerTest {


    /**
     * 1行のデータを読み込めること
     */
    @Test
    public void next() throws Exception {

        final CsvTokenizer sut = new CsvTokenizer(new BufferedReader(new StringReader("1,\"2\",3")), CsvDataBindConfig.DEFAULT);
        assertThat("1番目の要素:1", sut.next(), is("1"));
        assertThat("2番目の要素:2", sut.next(), is("2"));
        assertThat("3番目の要素:3", sut.next(), is("3"));
        assertThat("おわり", sut.next(), is(nullValue()));
    }

    /**
     * 内部のリーダの未実装メソッドのテスト
     */
    @Test
    public void unsupportedMethod() throws Exception {

        final CsvTokenizer sut = new CsvTokenizer(new BufferedReader(new StringReader("1,\"2\",3")), CsvDataBindConfig.DEFAULT);
        final BufferedReader reader = Deencapsulation.getField(sut, "reader");

        try {
            reader.skip(1);
            fail();
        } catch (UnsupportedOperationException ignore) {
        }

        try {
            reader.read(new char[1]);
            fail();
        } catch (UnsupportedOperationException ignore) {
        }

        try {
            reader.read(new char[1], 1, 1);
            fail();
        } catch (UnsupportedOperationException ignore) {
        }

        try {
            reader.read(CharBuffer.allocate(10));
            fail();
        } catch (UnsupportedOperationException ignore) {
        }

        try {
            reader.readLine();
            fail();
        } catch (UnsupportedOperationException ignore) {
        }
    }

    /**
     * カレントポジションを移動せずに次の一文字が読み取れること
     */
    @Test
    public void reader_readNextCharAndReset() throws Exception {
        final CsvTokenizer sut = new CsvTokenizer(new BufferedReader(new StringReader("1,\"2\",3")), CsvDataBindConfig.DEFAULT);
        final BufferedReader reader = Deencapsulation.getField(sut, "reader");

        int c = Deencapsulation.<Integer>invoke(reader, "readNextCharAndReset");
        assertThat((char) c, is('1'));

        c = reader.read();
        assertThat("カレントが戻っているので同じ文字がリードされる", (char) c, is('1'));

        c = reader.read();
        assertThat("readで次の文字が読める", (char) c, is(','));

    }

    /**
     * 行番号が取れること
     */
    @Test
    public void reader_getLineNumber() throws Exception {
        final CsvTokenizer sut = new CsvTokenizer(new BufferedReader(new StringReader("1\r\n2\r\n3\r\n")), CsvDataBindConfig.DEFAULT);
        final BufferedReader reader = Deencapsulation.getField(sut, "reader");

        long lineNumber = Deencapsulation.<Long>invoke(reader, "getLineNumber");
        assertThat(lineNumber, is(1L));

        reader.read();              // 1
        lineNumber = Deencapsulation.<Long>invoke(reader, "getLineNumber");
        assertThat(lineNumber, is(1L));

        reader.read();              // \r
        lineNumber = Deencapsulation.<Long>invoke(reader, "getLineNumber");
        assertThat(lineNumber, is(1L));

        reader.read();              // \n
        lineNumber = Deencapsulation.<Long>invoke(reader, "getLineNumber");
        assertThat(lineNumber, is(1L));

        reader.read();              // 2
        lineNumber = Deencapsulation.<Long>invoke(reader, "getLineNumber");
        assertThat(lineNumber, is(2L));
    }

    /**
     * 行番号の取得に失敗するケース
     */
    @Test(expected = RuntimeException.class)
    public void reader_getLineNumber_fail(@Mocked final BufferedReader mockReader) throws Exception {
        new Expectations() {{
            mockReader.read();
            result = new IOException("io error");
        }};

        final CsvTokenizer sut = new CsvTokenizer(mockReader, CsvDataBindConfig.DEFAULT);

        final BufferedReader reader = Deencapsulation.getField(sut, "reader");
        Deencapsulation.setField(reader, "lastChar", '\r');
        Deencapsulation.<Integer>invoke(reader, "getLineNumber");
    }
}