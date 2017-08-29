package nablarch.common.databind.csv;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import nablarch.common.databind.DataBindConfig;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;

/**
 * CSVのフォーマットを表すクラス。
 * <p/>
 * デフォルト設定を使用する場合は、{@link #DEFAULT}オブジェクトを使用する。<br/>
 * 独自の設定を行う場合は、{@link #CsvDataBindConfig(char, String, char, boolean, boolean, String[], String[], Charset, boolean, QuoteMode, List)}
 * を使用しオブジェクトを生成するか、{@link #DEFAULT}オブジェクトのセッタを実行して値を設定する。
 * <p/>
 * 下記にデフォルトの設定値を示す。
 * <pre>
 * 列区切り文字                          -->  ","
 * 行区切り文字                          -->  "\r\n"(CRLF)
 * フィールド囲み文字                    -->  """
 * 空行を無視するか否か                  -->  無視する(true)
 * ヘッダ行が必須か否か                  -->  必須(true)
 * ヘッダーに出力するタイトル            -->  空のString型配列
 * プロパティ名リスト                    -->  空のString型配列
 * 文字コード                            -->  UTF-8
 * 空のフィールドをnullに変換するか否か  --> 変換する(true)
 * フィールド囲み文字で囲む
 * フィールドを指定するモード            -->  NORMAL(フィールド囲み文字、フィールド区切り文字、改行が存在するフィールドが対象となる)
 * フィールド囲み文字で囲む
 * フィールドのリスト                    -->  空のリスト
 * </pre>
 *
 * @author Naoki Yamamoto
 */
@Published
public class CsvDataBindConfig implements DataBindConfig {

    /** 有効な改行文字 */
    private static final Pattern VALID_LINE_SEPARATOR = Pattern.compile("(\r\n|\n|\r)");

    /** 列区切り文字 */
    private final char fieldSeparator;

    /** 行区切り文字 */
    private final String lineSeparator;

    /** フィールド囲み文字 */
    private final char quote;

    /** 空行を無視するか否か */
    private final boolean ignoreEmptyLine;

    /** ヘッダ行(タイトル行)が必須か否か */
    private final boolean requiredHeader;

    /** 文字コード */
    private final Charset charset;

    /** 出力時にフィールド囲み文字で囲むフィールドを指定するモード */
    private final QuoteMode quoteMode;

    /** 空のフィールドをnullに変換するかどうか */
    private final boolean emptyToNull;

    /** 出力時にフィールド囲み文字で囲むフィールドのリスト */
    private final List<String> quotedColumnNames;

    /** ヘッダーレコードに出力するタイトルリスト */
    private final String[] headerTitles;

    /** プロパティ名リスト */
    private final String[] properties;

    /** デフォルトのフォーマット定義 */
    public static final CsvDataBindConfig DEFAULT = new CsvDataBindConfig(
            ',',                        // フィールドセパレータ
            "\r\n",                     // 改行
            '"',                        // フィールド囲み文字
            true,                       // 空行の扱い
            true,                       // ヘッダー
            new String[0],              // ヘッダーに出力するタイトル
            new String[0],              // プロパティ名リスト
            Charset.forName("UTF-8"),   // 文字コード
            true,                      // 空のフィールドをnullに変換するかどうか
            QuoteMode.NORMAL,           // 出力時にフィールド囲み文字で囲むフィールドを指定するモード
            Collections.<String>emptyList()  // フィールド囲み文字で囲むフィールドのリスト
    );

    /** RFC4180準拠のフォーマット定義 */
    public static final CsvDataBindConfig RFC4180 = DEFAULT.withIgnoreEmptyLine(false)
            .withRequiredHeader(false);

    /** EXCEL形式のCSVフォーマット定義 */
    public static final CsvDataBindConfig EXCEL = DEFAULT.withIgnoreEmptyLine(false)
            .withRequiredHeader(false);

    /** タブ区切り(TSV)のフォーマット定義 */
    public static final CsvDataBindConfig TSV = DEFAULT.withFieldSeparator('\t')
            .withRequiredHeader(false)
            .withIgnoreEmptyLine(false);

