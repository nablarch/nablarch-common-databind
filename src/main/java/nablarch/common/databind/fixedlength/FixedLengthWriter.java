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

import nablarch.core.util.StringUtil;

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
        final ByteBuffer byteBuffer = ByteBuffer.allocate(configLength);
        final MultiLayoutConfig multiLayoutConfig = config.getMultiLayoutConfig();
        final List<FieldConfig> fieldConfigList;
        final Map<String, ?> fields;
        if (multiLayoutConfig != null) {
            final MultiLayoutConfig.RecordName recordName = (MultiLayoutConfig.RecordName) map.get("recordName");
            fields = (Map<String, ?>) map.get(recordName.getRecordName());
            if (fields == null) {
                throw new IllegalArgumentException("record data is not found. record_name:" + recordName.getRecordName());
            }
            fieldConfigList = config.getRecordConfig(recordName.getRecordName()).getFieldConfigList();
        } else {
            fields = map;
            fieldConfigList = config.getRecordConfig(RecordConfig.SINGLE_LAYOUT_RECORD_NAME).getFieldConfigList();
        }

        for (final FieldConfig fieldConfig : fieldConfigList) {
            final byte[] value = fieldConfig.getFieldConverter().convertOfWrite(config, fieldConfig, fields.get(fieldConfig.getName()));
            try {
                byteBuffer.put(getFillBytes(fieldConfig.getOffset() - byteBuffer.position() - 1));
                byteBuffer.put(value);
            } catch (BufferOverflowException e) {
                throw new IllegalArgumentException(
                        "record length is invalid. expected_length:" + configLength + ", actual_length:" + (byteBuffer.position() + value.length), e);
            }
        }
        byteBuffer.put(getFillBytes(config.getLength() - byteBuffer.position()));
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

    /**
     * 指定された長さの埋め時のバイト配列を返す。
     * @param length 長さ
     * @return 埋め字のバイト配列
     */
    private byte[] getFillBytes(final int length) {
        if (length > 0) {
            byte[] bytes = StringUtil.getBytes(Character.toString(config.getFillChar()), config.getCharset());
            final ByteBuffer buffer = ByteBuffer.allocate(length);
            while (buffer.position() < buffer.limit()) {
                buffer.put(bytes);
            }
            return buffer.array();
        }
        return new byte[0];
    }

    @Override
    public void close() throws IOException {
        writableByteChannel.close();
    }
}
