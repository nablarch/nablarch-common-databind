package nablarch.common.databind.fixedlength;

import nablarch.common.databind.fixedlength.MultiLayoutConfig.RecordIdentifier;
import nablarch.core.util.annotation.Published;

/**
 * マルチレイアウトな固定長データを表すクラス。
 *
 * @author Naoki Yamamoto
 */
@Published
public abstract class MultiLayout {

    /** レコード名 */
    private MultiLayoutConfig.RecordName recordName;

    /**
     * レコード名を取得する。
     *
     * @return レコード名
     */
    public MultiLayoutConfig.RecordName getRecordName() {
        return recordName;
    }

    /**
     * レコード名を設定する。
     *
     * @param recordName レコード名
     */
    public void setRecordName(final MultiLayoutConfig.RecordName recordName) {
        this.recordName = recordName;
    }

    /**
     * レコード識別クラスを取得する。
     * @return レコード識別クラス
     */
    public abstract RecordIdentifier getRecordIdentifier();
}
