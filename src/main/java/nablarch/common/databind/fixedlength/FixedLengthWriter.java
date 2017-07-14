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

    /**
     * 固定長データのライタを構築する。
     * @param stream 出力ストリーム
     * @param config コンフィグ
     */
    public FixedLengthWriter(final OutputStream stream, final FixedLengthDataBindConfig config) {
        writableByteChannel = Channels.newChannel(stream);
        this.config = config;
    }

    /**
     * レコードを出力する。
     * @param map 出力データ
     */
    public void writeRecord(final Map<String, ?> map) {
        final int configLength = config.getLength();
        final ByteBuffer byteBuffer = ByteBuffer.allocate(configLength);
        final List<FieldConfig> fieldConfigList = config.getRecordConfig().getFieldConfigList();
        for (final FieldConfig fieldConfig : fieldConfigList) {
            final byte[] value = fieldConfig.convertWriteValue(map.get(fieldConfig.getName()), config);
            try {
                byteBuffer.put(value);
            } catch (BufferOverflowException e) {
                throwInvalidLengthException(configLength, byteBuffer.position() + value.length);
            }
        }
        if (byteBuffer.position() < configLength) {
            throwInvalidLengthException(configLength, byteBuffer.position());
        }

        byteBuffer.rewind();
        try {
            writableByteChannel.write(byteBuffer);
            writeLineSeparator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 出力されるレコードの長さが設定情報より異なることを示す例外を送出する。
     *
     * @param configLength 設定情報に定義されたレコードの長さ
     * @param actualLength 実際のレコードの長さ
     */
    private void throwInvalidLengthException(final int configLength, final int actualLength) {
        throw new IllegalArgumentException(
                "record length is invalid. expected_length:" + configLength + ", actual_length:" + actualLength);
    }

    /**
     * 改行コードを出力する。
     * @throws IOException 出力に失敗した場合
     */
    private void writeLineSeparator() throws IOException {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(config.getLineSeparator().length());
        byteBuffer.put(config.getLineSeparator().getBytes(config.getCharset()));
        byteBuffer.rewind();
        writableByteChannel.write(byteBuffer);
    }

    @Override
    public void close() throws IOException {
        writableByteChannel.close();
    }
}
