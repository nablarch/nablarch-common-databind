package nablarch.common.databind;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import nablarch.common.databind.csv.BeanCsvMapper;
import nablarch.common.databind.csv.CsvBeanMapper;
import nablarch.common.databind.csv.CsvDataBindConfig;
import nablarch.common.databind.csv.CsvMapMapper;
import nablarch.common.databind.csv.MapCsvMapper;
import nablarch.common.databind.fixedlength.BeanFixedLengthMapper;
import nablarch.common.databind.fixedlength.FixedLengthBeanMapper;
import nablarch.common.databind.fixedlength.FixedLengthDataBindConfig;
import nablarch.common.databind.fixedlength.FixedLengthMapMapper;
import nablarch.common.databind.fixedlength.MapFixedLengthMapper;

/**
 * マッパータイプ。
 *
 * @author siosio
 */
enum MapperType {
    /** CSVとBeanとのマッパー */
    CSV_BEAN {
        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config,
                final InputStream stream) {
            final CsvDataBindConfig csvDataBindConfig = CsvDataBindConfig.class.cast(config);
            return new CsvBeanMapper<T>(clazz, csvDataBindConfig, stream);
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final Reader reader) {
            final CsvDataBindConfig csvDataBindConfig = CsvDataBindConfig.class.cast(config);
            return new CsvBeanMapper<T>(clazz, csvDataBindConfig, reader);
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final OutputStream stream) {
            final CsvDataBindConfig csvDataBindConfig = CsvDataBindConfig.class.cast(config);
            return new BeanCsvMapper<T>(clazz, csvDataBindConfig, stream);
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final Writer writer) {
            final CsvDataBindConfig csvDataBindConfig = CsvDataBindConfig.class.cast(config);
            return new BeanCsvMapper<T>(clazz, csvDataBindConfig, writer);
        }
    },
    /** CSVとMapとのマッパー */
    @SuppressWarnings("unchecked")
    CSV_MAP {
        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config,
                final InputStream stream) {
            final CsvDataBindConfig csvDataBindConfig = CsvDataBindConfig.class.cast(config);
            return (ObjectMapper<T>) new CsvMapMapper(csvDataBindConfig, stream);
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final Reader reader) {
            final CsvDataBindConfig csvDataBindConfig = CsvDataBindConfig.class.cast(config);
            return (ObjectMapper<T>) new CsvMapMapper(csvDataBindConfig, reader);
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final OutputStream stream) {
            final CsvDataBindConfig csvDataBindConfig = CsvDataBindConfig.class.cast(config);
            return (ObjectMapper<T>) new MapCsvMapper(csvDataBindConfig, stream);
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final Writer writer) {
            final CsvDataBindConfig csvDataBindConfig = CsvDataBindConfig.class.cast(config);
            return (ObjectMapper<T>) new MapCsvMapper(csvDataBindConfig, writer);
        }
    },
    /** 固定長とBeanとのマッパー */
    FIXED_LENGTH_BEAN {
        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final InputStream stream) {
            final FixedLengthDataBindConfig fixedLengthConfig = FixedLengthDataBindConfig.class.cast(config);
            return new FixedLengthBeanMapper<T>(clazz, fixedLengthConfig, stream);
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final Reader reader) {
            throw new UnsupportedOperationException("fixed length type does not support reader.");
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final OutputStream stream) {
            final FixedLengthDataBindConfig fixedLengthConfig = FixedLengthDataBindConfig.class.cast(config);
            return (ObjectMapper<T>) new BeanFixedLengthMapper(clazz, fixedLengthConfig, stream);
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final Writer writer) {
            throw new UnsupportedOperationException("fixed length type does not support writer.");
        }
    },
    /** 固定長とMapとのマッパー */
    FIXED_LENGTH_MAP {
        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final InputStream stream) {
            final FixedLengthDataBindConfig fixedLengthConfig = FixedLengthDataBindConfig.class.cast(config);
            return (ObjectMapper<T>) new FixedLengthMapMapper(fixedLengthConfig, stream);
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final Reader reader) {
            throw new UnsupportedOperationException("fixed length type does not support reader.");
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final OutputStream stream) {
            final FixedLengthDataBindConfig fixedLengthConfig = FixedLengthDataBindConfig.class.cast(config);
            return (ObjectMapper<T>) new MapFixedLengthMapper(fixedLengthConfig, stream);
        }

        @Override
        <T> ObjectMapper<T> createMapper(final Class<T> clazz, final DataBindConfig config, final Writer writer) {
            throw new UnsupportedOperationException("fixed length type does not support writer.");
        }
    };

    /**
     * {@link InputStream}から読み込む{@link ObjectMapper}を生成する。
     *
     * @param clazz 読み込むクラス
     * @param config 設定情報
     * @param stream 読み込むリソースを示す{@link InputStream}
     * @param <T> 読み込む型
     * @return 生成した{@code ObjectMapper}
     */
    abstract <T> ObjectMapper<T> createMapper(Class<T> clazz, DataBindConfig config, InputStream stream);

    /**
     * {@link Reader}から読み込む{@link ObjectMapper}を生成する。
     *
     * @param clazz 読み込むクラス
     * @param config 設定情報
     * @param reader 読み込むリソースを示す{@link Reader}
     * @param <T> 読み込む型
     * @return 生成した{@code ObjectMapper}
     */
    abstract <T> ObjectMapper<T> createMapper(Class<T> clazz, DataBindConfig config, Reader reader);

    /**
     * {@link OutputStream}に出力する{@link ObjectMapper}を生成する。
     *
     * @param clazz 読み込むクラス
     * @param config 設定情報
     * @param stream 出力リソースを示す{@link OutputStream}
     * @param <T> 読み込む型
     * @return 生成した{@code ObjectMapper}
     */
    abstract <T> ObjectMapper<T> createMapper(Class<T> clazz, DataBindConfig config, OutputStream stream);

    /**
     * {@link Writer}に出力する{@link ObjectMapper}を生成する。
     *
     * @param clazz 読み込むクラス
     * @param config 設定情報
     * @param writer 出力リソースを示す{@link Writer}
     * @param <T> 読み込む型
     * @return 生成した{@code ObjectMapper}
     */
    abstract <T> ObjectMapper<T> createMapper(Class<T> clazz, DataBindConfig config, Writer writer);
}
