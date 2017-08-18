package nablarch.common.databind.fixedlength;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.common.databind.fixedlength.converter.DefaultConverter;
import nablarch.core.util.annotation.Published;

/**
 * シングルレイアウト用の設定構築クラス。
 *
 * @author Naoki Yamamoto
 */
public class SingleLayoutBuilder extends LayoutBuilderSupport {

    /** フィールド定義のリスト */
    private final List<FieldConfig> fieldConfigList = new ArrayList<FieldConfig>();

    /**
     * 与えられた情報をもとに本クラスのインスタンスを生成する。
     * @param length レコードの長さ
     * @param charset 文字セット
     * @param lineSeparator 改行を表す文字
     * @param fillChar 未定義部の埋め文字
     */
    public SingleLayoutBuilder(final int length, final Charset charset, final String lineSeparator, final char fillChar) {
        super(length, charset, lineSeparator, fillChar);
    }

    @Override
    @Published
    public SingleLayoutBuilder field(final String name, final int offset, final int length) {
        return field(name, offset, length, new DefaultConverter());
    }

    @Override
    @Published
    public SingleLayoutBuilder field(final String name, final int offset, final int length, final FieldConvert.FieldConverter converter) {
        fieldConfigList.add(new FieldConfig(name, offset, length, converter));
        return this;
    }

    @Override
    @Published
    public FixedLengthDataBindConfig build() {
        addFillerFieldConfig(fieldConfigList);
        final Map<String, RecordConfig> recordConfigMap = new HashMap<String, RecordConfig>();
        recordConfigMap.put(RecordConfig.SINGLE_LAYOUT_RECORD_NAME, new RecordConfig(RecordConfig.SINGLE_LAYOUT_RECORD_NAME, fieldConfigList));

        verifyFile();
        verifyRecordConfig(recordConfigMap);
        return new FixedLengthDataBindConfig(length, charset, lineSeparator, fillChar, recordConfigMap);
    }
}
