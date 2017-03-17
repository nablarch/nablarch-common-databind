package nablarch.common.databind.csv;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;

import nablarch.common.databind.InvalidDataFormatException;

import org.junit.Rule;
import org.junit.Test;

/**
 * {@link CsvDataBindConfig#TSV}を指定した場合のパーサーのテストクラス。
 */
public class CsvDataReaderTsvFormatTest {

    @Rule
    public CsvResource resource = new CsvResource("test.csv", "utf-8", "\r\n");

    /**
     * 改行コードのテスト。
     * 複数行あった場合でも正しく読み込めること。
     */
    @Test
    public void testLineSeparator() throws Exception {
        resource.writeLine("1\t2\t3");
        resource.writeLine("10\t20\t30");
        resource.close();

        final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.TSV);

        final String[] line1 = sut.read();
        assertThat("要素数は3", line1.length, is(3));
        assertThat(line1[0], is("1"));
        assertThat(line1[1], is("2"));
        assertThat(line1[2], is("3"));
        final String[] line2 = sut.read();
        assertThat("要素数は3", line2.length, is(3));
        assertThat(line2[0], is("10"));
        assertThat(line2[1], is("20"));
        assertThat(line2[2], is("30"));

        final String[] line3 = sut.read();
        assertThat("ファイルの終わりに到達したのでnull", line3, is(nullValue()));
    }

    /**
     * 途中に空行がある場合のテスト。
     *
     * 空行は、要素0の配列として読み込まれること。
     */
    @Test
    public void testEmptyLine() throws Exception {
        resource.writeLine("1\t2\t3");
        resource.writeLine("");
        resource.writeLine("10\t20\t30");
        resource.writeLine("");
        resource.close();

        final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.TSV);
        assertThat("要素数は3", sut.read(), is(new String[] {"1", "2", "3"}));
        assertThat("空行なので要素数は1", sut.read(), is(new String[] {null}));
        assertThat("要素数は3", sut.read(), is(new String[] {"10", "20", "30"}));
        assertThat("空行なので要素数は1", sut.read(), is(new String[] {null}));
        assertThat("ファイルの終わりに到達したのでnull", sut.read(), is(nullValue()));
    }

    /**
     * ダブルクォートで囲まれた文字列も読み込めること。
     *
     * <ul>
     *     <li>ダブルクォートは除去される</li>
     *     <li>ダブルクォートのエスケープも処理できる</li>
     *     <li>クォート内に改行がある場合でもそれが読み取れること</li>
     * </ul>
     */
    @Test
    public void testQuotedString() throws Exception {
        resource.writeLine("\"1\"\t\"2\"\t\"3\"");                    // 1,2,3
        resource.writeLine("\"1\r\n0\"\t\"2\"\"\r\n0\"\t\"3\n0\"");   // 1\r0,2"\r\n0,3\n0
        resource.close();

        final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.TSV);
        final String[] line1 = sut.read();
        assertThat("要素数は3", line1.length, is(3));
        assertThat(line1[0], is("1"));
        assertThat(line1[1], is("2"));
        assertThat(line1[2], is("3"));
        final String[] line2 = sut.read();
        assertThat("要素数は3", line2.length, is(3));
        assertThat(line2[0], is("1\r\n0"));
        assertThat(line2[1], is("2\"\r\n0"));
        assertThat(line2[2], is("3\n0"));
        final String[] line3 = sut.read();
        assertThat("ファイルの終わりに到達したのでnull", line3, is(nullValue()));
    }

    /**
     * 現在のレコード番号を取得できること
     */
    @Test
    public void testGetLineNumber() throws Exception {
        resource.writeLine("1\t2\t3");
        resource.writeLine("4\t5\t6");
        resource.writeLine("7\t8\t9");
        resource.close();

        final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.TSV);
        sut.read();
        assertThat("レコード番号は1", sut.getLineNumber(), is(1L));
        sut.read();
        assertThat("レコード番号は2", sut.getLineNumber(), is(2L));
        sut.read();
        assertThat("レコード番号は3", sut.getLineNumber(), is(3L));
    }

    /**
     * CSVの要素中にクォート文字(")が存在している場合エラーとなること。
     */
    @Test
    public void testContainsQuote() throws Exception {
        resource.writeLine("1\t2\t3");
        resource.writeLine("4\t\"5\t6");
        resource.writeLine("7\t8\t9");
        resource.close();

        final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.TSV);
        assertThat("1行目は問題なく読み込める", sut.read().length, is(3));

        try {
            sut.read();
            fail("フォーマット不正なので例外が発生する。");
        } catch (InvalidDataFormatException e) {
            assertThat("行番号がメッセージに含まれていること",
                    e.getMessage(), containsString("line number = [2]"));
            assertThat("引用符が閉じられていないことがメッセージに含まれていること",
                    e.getMessage(), is(containsString("EOF reached before quoted token finished.")));
        }
    }

    /**
     * 要素中に不正な改行文字(CR)が存在している場合、エラーとなること。
     */
    @Test
    public void testContainsCR() throws Exception {
        resource.writeLine("1\t\"2\r\"\t3");
        resource.writeLine("4\t5\t6");
        resource.writeLine("7\t\r8\t9");
        resource.close();

        final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.TSV);
        assertThat("1行目は問題なく読み込める", sut.read().length, is(3));
        assertThat("2行目は問題なく読み込める", sut.read().length, is(3));

        try {
            sut.read();
            fail("フォーマット不正なので例外が発生する。");
        } catch (InvalidDataFormatException e) {
            assertThat("行番号がメッセージに含まれていること",
                    e.getMessage(), containsString("line number = [4]"));
        }
    }

    /**
     * 要素中に不正な改行文字(LF)が存在している場合、エラーとなること。
     */
    @Test
    public void testContainsLF() throws Exception {
        resource.writeLine("1\t\"2\n\"\t3");
        resource.writeLine("4\t\n5\t6");
        resource.writeLine("7\t8\t9");
        resource.close();

        final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.TSV);
        assertThat("1行目は問題なく読み込める", sut.read().length, is(3));

        try {
            sut.read();
            fail("フォーマット不正なので例外が発生する。");
        } catch (InvalidDataFormatException e) {
            assertThat("行番号がメッセージに含まれていること",
                    e.getMessage(), containsString("line number = [3]"));
        }
    }

    /**
     * リーダーがクローズされている場合、エラーとなること。
     */
    @Test
    public void testReaderClosed() throws Exception {
        resource.writeLine("1,2,3");
        resource.writeLine("4,5,6");
        resource.writeLine("7,8,9");
        resource.close();

        BufferedReader reader = resource.createReader();
        reader.close();

        final CsvDataReader sut = new CsvDataReader(reader, CsvDataBindConfig.TSV);

        try {
            sut.read();
            fail("クローズされているため、エラーが発生する");
        } catch (RuntimeException e) {
            assertThat("想定したメッセージであること", e.getMessage(), is("failed to read file."));
            assertThat("IOExceptionが発生していること", e.getCause(), instanceOf(IOException.class));
        }
    }
}
