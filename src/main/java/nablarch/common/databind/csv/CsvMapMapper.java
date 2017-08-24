package nablarch.common.databind.csv;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import nablarch.common.databind.InvalidDataFormatException;
import nablarch.core.util.StringUtil;

/**
 * CSVフォーマットと{@link Map}をマッピングするクラス。
 *
 * @author Naoki Yamamoto
 */
public class CsvMapMapper extends CsvObjectMapperSupport<Map<String, ?>> {

    /**
     * コンストラクタ。
     *
     * @param config CSV用の設定情報
     * @param inputStream 入力ストリーム
     */
    public CsvMapMapper(final CsvDataBindConfig config, final InputStream inputStream) {
        this(config, new InputStreamReader(inputStream, config.getCharset()));
    }

    /**
     * コンストラクタ。
     *
     * @param config CSV用の設定情報
     * @param reader リーダー
     */
    public CsvMapMapper(final CsvDataBindConfig config, final Reader reader) {
        super(config, reader);
        verify();
        readHeader();
    }

    @Override
    protected Map<String, ?> createObject(final String[] record) {

        verifyFieldCount(record);

        return createMap(record);
    }

    /**
     * レコードを{@link Map}に変換する。
     *
     * @param record レコード
     * @return 変換した{@link Map}
     */
    private Map<String, String> createMap(final String[] record) {
        final Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < record.length; i++) {
            map.put(properties[i], record[i]);
        }
        return map;
    }

    /**
     * フィールド数がヘッダーのフィールド数と同じであることの検証を行う。
     *
     * @param record 検証対象のレコード
     */
    private void verifyFieldCount(final String[] record) {
        if (properties.length != record.length) {
            throw new InvalidDataFormatException("property size does not match."
                    + " expected field count = [" + properties.length + "],"
                    + " actual field count = [" + record.length + "].", reader.getLineNumber());
        }
    }

    /**
     * オブジェクトの妥当性検証を行う。
     * <p/>
     * 以下の場合に検証エラーとする。
     * <ul>
     *     <li>ヘッダが必須でヘッダタイトルが未設定</li>
     *     <li>ヘッダが任意でプロパティ名が未設定</li>
     *     <li>ヘッダが必須でヘッダタイトルとプロパティ名のサイズが一致しない</li>
     * </ul>
     */
    private void verify() {
        final String[] headers = config.getHeaderTitles();
        final String[] properties = config.getProperties();

        if (config.isRequiredHeader() && StringUtil.isNullOrEmpty(headers)) {
            throw new IllegalArgumentException("this csv is require header.");
        }

        if (!config.isRequiredHeader() && StringUtil.isNullOrEmpty(properties)) {
            throw new IllegalArgumentException("this csv is require header or property.");
        }

        if (config.isRequiredHeader() && StringUtil.hasValue(properties) && headers.length != properties.length) {
            throw new IllegalArgumentException("header size and property size does not match.");
        }
    }
}
