package nablarch.common.databind.csv;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import nablarch.common.databind.DataBindUtil;
import nablarch.common.databind.InvalidDataFormatException;
import nablarch.core.util.StringUtil;

/**
 * CSVフォーマットとJava Beanをマッピングするクラス。
 *
 * @param <T> 読み取り、書き込み対象のJava型
 * @author Hisaaki Sioiri
 */
public class CsvBeanMapper<T> extends CsvObjectMapperSupport<T> {

    /** Beanのクラス */
    private final Class<T> clazz;

    /** 行番号を格納するプロパティ名 */
    private final String lineNumberPropertyName;

    /**
     * コンストラクタ。
     *
     * @param clazz Beanの{@link Class}
     * @param config CSV用の設定情報
     * @param inputStream 入力ストリーム
     */
    public CsvBeanMapper(final Class<T> clazz, final CsvDataBindConfig config, final InputStream inputStream) {
        this(clazz, config, new InputStreamReader(inputStream, config.getCharset()));
    }

    /**
     * コンストラクタ。
     *
     * @param clazz Beanの{@link Class}
     * @param config CSV用の設定情報
     * @param reader リーダー
     */
    public CsvBeanMapper(final Class<T> clazz, final CsvDataBindConfig config, final Reader reader) {
        super(config, reader);
        this.clazz = clazz;
        lineNumberPropertyName = DataBindUtil.findLineNumberProperty(clazz);
        readHeader();
    }

    @Override
    protected T createObject(final String[] record) {
        final String[] keys = config.getKeys();
        if (keys.length != record.length) {
            throw new InvalidDataFormatException(
                    "property size does not match. expected field count = [" + keys.length + "],"
                            + " actual field count = [" + record.length + "].", reader.getLineNumber());
        }

        if(StringUtil.isNullOrEmpty(lineNumberPropertyName)){
            return DataBindUtil.getInstance(clazz, keys, record);
        }else{
            return DataBindUtil.getInstanceWithLineNumber(clazz, keys, record, lineNumberPropertyName, reader.getLineNumber());
        }
    }
}

