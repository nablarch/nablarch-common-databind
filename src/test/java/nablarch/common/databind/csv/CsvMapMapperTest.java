package nablarch.common.databind.csv;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;

import nablarch.common.databind.DataBindConfig;
import nablarch.common.databind.InvalidDataFormatException;
import nablarch.common.databind.ObjectMapper;
import nablarch.common.databind.ObjectMapperFactory;

import org.junit.Rule;
import org.junit.Test;

/**
 * {@link CsvMapMapper}のテストクラス。
 */
public class CsvMapMapperTest {

    @Rule
    public CsvResource resource = new CsvResource("test.csv", "utf-8", "\r\n");

    /**
     * {@link CsvMapMapper}のコンストラクタに{@link java.io.InputStream}を指定した場合、
     * CSVのレコードを1件読み込めること
     *
     * @throws Exception
     */
    @Test
    public void testRead_inputstream() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.close();

        final ObjectMapper<Map> mapper = ObjectMapperFactory.create(Map.class, resource.createInputStream(),
                CsvDataBindConfig.DEFAULT.withHeaderTitles(new String[]{"年齢", "氏名"}));
        Map<String, String> map  = mapper.read();
        mapper.close();

        assertThat(map.get("年齢"), is("20"));
        assertThat(map.get("氏名"), is("山田太郎"));
    }

    /**
     * {@link CsvMapMapper}のコンストラクタに{@link java.io.Reader}を指定した場合、
     * CSVのレコードを1件読み込めること
     *
     * @throws Exception
     */
    @Test
    public void testRead_reader() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.close();

        final ObjectMapper<Map> mapper = ObjectMapperFactory.create(Map.class, resource.createReader(),
                CsvDataBindConfig.DEFAULT.withHeaderTitles(new String[]{"年齢", "氏名"}));
        Map<String, String> map  = mapper.read();
        mapper.close();

        assertThat(map.get("年齢"), is("20"));
        assertThat(map.get("氏名"), is("山田太郎"));
    }

    /**
     * CSVのレコードを複数件読み込めること
     *
     * @throws Exception
     */
    @Test
    public void testRead_multi() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.writeLine("25,田中次郎");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        final ObjectMapper<Map> mapper = ObjectMapperFactory.create(Map.class, resource.createReader(),
                CsvDataBindConfig.DEFAULT.withHeaderTitles(new String[]{"年齢", "氏名"}));

        Map<String, String> map  = mapper.read();
        assertThat(map.get("年齢"), is("20"));
        assertThat(map.get("氏名"), is("山田太郎"));

        map  = mapper.read();
        assertThat(map.get("年齢"), is("25"));
        assertThat(map.get("氏名"), is("田中次郎"));

        map  = mapper.read();
        assertThat(map.get("年齢"), is("30"));
        assertThat(map.get("氏名"), is("鈴木三郎"));

        map  = mapper.read();
        assertThat(map, is(nullValue()));
    }

    /**
     * 空行を無視する設定で、空行を含むCSVを読み込んだ場合、空行をスキップすること
     *
     * @throws Exception
     */
    @Test
    public void testRead_ignore_empty_line() throws Exception {
        resource.writeLine("");
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.writeLine("");
        resource.writeLine("");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        final ObjectMapper<Map> mapper = ObjectMapperFactory.create(Map.class, resource.createReader(),
                CsvDataBindConfig.DEFAULT.withIgnoreEmptyLine(true).withHeaderTitles(new String[]{"年齢", "氏名"}));
        Map<String, String> map = mapper.read();
        assertThat(map.get("年齢"), is("20"));
        assertThat(map.get("氏名"), is("山田太郎"));

        map  = mapper.read();
        assertThat(map.get("年齢"), is("30"));
        assertThat(map.get("氏名"), is("鈴木三郎"));

        mapper.close();
    }

    /**
     * 空行を無視しない設定で、空行を含むCSVを読み込んだ場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testRead_not_ignore_empty_line() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.writeLine("");
        resource.writeLine("");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        final ObjectMapper<Map> mapper = ObjectMapperFactory.create(Map.class, resource.createReader(),
                CsvDataBindConfig.DEFAULT.withIgnoreEmptyLine(false).withHeaderTitles(new String[]{"年齢", "氏名"}));
        Map<String, String> map = mapper.read();
        assertThat(map.get("年齢"), is("20"));
        assertThat(map.get("氏名"), is("山田太郎"));

        try {
            mapper.read();
            fail("空行をバインドしようとして例外が発生");
        } catch (InvalidDataFormatException e) {
            assertThat(e.getMessage(), containsString("property size does not match."
                    + " expected field count = [2], actual field count = [1]. line number = [3]"));
        }

        mapper.close();
    }

    /**
     * ヘッダなしの設定でCSVを読み込んだ場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testRead_not_exists_header() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.writeLine("25,田中次郎");
        resource.writeLine("30,鈴木三郎");
        resource.close();


        try {
            final ObjectMapper<Map> mapper = ObjectMapperFactory.create(Map.class, resource.createReader(),
                    CsvDataBindConfig.DEFAULT.withRequiredHeader(false));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("this csv is require header."));
        }
    }

    /**
     * ヘッダありの設定かつ、{@link DataBindConfig}に
     * ヘッダが設定されていない状態でCSVを読み込んだ場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testRead_empty_header() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.writeLine("25,田中次郎");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        // ヘッダがnullのケース
        try {
            ObjectMapperFactory.create(Map.class, resource.createReader(),
                    CsvDataBindConfig.DEFAULT.withRequiredHeader(true).withHeaderTitles((String[]) null));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("this csv is require header."));
        }

        // ヘッダが空のケース
        try {
            ObjectMapperFactory.create(Map.class, resource.createReader(),
                    CsvDataBindConfig.DEFAULT.withRequiredHeader(true).withHeaderTitles());
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("this csv is require header."));
        }
    }

    /**
     * プロパティ名配列とレコードの項目数に差異がある場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testRead_mismatch_property_size() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎,mismatch");
        resource.close();

        final ObjectMapper<Map> mapper = ObjectMapperFactory.create(Map.class, resource.createReader(),
                CsvDataBindConfig.DEFAULT.withHeaderTitles(new String[]{"年齢", "氏名"}));

        try {
            mapper.read();
            fail("項目数が異なるため、読み込みできないこと");
        } catch (InvalidDataFormatException e) {
            assertThat(e.getMessage(), containsString("property size does not match."
                    + " expected field count = [2], actual field count = [3]. line number = [2]"));
        } finally {
            mapper.close();
        }
    }

    /**
     * 既にクローズされている場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testRead_closed() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.close();

        // ファイルが存在する場合
        final ObjectMapper<Map> mapper = ObjectMapperFactory.create(Map.class, resource.createReader(),
                CsvDataBindConfig.DEFAULT.withHeaderTitles(new String[]{"年齢", "氏名"}));

        // クローズ前であれば読み込みできること
        Map<String, String> map = mapper.read();
        assertThat(map.get("年齢"), is("20"));
        assertThat(map.get("氏名"), is("山田太郎"));

        mapper.close();
        try {
            mapper.read();
            fail("既にクローズされているため、読み込みできないこと");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("failed to read file."));
        }
    }

}