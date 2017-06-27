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

    /**
     * このレコード内のフィールドの定義。
     */
    private final List<FieldConfig> fieldConfigList;

    /**
     * レコードの定義を構築する。
     *
     * @param fieldConfigList フィールドの定義
     */
    public RecordConfig(final List<FieldConfig> fieldConfigList) {
        this.fieldConfigList = Collections.unmodifiableList(fieldConfigList);
    }

    /**
     * このレコードのフィールド定義を返す。
     * @return このレコードのフィールド定義
     */
    public List<FieldConfig> getFieldConfigList() {
        return fieldConfigList;
    }
}
