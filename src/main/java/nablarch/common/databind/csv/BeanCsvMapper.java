package nablarch.common.databind.csv;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import nablarch.core.beans.BeanUtil;

/**
 * Java BeansオブジェクトをCSVにマッピングするクラス。
 *
 * @param <T> Java Beansクラス
 * @author Hisaaki Shioiri
 */
public class BeanCsvMapper<T> extends ObjectCsvMapperSupport<T> {

    /**
     * コンストラクタ。
     *
     * @param clazz Beanクラス
     * @param config フォーマット定義
     * @param outputStream 出力リソース
     */
    public BeanCsvMapper(final Class<T> clazz, final CsvDataBindConfig config, final OutputStream outputStream) {
        this(clazz, config, new OutputStreamWriter(outputStream, config.getCharset()));
    }

    /**
     * コンストラクタ。
     *
     * @param clazz Beanクラス
     * @param config フォーマット定義
     * @param writer 出力リソース
     */
    public BeanCsvMapper(final Class<T> clazz, final CsvDataBindConfig config, final Writer writer) {
        super(config, writer);
        writeHeader();
    }

    @Override
    public Object[] convertValues(T object) {
        final Object[] fieldValues = new Object[properties.length];
        for (int i = 0; i < properties.length; i++) {
            final String propertyName = properties[i];
            fieldValues[i] = BeanUtil.getProperty(object, propertyName);
        }
        return fieldValues;
    }
}
