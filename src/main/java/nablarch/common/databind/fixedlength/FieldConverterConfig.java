package nablarch.common.databind.fixedlength;

import java.lang.annotation.Annotation;

import nablarch.common.databind.fixedlength.FieldConvert.FieldConverter;

/**
 * 値変換の設定情報。
 *
 * @author siosio
 */
public class FieldConverterConfig {

    /** 値変換の設定を持つアノテーション */
    private final Annotation converterConfig;

    /** コンバータ */
    @SuppressWarnings("rawtypes")
    private final FieldConverter fieldConverter;

    /**
     * 値変換の設定情報を構築する。
     * @param converterConfig 値変換の設定情報を持つアノテーション
     * @param fieldConverter コンバータ
     */
    public FieldConverterConfig(final Annotation converterConfig, final FieldConverter<?, ?> fieldConverter) {
        this.converterConfig = converterConfig;
        this.fieldConverter = fieldConverter;
    }

    /**
     * 読み込み時の変換処理を行う。
     * @param fixedLengthDataBindConfig 固定長設定
     * @param fieldConfig フィールドの設定
     * @param value 変換対象の値
     * @return 変換した値
     */
    @SuppressWarnings("unchecked")
    public Object convertOfRead(
            final FixedLengthDataBindConfig fixedLengthDataBindConfig, final FieldConfig fieldConfig, final byte[] value) {
        return fieldConverter.convertOfRead(fixedLengthDataBindConfig, fieldConfig, converterConfig, value);
    }
}
