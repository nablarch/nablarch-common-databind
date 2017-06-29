package nablarch.common.databind.fixedlength.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.common.databind.fixedlength.FieldConfig;
import nablarch.common.databind.fixedlength.FieldConvert;
import nablarch.common.databind.fixedlength.FixedLengthDataBindConfig;

/**
 * バイナリであることを示す。
 *
 * @author siosio
 */
@FieldConvert(Binary.BinaryConverter.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Binary {

    /**
     * バイナリのため変換を行わずそのまま移送する。
     */
    class BinaryConverter implements FieldConvert.FieldConverter<Binary, byte[]> {

        @Override
        public byte[] convertOfRead(final FixedLengthDataBindConfig fixedLengthDataBindConfig,
                final FieldConfig fieldConfig,
                final Binary converterConfig, final byte[] input) {
            return input;
        }

        @Override
        public byte[] convertOfWrite(final FixedLengthDataBindConfig fixedLengthDataBindConfig,
                final FieldConfig fieldConfig,
                final Binary converterConfig, final byte[] output) {
            return output;
        }
    }
}
