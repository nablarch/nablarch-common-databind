package nablarch.common.databind;

import java.beans.PropertyDescriptor;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import nablarch.common.databind.csv.Csv;
import nablarch.common.databind.csv.CsvDataBindConfigConverter;
import nablarch.core.beans.BeanUtil;
import nablarch.core.beans.BeansException;
import nablarch.core.util.annotation.Published;

/**
 * データバインドに関するユーティリティクラス。
 *
 * @author Naoki Yamamoto
 */
public final class DataBindUtil {

    /** 隠蔽コンストラクタ */
    private DataBindUtil() {
    }

    /** 対応している形式のリスト */
    private static final List<DataBindConfigConverter<?>> CONVERTERS = Arrays.<DataBindConfigConverter<?>>asList(
            new CsvDataBindConfigConverter());

    /** {@link Csv#properties()}に設定されているプロパティ名配列のキャッシュ */
    private static final Map<Class<?>, String[]> CSV_PROPERTY_NAMES_MAP = new WeakHashMap<Class<?>, String[]>();

    /**
     * クラスに対応したCSVのプロパティ情報を取得する。
     * <p/>
     * キャッシュ上にCSVのプロパティ情報が存在する場合はその情報を返す。
     * まだキャッシュされていない場合には、CSVのプロパティ情報を生成しキャッシュに格納する。
     *
     * @param clazz Beanの{@link Class}
     * @return CSVのプロパティ情報
     */
    public static synchronized String[] findCsvProperties(final Class<?> clazz) {
        String[] propertyNames = CSV_PROPERTY_NAMES_MAP.get(clazz);
        if (propertyNames == null) {
            propertyNames = clazz.getAnnotation(Csv.class)
                                 .properties();
            CSV_PROPERTY_NAMES_MAP.put(clazz, propertyNames);
        }
        return propertyNames;
    }

    /**
     * クラスに対応したファイル行数を保持するプロパティの情報を取得する。
     *
     * @param clazz Beanの{@link Class}
     * @return ファイル行数を保持するプロパティの情報
     */
    @Published(tag = "architect")
    public static String findLineNumberProperty(final Class<?> clazz) {
        String propertyName = null;
        for (PropertyDescriptor pd : BeanUtil.getPropertyDescriptors(clazz)) {
            if (hasLineNumberProperty(pd)) {
                if (propertyName != null) {
                    // ファイル行数を保持するプロパティは0個か1個であるべき
                    throw new IllegalStateException(
                            "line number column should be defined only one. class = [" + clazz.getName() + "]");
                }
                propertyName = pd.getName();
            }
        }
        return propertyName;
    }

    /**
     * Beanの{@link Class}に設定された{@link DataBindConfig}を取得する。
     *
     * @param clazz Beanクラス
     * @param <T> 総称型
     * @return {@link DataBindConfig}オブジェクト
     */
    public static <T> DataBindConfig createDataBindConfig(final Class<T> clazz) {
        for (final DataBindConfigConverter<?> converter : CONVERTERS) {
            if (clazz.getAnnotation(converter.getType()) != null) {
                return converter.convert(clazz);
            }
        }
        throw new IllegalStateException(MessageFormat.format("can not find config. class = [{0}]", clazz.getName()));
    }

    /**
     * Beanのインスタンスを生成する。
     *
     * @param clazz Beanクラス
     * @param propertyNames プロパティ名の配列
     * @param values プロパティに設定する値
     * @param <T> 総称型
     * @return Beanのインスタンス
     */
    @Published(tag = "architect")
    public static <T> T getInstance(Class<T> clazz, String[] propertyNames, String[] values) {
        T bean;
        try {
            bean = clazz.newInstance();
        } catch (Exception e) {
            throw new BeansException(e);
        }

        for (int i = 0; i < values.length; i++) {
            BeanUtil.setProperty(bean, propertyNames[i], values[i]);
        }
        return bean;
    }

    /**
     * ファイル行数を持つBeanのインスタンスを生成する。
     *
     * @param clazz Beanクラス
     * @param propertyNames プロパティ名の配列
     * @param values プロパティに設定する値
     * @param lineNumberPropertyName ファイル行数を保持するプロパティの名称
     * @param lineNumber ファイル行数
     * @param <T> 総称型
     * @return Beanのインスタンス
     */
    @Published(tag = "architect")
    public static <T> T getInstanceWithLineNumber(Class<T> clazz, String[] propertyNames, String[] values,
            String lineNumberPropertyName, long lineNumber) {

        T bean;
        try {
            bean = clazz.newInstance();
        } catch (Exception e) {
            throw new BeansException(e);
        }

        for (int i = 0; i < values.length; i++) {
            BeanUtil.setProperty(bean, propertyNames[i], values[i]);
        }

        BeanUtil.setProperty(bean, lineNumberPropertyName, lineNumber);

        return bean;
    }

    /**
     * {@link LineNumber}アノテーションが設定されたプロパティが存在するか否かを判定する。
     *
     * @param pd Beanの{@link PropertyDescriptor}
     * @return {@link LineNumber}アノテーションが設定されたプロパティが存在する場合は{@code true}
     */
    private static boolean hasLineNumberProperty(PropertyDescriptor pd) {
        return pd.getReadMethod() != null && pd.getReadMethod()
                                               .getAnnotation(LineNumber.class) != null;
    }
}
