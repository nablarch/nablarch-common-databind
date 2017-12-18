package nablarch.common.databind.csv;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import nablarch.common.databind.InvalidDataFormatException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * {@link CsvDataReader}の1レコードをパースする部分のテストクラス。
 */
@RunWith(Parameterized.class)
public class CsvDataReaderLineTest {

    @Parameters
    public static List<Param[]> parameters() {
        // @formatter:off
        return Arrays.asList(
                //------------------------------ valid case
                new Param[] {new Param("空の場合", "", (String[]) null)},
                new Param[] {new Param("1カラムの場合", "1カラム",  new String[] {"1カラム"})},
                new Param[] {new Param("2カラムの場合", "1カラム,2カラム",  new String[] {"1カラム", "2カラム"})},
                new Param[] {new Param("空要素がある場合", ",,", new String[] {null, null, null})},
                new Param[] {new Param("クォートで囲まれている場合", "\"囲まれている\"",  new String[] {"囲まれている"})},
                new Param[] {new Param("ダブルクォート内でダブルクォートがエスケープされている", "\"エスケープ有り→\"\"←これエスケープ\"",  new String[] {"エスケープ有り→\"←これエスケープ"})},
                new Param[] {new Param("ダブルクォート内にCRが存在している", "\"CR→\r←CR\"",  new String[] {"CR→\r←CR"})},
                new Param[] {new Param("ダブルクォート内にLFが存在している", "\"LF→\n←LF\"",  new String[] {"LF→\n←LF"})},
                new Param[] {new Param("ダブルクォート内にCRLFが存在している", "\"CRLF→\r\n←CRLF\"",  new String[] {"CRLF→\r\n←CRLF"})},
                // esc"lf\nesc"crlf\r\nesc"
                new Param[] {new Param("ダブルクォート内にエスケーや改行が複数ある", "\"esc\"\"lf\nesc\"\"crlf\r\nesc\"\"\"",  new String[] {"esc\"lf\nesc\"crlf\r\nesc\""})},
                new Param[] {new Param("ダブルクォート有りの2カラムの場合", "\"1カラム\",\"2カラム\"",  new String[] {"1カラム", "2カラム"})},
                //------------------------------ invalid case
                new Param[] {new Param("ダブルクォートのみの場合", "\"",new InvalidDataFormatException("EOF reached before quoted token finished.", 1L))},
                new Param[] {new Param("ダブルクォートで囲まれていないのにダブルクォートがある", "NGパターン\"", new InvalidDataFormatException("invalid quote character.", 1L))},
                new Param[] {new Param("許容されない改行(CR)の場合", "1,\r", new InvalidDataFormatException("invalid line separator.", 1L))},
                new Param[] {new Param("許容されない改行(LF)の場合", "1,\n", new InvalidDataFormatException("invalid line separator.", 1L))},
                new Param[] {new Param("ダブルクォートで囲まれていないのにCRがある", "NG\rNG", new InvalidDataFormatException("invalid line separator.", 1L))},
                new Param[] {new Param("ダブルクォートで囲まれていないのにLFがある", "NG\nNG", new InvalidDataFormatException("invalid line separator.", 1L))},
                new Param[] {new Param("ダブルクォート内でダブルクォートの次に不正な文字がある", "\"\"NGパターン\"", new InvalidDataFormatException("unescaped quote character.", 1L))},
                new Param[] {new Param("スペースの場合", " ", new String[] {" "})},
                new Param[] {new Param("前後にスペースがある場合", "   space   ", new String[] {"   space   "})},
                new Param[] {new Param("ダブルクォート内に空文字", "\"\"", new String[] {""})},
                new Param[] {new Param("ダブルクォート内にカンマ", "\",\",\",,\",\",,,\"", new String[] {",", ",,", ",,,"})},
                new Param[] {new Param("ダブルクォート内に前後にスペースがあるカンマ", "\" , \",\"  ,,  \",\"   ,,,   \"", new String[] {" , ", "  ,,  ", "   ,,,   "})},
                new Param[] {new Param("ダブルクォート内にCSVデータ", "\"a1,b1,c1\",\"\"\"a2\"\",b2,\"\"c2\"\"\"", new String[] {"a1,b1,c1", "\"a2\",b2,\"c2\""})},
                new Param[] {new Param("ダブルクォートありなし混在", "a1,\"b1\",c1", new String[] {"a1", "b1", "c1"})},
                new Param[] {new Param("セパレータの間にスペース", " a1 , b1  ,  c1 ", new String[] {" a1 ", " b1  ", "  c1 "})},
                new Param[] {new Param("セパレータの間にスペースでクォート付き", " \"a1\" , \"b1\"  ,  \"c1\" ", new InvalidDataFormatException("invalid quote character.", 1L))},
                //サロゲート文字対応
                new Param[] {new Param("サロゲート文字がある場合", "🙀",  new String[] {"🙀"})}

        );
        // @formatter:on
    }

    /** テストデータ */
    private Param param;

    public CsvDataReaderLineTest(Param param) {
        this.param = param;
    }

    /**
     * 行をパースできること
     */
    @Test
    public void testSingleColumn() throws Exception {
        System.out.println("test case: " + param.caseText);
        final CsvDataReader sut = new CsvDataReader(createReader(param.input));

        if (param.isNull()) {
            final String[] result = sut.read();
            assertThat("nullが戻されること", result, is(nullValue()));
        } else if (param.isValidCase()) {
            final String[] result = sut.read();
            assertThat("カラムサイズ", result.length, is(param.expected.length));
            for (int i = 0; i < param.expected.length; i++) {
                assertThat("カラム:" + (i + 1), result[i], is(param.expected[i]));
            }
        } else {
            try {
                sut.read();
                fail("NGフォーマットなのでここはとおらない");
            } catch (Exception e) {
                assertThat("例外クラス", e, is(instanceOf(param.expectedException.getClass())));
                assertThat("メッセージ", e.getMessage(), is(param.expectedException.getMessage()));
            }
        }
    }

    private BufferedReader createReader(String text) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes())));
    }

    private static class Param {

        final String caseText;

        final String input;

        final String[] expected;

        final Exception expectedException;

        public Param(String caseText, String input, String[] expected) {
            this.caseText = caseText;
            this.input = input;
            this.expected = expected;
            this.expectedException = null;
        }

        public Param(String caseText, String input, Exception expectedException) {
            this.caseText = caseText;
            this.input = input;
            this.expected = null;
            this.expectedException = expectedException;
        }

        boolean isValidCase() {
            return expectedException == null;
        }

        boolean isNull() {
            return expectedException == null && expected == null;
        }
    }
}