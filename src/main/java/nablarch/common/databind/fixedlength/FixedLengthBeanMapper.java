package nablarch.common.databind.fixedlength;

import java.io.InputStream;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;
import nablarch.common.databind.fixedlength.FixedLengthReader.Record;
import nablarch.core.beans.BeanUtil;
import nablarch.core.util.FileUtil;

/**
 * 固定長をBeanにマッピングする{@link ObjectMapper}
 *
 * @author Naoki Yamamoto
 * @param <T> 読み取る型
 */
public class FixedLengthBeanMapper<T> implements ObjectMapper<T> {

    /** レコードをマッピングするクラス */
    private final Class<T> clazz;

    /** 固定長のリーダ */
    private final FixedLengthReader reader;

    /**
     * 固定長データをBeanにマッピングするクラスを構築する。
     * @param clazz マッピング対象のBeanクラス
     * @param config 固定長の設定情報
     * @param stream 固定長データ
     */
    public FixedLengthBeanMapper(final Class<T> clazz, final FixedLengthDataBindConfig config, final InputStream stream) {
        this.clazz = clazz;
        reader = new FixedLengthReader(stream, config);
    }

    @Override
    public void write(T object) {
        throw new UnsupportedOperationException("unsupported write method.");
    }

    @Override
    public T read() {
        final Record record = reader.readRecord();
        if (record == null) {
            return null;
        }
        final Map<String, Object> fields = record.readFields();
        return BeanUtil.createAndCopy(clazz, fields);
    }

    @Override
    public void close() {
        FileUtil.closeQuietly(reader);
    }
}
