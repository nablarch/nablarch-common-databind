package nablarch.common.databind.csv;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;
import nablarch.common.databind.ObjectMapperFactory;

import org.junit.Rule;
import org.junit.Test;

/**
 * {@link MapCsvMapper}のテストクラス。
 */
public class MapCsvMapperTest {

    @Rule
    public CsvResource resource = new CsvResource("test.csv", "utf-8", "\r\n");

    /**
     * CSVに1レコード書き込めること。
     *
     * @throws Exception
     */
    @Test
    public void testWrite_single() throws Exception {
        StringWriter writer = new StringWriter();
        final ObjectMapper<Map> mapper = ObjectMapperFactory.create(Map.class, writer,
                CsvDataBindConfig.DEFAULT.withHeaderTitles(new String[]{"年齢", "氏名"}));

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("年齢",20);
        map.put("氏名", "山田太郎");
        mapper.write(map);
        mapper.close();

        assertThat("CSVが書き込まれていること", readFile(new StringReader(writer.toString())),
                is("年齢,氏名\r\n20,山田太郎\r\n"));
    }

    /**
     * CSVに複数レコード書き込めること。
     *
     * @throws Exception
     */
    @Test
    public void testWrite_multi() throws Exception {
        StringWriter writer = new StringWriter();
        final ObjectMapper<Map> mapper = ObjectMapperFactory.create(Map.class, writer,
                CsvDataBindConfig.DEFAULT.withHeaderTitles(new String[]{"年齢", "氏名"}));

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("年齢",20);
        map.put("氏名", "山田太郎");
        mapper.write(map);

        map = new HashMap<String, Object>();
        map.put("年齢",25);
        map.put("氏名", "田中次郎");
        mapper.write(map);

        map = new HashMap<String, Object>();
        map.put("年齢", 30);
        map.put("氏名", "鈴木三郎");
        mapper.write(map);

        mapper.close();

        assertThat("CSVが書き込まれていること", readFile(new StringReader(writer.toString())),
                is("年齢,氏名\r\n20,山田太郎\r\n25,田中次郎\r\n30,鈴木三郎\r\n"));
    }

    /**
     * ヘッダなしの設定の場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testWrite_header() throws Exception {
        StringWriter writer = new StringWriter();

        try {
            ObjectMapperFactory.create(Map.class, writer,
                    CsvDataBindConfig.DEFAULT.withRequiredHeader(false));
            fail("ヘッダなしが設定されたため、例外が発生");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("csv header is required."));
        }
    }

    /**
     * ヘッダーありの設定だがヘッダが未設定(null)の場合例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testWrite_null_header() throws Exception {
        StringWriter writer = new StringWriter();

        try {
            ObjectMapperFactory.create(Map.class, writer,
                    CsvDataBindConfig.DEFAULT.withRequiredHeader(true).withHeaderTitles(null));
            fail("ヘッダなしが設定されたため、例外が発生");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("csv header is required."));
        }
    }

    /**
     * ヘッダありの設定かつヘッダが設定されていない場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testWrite_empty_header() throws Exception {
        StringWriter writer = new StringWriter();

        try {
            ObjectMapperFactory.create(Map.class, writer,
                    CsvDataBindConfig.DEFAULT.withRequiredHeader(true));
            fail("ヘッダが設定されていないため、例外が発生");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("csv header is required."));
        }
    }

    /**
     * テストで出力されたファイルを読み込む。
     *
     * @param reader リソース
     * @return 読み込んだ結果
     */
    private String readFile(Reader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        int read;
        while ((read = reader.read()) != -1) {
            sb.append((char) read);
        }
        reader.close();
        return sb.toString();
    }

}