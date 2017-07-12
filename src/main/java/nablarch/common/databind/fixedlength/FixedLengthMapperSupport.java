package nablarch.common.databind.fixedlength;

import nablarch.core.util.FileUtil;

import java.io.InputStream;
import java.util.Map;

/**
 * 固定長をオブジェクトにマッピングするのをサポートするクラス。
 *
 * @param <T> マッピング対象のクラス
 * @author MAENO Daisuke.
 */
public class FixedLengthMapperSupport<T> {

    /** 固定長のリーダ */
    private final FixedLengthReader reader;

    /**
     * 固定長データをマッピングするクラスを構築する。
     * @param config 固定長の設定情報
     * @param stream 固定長データ
     */
    public FixedLengthMapperSupport(final FixedLengthDataBindConfig config, final InputStream stream) {
        reader = new FixedLengthReader(stream, config);
    }

    public void write(final T object) {
        throw new UnsupportedOperationException("unsupported write method.");
    }

    public Map<String, Object> read() {
        final FixedLengthReader.Record record = reader.readRecord();
        if (record == null) {
            return null;
        }
        return record.readFields();
    }

    public void close() {
        FileUtil.closeQuietly(reader);
    }
}