    /**
     * CSVのフォーマット定義を生成する。
     *
     * @param fieldSeparator 列区切り文字
     * @param lineSeparator 行区切り文字(\r\n(CRLF) or \r(CR) or \n(LF)であること)
     * @param quote フィールド囲み文字
     * @param ignoreEmptyLine 空行を無視するか否か
     * @param requiredHeader ヘッダ行(タイトル行)が必須か否か
     * @param headerTitles ヘッダーに出力するタイトル
     * @param charset 文字コード
     * @param emptyToNull 空のフィールドをnullに変換するかどうか
     * @param quoteMode 出力時にフィールド囲み文字で囲むフィールドを指定するモード
     * @param quotedColumnNames フィールド囲み文字で囲むフィールドのリスト
     * @throws IllegalArgumentException 行区切り文字が「\r\n(CRLF)・\r(CR)・\n(LF)」以外の場合
     */
    public CsvDataBindConfig(
            final char fieldSeparator,
            final String lineSeparator,
            final char quote,
            final boolean ignoreEmptyLine,
            final boolean requiredHeader,
            final String[] headerTitles,
            final Charset charset,
            final boolean emptyToNull,
            final QuoteMode quoteMode,
            final List<String> quotedColumnNames) {

        if (!VALID_LINE_SEPARATOR.matcher(lineSeparator)
                .matches()) {
            throw new IllegalArgumentException("invalid line separator. must be set '\\r\\n or \\n or \\r'");
        }
        this.fieldSeparator = fieldSeparator;
        this.lineSeparator = lineSeparator;
        this.quote = quote;
        this.ignoreEmptyLine = ignoreEmptyLine;
        this.requiredHeader = requiredHeader;
        this.charset = charset;
        this.emptyToNull = emptyToNull;
        this.headerTitles = headerTitles;
        this.properties = new String[0];
        this.quoteMode = quoteMode;
        this.quotedColumnNames = quotedColumnNames;
    }

    /**
     * CSVのフォーマット定義を生成する。
     *
     * @param fieldSeparator 列区切り文字
     * @param lineSeparator 行区切り文字(\r\n(CRLF) or \r(CR) or \n(LF)であること)
     * @param quote フィールド囲み文字
     * @param ignoreEmptyLine 空行を無視するか否か
     * @param requiredHeader ヘッダ行(タイトル行)が必須か否か
     * @param headerTitles ヘッダーに出力するタイトル
     * @param properties プロパティ名リスト
     * @param charset 文字コード
     * @param emptyToNull 空のフィールドをnullに変換するかどうか
     * @param quoteMode 出力時にフィールド囲み文字で囲むフィールドを指定するモード
     * @param quotedColumnNames フィールド囲み文字で囲むフィールドのリスト
     * @throws IllegalArgumentException 行区切り文字が「\r\n(CRLF)・\r(CR)・\n(LF)」以外の場合
     */
    public CsvDataBindConfig(
            final char fieldSeparator,
            final String lineSeparator,
            final char quote,
            final boolean ignoreEmptyLine,
            final boolean requiredHeader,
            final String[] headerTitles,
            final String[] properties,
            final Charset charset,
            final boolean emptyToNull,
            final QuoteMode quoteMode,
            final List<String> quotedColumnNames) {

        if (!VALID_LINE_SEPARATOR.matcher(lineSeparator)
                .matches()) {
            throw new IllegalArgumentException("invalid line separator. must be set '\\r\\n or \\n or \\r'");
        }
        this.fieldSeparator = fieldSeparator;
        this.lineSeparator = lineSeparator;
        this.quote = quote;
        this.ignoreEmptyLine = ignoreEmptyLine;
        this.requiredHeader = requiredHeader;
        this.charset = charset;
        this.emptyToNull = emptyToNull;
        this.headerTitles = headerTitles;
        this.properties = properties;
        this.quoteMode = quoteMode;
        this.quotedColumnNames = quotedColumnNames;
    }

    /**
     * 列区切り文字を取得する。
     *
     * @return 列区切り文字
     */
    public char getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * 列区切り文字を設定する。
     *
     * @param newFieldSeparator 新しい列区切り文字
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withFieldSeparator(final char newFieldSeparator) {
        return new CsvDataBindConfig(
                newFieldSeparator,
                lineSeparator,
                quote,
                ignoreEmptyLine,
                requiredHeader,
                headerTitles,
                properties,
                charset,
                emptyToNull,
                quoteMode,
                quotedColumnNames);
    }

