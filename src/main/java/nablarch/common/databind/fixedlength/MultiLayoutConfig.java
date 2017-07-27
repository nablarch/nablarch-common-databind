package nablarch.common.databind.fixedlength;

/**
 * マルチレイアウトの定義を表すクラス
 *
 * @author Naoki Yamamoto
 */
public class MultiLayoutConfig {

    /** レコード識別クラス */
    private final RecordIdentifier recordIdentifier;

    /**
     * マルチレイアウトの定義を表すクラスのインスタンスを生成する。
     *
     * @param recordIdentifier レコード識別クラス
     */
    public MultiLayoutConfig(final RecordIdentifier recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }

    /**
     * レコード識別クラスを取得する。
     * @return レコード識別クラス
     */
    public RecordIdentifier getRecordIdentifier() {
        return recordIdentifier;
    }

    /**
     * マルチレイアウトな固定長データのレコードを識別するインタフェース。
     */
    public interface RecordIdentifier {
        /**
         * レコードを識別する。
         * @param record レコード情報
         * @return レコード名
         */
        RecordName identify(byte[] record);
    }

    /**
     * レコード名を扱うインタフェース
     */
    public interface RecordName {
        /**
         * レコード名を取得する。
         * @return レコード名
         */
        String getRecordName();
    }
}
