package nablarch.common.databind.csv;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

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
        writeHeader();
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
