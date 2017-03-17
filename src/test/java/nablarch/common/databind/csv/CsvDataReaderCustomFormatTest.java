package nablarch.common.databind.csv;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsArrayContainingInOrder.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import nablarch.common.databind.InvalidDataFormatException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * 任意のフォーマットを指定してCSVファイルが読み込めることをテストするクラス。
 */
@RunWith(Enclosed.class)
public class CsvDataReaderCustomFormatTest {

    /**
     * 囲み文字がシングルクォートのファイルのケース。
     */
    public static class SingleQuotedFile {

        @Rule
        public CsvResource resource = new CsvResource("test.csv", "utf-8", "\r\n");

        /**
         * シングルクォートで囲まれた要素が読み込めること。
         *
         * @throws Exception
         */
        @Test
        public void testReadLine() throws Exception {
            resource.writeLine("1,'2',3");
            resource.writeLine("4,'5',6");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.DEFAULT.withQuote('\''));
            assertThat("行が読み取れること", sut.read(), is(new String[] {"1", "2", "3"}));
            assertThat("行が読み取れること", sut.read(), is(new String[] {"4", "5", "6"}));
            assertThat("ファイルの終わりに達したのでnull", sut.read(), is(nullValue()));
        }

        /**
         * シングルクォートで囲まれた文字列も読み込めること。
         * <p/>
         * <ul>
         * <li>シングルクォートは除去される</li>
         * <li>シングルクォートのエスケープも処理できる</li>
         * <li>クォート内に改行がある場合でもそれが読み取れること</li>
         * </ul>
         */
        @Test
        public void testQuotedString() throws Exception {
            resource.writeLine("'1','2','3'");                    // 1,2,3
            resource.writeLine("'1\r0','2''\r\n0','3\n0\"'");   // 1\r0,2'\r\n0,3\n0
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.DEFAULT.withQuote('\''));
            assertThat("1行目", sut.read(), is(new String[] {"1", "2", "3"}));
            assertThat("2行目", sut.read(), is(new String[] {"1\r0", "2'\r\n0", "3\n0\""}));
            assertThat("ファイルの終わりに到達したのでnull", sut.read(), is(nullValue()));
        }

        /**
         * CSVの要素中にクォート文字(')が存在している場合エラーとなること。
         */
        @Test
        public void testContainsQuote() throws Exception {
            resource.writeLine("1,2,3");
            resource.writeLine("4,'5,6");
            resource.writeLine("7,8,9");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.DEFAULT.withQuote('\''));
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
            resource.writeLine("1,'2\r',3");
            resource.writeLine("4,5,6");
            resource.writeLine("7,\r8,9");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.DEFAULT.withQuote('\''));
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
            resource.writeLine("1,'2\n',3");
            resource.writeLine("4,\n5,6");
            resource.writeLine("7,8,9");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(), CsvDataBindConfig.DEFAULT.withQuote('\''));
            assertThat("1行目は問題なく読み込める", sut.read().length, is(3));

            try {
                sut.read();
                fail("フォーマット不正なので例外が発生する。");
            } catch (InvalidDataFormatException e) {
                assertThat("行番号がメッセージに含まれていること",
                        e.getMessage(), containsString("line number = [3]"));
            }
        }
    }

    /**
     * 改行コードがCRのTSVファイル(タブ区切り)のケース
     */
    public static class CrTsvFile {
        @Rule
        public CsvResource resource = new CsvResource("test.tsv", "utf-8", "\r");

        /**
         * CR区切りのファイルが読み込めること
         *
         * @throws Exception
         */
        @Test
        public void testReadLine() throws Exception {
            resource.writeLine("1\t2\t3");
            resource.writeLine("4\t5\t6");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(),
                    CsvDataBindConfig.TSV.withQuote('\'').withLineSeparator("\r"));
            assertThat("行が読み取れること", sut.read(), is(new String[] {"1", "2", "3"}));
            assertThat("行が読み取れること", sut.read(), is(new String[] {"4", "5", "6"}));
            assertThat("ファイルの終わりに達したのでnull", sut.read(), is(nullValue()));
        }

        /**
         * 要素中に不正な改行文字(CRLF)が存在している場合、エラーとなること。
         */
        @Test
        public void testContainsCRLF() throws Exception {
            resource.writeLine("1\t'2\r'\t3");
            resource.writeLine("4\t'5\n'\t6");
            resource.writeLine("7\t\r\n8\t9");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(),
                    CsvDataBindConfig.TSV.withQuote('\'').withLineSeparator("\r"));
            assertThat("1行目は問題なく読み込める", sut.read().length, is(3));
            assertThat("2行目は問題なく読み込める", sut.read().length, is(3));

            try {
                sut.read();
                fail("フォーマット不正なので例外が発生する。");
            } catch (InvalidDataFormatException e) {
                assertThat("行番号がメッセージに含まれていること",
                        e.getMessage(), containsString("line number = [5]"));
            }
        }

        /**
         * 要素中に不正な改行文字(LF)が存在している場合、エラーとなること。
         */
        @Test
        public void testContainsLF() throws Exception {
            resource.writeLine("1\t'2\r'\t3");
            resource.writeLine("4\t'5\r\n'\t6");
            resource.writeLine("7\t\n8\t9");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(),
                    CsvDataBindConfig.TSV.withQuote('\'').withLineSeparator("\r"));
            assertThat("1行目は問題なく読み込める", sut.read().length, is(3));
            assertThat("2行目は問題なく読み込める", sut.read().length, is(3));

            try {
                sut.read();
                fail("フォーマット不正なので例外が発生する。");
            } catch (InvalidDataFormatException e) {
                assertThat("行番号がメッセージに含まれていること",
                        e.getMessage(), containsString("line number = [5]"));
            }
        }
    }

    /**
     * 改行コードがLFのCSVファイルのケース
     */
    public static class LfCsvFile {
        @Rule
        public CsvResource resource = new CsvResource("test.csv", "utf-8", "\n");

        /**
         * CR区切りのファイルが読み込めること
         *
         * @throws Exception
         */
        @Test
        public void testReadLine() throws Exception {
            resource.writeLine("1,2,3");
            resource.writeLine("4,5,6");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(),
                    CsvDataBindConfig.RFC4180.withLineSeparator("\n"));
            assertThat("行が読み取れること", sut.read(), is(new String[] {"1", "2", "3"}));
            assertThat("行が読み取れること", sut.read(), is(new String[] {"4", "5", "6"}));
            assertThat("ファイルの終わりに達したのでnull", sut.read(), is(nullValue()));
        }

        /**
         * 要素中に不正な改行文字(CRLF)が存在している場合、エラーとなること。
         */
        @Test
        public void testContainsCRLF() throws Exception {
            resource.writeLine("1,'2\n',3");
            resource.writeLine("4,'5\r\n',6");
            resource.writeLine("7,\r\n8,9");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(),
                    CsvDataBindConfig.RFC4180.withQuote('\'').withLineSeparator("\n"));
            assertThat("1行目は問題なく読み込める", sut.read().length, is(3));
            assertThat("2行目は問題なく読み込める", sut.read().length, is(3));

            try {
                sut.read();
                fail("フォーマット不正なので例外が発生する。");
            } catch (InvalidDataFormatException e) {
                assertThat("行番号がメッセージに含まれていること",
                        e.getMessage(), containsString("line number = [5]"));

            }
        }

        /**
         * 要素中に不正な改行文字(LF)が存在している場合、エラーとなること。
         */
        @Test
        public void testContainsLF() throws Exception {
            resource.writeLine("1,'2\r',3");
            resource.writeLine("4,'5\r\n','6'");
            resource.writeLine("7,\r\n8,9");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(),
                    CsvDataBindConfig.RFC4180.withQuote('\'').withLineSeparator("\n"));
            assertThat("1行目は問題なく読み込める", sut.read().length, is(3));
            assertThat("2行目は問題なく読み込める", sut.read().length, is(3));

            try {
                sut.read();
                fail("フォーマット不正なので例外が発生する。");
            } catch (InvalidDataFormatException e) {
                assertThat("行番号がメッセージに含まれていること",
                        e.getMessage(), containsString("line number = [5]"));
            }
        }
    }

    /**
     * フィールドの区切り文字が、\rなフォーマットのケース。
     */
    public static class CustomFieldSeparator {
        @Rule
        public CsvResource resource = new CsvResource("test.csv", "utf-8", "\r\n");

        /**
         * \r区切りのフィールドが読み取れること
         *
         * @throws Exception
         */
        @Test
        public void testReadLine() throws Exception {
            resource.writeLine("1\r2\r3");
            resource.writeLine("4\r5\r6");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(),
                    CsvDataBindConfig.RFC4180.withFieldSeparator('\r'));
            assertThat("行が読み取れること", sut.read(), is(new String[] {"1", "2", "3"}));
            assertThat("行が読み取れること", sut.read(), is(new String[] {"4", "5", "6"}));
            assertThat("ファイルの終わりに達したのでnull", sut.read(), is(nullValue()));
        }

        /**
         * クォート内にフィールドの区切り文字があっても正しく読み取れること
         */
        @Test
        public void testContainsFieldSeparator() throws Exception {
            resource.writeLine("1\r'2\r'\r3");
            resource.writeLine("4\r'5\r'\r6");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(),
                    CsvDataBindConfig.RFC4180.withFieldSeparator('\r').withQuote('\''));
            assertThat("行が読み取れること", sut.read(), is(new String[] {"1", "2\r", "3"}));
            assertThat("行が読み取れること", sut.read(), is(new String[] {"4", "5\r", "6"}));
            assertThat("ファイルの終わりに達したのでnull", sut.read(), is(nullValue()));

        }

        /**
         * 要素中に不正な改行文字(LF)が存在している場合、エラーとなること。
         */
        @Test
        public void testContainsLF() throws Exception {
            resource.writeLine("1\r'\r\n\n\r'\r3");
            resource.writeLine("2\r111\n222");
            resource.close();

            final CsvDataReader sut = new CsvDataReader(resource.createReader(),
                    CsvDataBindConfig.RFC4180.withQuote('\'').withFieldSeparator('\r'));
            assertThat("1行目は問題なく読み込める", sut.read().length, is(3));

            try {
                sut.read();
                fail("フォーマット不正なので例外が発生する。");
            } catch (InvalidDataFormatException e) {
                assertThat("行番号がメッセージに含まれていること",
                        e.getMessage(), containsString("line number = [8]"));

            }
        }
    }

    /**
     * 空フィールドの扱いのテスト
     */
    public static class EmptyToNull {

        @Rule
        public CsvResource resource = new CsvResource("test.csv", "utf-8", "\r\n");

        @Test
        public void testEmptyToEmpty() throws Exception {
            resource.writeLine("1,2");
            resource.writeLine(",22");
            resource.writeLine("111,");
            resource.close();

            final CsvDataBindConfig config = CsvDataBindConfig.DEFAULT.withEmptyToNull(false);

            final CsvDataReader sut = new CsvDataReader(resource.createReader(), config);

            assertThat("1レコード目", sut.read(), is(arrayContaining("1", "2")));
            assertThat("2レコード目", sut.read(), is(arrayContaining("", "22")));
            assertThat("3レコード目", sut.read(), is(arrayContaining("111", "")));
            assertThat("全て読み終わったのでnull", sut.read(), is(nullValue()));
        }
        
        @Test
        public void testEmptyToNull() throws Exception {
            resource.writeLine("1,2");
            resource.writeLine(",22");
            resource.writeLine("111,");
            resource.close();

            final CsvDataBindConfig config = CsvDataBindConfig.DEFAULT.withEmptyToNull(true);

            final CsvDataReader sut = new CsvDataReader(resource.createReader(), config);

            assertThat("1レコード目", sut.read(), is(arrayContaining("1", "2")));
            assertThat("2レコード目", sut.read(), is(arrayContaining(nullValue(), is("22"))));
            assertThat("3レコード目", sut.read(), is(arrayContaining(is("111"), nullValue())));
            assertThat("全て読み終わったのでnull", sut.read(), is(nullValue()));
        }
    }
}

