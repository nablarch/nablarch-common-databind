package nablarch.common.databind.fixedlength;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import nablarch.core.util.annotation.Published;

/**
 * 固定長のフォーマットを現す{@link FixedLengthDataBindConfig}を構築するクラス。
 *
 * @author siosio
 */
@Published
public class FixedLengthDataBindConfigBuilder {

    /** レコードの長さ(バイト数) */
    private int length;

    /** 文字セット */
    private Charset charset;

    /** 改行を現す文字 */
    private String lineSeparator;

    /** レコードの定義 */
    private final Map<String, RecordConfig> recordConfigMap = new HashMap<String, RecordConfig>();

    /**
     * 隠蔽コンストラクタ。
     */
    private FixedLengthDataBindConfigBuilder() {
    }

    /**
     * 新しいビルダーを生成する。
     *
     * @return 新しいビルダー
     */
    public static FixedLengthDataBindConfigBuilder newBuilder() {
        return new FixedLengthDataBindConfigBuilder();
    }

    /**
     * レコードの長さを設定する。
     *
     * @param length レコードの長さ
     * @return 自身のインスタンス
     */
    public FixedLengthDataBindConfigBuilder length(final int length) {
        this.length = length;
        return this;
    }

    /**
     * 文字セットを設定する。
     *
     * @param charset 文字セット
     * @return 自身のインスタンス
     */
    public FixedLengthDataBindConfigBuilder charset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 改行をあらわす文字を設定する。
     *
     * @param lineSeparator 改行をあらわす文字
     * @return 自身のインスタンス
     */
    public FixedLengthDataBindConfigBuilder lineSeparator(final String lineSeparator) {
        this.lineSeparator = lineSeparator;
        return this;
    }

    /**
     * レコードの定義を設定する。
     *
     * @param recordConfig レコードの定義
     * @return 自身のインスタンス
     */
    public FixedLengthDataBindConfigBuilder addRecord(final RecordConfig recordConfig) {
        recordConfigMap.put(RecordConfig.SINGLE_LAYOUT_RECORD_NAME, recordConfig);
        return this;
    }

    /**
     * 与えられた情報を元に{@link FixedLengthDataBindConfig}を生成して返す。
     *
     * @return {@code FixedLengthDataBindConfig}
     */
    public FixedLengthDataBindConfig build() {
        verifyFile();
        verifyRecordConfig();
        return new FixedLengthDataBindConfig(length, charset, lineSeparator, recordConfigMap);
    }

    /**
     * 固定長定義部の正しさを検証する。
     */
    private void verifyFile() {
        if (length <= 0) {
            throw new IllegalStateException("length is invalid. must set greater than 0.");
        }
    }

    /**
     * レコード定義の正しさを検証する。
     */
    private void verifyRecordConfig() {
        for (final Map.Entry<String, RecordConfig> entry : recordConfigMap.entrySet()) {
            final String recordName = entry.getKey();
            final RecordConfig recordConfig = entry.getValue();

            int expectedOffset = 1;
            FieldConfig lastField = null;
            for (final FieldConfig fieldConfig : recordConfig.getFieldConfigList()) {
                if (expectedOffset != fieldConfig.getOffset()) {
                    throw new IllegalStateException(
                            "field offset is invalid." 
                                    + " record_name:" + recordName
                                    + ", field_name:" + fieldConfig.getName()
                                    + ", expected offset:" + expectedOffset + " but was " + fieldConfig.getOffset());
                }
                expectedOffset += fieldConfig.getLength();
                lastField = fieldConfig;
            }

            if (lastField == null) {
                throw new IllegalStateException("field was not found. record_name:" + recordName);
            }
            if (length != lastField.getOffset() + lastField.getLength() - 1) {
                throw new IllegalStateException(
                        "field length is invalid."
                                + " record_name:" + recordName
                                + ", field_name:" + lastField.getName()
                                + ", expected length:" + (length - lastField.getOffset() + 1) + " but was "
                                + lastField.getLength());
            }
        }
    }
}
