package nablarch.common.databind.fixedlength;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nablarch.common.databind.InvalidDataFormatException;
import nablarch.core.util.StringUtil;

/**
 * 固定長データを読み取るクラス。
 *
 * @author siosio
 */
public class FixedLengthReader implements Closeable {

    /** 入力元 */
    private final ReadableByteChannel readableChannel;

    /** 固定長データの設定情報 */
    private final FixedLengthDataBindConfig config;

    /** レコード番号 */
    private Long lineNumber = 1L;

    /**
     * 固定長のリーダーを構築する。
     *
     * @param inputStream 読み取る対象
     * @param config 固定長の設定情報
     */
    public FixedLengthReader(final InputStream inputStream, final FixedLengthDataBindConfig config) {
        readableChannel = Channels.newChannel(inputStream);
        this.config = config;
    }

    /**
     * レコードをリードする。
     *
     * @return レコード
     */
    public Map<String, Object> readRecord() {
        final ByteBuffer buffer = ByteBuffer.allocate(config.getLength());
        try {
            final int readLength = readableChannel.read(buffer);
            if (readLength < 0) {
                return null;
            }
            if (readLength != config.getLength()) {
                throw new InvalidDataFormatException("last record is short.", lineNumber);
            }

            skipLineSeparator();

            lineNumber++;
            final Map<String, Object> fields = new HashMap<String, Object>();
            final MultiLayoutConfig multiLayoutConfig = config.getMultiLayoutConfig();
            final List<FieldConfig> fieldConfigList;
            if (multiLayoutConfig != null) {
                final String recordName = multiLayoutConfig.getRecordIdentifier().identify(buffer.array());
                fields.put("recordName", recordName);
                fieldConfigList = config.getRecordConfig(recordName).getFieldConfigList();
                for (final FieldConfig fieldConfig : fieldConfigList) {
                    fields.put(recordName + '.' + fieldConfig.getName(), readValue(buffer.array(), config, fieldConfig));
                }
            } else {
                fieldConfigList = config.getRecordConfig(RecordConfig.SINGLE_LAYOUT_RECORD_NAME).getFieldConfigList();
                for (final FieldConfig fieldConfig : fieldConfigList) {
                    fields.put(fieldConfig.getName(), readValue(buffer.array(), config, fieldConfig));
                }
            }
            return fields;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 改行文字を読み飛ばす。
     *
     * @throws IOException 読み飛ばす処理に失敗した場合
     */
    private void skipLineSeparator() throws IOException {
        if (StringUtil.isNullOrEmpty(config.getLineSeparator())) {
            return;
        }
        final int length = config.getLineSeparator()
                                 .length();

        final ByteBuffer buffer = ByteBuffer.allocate(length);
        final int readLength = readableChannel.read(buffer);

        if (readLength == -1) {
            return;
        } else if (readLength != length) {
            throw new InvalidDataFormatException("line separator is invalid.", lineNumber);
        } else if (!new String(buffer.array(), config.getCharset()).equals(config.getLineSeparator())) {
            throw new InvalidDataFormatException("line separator is invalid.", lineNumber);
        }
    }

    /**
     * バイト配列から自身のフィールド部分を抜き出し返却する。
     *
     * @param record レコード情報
     * @param fixedLengthDataBindConfig 固定長の設定値
     * @param fieldConfig フィールドの設定値
     *
     * @return 読み込んだ値
     */
    public Object readValue(final byte[] record, final FixedLengthDataBindConfig fixedLengthDataBindConfig, final FieldConfig fieldConfig) {
        final int zeroOffset = fieldConfig.getOffset() - 1;
        final byte[] fieldValue = Arrays.copyOfRange(record, zeroOffset, zeroOffset + fieldConfig.getLength());
        return fieldConfig.getFieldConverter().convertOfRead(fixedLengthDataBindConfig, fieldConfig, fieldValue);
    }

    @Override
    public void close() throws IOException {
        readableChannel.close();
    }
}
