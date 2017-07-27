package nablarch.common.databind.fixedlength;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;
import nablarch.core.beans.BeanUtil;
import nablarch.core.util.StringUtil;

/**
 * 固定長をBeanにマッピングする{@link ObjectMapper}
 *
 * @param <T> 読み取る型
 * @author Naoki Yamamoto
 */
public class FixedLengthBeanMapper<T> implements ObjectMapper<T> {

    /** レコードをマッピングするクラス */
    private final Class<T> clazz;

    /** 固定長の設定情報 */
    private final FixedLengthDataBindConfig config;

    /** 固定長をMapに変換するクラス */
    private final FixedLengthMapMapper fixedLengthMapMapper;

    /**
     * 固定長をBeanにマッピングするクラスを構築する。
     *
     * @param clazz マッピング対象のBeanクラス
     * @param config 固定長の設定情報
     * @param stream 固定長データ
     */
    public FixedLengthBeanMapper(final Class<T> clazz, final FixedLengthDataBindConfig config, final InputStream stream) {
        this.clazz = clazz;
        this.config = config;
        fixedLengthMapMapper = new FixedLengthMapMapper(config, stream);
    }

    @Override
    public void write(final T object) {
        throw new UnsupportedOperationException("unsupported write method.");
    }

    @Override
    public T read() {
        final Map<String, ?> read = fixedLengthMapMapper.read();
        if (read == null) {
            return null;
        }

        if (config.getMultiLayoutConfig() != null) {
            final T bean = BeanUtil.createAndCopy(clazz, read);
            final String recordName = StringUtil.toString(read.get("recordName"));
            final PropertyDescriptor descriptor = BeanUtil.getPropertyDescriptor(clazz, recordName);
            final Object record = BeanUtil.createAndCopy(descriptor.getPropertyType(), (Map<String, ?>)read.get(recordName));
            BeanUtil.setProperty(bean, recordName, record);
            return bean;

        } else {
            return BeanUtil.createAndCopy(clazz, read);
        }

    }

    @Override
    public void close() {
        fixedLengthMapMapper.close();
    }
}
