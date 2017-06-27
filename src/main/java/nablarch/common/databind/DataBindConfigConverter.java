package nablarch.common.databind;

import java.lang.annotation.Annotation;

/**
 * アノテーションに定義された変換対象のレイアウト情報を{@link DataBindConfig}に変換するインタフェース。
 *
 * @param <T> 変換対象のアノテーションの型
 * @author siosio
 */
public interface DataBindConfigConverter<T extends Annotation> {

    /**
     * 指定されたBeanクラスに設定されているアノテーションから{@link DataBindConfig}に変換する。
     *
     * @param beanClass Beanクラス
     * @return 変換した{@code DataBindConfig}
     */
    DataBindConfig convert(Class<?> beanClass);

    /**
     * 変換対象のアノテーションのタイプを返す。
     *
     * @return 変換対象のアノテーションタイプ
     */
    Class<T> getType();
}
