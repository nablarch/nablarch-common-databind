package nablarch.common.databind.fixedlength.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.common.databind.fixedlength.FieldConfig;
import nablarch.common.databind.fixedlength.FieldConvert;
import nablarch.common.databind.fixedlength.FixedLengthDataBindConfig;
import nablarch.core.util.annotation.Published;

/**
 * バイナリであることを示す。
 *
 * @author siosio
 */
@FieldConvert(Binary.BinaryConverter.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Binary {

    /**
     * バイナリのため変換を行わずそのまま移送する。
     */
    class BinaryConverter implements FieldConvert.FieldConverter<Binary> {

        /**
         * コンストラクタ。
         */
        @Published
        public BinaryConverter() {
        }

        @Override
        public void initialize(final Binary annotation) {
        }

        @Override
        public Object convertOfRead(final FixedLengthDataBindConfig fixedLengthDataBindConfig,
                final FieldConfig fieldConfig, final byte[] input) {
            return input;
        }

        @Override
        public byte[] convertOfWrite(final FixedLengthDataBindConfig fixedLengthDataBindConfig,
                final FieldConfig fieldConfig, final Object output) {
            if (output instanceof byte[]) {
                final byte[] bytes = (byte[]) output;
                if (bytes.length != fieldConfig.getLength()) {
                    throw new IllegalArgumentException("length is invalid."
                            + " expected length " + fieldConfig.getLength()
                            + " but was actual length " + bytes.length + '.'
                            + " field_name: " + fieldConfig.getName());
                }
                return bytes;
            } else {
                throw new IllegalArgumentException("output is byte array only.");
            }
        }
    }
}
