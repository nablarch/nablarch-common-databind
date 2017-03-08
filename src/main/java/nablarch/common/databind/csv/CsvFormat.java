package nablarch.common.databind.csv;

import nablarch.core.util.annotation.Published;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Beanに紐づくCSVのフォーマットを個別に定義するアノテーション。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface CsvFormat {

    /** 列区切り文字 */
    char fieldSeparator();

    /**
     * 行区切り文字。
     * <p/>
     * \r\n(CRLF) or \r(CR) or \n(LF)で指定すること。
     */
    String lineSeparator();

    /**
     * フィールド囲み文字。
     * <p/>
     * 指定した文字で囲むフィールドは、{@link #quoteMode}で指定する。
     * <p/>
     * 例:ダブルクォート(")、シングルクォート(')。
     */
    char quote();

    /** 空行を無視するか否か */
    boolean ignoreEmptyLine();

    /** ヘッダ行(タイトル行)が必須か否か */
    boolean requiredHeader();

    /** 文字コード */
    String charset();

    /** 空フィールドをnullに置き換えるかどうか */
    boolean emptyToNull();

    /**
     * 出力時に{@link #quote}で囲むフィールド。
     * <p/>
     * {@link CsvDataBindConfig.QuoteMode#CUSTOM}を指定した場合、</br>
     * 囲む要素に対応したgetterに{@link Quoted}を付与すること。
     */
    CsvDataBindConfig.QuoteMode quoteMode();
}
