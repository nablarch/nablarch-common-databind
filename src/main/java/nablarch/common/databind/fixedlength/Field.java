package nablarch.common.databind.fixedlength;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * レコードのフィールドであることを示すアノテーション。
 *
 * @author siosio
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Field {

    /**
     * フィールドの開始位置(1始まり)
     *
     * @return フィールドの開始位置
     */
    int offset();

    /**
     * フィールドの長さ（バイト数）
     *
     * @return フィールドの長さ（バイト数）
     */
    int length();
}
