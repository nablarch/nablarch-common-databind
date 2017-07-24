package nablarch.common.databind.fixedlength;

import nablarch.common.databind.fixedlength.FieldConvert.FieldConverter;

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

    /** コンバータ */
    private final FieldConvert.FieldConverter fieldConverter;

    /**
     * フィールド定義を構築する。
     *
     * @param name フィールド名
     * @param offset 開始位置(1始まり)
     * @param length 長さ(バイト数)
     * @param fieldConverter 入出力時の変換を行うコンバータ
     */
    public FieldConfig(
            final String name, final int offset, final int length, final FieldConvert.FieldConverter fieldConverter) {
        this.name = name;
        this.offset = offset;
        this.length = length;
        this.fieldConverter = fieldConverter;
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
     * フィールドコンバータを返す。
     *
     * @return フィールドコンバータ
     */
    public FieldConverter getFieldConverter() {
        return fieldConverter;
    }
}
