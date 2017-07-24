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
import nablarch.common.databind.fixedlength.converter.Rpad.RpadConverter;
import nablarch.core.util.StringUtil;

/**
 * 値の末尾に指定の文字を付加(読み込み時は除去)することを示す。
 *
 * @author siosio
 */
@FieldConvert(RpadConverter.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Rpad {

    /**
     * 値の末尾に設定する文字。
     * <p>
     * デフォルトは、半角スペース
     *
     * @return 値の末尾に設定する文字
     */
    char value() default ' ';

    /**
     * 値の変換処理を行う。
     */
    class RpadConverter implements FieldConvert.FieldConverter<Rpad> {

        /**
         * 値の先頭に設定する文字
         */
        private char padChar;

        /**
         * 値の変換処理を行うクラスを構築する。
         */
        public RpadConverter() {
        }

        /**
         * 指定された値を用いて値の変換処理を行うクラスを構築する。
         *
         * @param padChar 値の末尾に設定する文字
         */
        public RpadConverter(final char padChar) {
            this.padChar = padChar;
        }

        @Override
        public void initialize(final Rpad annotation) {
            padChar = annotation.value();
        }

        @Override
        public Object convertOfRead(
                final FixedLengthDataBindConfig fixedLengthDataBindConfig,
                final FieldConfig fieldConfig,
                final byte[] input) {

            final String value = StringUtil.toString(input, fixedLengthDataBindConfig.getCharset());
            int chopPos = value.length() - 1;
            while ((chopPos >= 0) && (value.charAt(chopPos) == padChar)) {
                chopPos--;
            }
            return value.substring(0, chopPos + 1);
        }

        @Override
        public byte[] convertOfWrite(
                final FixedLengthDataBindConfig fixedLengthDataBindConfig,
                final FieldConfig fieldConfig,
                final Object output) {

            final String value = output != null ? StringUtil.toString(output) : "";
            final byte[] paddingChar = StringUtil.getBytes(
                    Character.toString(padChar), fixedLengthDataBindConfig.getCharset());
            
            final ByteBuffer buffer = ByteBuffer.allocate(fieldConfig.getLength());
            buffer.put(StringUtil.getBytes(value, fixedLengthDataBindConfig.getCharset()));
            
            while (buffer.position() < buffer.limit()) {
                try {
                    buffer.put(paddingChar);
                } catch (BufferOverflowException e) {
                    throw new IllegalArgumentException("length after padding is invalid."
                            + " expected length " + fieldConfig.getLength()
                            + " but was actual length " + (buffer.position() + paddingChar.length) + '.'
                            + " field_name: " + fieldConfig.getName()
                            + " output value: " + output
                            + " padding_char: " + padChar, e);
                }
            }
            return buffer.array();
        }
    }
}
