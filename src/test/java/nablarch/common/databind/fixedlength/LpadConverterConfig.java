package nablarch.common.databind.fixedlength;

import nablarch.common.databind.fixedlength.converter.Lpad;

import java.lang.annotation.Annotation;

/**
 * Lpadの設定情報。
 *
 * @author MAENO Daisuke.
 */
public class LpadConverterConfig extends FieldConverterConfig {

    /**
     * 値変換の設定情報を構築する。
     *
     * @param padChar  値の先頭に設定する文字
     */
    public LpadConverterConfig(final char padChar) {
        super(new Lpad() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Lpad.class;
            }

            @Override
            public char value() {
                return padChar;
            }
        }, new Lpad.LpadConverter());
    }
}
