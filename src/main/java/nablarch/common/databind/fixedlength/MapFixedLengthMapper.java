package nablarch.common.databind.fixedlength;

import java.io.OutputStream;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;
import nablarch.core.util.FileUtil;

/**
 * Mapを固定長にマッピングする{@link ObjectMapper}
 *
 * @author Naoki Yamamoto
 */
public class MapFixedLengthMapper implements ObjectMapper<Map<String, ?>> {

    /** 固定長のライタ */
    private final FixedLengthWriter writer;

    /**
     * Mapを固定長にマッピングするクラスを構築する。
     * @param config 固定長の設定情報
     * @param stream 出力ストリーム
     */
    public MapFixedLengthMapper(final FixedLengthDataBindConfig config, final OutputStream stream) {
        writer = new FixedLengthWriter(stream, config);
    }

    @Override
    public void write(Map<String, ?> object) {
        writer.writeRecord(object);
    }

    @Override
    public Map<String, ?> read() {
        throw new UnsupportedOperationException("unsupported read method.");
    }

    @Override
    public void close() {
        FileUtil.closeQuietly(writer);
    }
}
