package nablarch.common.databind.fixedlength;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;

/**
 * 固定長データを出力するクラス。
 * @author Naoki Yamamoto
 */
public class FixedLengthWriter implements Closeable {

    /** 出力先 */
    private final WritableByteChannel writableByteChannel;

    /** 固定長データの設定情報 */
    private final FixedLengthDataBindConfig config;

    /** 改行コードの{@link ByteBuffer} */
    private final ByteBuffer lineSeparatorByteBuffer;

    /**
     * 固定長データのライタを構築する。
     * @param stream 出力ストリーム
     * @param config コンフィグ
     */
    public FixedLengthWriter(final OutputStream stream, final FixedLengthDataBindConfig config) {
        writableByteChannel = Channels.newChannel(stream);
        lineSeparatorByteBuffer = ByteBuffer.allocate(config.getLineSeparator().length());
        lineSeparatorByteBuffer.put(config.getLineSeparator().getBytes(config.getCharset()));
        this.config = config;
    }

    /**
     * レコードを出力する。
     * @param map 出力データ
     */
    public void writeRecord(final Map<String, ?> map) {
        final int configLength = config.getLength();
        final List<FieldConfig> fieldConfigList;
        final Map<String, ?> fields;
        if (config.isMultiLayout()) {
            final MultiLayoutConfig.RecordName recordName = (MultiLayoutConfig.RecordName) map.get("recordName");
            try {
                fields = (Map<String, ?>) map.get(recordName.getRecordName());
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("record data must be " + Map.class.getName() + " type.", e);
            }
            if (fields == null) {
                throw new IllegalArgumentException("record data is not found. record_name:" + recordName.getRecordName());
            }
            fieldConfigList = config.getRecordConfig(recordName.getRecordName()).getFieldConfigList();
        } else {
            fields = map;
            fieldConfigList = config.getRecordConfig(RecordConfig.SINGLE_LAYOUT_RECORD_NAME).getFieldConfigList();
        }

        final ByteBuffer byteBuffer = ByteBuffer.allocate(configLength);
        for (final FieldConfig fieldConfig : fieldConfigList) {
            final byte[] value = fieldConfig.getFieldConverter().convertOfWrite(config, fieldConfig, fields.get(fieldConfig.getName()));
            try {
                byteBuffer.put(value);
            } catch (BufferOverflowException e) {
                throw new IllegalArgumentException(
                        "record length is invalid. expected_length:" + configLength + ", actual_length:" + (byteBuffer.position() + value.length), e);
            }
        }
        write(byteBuffer);
        write(lineSeparatorByteBuffer);
    }

    /**
     * 指定された{@link ByteBuffer}を書き込む。
     * @param byteBuffer バイトバッファ
     */
    private void write(final ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        try {
            writableByteChannel.write(byteBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        writableByteChannel.close();
    }
}
