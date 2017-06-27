package nablarch.common.databind.fixedlength;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import nablarch.common.databind.DataBindConfig;

/**
 * 固定長のフォーマットをあらわすクラス。
 *
 * @author siosio
 */
public class FixedLengthDataBindConfig implements DataBindConfig {

    /** レコードの長さ(バイト数) */
    private final int length;

    /** 固定長データの文字セット */
    private final Charset charset;

    /** 改行をあらす文字 */
    private final String lineSeparator;

    /** レコードの定義 */
    private final Map<String, RecordConfig> recordConfigs;

    /**
     * 固定長のフォーマットを構築する。
     *
     * @param length レコードの長さ(バイト数)
     * @param charset 文字セット
     * @param lineSeparator 改行をあらす文字
     * @param recordConfigs レコードの定義
     */
    public FixedLengthDataBindConfig(
            final int length,
            final Charset charset,
            final String lineSeparator,
            final Map<String, RecordConfig> recordConfigs) {

        this.length = length;
        this.charset = charset;
        this.lineSeparator = lineSeparator;
        this.recordConfigs = Collections.unmodifiableMap(recordConfigs);
    }

    /**
     * レコードの長さ(バイト数)を返す。
     *
     * @return レコードの長さ(バイト数)
     */
    public int getLength() {
        return length;
    }

    /**
     * 文字セットを返す。
     *
     * @return 文字セット
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * 改行をあらわす文字を返す。
     *
     * @return 改行をあらわす文字
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * レコードの定義を返す。
     *
     * @return レコードの定義
     */
    public RecordConfig getRecordConfig() {
        return recordConfigs.get(RecordConfig.SINGLE_LAYOUT_RECORD_NAME);
    }
}