    /**
     * 行区切り文字を取得する。
     *
     * @return 行区切り文字
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * 改行文字を設定する。
     * <p/>
     * 改行文字が(CR|LF|CRLF)以外の場合はエラーとする。
     *
     * @param newLineSeparator 改行文字
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withLineSeparator(final String newLineSeparator) {
        return new CsvDataBindConfig(
                fieldSeparator,
                newLineSeparator,
                quote,
                ignoreEmptyLine,
                requiredHeader,
                headerTitles,
                properties,
                charset,
                emptyToNull,
                quoteMode,
                quotedColumnNames);
    }

    /**
     * フィールド囲み文字を取得する。
     *
     * @return フィールド囲み文字
     */
    public char getQuote() {
        return quote;
    }

    /**
     * フィールド囲み文字を設定する。
     *
     * @param newQuote フィールド囲み文字
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withQuote(final char newQuote) {
        return new CsvDataBindConfig(
                fieldSeparator,
                lineSeparator,
                newQuote,
                ignoreEmptyLine,
                requiredHeader,
                headerTitles,
                properties,
                charset,
                emptyToNull,
                quoteMode,
                quotedColumnNames);
    }

    /**
     * 空行を無視するか否かを取得する。
     *
     * @return 空行を無視する場合{@code true}
     */
    public boolean isIgnoreEmptyLine() {
        return ignoreEmptyLine;
    }

    /**
     * 空行を無視する。
     *
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withIgnoreEmptyLine() {
        return withIgnoreEmptyLine(true);
    }

    /**
     * 空行を無視するか否かを設定する。
     *
     * @param newOption 空行を無視する場合{@code true}
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withIgnoreEmptyLine(final boolean newOption) {
        return new CsvDataBindConfig(
                fieldSeparator,
                lineSeparator,
                quote,
                newOption,
                requiredHeader,
                headerTitles,
                properties,
                charset,
                emptyToNull,
                quoteMode,
                quotedColumnNames);
    }

    /**
     * ヘッダー行(タイトル行)が必須か否か。
     *
     * @return ヘッダー行(タイトル行)が必須の場合{@code true}
     */
    public boolean isRequiredHeader() {
        return requiredHeader;
    }

    /**
     * ヘッダー行(タイトル行)を必須に設定する。
     *
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withRequiredHeader() {
        return withRequiredHeader(true);
    }

    /**
     * ヘッダー行(タイトル行)を必須とするか否かを設定する。
     *
     * @param newOption ヘッダーが必須な場合{@code true}
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withRequiredHeader(final boolean newOption) {
        return new CsvDataBindConfig(
                fieldSeparator,
                lineSeparator,
                quote,
                ignoreEmptyLine,
                newOption,
                headerTitles,
                properties,
                charset,
                emptyToNull,
                quoteMode,
                quotedColumnNames);
    }

    /**
     * ヘッダー行(タイトル行)に出力するタイトルのリスト。
     *
     * @return ヘッダー行に出力するタイトル
     */
    public String[] getHeaderTitles() {
        return headerTitles;
    }

    /**
     * ヘッダー行(タイトル行)に出力するタイトルを設定する。
     *
     * @param newHeaderTitles ヘッダー行(タイトル行)に出力するタイトル
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withHeaderTitles(final String... newHeaderTitles) {
        return new CsvDataBindConfig(
                fieldSeparator,
                lineSeparator,
                quote,
                ignoreEmptyLine,
                requiredHeader,
                newHeaderTitles,
                properties,
                charset,
                emptyToNull,
                quoteMode,
                quotedColumnNames);
    }

    /**
     * プロパティ名リストを取得する。
     *
     * @return プロパティ名リスト
     */
    public String[] getProperties() {
        return properties;
    }

    /**
     * プロパティ名リストを設定する。
     *
     * @param newProperties プロパティ名リスト
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withProperties(final String... newProperties) {
        return new CsvDataBindConfig(
                fieldSeparator,
                lineSeparator,
                quote,
                ignoreEmptyLine,
                requiredHeader,
                headerTitles,
                newProperties,
                charset,
                emptyToNull,
                quoteMode,
                quotedColumnNames);
    }

    /**
     * オブジェクトにマッピングする際に使用するキーのリストを取得する。
     * <p>
     * {@link #properties}が設定されていれば、{@link #properties}をキーとして返す。<br>
     * {@link #properties}が設定されていなければ、{@link #headerTitles}をキーとして返す。
     * @return キーのリスト
     */
    public String[] getKeys() {
        return StringUtil.hasValue(properties) ? properties : headerTitles;
    }

