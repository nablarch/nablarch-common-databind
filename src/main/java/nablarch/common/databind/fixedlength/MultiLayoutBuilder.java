package nablarch.common.databind.fixedlength;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.common.databind.fixedlength.converter.DefaultConverter;
import nablarch.core.util.annotation.Published;

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
     * 与えられた情報をもとに本クラスのインスタンスを生成する。
     * @param length レコードの長さ
     * @param charset 文字セット
     * @param lineSeparator 改行を表す文字
     * @param fillChar 未定義部の埋め文字
     */
    public MultiLayoutBuilder(final int length, final Charset charset, final String lineSeparator, final char fillChar) {
        super(length, charset, lineSeparator, fillChar);
    }

    @Override
    @Published
    public MultiLayoutBuilder field(final String name, final int offset, final int length) {
        return field(name, offset, length, new DefaultConverter());
    }

    @Override
    @Published
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
    @Published
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
    @Published
    public MultiLayoutBuilder recordIdentifier(final MultiLayoutConfig.RecordIdentifier recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
        return this;
    }

    @Override
    @Published
    public FixedLengthDataBindConfig build() {
        if (recordIdentifier == null) {
            throw new IllegalStateException("record identifier is undefined.");
        }

        final Map<String, RecordConfig> recordConfigMap = new HashMap<String, RecordConfig>();
        for (Map.Entry<String, List<FieldConfig>> entry : fieldConfigMap.entrySet()) {
            addFillerFieldConfig(entry.getValue());
            recordConfigMap.put(entry.getKey(), new RecordConfig(entry.getKey(), entry.getValue()));
        }

        verifyFile();
        verifyRecordConfig(recordConfigMap);
        return new FixedLengthDataBindConfig(
                length, charset, lineSeparator, fillChar, recordConfigMap, new MultiLayoutConfig(recordIdentifier));
    }
}
