package nablarch.common.databind.fixedlength;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nablarch.common.databind.fixedlength.converter.DefaultConverter;

/**
 * レコード定義を構築する。
 *
 * @author Naoki Yamamoto
 */
public class RecordBuilder {

    /** フィールド定義のリスト */
    private final List<FieldConfig> fieldConfigList = new ArrayList<FieldConfig>();

    /**
     * フィールドを追加する。
     * @param name フィールド名
     * @param offset オフセット
     * @param length 長さ
     * @return 本インスタンス
     */
    public RecordBuilder addField(final String name, final int offset, final int length) {
        return addField(name, offset, length, new DefaultConverter());
    }

    /**
     * フィールドを追加する。
     * @param name フィールド名
     * @param offset オフセット
     * @param length 長さ
     * @param converter フィールドコンバータ
     * @return 本インスタンス
     */
    public RecordBuilder addField(final String name, final int offset, final int length, final FieldConvert.FieldConverter converter) {
        fieldConfigList.add(new FieldConfig(name, offset, length, converter));
        return this;
    }

    /**
     * レコード定義を構築する。
     * @return レコード定義
     */
    public RecordConfig build() {
        Collections.sort(fieldConfigList, new FieldConfigComparator());
        return new RecordConfig(fieldConfigList);
    }

    /**
     * フィールドのオフセットを基準に比較を行うクラス。
     */
    @SuppressWarnings("ComparatorNotSerializable")
    private static class FieldConfigComparator implements Comparator<FieldConfig> {

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
