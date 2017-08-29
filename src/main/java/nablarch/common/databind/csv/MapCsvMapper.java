package nablarch.common.databind.csv;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import nablarch.core.util.StringUtil;

/**
 * MapをCSVにマッピングするのをサポートするクラス。
 *
 * @author Hisaaki Shioiri
 */
public class MapCsvMapper extends ObjectCsvMapperSupport<Map<String, ?>> {

    /**
     * コンストラクタ。
     *
     * @param config フォーマット定義
     * @param outputStream 出力リソース
     */
    public MapCsvMapper(final CsvDataBindConfig config, final OutputStream outputStream) {
        this(config, new OutputStreamWriter(outputStream, config.getCharset()));
    }

    /**
     * コンストラクタ。
     *
     * @param config フォーマット定義
     * @param writer 出力リソース
     */
    public MapCsvMapper(final CsvDataBindConfig config, final Writer writer) {
        super(config, writer);
        verify();
        writeHeader();
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
            throw new IllegalArgumentException("csv header is required.");
        }

        if (!config.isRequiredHeader() && StringUtil.isNullOrEmpty(properties)) {
            throw new IllegalArgumentException("csv header or property is required.");
        }

        if (config.isRequiredHeader() && StringUtil.hasValue(properties) && headers.length != properties.length) {
            throw new IllegalArgumentException("csv header size and property size does not match.");
        }
    }

    @Override
    public Object[] convertValues(final Map<String, ?> object) {
        final String[] keys = config.getKeys();
        final Object[] fieldValues = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            fieldValues[i] = object.get(keys[i]);
        }
        return fieldValues;
    }
}
