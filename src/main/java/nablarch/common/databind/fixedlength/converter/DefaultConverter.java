package nablarch.common.databind.fixedlength.converter;

import nablarch.common.databind.fixedlength.FieldConfig;
import nablarch.common.databind.fixedlength.FieldConvert;
import nablarch.common.databind.fixedlength.FixedLengthDataBindConfig;
import nablarch.core.util.StringUtil;

/**
 * デフォルトのコンバータ。
 *
 * @author Naoki Yamamoto
 */
public class DefaultConverter implements FieldConvert.FieldConverter {
    @Override
    public Object convertOfRead(final FixedLengthDataBindConfig fixedLengthDataBindConfig, final FieldConfig fieldConfig, final byte[] input) {
        return new String(input, fixedLengthDataBindConfig.getCharset());
    }

    @Override
    public byte[] convertOfWrite(final FixedLengthDataBindConfig fixedLengthDataBindConfig, final FieldConfig fieldConfig, final Object output) {
        return StringUtil.getBytes(output.toString(), fixedLengthDataBindConfig.getCharset());
    }
}
