package nablarch.common.databind;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

import nablarch.common.databind.csv.BeanCsvMapper;
import nablarch.common.databind.csv.CsvBeanMapper;
import nablarch.common.databind.csv.CsvDataBindConfig;
import nablarch.common.databind.csv.CsvMapMapper;
import nablarch.common.databind.csv.MapCsvMapper;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.annotation.Published;

/**
 * {@link ObjectMapper}を生成するクラス。
 *
 * ObjectMapper生成に利用するファクトリクラス({@link ObjectMapperFactory})の実装クラスは、以下の通り決定される。
 * <ul>
 *     <li>{@link SystemRepository}にコンポーネント名"objectMapperFactory"でオブジェクトが登録されている場合、
 *     そのオブジェクトを利用する。</li>
 *     <li>SystemRepositoryに登録されていない場合、本クラスをファクトリクラスとして利用する。</li>
 * </ul>
 *
 * @see ObjectMapper
 *
 * @author Hisaaki Shioiri
 */
@Published(tag = "architect")
public abstract class ObjectMapperFactory {

    /** 唯一のインスタンス */
    private static final ObjectMapperFactory FACTORY = new BasicObjectMapperFactory();

    // -------------------------------------------------- static factory method
    /**
     * 入力用の{@link ObjectMapper}を生成する。
     * <p/>
     * {@code stream}は、使用後に{@link ObjectMapper#close()}を呼び出して閉じること。
     *
     * @param clazz バインディング対象のJavaのクラス
     * @param stream 入力ストリーム
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    @Published
    public static <T> ObjectMapper<T> create(final Class<T> clazz, final InputStream stream) {
        final ObjectMapperFactory factory = createFactory();
        return factory.createMapper(clazz, stream);
    }

    /**
     * 入力用の{@link ObjectMapper}を生成する。
     * <p/>
     * {@code stream}は、使用後に{@link ObjectMapper#close()}を呼び出して閉じること。
     *
     * @param clazz バインディング対象のJavaのクラス
     * @param stream 入力ストリーム
     * @param dataBindConfig マッパー設定
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    @Published
    public static <T> ObjectMapper<T> create(
            final Class<T> clazz, final InputStream stream, final DataBindConfig dataBindConfig) {
        final ObjectMapperFactory factory = createFactory();
        return factory.createMapper(clazz, stream, dataBindConfig);
    }

    /**
     * 入力用の{@link ObjectMapper}を生成する。
     * <p/>
     * {@code stream}は、使用後に{@link ObjectMapper#close()}を呼び出して閉じること。
     *
     * @param clazz バインディング対象のJavaのクラス
     * @param reader リーダ
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    @Published
    public static <T> ObjectMapper<T> create(final Class<T> clazz, final Reader reader) {
        final ObjectMapperFactory factory = createFactory();
        return factory.createMapper(clazz, reader);
    }

    /**
     * 入力用の{@link ObjectMapper}を生成する。
     * <p/>
     * {@code reader}は、使用後に{@link ObjectMapper#close()}を呼び出して閉じること。
     *
     * @param clazz バインディング対象のJavaのクラス
     * @param reader リーダ
     * @param dataBindConfig マッパー設定
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    @Published
    public static <T> ObjectMapper<T> create(
            final Class<T> clazz, final Reader reader, final DataBindConfig dataBindConfig) {
        final ObjectMapperFactory factory = createFactory();
        return factory.createMapper(clazz, reader, dataBindConfig);
    }

    /**
     * 入力用の{@link ObjectMapper}を生成する。
     * <p/>
     * 使用後に{@link ObjectMapper#close()}を呼び出してストリームを閉じること。
     *
     * @param clazz バインディング対象のJavaのクラス
     * @param input 入力テキスト
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    @Published
    public static <T> ObjectMapper<T> create(final Class<T> clazz, final String input) {
        final ObjectMapperFactory factory = createFactory();
        return factory.createMapper(clazz, new StringReader(input));
    }

    /**
     * 入力用の{@link ObjectMapper}を生成する。
     * <p/>
     * 使用後に{@link ObjectMapper#close()}を呼び出してストリームを閉じること。
     *
     * @param clazz バインディング対象のJavaのクラス
     * @param input 入力テキスト
     * @param dataBindConfig マッパー設定
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    @Published
    public static <T> ObjectMapper<T> create(
            final Class<T> clazz, final String input, final DataBindConfig dataBindConfig) {
        final ObjectMapperFactory factory = createFactory();
        return factory.createMapper(clazz, new StringReader(input), dataBindConfig);
    }

    /**
     * 出力用の{@link ObjectMapper}を生成する。
     * <p/>
     * {@code stream}は、使用後に{@link ObjectMapper#close()}を呼び出して閉じること。
     *
     * @param clazz バインディング対象のJavaのクラス
     * @param stream 出力ストリーム
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    @Published
    public static <T> ObjectMapper<T> create(final Class<T> clazz, final OutputStream stream) {
        final ObjectMapperFactory factory = createFactory();
        return factory.createMapper(clazz, stream);
    }

    /**
     * 出力用の{@link ObjectMapper}を生成する。
     * <p/>
     * {@code stream}は、使用後に{@link ObjectMapper#close()}を呼び出して閉じること。
     *
     * @param clazz バインディング対象のJavaのクラス
     * @param stream 出力ストリーム
     * @param dataBindConfig マッパー設定
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    @Published
    public static <T> ObjectMapper<T> create(
            final Class<T> clazz, final OutputStream stream, final DataBindConfig dataBindConfig) {
        final ObjectMapperFactory factory = createFactory();
        return factory.createMapper(clazz, stream, dataBindConfig);
    }

    /**
     * 出力用の{@link ObjectMapper}を生成する。
     * <p/>
     * {@code writer}は、使用後に{@link ObjectMapper#close()}を呼び出して閉じること。
     *
     * @param clazz バインディング対象のJavaのクラス
     * @param writer 出力ストリーム
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    @Published
    public static <T> ObjectMapper<T> create(final Class<T> clazz, final Writer writer) {
        final ObjectMapperFactory factory = createFactory();
        return factory.createMapper(clazz, writer);
    }

    /**
     * 出力用の{@link ObjectMapper}を生成する。
     * <p/>
     * {@code writer}は、使用後に{@link ObjectMapper#close()}を呼び出して閉じること。
     *
     * @param clazz バインディング対象のJavaのクラス
     * @param writer 出力ストリーム
     * @param dataBindConfig マッパー設定
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    @Published
    public static <T> ObjectMapper<T> create(
            final Class<T> clazz, final Writer writer, final DataBindConfig dataBindConfig) {
        final ObjectMapperFactory factory = createFactory();
        return factory.createMapper(clazz, writer, dataBindConfig);
    }

    /**
     * {@link ObjectMapper}を生成する。
     *
     * @param clazz データとのバインディングを行うクラス
     * @param stream 入力ストリーム
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    public abstract <T> ObjectMapper<T> createMapper(final Class<T> clazz, final InputStream stream);

    /**
     * {@link ObjectMapper}を生成する。
     *
     * @param clazz データとのバインディングを行うクラス
     * @param stream 入力ストリーム
     * @param dataBindConfig マッピング設定
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    public abstract <T> ObjectMapper<T> createMapper(final Class<T> clazz, final InputStream stream, final DataBindConfig dataBindConfig);

    /**
     * {@link ObjectMapper}を生成する。
     *
     * @param clazz データとのバインディングを行うクラス
     * @param reader 入力ストリーム
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    public abstract <T> ObjectMapper<T> createMapper(final Class<T> clazz, final Reader reader);

    /**
     * {@link ObjectMapper}を生成する。
     *
     * @param clazz データとのバインディングを行うクラス
     * @param reader 入力ストリーム
     * @param dataBindConfig マッピング設定
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    public abstract <T> ObjectMapper<T> createMapper(final Class<T> clazz, final Reader reader, final DataBindConfig dataBindConfig);

    /**
     * {@link ObjectMapper}を生成する。
     *
     * @param clazz データとのバインディングを行うクラス
     * @param stream 出力ストリーム
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    public abstract <T> ObjectMapper<T> createMapper(final Class<T> clazz, final OutputStream stream);
    
    /**
     * {@link ObjectMapper}を生成する。
     *
     * @param clazz データとのバインディングを行うクラス
     * @param stream 出力ストリーム
     * @param dataBindConfig マッピング設定
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    public abstract <T> ObjectMapper<T> createMapper(final Class<T> clazz, final OutputStream stream, final DataBindConfig dataBindConfig);

    /**
     * {@link ObjectMapper}を生成する。
     *
     * @param clazz データとのバインディングを行うクラス
     * @param writer Writer
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    public abstract <T> ObjectMapper<T> createMapper(final Class<T> clazz, final Writer writer);

    /**
     * {@link ObjectMapper}を生成する。
     *
     * @param clazz データとのバインディングを行うクラス
     * @param writer Writer
     * @param dataBindConfig マッピング設定
     * @param <T> バインディング対象のJavaのクラス
     * @return データとJava ObjectのMapper
     */
    public abstract <T> ObjectMapper<T> createMapper(final Class<T> clazz, final Writer writer, final DataBindConfig dataBindConfig);

    /**
     * {@code ObjectMapperFactory}を生成する。
     * <p/>
     * {@link SystemRepository}上に存在する場合には、その値を返却する。
     * 存在しない場合には、{@link #FACTORY}を返す。
     *
     * @return {@code ObjectMapperFactory}
     */
    private static ObjectMapperFactory createFactory() {
        final ObjectMapperFactory factory = SystemRepository.get("objectMapperFactory");
        return factory == null ? FACTORY : factory;
    }
}

