package nablarch.common.databind.fixedlength;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.common.databind.fixedlength.converter.DefaultConverter;

/**
 * マルチレイアウト用の設定構築クラス。
 *
 * @author Naoki Yamamoto
 */
public class MultiLayoutBuilder extends LayoutBuilderSupport {

    /** レコード識別クラス */
    private MultiLayoutConfig.RecordIdentifier recordIdentifier;

    /** レコード毎のフィールド定義リスト */
    private final Map<String, List<FieldConfig>> fieldConfigMap = new HashMap<String, List<FieldConfig>>();

    /** レコード名 */
    private String recordName;

    /**
     * {@link FixedLengthDataBindConfigBuilder}をもとに本クラスを構築する。
     * @param dataBindConfigBuilder 固定長データ用のデータバインドコンフィグ構築クラス
     */
    public MultiLayoutBuilder(final FixedLengthDataBindConfigBuilder dataBindConfigBuilder) {
        super(dataBindConfigBuilder);
    }

    @Override
    public MultiLayoutBuilder field(final String name, final int offset, final int length) {
        return field(name, offset, length, new DefaultConverter());
    }

    @Override
    public MultiLayoutBuilder field(final String name, final int offset, final int length, final FieldConvert.FieldConverter converter) {
        if (recordName == null) {
            throw new IllegalStateException("must be calling record method before calling field method.");
        }
        fieldConfigMap.get(recordName).add(new FieldConfig(name, offset, length, converter));
        return this;
    }

    /**
     * レコードを追加する。
     * @param name レコード名
     * @return 本インスタンス
     */
    public MultiLayoutBuilder record(final String name) {
        fieldConfigMap.put(name, new ArrayList<FieldConfig>());
        recordName = name;
        return this;
    }

    /**
     * レコード識別クラスを設定する。
     * @param recordIdentifier レコード識別クラス
     * @return 本インスタンス
     */
    public MultiLayoutBuilder recordIdentifier(final MultiLayoutConfig.RecordIdentifier recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
        return this;
    }

    @Override
    public FixedLengthDataBindConfig build() {
        if (recordIdentifier == null) {
            throw new IllegalStateException("record identifier is undefined.");
        }

        Map<String, RecordConfig> recordConfigMap = new HashMap<String, RecordConfig>();
        for (Map.Entry<String, List<FieldConfig>> entry : fieldConfigMap.entrySet()) {
            Collections.sort(entry.getValue(), new FieldConfigComparator());
            recordConfigMap.put(entry.getKey(), new RecordConfig(entry.getKey(), entry.getValue()));
        }
        return dataBindConfigBuilder.build(recordConfigMap, recordIdentifier);
    }
}
