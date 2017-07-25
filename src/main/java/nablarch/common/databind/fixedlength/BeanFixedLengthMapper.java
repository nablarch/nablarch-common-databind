package nablarch.common.databind.fixedlength;

import java.io.OutputStream;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;
import nablarch.core.beans.BeanUtil;

/**
 * Beanを固定長にマッピングする{@link ObjectMapper}
 *
 * @param <T> 出力する型
 * @author Naoki Yamamoto
 */
public class BeanFixedLengthMapper<T> implements ObjectMapper<T> {

    /** 固定長をMapに変換するクラス */
    private final MapFixedLengthMapper mapFixedLengthMapper;

    /**
     * Beanを固定長にマッピングするクラスを構築する。
     * @param clazz 出力する型
     * @param config 固定長の設定情報
     * @param stream 出力ストリーム
     */
    public BeanFixedLengthMapper(final Class<T> clazz, final FixedLengthDataBindConfig config, final OutputStream stream) {
        mapFixedLengthMapper = new MapFixedLengthMapper(config, stream);
    }

    @Override
    public void write(final T object) {
        final Map<String, Object> map = BeanUtil.createMapAndCopy(object);
        mapFixedLengthMapper.write(map);
    }

    @Override
    public T read() {
        throw new UnsupportedOperationException("unsupported read method.");
    }

    @Override
    public void close() {
        mapFixedLengthMapper.close();
    }
}
