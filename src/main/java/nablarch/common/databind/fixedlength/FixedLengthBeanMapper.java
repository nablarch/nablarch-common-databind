package nablarch.common.databind.fixedlength;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.util.Map;

import nablarch.common.databind.DataBindUtil;
import nablarch.common.databind.ObjectMapper;
import nablarch.common.databind.fixedlength.FixedLengthReader.ReadRecord;
import nablarch.core.beans.BeanUtil;
import nablarch.core.util.FileUtil;
import nablarch.core.util.StringUtil;

/**
 * 固定長をBeanにマッピングする{@link ObjectMapper}
 *
 * @param <T> 読み取る型
 * @author Naoki Yamamoto
 */
public class FixedLengthBeanMapper<T> implements ObjectMapper<T> {

    /** レコードをマッピングするクラス */
    private final Class<T> clazz;

    /** 固定長の設定情報 */
    private final FixedLengthDataBindConfig config;

    /** 固定長ファイルを読み取るリーダ */
    private final FixedLengthReader reader;

    /** 行番号を格納するプロパティ名 */
    private final String lineNumberPropertyName;

    /**
     * 固定長をBeanにマッピングするクラスを構築する。
     *
     * @param clazz マッピング対象のBeanクラス
     * @param config 固定長の設定情報
     * @param stream 固定長データ
     */
    public FixedLengthBeanMapper(final Class<T> clazz, final FixedLengthDataBindConfig config, final InputStream stream) {
        this.clazz = clazz;
        this.config = config;
        this.reader  = new FixedLengthReader(stream, config);
        lineNumberPropertyName = DataBindUtil.findLineNumberProperty(clazz);
    }

    @Override
    public void write(final T object) {
        throw new UnsupportedOperationException("unsupported write method.");
    }

    @Override
    public T read() {
        ReadRecord read = reader.readRecord();
        if (read == null) {
            return null;
        }

        final T bean = createBean(read.getData());
        if (StringUtil.hasValue(lineNumberPropertyName)) {
            BeanUtil.setProperty(bean, lineNumberPropertyName, read.getLineNumber());
        }
        return bean;
    }

    /**
     * 読み込んだデータから対応するBeanを生成する。
     * @param read 読み込んだデータ
     * @return 生成されたBean
     */
    private T createBean(Map<String, ?> read) {
        if (config.isMultiLayout()) {
            final T bean = BeanUtil.createAndCopy(clazz, read);
            final MultiLayoutConfig.RecordName recordName = (MultiLayoutConfig.RecordName) read.get("recordName");
            final PropertyDescriptor descriptor = BeanUtil.getPropertyDescriptor(clazz, recordName.getRecordName());
            final Object record = BeanUtil.createAndCopy(descriptor.getPropertyType(),
                                                         (Map<String, ?>) read.get(recordName.getRecordName()));
            BeanUtil.setProperty(bean, recordName.getRecordName(), record);
            return bean;

        } else {
            return BeanUtil.createAndCopy(clazz, read);
        }

    }

    @Override
    public void close() {
        FileUtil.closeQuietly(reader);
    }
}
