package nablarch.common.databind.fixedlength;

import java.util.Arrays;

/**
 * フィールドの定義をあらわすクラス。
 *
 * @author siosio
 */
public class FieldConfig {

    /** フィールド名 */
    private final String name;

    /** 開始位置(1始まり) */
    private final int offset;

    /** 長さ(バイト数) */
    private final int length;

    /**
     * フィールド定義を構築する。
     *
     * @param name フィールド名
     * @param offset 開始位置(1始まり)
     * @param length 長さ(バイト数)
     */
    public FieldConfig(final String name, final int offset, final int length) {
        this.name = name;
        this.offset = offset;
        this.length = length;
    }

    /**
     * フィールド名を返す。
     *
     * @return フィールド名
     */
    public String getName() {
        return name;
    }

    /**
     * 開始位置を返す。
     *
     * @return 開始位置(1始まり)
     */
    public int getOffset() {
        return offset;
    }

    /**
     * 長さ(バイト数)を返す。
     *
     * @return 長さ(バイト数)
     */
    public int getLength() {
        return length;
    }

    /**
     * バイト配列から自身のフィールド部分を抜き出し返却する。
     * 
     * @param record レコード情報
     * @param fixedLengthDataBindConfig 固定長の設定値
     * @return 読み込んだ値
     */
    public Object readValue(final byte[] record, final FixedLengthDataBindConfig fixedLengthDataBindConfig) {
        final int zeroOffset = offset - 1;
        final byte[] fieldValue = Arrays.copyOfRange(record, zeroOffset, zeroOffset + length);
        return new String(fieldValue, fixedLengthDataBindConfig.getCharset());
    }
    
    
}