    /**
     * 文字コードを取得する。
     *
     * @return 文字コード
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * 文字コードを設定する。
     *
     * @param newCharset 文字コード
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withCharset(final String newCharset) {
        return withCharset(Charset.forName(newCharset));
    }

    /**
     * 文字コードを設定する。
     *
     * @param newCharset 文字コード
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withCharset(final Charset newCharset) {
        return new CsvDataBindConfig(
                fieldSeparator,
                lineSeparator,
                quote,
                ignoreEmptyLine,
                requiredHeader,
                headerTitles,
                properties,
                newCharset,
                emptyToNull,
                quoteMode,
                quotedColumnNames);
    }

    /**
     * 空フィールドをnullに置き換えるか否か。
     *
     * @return 置き換える場合は{@code true}
     */
    public boolean isEmptyToNull() {
        return emptyToNull;
    }

    /**
     * 空フィールドをnullに置き換えるか否かを設定する。
     *
     * @param newEmptyToNull nullに置き換える場合は{@code true}
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withEmptyToNull(final boolean newEmptyToNull) {
        return new CsvDataBindConfig(
                fieldSeparator,
                lineSeparator,
                quote,
                ignoreEmptyLine,
                requiredHeader,
                headerTitles,
                properties,
                charset,
                newEmptyToNull,
                quoteMode,
                quotedColumnNames);
    }

    /**
     * フィールド囲み文字で囲むフィールドを取得する。
     *
     * @return フィールド囲み文字で囲むフィールド
     */
    public QuoteMode getQuoteMode() {
        return quoteMode;
    }

    /**
     * 出力時にフィールド囲み文字で囲むフィールドを設定する。
     *
     * @param newQuoteMode フィールド囲み文字で囲むフィールドを指定するモード
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withQuoteMode(final QuoteMode newQuoteMode) {
        return new CsvDataBindConfig(
                fieldSeparator,
                lineSeparator,
                quote,
                ignoreEmptyLine,
                requiredHeader,
                headerTitles,
                properties,
                charset,
                emptyToNull,
                newQuoteMode,
                quotedColumnNames);
    }

    /**
     * 出力時にフィールド囲み文字({@link #getQuote()})で囲むフィールドのリストを取得する。
     *
     * @return フィールド囲み文字で囲むフィールドのリスト
     */
    public List<String> getQuotedColumnNames() {
        return Collections.unmodifiableList(quotedColumnNames);
    }

    /**
     * フィールド囲み文字({@link #getQuote()}で囲むフィールドのリストを設定する。
     * <p/>
     * {@link #getQuoteMode()}が{@link CsvDataBindConfig.QuoteMode#CUSTOM}の場合に、
     * 設定したフィールドがフィールド囲み文字で囲まれる。
     *
     * @param fieldNames フィールド囲み文字で囲むフィールド名称
     * @return 新しい{@link CsvDataBindConfig}
     */
    public CsvDataBindConfig withQuotedColumnNames(final String... fieldNames) {
        return new CsvDataBindConfig(
                fieldSeparator,
                lineSeparator,
                quote,
                ignoreEmptyLine,
                requiredHeader,
                headerTitles,
                properties,
                charset,
                emptyToNull,
                quoteMode,
                Arrays.asList(fieldNames));
    }

    /**
     * 出力時にフィールド囲み文字で囲むフィールドを指定するモードの定義。
     */
    @Published
    public enum QuoteMode {
        /** フィールド囲み文字、フィールド区切り文字、改行が存在するフィールドが対象となるモード */
        NORMAL,
        /** 全てのフィールドが対象となるモード */
        ALL,
        /** 非数値型(Numberクラス以外)の型が対象となるモード */
        NOT_NUMERIC,
        /** フィールドを任意に定義するモード */
        CUSTOM
    }

    /**
     * コンフィグの妥当性検証を行う。
     * <p/>
     * 以下の場合に検証エラーとする。
     * <ul>
     *     <li>ヘッダが必須でヘッダタイトルが未設定</li>
     *     <li>ヘッダが任意でプロパティ名が未設定</li>
     *     <li>ヘッダが必須でヘッダタイトルとプロパティ名のサイズが一致しない</li>
     * </ul>
     */
    public void verify() {
        if (requiredHeader && StringUtil.isNullOrEmpty(headerTitles)) {
            throw new IllegalArgumentException("csv header is required.");
        }

        if (!requiredHeader && StringUtil.isNullOrEmpty(properties)) {
            throw new IllegalArgumentException("csv header or property is required.");
        }

        if (requiredHeader && StringUtil.hasValue(properties) && headerTitles.length != properties.length) {
            throw new IllegalArgumentException("csv header size and property size does not match.");
        }
    }
}
