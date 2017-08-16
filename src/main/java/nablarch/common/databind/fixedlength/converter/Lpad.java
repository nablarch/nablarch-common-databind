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
import nablarch.core.util.annotation.Published;

/**
 * 値の先頭に指定の文字を付加(読み込み時は除去)することを示す。
 *
 * @author siosio
 */
@FieldConvert(LpadConverter.class)
@Target(ElementType.FIELD)
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
    class LpadConverter implements FieldConvert.FieldConverter<Lpad> {

        /**
         * 値の先頭に設定する文字
         */
        private char padChar;

        /**
         * 値の変換処理を行うクラスを構築する。
         */
        @Published
        public LpadConverter() {
        }

        /**
         * 指定された値を用いて値の変換処理を行うクラスを構築する。
         *
         * @param padChar 値の先頭に設定する文字
         */
        public LpadConverter(final char padChar) {
            this.padChar = padChar;
        }

        @Override
        public void initialize(final Lpad annotation) {
            padChar = annotation.value();
        }

        @Override
        public Object convertOfRead(
                final FixedLengthDataBindConfig fixedLengthDataBindConfig,
                final FieldConfig fieldConfig,
                final byte[] input) {

            final String value = StringUtil.toString(input, fixedLengthDataBindConfig.getCharset());
            int charPos = 0;
            for (; charPos < value.length(); charPos++) {
                if (value.charAt(charPos) != padChar) {
                    break;
                }
            }
            return value.substring(charPos);
        }

        @Override
        public byte[] convertOfWrite(
                final FixedLengthDataBindConfig fixedLengthDataBindConfig,
                final FieldConfig fieldConfig,
                final Object output) {

            final String strValue = output != null ? StringUtil.toString(output) : "";
            final byte[] paddingChar = StringUtil.getBytes(
                    Character.toString(padChar), fixedLengthDataBindConfig.getCharset());
            
            final ByteBuffer buffer = ByteBuffer.allocate(fieldConfig.getLength());

            final byte[] value = StringUtil.getBytes(strValue, fixedLengthDataBindConfig.getCharset());
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
                        + " output value: " + strValue
                        + " padding_char: " + padChar, e);
            }
            return buffer.array();
        }
    }
}
