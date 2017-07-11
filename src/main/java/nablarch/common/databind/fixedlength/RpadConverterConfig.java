package nablarch.common.databind.fixedlength;

import nablarch.common.databind.fixedlength.converter.Rpad;

import java.lang.annotation.Annotation;

/**
 * Rpadの設定情報。
 *
 * @author MAENO Daisuke.
 */
public class RpadConverterConfig extends FieldConverterConfig {

    /**
     * 値変換の設定情報を構築する。
     *
     * @param padChar  値の末尾に設定する文字
     */
    public RpadConverterConfig(final char padChar) {
        super(new Rpad() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Rpad.class;
            }

            @Override
            public char value() {
                return padChar;
            }
        }, new Rpad.RpadConverter());
    }
}
