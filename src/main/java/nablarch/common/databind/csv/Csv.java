package nablarch.common.databind.csv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.common.databind.DataBindConfig;
import nablarch.core.util.annotation.Published;

/**
 * CSVにバインドするBeanであることを表すアノテーション。
 * <p/>
 * CSVにバインドするBeanに定義することで、CSVのレコードをBeanオブジェクトとして扱うことができる。
 * <p/>
 * CSVを読込む場合の例を以下に示す。
 * (フォーマットがデフォルト、プロパティ名がage(年齢)、name(氏名)、address(住所)、項目順が年齢、氏名、住所）
 * <pre>
 * {@code @Csv(type = Csv.CsvType.DEFAULT, properties = {"age", "name", "address"})
 *   public class Person {...}
 * }
 * </pre>
 *
 * CSVを出力する場合の例を以下に示す。
 * (フォーマットがデフォルト、プロパティ名がage(年齢)、name(氏名)、address(住所)、項目順が年齢、氏名、住所)
 * <pre>
 * {@code @Csv(type = Csv.CsvType.DEFAULT, properties = {"age", "name", "address"}, headers = {"年齢", "氏名", "住所"})
 *   public class Person {...}
 * }
 * </pre>
 *
 * @author Naoki Yamamoto
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface Csv {

    /**
     * {@link CsvDataBindConfig}のフォーマットを表す列挙型
     */
    enum CsvType {
        /** デフォルトのフォーマット定義 */
        DEFAULT {
            @Override
            public DataBindConfig getConfig() {
                return CsvDataBindConfig.DEFAULT;
            }
        },
        /** RFC4180準拠のフォーマット定義 */
        RFC4180 {
            @Override
            public DataBindConfig getConfig() {
                return CsvDataBindConfig.RFC4180;
            }
        },
        /** EXCEL形式のCSVフォーマット定義 */
        EXCEL {
            @Override
            public DataBindConfig getConfig() {
                return CsvDataBindConfig.EXCEL;
            }
        },
        /** タブ区切り(TSV)のフォーマット定義 */
        TSV {
            @Override
            public DataBindConfig getConfig() {
                return CsvDataBindConfig.TSV;
            }
        },
        /** カスタムのフォーマット定義 */
        CUSTOM {
            @Override
            public DataBindConfig getConfig() {
                return null;
            }
        };

        /**
         * 列挙子に紐づく{@link CsvDataBindConfig}を取得する。
         *
         * @return {@link CsvDataBindConfig}
         */
        public abstract DataBindConfig getConfig();
    }

    /**
     * フォーマット定義。
     * <p/>
     * {@link CsvType#CUSTOM}を選択した場合、{@link CsvFormat}アノテーションを使用し、
     * フォーマットを指定すること。
     */
    CsvType type();

    /**
     * CSVの項目に対応したプロパティのリスト。
     * <p/>
     * 設定された順序で読込・出力されるため、
     * CSVの項目順にプロパティ名を列挙する。
     */
    String[] properties();

    /**
     * CSVのヘッダーのリスト。
     * <p/>
     * {@link CsvType#properties()} と同じ要素数でヘッダーを指定する。
     * {@link CsvFormat#requiredHeader()}が{@code true}である場合は、必ず指定すること。</br>
     */
    String[] headers() default {};
}
