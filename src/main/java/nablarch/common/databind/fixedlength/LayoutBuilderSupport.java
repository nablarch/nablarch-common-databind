package nablarch.common.databind.fixedlength;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import nablarch.common.databind.fixedlength.converter.DefaultConverter;

/**
 * シングルレイアウトやマルチレイアウト用の設定を構築するクラスのサポートクラス。
 *
 * @author Naoki Yamamoto
 */
public abstract class LayoutBuilderSupport {

    /** レコードの長さ(バイト数) */
    protected final int length;

    /** 文字セット */
    protected final Charset charset;

    /** 改行を現す文字 */
    protected final String lineSeparator;

    /** 未定義部の埋め文字 */
    protected final char fillChar;

    /**
     * 与えられた情報をもとに本クラスのインスタンスを生成する。
     * @param length レコードの長さ
     * @param charset 文字セット
     * @param lineSeparator 改行を表す文字
     * @param fillChar 未定義部の埋め文字
     */
    public LayoutBuilderSupport(final int length, final Charset charset, final String lineSeparator, final char fillChar) {
        this.length = length;
        this.charset = charset;
        this.lineSeparator = lineSeparator;
        this.fillChar = fillChar;
    }

    /**
     * フィールドを追加する。
     * @param name フィールド名
     * @param offset オフセット
     * @param length 長さ
     * @return 本インスタンス
     */
    public abstract LayoutBuilderSupport field(final String name, final int offset, final int length);

    /**
     * フィールドを追加する。
     * @param name フィールド名
     * @param offset オフセット
     * @param length 長さ
     * @param converter フィールドコンバータ
     * @return 本インスタンス
     */
    public abstract LayoutBuilderSupport field(final String name, final int offset, final int length, final FieldConvert.FieldConverter converter);

    /**
     * 与えられた情報を元に{@link FixedLengthDataBindConfig}を生成して返す。
     *
     * @return {@code FixedLengthDataBindConfig}
     */
    public abstract FixedLengthDataBindConfig build();

    /**
     * filler用の{@link FieldConfig}を生成してフィールド定義リストに追加する。
     *
     * @param fieldConfigList フィールド定義リスト
     */
    protected void addFillerFieldConfig(final List<FieldConfig> fieldConfigList) {
        Collections.sort(fieldConfigList, new FieldConfigComparator());
        final List<FieldConfig> fillers = new ArrayList<FieldConfig>();
        int position = 1;
        for (FieldConfig fieldConfig : fieldConfigList) {
            final int fillSize = fieldConfig.getOffset() - position;
            if (fillSize > 0) {
                fillers.add(new FieldConfig("filler", position, fillSize, new DefaultConverter()));
            }
            position = fieldConfig.getOffset() + fieldConfig.getLength();
        }
        final int fillSize = (length + 1) - position;
        if (fillSize > 0) {
            fillers.add(new FieldConfig("filler", position, fillSize, new DefaultConverter()));
        }
        fieldConfigList.addAll(fillers);
        Collections.sort(fieldConfigList, new FieldConfigComparator());
    }

    /**
     * 固定長定義部の正しさを検証する。
     */
    protected void verifyFile() {
        if (length <= 0) {
            throw new IllegalStateException("length is invalid. must set greater than 0.");
        }
    }

    /**
     * レコード定義の正しさを検証する。
     *
     * @param recordConfigMap レコード定義のマップ
     */
    protected void verifyRecordConfig(final Map<String, RecordConfig> recordConfigMap) {
        for (final Map.Entry<String, RecordConfig> entry : recordConfigMap.entrySet()) {
            final String recordName = entry.getKey();
            final RecordConfig recordConfig = entry.getValue();

            int expectedOffset = 1;
            FieldConfig lastField = null;
            for (final FieldConfig fieldConfig : recordConfig.getFieldConfigList()) {
                if (FieldConfig.FILLER_FIELD_NAME.equals(fieldConfig.getName())) {
                    continue;
                }

                if (expectedOffset > fieldConfig.getOffset()) {
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
            if (length < lastField.getOffset() + lastField.getLength() - 1) {
                throw new IllegalStateException(
                        "field length is invalid."
                                + " record_name:" + recordName
                                + ", field_name:" + lastField.getName()
                                + ", expected length:" + (length - lastField.getOffset() + 1) + " but was "
                                + lastField.getLength());
            }
        }
    }

    /**
     * フィールドのオフセットを基準に比較を行うクラス。
     */
    @SuppressWarnings("ComparatorNotSerializable")
    protected static class FieldConfigComparator implements Comparator<FieldConfig> {

        @Override
        public int compare(final FieldConfig o1, final FieldConfig o2) {
            final int first = o1.getOffset();
            final int second = o2.getOffset();

            if (first < second) {
                return -1;
            } else if (first > second) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
