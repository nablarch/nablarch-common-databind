package nablarch.common.databind.fixedlength;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * マルチレイアウトのレコードであることを表すアノテーション。
 *
 * @author Naoki Yamamoto
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Record {
}
