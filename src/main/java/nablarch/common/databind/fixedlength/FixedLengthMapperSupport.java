package nablarch.common.databind.fixedlength;

import nablarch.common.databind.ObjectMapper;
import nablarch.core.util.FileUtil;

import java.io.InputStream;
import java.util.Map;

/**
 * 固定長をオブジェクトにマッピングするのをサポートするクラス。
 *
 * @param <T> マッピング対象のクラス
 * @author MAENO Daisuke.
 */
public abstract class FixedLengthMapperSupport<T> implements ObjectMapper<T> {
    /** レコードをマッピングするクラス */
    private final Class<T> clazz;

    /** 固定長のリーダ */
    private final FixedLengthReader reader;

    /**
     * 固定長データをマッピングするクラスを構築する。
     * @param clazz マッピング対象のBeanクラス
     * @param config 固定長の設定情報
     * @param stream 固定長データ
     */
    public FixedLengthMapperSupport(final Class<T> clazz, final FixedLengthDataBindConfig config, final InputStream stream) {
        this.clazz = clazz;
        reader = new FixedLengthReader(stream, config);
    }

    @Override
    public void write(final T object) {
        throw new UnsupportedOperationException("unsupported write method.");
    }

    @Override
    public T read() {
        final FixedLengthReader.Record record = reader.readRecord();
        if (record == null) {
            return null;
        }
        final Map<String, Object> fields = record.readFields();
        return createObject(clazz, fields);
    }

    abstract T createObject(Class<T> clazz, Map<String, Object> fields);

    @Override
    public void close() {
        FileUtil.closeQuietly(reader);
    }
}
