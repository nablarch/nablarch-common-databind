package nablarch.common.databind.fixedlength;

import nablarch.common.databind.fixedlength.MultiLayoutConfig.RecordIdentifier;

/**
 * マルチレイアウトな固定長データを表すクラス。
 *
 * @author Naoki Yamamoto
 */
public abstract class MultiLayout {

    /** レコード名 */
    private String recordName;

    /**
     * レコード名を取得する。
     *
     * @return レコード名
     */
    public String getRecordName() {
        return recordName;
    }

    /**
     * レコード名を設定する。
     *
     * @param recordName レコード名
     */
    public void setRecordName(final String recordName) {
        this.recordName = recordName;
    }

    /**
     * レコード識別クラスを取得する。
     * @return レコード識別クラス
     */
    public abstract RecordIdentifier getRecordIdentifier();
}
