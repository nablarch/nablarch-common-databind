package nablarch.common.databind.fixedlength;

import java.io.InputStream;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;
import nablarch.common.databind.fixedlength.FixedLengthReader.Record;
import nablarch.core.util.FileUtil;

/**
 * 固定長をMapにマッピングする{@link ObjectMapper}
 *
 * @author MAENO Daisuke.
 */
public class FixedLengthMapMapper implements ObjectMapper<Map<String, ?>> {

    /** 固定長のリーダ */
    private final FixedLengthReader reader;

    /**
     * 固定長データをMapにマッピングするクラスを構築する。
     * @param config 固定長の設定情報
     * @param stream 固定長データ
     */
    public FixedLengthMapMapper(final FixedLengthDataBindConfig config, final InputStream stream) {
        reader = new FixedLengthReader(stream, config);
    }

    @Override
    public void write(Map<String, ?> object) {
        throw new UnsupportedOperationException("unsupported write method.");
    }

    @Override
    public Map<String, ?> read() {
        final Record record = reader.readRecord();
        if (record == null) {
            return null;
        }
        return record.readFields();
    }

    @Override
    public void close() {
        FileUtil.closeQuietly(reader);
    }

}
