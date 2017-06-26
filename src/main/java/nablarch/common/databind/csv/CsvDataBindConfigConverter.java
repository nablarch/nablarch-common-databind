package nablarch.common.databind.csv;

import java.beans.PropertyDescriptor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import nablarch.common.databind.DataBindConfig;
import nablarch.common.databind.DataBindConfigConverter;
import nablarch.core.beans.BeanUtil;

/**
 * {@link Csv}アノテーションを{@link CsvDataBindConfig}に変換するクラス。
 *
 * @author siosio
 */
public class CsvDataBindConfigConverter implements DataBindConfigConverter<Csv> {

    @Override
    public DataBindConfig convert(final Class<?> beanClass) {
        final Csv csv = beanClass.getAnnotation(Csv.class);
        verifyCsvConfig(beanClass, csv);

        final CsvFormat csvFormat = beanClass.getAnnotation(CsvFormat.class);
        verifyCsvFormat(beanClass, csv, csvFormat);

        CsvDataBindConfig config;
        if (csvFormat == null) {
            config = (CsvDataBindConfig) csv.type().getConfig();
        } else {
            config = CsvDataBindConfig.DEFAULT
                    .withFieldSeparator(csvFormat.fieldSeparator())
                    .withLineSeparator(csvFormat.lineSeparator())
                    .withQuote(csvFormat.quote())
                    .withIgnoreEmptyLine(csvFormat.ignoreEmptyLine())
                    .withRequiredHeader(csvFormat.requiredHeader())
                    .withCharset(csvFormat.charset())
                    .withEmptyToNull(csvFormat.emptyToNull())
                    .withQuoteMode(csvFormat.quoteMode());
        }

        if (config.getQuoteMode() == CsvDataBindConfig.QuoteMode.CUSTOM) {
            config = config.withQuotedColumnNames(findQuotedItemList(beanClass));
        }

        if (config.isRequiredHeader()) {
            if (csv.headers().length == csv.properties().length) {
                config = config.withHeaderTitles(csv.headers());
            } else {
                throw new IllegalStateException(MessageFormat.format(
                        "headers and properties size does not match. class = [{0}]", beanClass.getName()));
            }
        }

        return config;
    }

    @Override
    public Class<Csv> getType() {
        return Csv.class;
    }

    /**
     * CSVフォーマットの設定が正しいことを検証する。
     * @param clazz Beanクラス
     * @param csv CSV設定
     * @param csvFormat CSVフォーマット
     * @param <T> Beanクラスの型
     */
    private static <T> void verifyCsvFormat(Class<T> clazz, Csv csv, CsvFormat csvFormat) {
        if (csv.type() != Csv.CsvType.CUSTOM && csvFormat != null) {
            throw new IllegalStateException(MessageFormat.format(
                    "CsvFormat annotation can not defined because CsvType is not CUSTOM. class = [{0}]", clazz.getName()));
        }
    }

    /**
     * CSVの設定が正しいことを検証する。
     * @param clazz Beanクラス
     * @param csv CSV設定
     * @param <T> Beanクラスの型
     */
    private static <T> void verifyCsvConfig(final Class<T> clazz, final Csv csv) {
        if (csv.properties().length == 0) {
            throw new IllegalStateException(MessageFormat.format(
                    "properties is required. class = [{0}]", clazz.getName()));
        }
    }

    /**
     * クォート文字で囲む対象の項目リストを取得する。
     * @param clazz Beanクラス
     * @param <T> Beanクラスの型
     * @return クォート文字で囲む対象の項目
     */
    private static <T> String[] findQuotedItemList(final Class<T> clazz) {
        final List<String> quotedColumnNames = new ArrayList<String>();
        PropertyDescriptor[] pds = BeanUtil.getPropertyDescriptors(clazz);
        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod().getAnnotation(Quoted.class) != null) {
                quotedColumnNames.add(pd.getName());
            }
        }
        return quotedColumnNames.toArray(new String[quotedColumnNames.size()]);
    }
}
