package nablarch.common.databind.fixedlength;

import java.io.InputStream;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;

/**
 * 固定長をMapにマッピングする{@link ObjectMapper}
 *
 * @author MAENO Daisuke.
 */
public class FixedLengthMapMapper implements ObjectMapper<Map<String, ?>> {

    /** マッピングサポートクラス */
    private final FixedLengthMapperSupport<Map<String, ?>> fixedLengthMapperSupport;

    /**
     * 固定長をMapにマッピングするクラスを構築する。
     *
     * @param config 固定長の設定情報
     * @param stream 固定長データ
     */
    public FixedLengthMapMapper(FixedLengthDataBindConfig config, InputStream stream) {
        fixedLengthMapperSupport = new FixedLengthMapperSupport<Map<String, ?>>(config, stream);
    }

    @Override
    public void write(Map<String, ?> object) {
        fixedLengthMapperSupport.write(object);
    }

    @Override
    public Map<String, ?> read() {
        // Mapで返却されるため、そのまま渡す
        return fixedLengthMapperSupport.read();
    }

    @Override
    public void close() {
        fixedLengthMapperSupport.close();
    }
}
