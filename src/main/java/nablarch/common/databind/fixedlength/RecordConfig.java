package nablarch.common.databind.fixedlength;

import java.util.Collections;
import java.util.List;

/**
 * レコードの定義をあらわすクラス。
 *
 * @author siosio
 */
public class RecordConfig {

    /** シングルレイアウトの場合に使用するレコード名 */
    public static final String SINGLE_LAYOUT_RECORD_NAME = "single";

    /** レコード名 */
    private final String recordName;

    /**
     * このレコード内のフィールドの定義。
     */
    private final List<FieldConfig> fieldConfigList;

    /**
     * レコードの定義を構築する。
     *
     * @param recordName レコード名
     * @param fieldConfigList フィールドの定義
     */
    public RecordConfig(final String recordName, final List<FieldConfig> fieldConfigList) {
        this.recordName = recordName;
        this.fieldConfigList = Collections.unmodifiableList(fieldConfigList);
    }

    /**
     * レコードの定義を構築する。
     *
     * @param fieldConfigList フィールドの定義
     */
    public RecordConfig(final List<FieldConfig> fieldConfigList) {
        recordName = SINGLE_LAYOUT_RECORD_NAME;
        this.fieldConfigList = Collections.unmodifiableList(fieldConfigList);
    }

    /**
     * このレコードのレコード名を返す。
     * @return このレコードのレコード名
     */
    public String getRecordName() {
        return recordName;
    }

    /**
     * このレコードのフィールド定義を返す。
     * @return このレコードのフィールド定義
     */
    public List<FieldConfig> getFieldConfigList() {
        return fieldConfigList;
    }
}
