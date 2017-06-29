package nablarch.common.databind.fixedlength;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 値を変換することを示すアノテーション。
 *
 * @author siosio
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Documented
public @interface FieldConvert {

    /**
     * 値の変換を行うクラス。
     *
     * @return 値の変換を行うクラス。
     */
    Class<? extends FieldConverter<?, ?>> value();

    /**
     * 値を変換するインタフェース。
     */
    interface FieldConverter<C extends Annotation, T> {

        /**
         * 読み込み時の変換を行う。
         *
         * @param fixedLengthDataBindConfig 固定長の設定
         * @param fieldConfig フィールドの設定
         * @param converterConfig 変換用アノテーション
         * @param input 入力値
         * @return 変換後の値
         */
        T convertOfRead(
                FixedLengthDataBindConfig fixedLengthDataBindConfig, FieldConfig fieldConfig, C converterConfig, byte[] input);

        /**
         * 書き込み時の変換を行う。
         *
         * @param fixedLengthDataBindConfig 固定長の設定
         * @param fieldConfig フィールドの設定
         * @param converterConfig 変換用のアノテーション
         * @param output 出力値
         * @return 出力後の値
         */
        byte[] convertOfWrite(
                FixedLengthDataBindConfig fixedLengthDataBindConfig, FieldConfig fieldConfig, C converterConfig, T output);
    }
}
