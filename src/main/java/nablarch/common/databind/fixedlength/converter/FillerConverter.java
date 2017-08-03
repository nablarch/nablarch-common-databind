package nablarch.common.databind.fixedlength.converter;

import java.lang.annotation.Annotation;
import java.nio.ByteBuffer;

import nablarch.common.databind.fixedlength.FieldConfig;
import nablarch.common.databind.fixedlength.FieldConvert;
import nablarch.common.databind.fixedlength.FixedLengthDataBindConfig;
import nablarch.core.util.StringUtil;

/**
 * 未定義領域の文字埋めを行うコンバータ。
 *
 * @author Naoki Yamamoto
 */
public class FillerConverter implements FieldConvert.FieldConverter<Annotation> {

    /** 未定義領域を埋める文字 */
    private final char fillChar;

    /**
     * 未定義領域を埋める文字をもとにインスタンスを生成する。
     * @param fillChar 未定義領域を埋める文字
     */
    public FillerConverter(final char fillChar) {
        this.fillChar = fillChar;
    }

    @Override
    public void initialize(final Annotation annotation) {
    }

    @Override
    public Object convertOfRead(final FixedLengthDataBindConfig fixedLengthDataBindConfig, final FieldConfig fieldConfig, final byte[] input) {
        return new String(input, fixedLengthDataBindConfig.getCharset());
    }

    @Override
    public byte[] convertOfWrite(final FixedLengthDataBindConfig fixedLengthDataBindConfig, final FieldConfig fieldConfig, final Object output) {
        final byte[] bytes = StringUtil.getBytes(Character.toString(fillChar), fixedLengthDataBindConfig.getCharset());
        final ByteBuffer buffer = ByteBuffer.allocate(fieldConfig.getLength());
        while (buffer.position() < buffer.limit()) {
            buffer.put(bytes);
        }
        return buffer.array();
    }
}
