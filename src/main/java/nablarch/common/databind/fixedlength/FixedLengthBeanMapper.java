package nablarch.common.databind.fixedlength;

import java.io.InputStream;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;
import nablarch.core.beans.BeanUtil;

/**
 * 固定長をBeanにマッピングする{@link ObjectMapper}
 *
 * @param <T> 読み取る型
 * @author Naoki Yamamoto
 */
public class FixedLengthBeanMapper<T> implements ObjectMapper<T> {

    /** レコードをマッピングするクラス */
    private final Class<T> clazz;

    /** マッピングサポートクラス */
    private final FixedLengthMapperSupport<T> fixedLengthMapperSupport;

    /**
     * 固定長をBeanにマッピングするクラスを構築する。
     *
     * @param clazz マッピング対象のBeanクラス
     * @param config 固定長の設定情報
     * @param stream 固定長データ
     */
    public FixedLengthBeanMapper(Class<T> clazz, FixedLengthDataBindConfig config, InputStream stream) {
        this.clazz = clazz;
        fixedLengthMapperSupport = new FixedLengthMapperSupport<T>(config, stream);
    }

    @Override
    public void write(T object) {
        fixedLengthMapperSupport.write(object);
    }

    @Override
    public T read() {
        final Map<String, Object> read = fixedLengthMapperSupport.read();
        if (read == null) {
            return null;
        }
        return BeanUtil.createAndCopy(clazz, read);
    }

    @Override
    public void close() {
        fixedLengthMapperSupport.close();
    }
}
