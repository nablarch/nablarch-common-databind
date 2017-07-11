package nablarch.common.databind.fixedlength;

import java.io.InputStream;
import java.util.Map;

import nablarch.common.databind.ObjectMapper;
import nablarch.core.beans.BeanUtil;

/**
 * 固定長をBeanにマッピングする{@link ObjectMapper}
 *
 * @author Naoki Yamamoto
 * @param <T> 読み取る型
 */
public class FixedLengthBeanMapper<T> extends FixedLengthMapperSupport<T> {

    public FixedLengthBeanMapper(Class<T> clazz, FixedLengthDataBindConfig config, InputStream stream) {
        super(clazz, config, stream);
    }

    @Override
    T createObject(Class<T> clazz, Map<String, Object> fields) {
        return BeanUtil.createAndCopy(clazz, fields);
    }
}
