package nablarch.common.databind.fixedlength;

import java.io.InputStream;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;
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
     * 固定長をMapにマッピングするクラスを構築する。
     *
     * @param config 固定長の設定情報
     * @param stream 固定長データ
     */
    public FixedLengthMapMapper(final FixedLengthDataBindConfig config, final InputStream stream) {
        reader = new FixedLengthReader(stream, config);
    }

    @Override
    public void write(final Map<String, ?> object) {
        throw new UnsupportedOperationException("unsupported write method.");
    }

    @Override
    public Map<String, ?> read() {
        return reader.readRecord();
    }

    @Override
    public void close() {
        FileUtil.closeQuietly(reader);
    }
}
