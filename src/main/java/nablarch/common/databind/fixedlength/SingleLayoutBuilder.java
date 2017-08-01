package nablarch.common.databind.fixedlength;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.common.databind.fixedlength.converter.DefaultConverter;

/**
 * シングルレイアウト用の設定構築クラス。
 *
 * @author Naoki Yamamoto
 */
public class SingleLayoutBuilder extends LayoutBuilderSupport {

    /** フィールド定義のリスト */
    private final List<FieldConfig> fieldConfigList = new ArrayList<FieldConfig>();

    /**
     * {@link FixedLengthDataBindConfigBuilder}をもとに本クラスを構築する。
     * @param dataBindConfigBuilder 固定長データ用のデータバインドコンフィグ構築クラス
     */
    public SingleLayoutBuilder(final FixedLengthDataBindConfigBuilder dataBindConfigBuilder) {
        super(dataBindConfigBuilder);
    }

    @Override
    public SingleLayoutBuilder field(final String name, final int offset, final int length) {
        return field(name, offset, length, new DefaultConverter());
    }

    @Override
    public SingleLayoutBuilder field(final String name, final int offset, final int length, final FieldConvert.FieldConverter converter) {
        fieldConfigList.add(new FieldConfig(name, offset, length, converter));
        return this;
    }

    @Override
    public FixedLengthDataBindConfig build() {
        Collections.sort(fieldConfigList, new FieldConfigComparator());
        Map<String, RecordConfig> recordConfigMap = new HashMap<String, RecordConfig>();
        recordConfigMap.put(RecordConfig.SINGLE_LAYOUT_RECORD_NAME, new RecordConfig(RecordConfig.SINGLE_LAYOUT_RECORD_NAME, fieldConfigList));
        return dataBindConfigBuilder.build(recordConfigMap);
    }
}
