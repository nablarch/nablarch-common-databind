package nablarch.common.databind.fixedlength;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 固定長データにバインドするBeanであることを示すアノテーション。
 *
 * @author siosio
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface FixedLength {

    /**
     * レコードの長さ(バイト数)
     *
     * @return レコードの長さ(バイト数)
     */
    int length();

    /**
     * 文字セット
     *
     * @return 文字セット
     */
    String charset();

    /**
     * 改行を現す文字
     *
     * @return 改行を現す文字
     */
    String lineSeparator();

    /**
     * 未定義領域を埋める文字
     * <p>
     * デフォルトは半角スペース。
     * @return 未定義領域を埋める文字
     */
    char fillChar() default ' ';

    /**
     * マルチレイアウトか否か
     * <p>
     * デフォルトは{@code false}
     * @return マルチレイアウトか否か
     */
    boolean multiLayout() default false;
}
