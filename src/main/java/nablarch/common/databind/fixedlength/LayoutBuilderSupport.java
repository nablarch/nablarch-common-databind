package nablarch.common.databind.fixedlength;

import java.util.Comparator;

import nablarch.common.databind.fixedlength.converter.DefaultConverter;

/**
 * シングルレイアウトやマルチレイアウト用の設定を構築するクラスのサポートクラス。
 *
 * @author Naoki Yamamoto
 */
public abstract class LayoutBuilderSupport {

    /** データバインドコンフィグのビルダー */
    protected final FixedLengthDataBindConfigBuilder dataBindConfigBuilder;

    /**
     * {@link FixedLengthDataBindConfigBuilder}をもとに本クラスを構築する。
     * @param dataBindConfigBuilder 固定長データ用のデータバインドコンフィグ構築クラス
     */
    public LayoutBuilderSupport(final FixedLengthDataBindConfigBuilder dataBindConfigBuilder) {
        this.dataBindConfigBuilder = dataBindConfigBuilder;
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
