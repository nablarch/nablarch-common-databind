package nablarch.common.databind.fixedlength.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import nablarch.common.databind.fixedlength.FieldConfig;
import nablarch.common.databind.fixedlength.FieldConvert;
import nablarch.common.databind.fixedlength.FixedLengthDataBindConfig;
import nablarch.common.databind.fixedlength.converter.Lpad.LpadConverter;
import nablarch.core.util.StringUtil;

/**
 * 値の先頭に指定の文字を付加(読み込み時は除去)することを示す。
 *
 * @author siosio
 */
@FieldConvert(LpadConverter.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lpad {

    /**
     * 値の先頭に設定する文字。
     * <p>
     * デフォルトは半角の0
     *
     * @return 値の先頭に設定する文字
     */
    char value() default '0';

    /**
     * 値の変換を行う。
     */
    class LpadConverter implements FieldConvert.FieldConverter<Lpad, String> {

        @Override
        public String convertOfRead(
                final FixedLengthDataBindConfig fixedLengthDataBindConfig,
                final FieldConfig fieldConfig,
                final Lpad converterConfig, final byte[] input) {
            final String value = StringUtil.toString(input, fixedLengthDataBindConfig.getCharset());

            final char padChar = converterConfig.value();
            int charPos = 0;
            for (; charPos < value.length(); charPos++) {
                if (value.charAt(charPos) != padChar) {
                    break;
                }
            }
            return value.substring(charPos);
        }

        @Override
        public byte[] convertOfWrite(final FixedLengthDataBindConfig fixedLengthDataBindConfig,
                final FieldConfig fieldConfig,
                final Lpad converterConfig, final String output) {

            final byte[] paddingChar = StringUtil.getBytes(
                    Character.toString(converterConfig.value()), fixedLengthDataBindConfig.getCharset());
            
            final ByteBuffer buffer = ByteBuffer.allocate(fieldConfig.getLength());

            final byte[] value = StringUtil.getBytes(output, fixedLengthDataBindConfig.getCharset());
            while (buffer.position() < fieldConfig.getLength() - value.length) {
                buffer.put(paddingChar);
            }
            try {
                buffer.put(value);
            } catch (BufferOverflowException e) {
                throw new IllegalArgumentException("length after padding is invalid."
                        + " expected length " + fieldConfig.getLength()
                        + " but was actual length " + (buffer.position() + value.length) + '.'
                        + " field_name: " + fieldConfig.getName()
                        + " output value: " + output
                        + " padding_char: " + converterConfig.value(), e);
            }
            return buffer.array();
        }
    }
}
