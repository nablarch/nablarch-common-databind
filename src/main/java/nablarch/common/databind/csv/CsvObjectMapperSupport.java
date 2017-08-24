package nablarch.common.databind.csv;

import java.io.BufferedReader;
import java.io.Reader;

import nablarch.common.databind.ObjectMapper;
import nablarch.core.util.FileUtil;
import nablarch.core.util.StringUtil;

/**
 * CSVをオブジェクトにマッピングするのをサポートするクラス。
 *
 * @param <T> マッピング対象のクラス
 * @author Naoki Yamamoto
 */
public abstract class CsvObjectMapperSupport<T> implements ObjectMapper<T> {

    /** CSV用の設定情報 */
    protected final CsvDataBindConfig config;

    /** プロパティ名の配列 */
    protected final String[] properties;

    /** CSVのリーダ */
    protected final CsvDataReader reader;

    /**
     * CSV定義と入力リソースを持つ{@code AbstractCsvMapper}を生成する。
     *
     * @param config CSVの定義
     * @param reader 入力リソース
     */
    public CsvObjectMapperSupport(final CsvDataBindConfig config, final Reader reader) {
        this.config = config;
        this.properties = getProperties();
        this.reader = new CsvDataReader(toBufferedReader(reader), config);
    }

    /**
     * {@link BufferedReader}に変換する。
     *
     * @param reader リーダ
     * @return {@link BufferedReader}
     */
    public BufferedReader toBufferedReader(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    @Override
    public void write(final T object) {
        throw new UnsupportedOperationException("unsupported write method.");
    }

    @Override
    public T read() {
        final String[] record = readLine();
        if (record == null) {
            return null;
        }
        return createObject(record);
    }

    /**
     * ヘッダが必須の場合、ヘッダー行を読み込む。
     * <p>
     * ヘッダが必須でない場合は読み込みは行わず{@code null}を返す。
     *
     * @return ヘッダー行
     */
    protected String[] readHeader() {
        return config.isRequiredHeader() ? readLine() : null;
    }

    /**
     * レコードをJavaObjectに変換する。
     *
     * @param record レコード
     * @return 変換したJavaObject
     */
    protected abstract T createObject(String[] record);

    /**
     * 1レコード文の情報を読み取る。
     *
     * @return 1行の情報
     */
    protected String[] readLine() {
        String[] record = reader.read();
        while (config.isIgnoreEmptyLine() && isEmptyLine(record)) {
            record = reader.read();
        }
        return record;
    }

    /**
     * ストリームを閉じてリソースを解放する。
     */
    @Override
    public void close() {
        FileUtil.closeQuietly(reader);
    }

    /**
     * レコードが空行か否か。
     *
     * @param record 1レコード文のデータ
     * @return 空行であれば{@code true}
     */
    private static boolean isEmptyLine(final String[] record) {
        return record != null && record.length == 1 && StringUtil.isNullOrEmpty(record[0]);
    }

    /**
     * オブジェクトにマッピングする際に使用するプロパティ名のリストを取得する。
     * @return プロパティ名リスト
     */
    protected String[] getProperties() {
        if (StringUtil.hasValue(config.getProperties())) {
            return config.getProperties();
        } else {
            return config.getHeaderTitles();
        }
    }
}

