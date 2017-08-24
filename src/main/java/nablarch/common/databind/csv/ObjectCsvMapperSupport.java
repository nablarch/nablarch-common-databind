package nablarch.common.databind.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import nablarch.common.databind.ObjectMapper;
import nablarch.core.util.FileUtil;
import nablarch.core.util.StringUtil;

/**
 * オブジェクトをCSVにマッピングするのをサポートするクラス。
 *
 * @param <T> マッピング対象のクラス
 * @author Naoki Yamamoto
 */
public abstract class ObjectCsvMapperSupport<T> implements ObjectMapper<T> {

    /** CSV用の設定情報 */
    protected final CsvDataBindConfig config;

    /** プロパティ名のリスト */
    protected final String[] properties;

    /** 1レコードずつ書き込むライター */
    private final CsvDataWriter writer;

    /**
     * コンストラクタ。
     *
     * @param config フォーマット定義
     * @param writer 出力リソース
     */
    public ObjectCsvMapperSupport(final CsvDataBindConfig config, final Writer writer) {
        this.config = config;
        this.properties = getProperties();
        this.writer = new CsvDataWriter(toBufferedWriter(writer), config, properties);
    }

    /**
     * オブジェクトのプロパティ名のリストを取得する。
     * @return プロパティ名リスト
     */
    protected String[] getProperties() {
        if (StringUtil.hasValue(config.getProperties())) {
            return config.getProperties();
        } else {
            return config.getHeaderTitles();
        }
    }

    /**
     * ヘッダーレコードを書き込む。
     */
    protected void writeHeader() {
        if (!config.isRequiredHeader()) {
            return;
        }
        try {
            writer.write(config.getHeaderTitles());
        } catch (IOException e) {
            throw new RuntimeException("failed to writer header.", e);
        }
    }

    /**
     * {@link BufferedWriter}を生成する。
     *
     * @param writer {@link Writer}
     * @return {@link BufferedWriter}
     */
    private static BufferedWriter toBufferedWriter(final Writer writer) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
    }

    @Override
    public T read() {
        throw new UnsupportedOperationException("unsupported read method.");
    }

    @Override
    public void write(T object) {
        try {
            writer.write(convertValues(object));
        } catch (IOException e) {
            throw new RuntimeException("failed to write.", e);
        }
    }

    /**
     * JavaオブジェクトをCSVに出力するための{@link Object}配列に変換する。
     * <p/>
     * 変換するObject配列は、CSVファイルに出力する要素順に並べる必要がある。
     *
     * @param object Javaオブジェクト
     * @return CSV出力用のObject配列
     */
    protected abstract Object[] convertValues(T object);

    /**
     * ストリームを閉じてリソースを解放する。
     */
    @Override
    public void close() {
        FileUtil.closeQuietly(writer);
    }
}

