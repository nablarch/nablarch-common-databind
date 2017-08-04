package nablarch.common.databind.fixedlength;

import java.nio.charset.Charset;

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

    /** 未定義部の埋め文字 */
    private char fillChar = ' ';

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
     * 未定義部の埋め文字を設定する。
     *
     * @param fillChar 未定義部の埋め文字
     * @return 自身のインスタンス
     */
    public FixedLengthDataBindConfigBuilder fillChar(final char fillChar) {
        this.fillChar = fillChar;
        return this;
    }

    /**
     * シングルレイアウト用の{@link FixedLengthDataBindConfig}を構築する。
     * @return シングルレイアウト用の{@link FixedLengthDataBindConfig}を構築するクラス
     */
    public SingleLayoutBuilder singleLayout() {
        return new SingleLayoutBuilder(length, charset, lineSeparator, fillChar);
    }

    /**
     * マルチレイアウト用の{@link FixedLengthDataBindConfig}を構築する。
     * @return マルチレイアウト用の{@link FixedLengthDataBindConfig}を構築するクラス
     */
    public MultiLayoutBuilder multiLayout() {
        return new MultiLayoutBuilder(length, charset, lineSeparator, fillChar);
    }
}
