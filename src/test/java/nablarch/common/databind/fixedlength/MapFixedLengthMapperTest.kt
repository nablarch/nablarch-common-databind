package nablarch.common.databind.fixedlength

import nablarch.common.databind.ObjectMapperFactory
import nablarch.common.databind.fixedlength.converter.Lpad
import nablarch.common.databind.fixedlength.converter.Rpad
import org.hamcrest.Matchers
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import sun.nio.cs.ext.MS932
import java.io.ByteArrayOutputStream
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.nio.BufferOverflowException

/**
 * {@link MapFixedLengthMapper}のテスト
 */
class MapFixedLengthMapperTest {

    @get:Rule
    var expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun `シンプルなMapを固定長に変換できること`() {

        val stream = ByteArrayOutputStream()
        val recordConfig = RecordBuilder()
                .addField("name", 1, 8, Rpad.RpadConverter(' '))
                .addField("text", 9, 8, Rpad.RpadConverter(' '))
                .addField("age", 17, 3, Lpad.LpadConverter('0'))
                .build()

        val config = FixedLengthDataBindConfigBuilder
                .newBuilder()
                .charset(MS932())
                .length(19)
                .lineSeparator("\r\n")
                .addRecord(recordConfig)
                .build()

        ObjectMapperFactory.create(Map::class.java, stream, config).use { sut ->
            assertThat(sut, Matchers.instanceOf(MapFixedLengthMapper::class.java))
            sut.write(mapOf("name" to "testname", "text" to "testtext", "age" to 100))
            assertThat(stream.toString(), Matchers.`is`("testnametesttext100\r\n"))

            sut.write(mapOf("name" to "name", "text" to "text", "age" to 12))
            assertThat(stream.toString(), Matchers.`is`("testnametesttext100\r\nname    text    012\r\n"))
            sut.close()
        }
    }

    @Test
    fun `マルチレイアウトなMapを固定長に変換できること`() {

        val stream = ByteArrayOutputStream()
        val config = FixedLengthDataBindConfigBuilder
                .newBuilder()
                .charset(MS932())
                .length(8)
                .lineSeparator("\r\n")
                .addRecord(RecordBuilder()
                        .recordName("header")
                        .addField("id", 1, 1)
                        .addField("field", 2, 7, Rpad.RpadConverter(' '))
                        .build())
                .addRecord(RecordBuilder()
                        .recordName("data")
                        .addField("id", 1, 1)
                        .addField("name", 2, 4, Rpad.RpadConverter(' '))
                        .addField("age", 6, 3, Lpad.LpadConverter('0'))
                        .build())
                .multiLayout(MultiLayoutConfig(MultiLayoutConfig.RecordIdentifier {
                    if (it.first().toInt() == 0x31) {
                        RecordType.HEADER
                    } else {
                        RecordType.DATA
                    }
                }))
                .build()

        ObjectMapperFactory.create(Map::class.java, stream, config).use { sut ->
            assertThat(sut, Matchers.instanceOf(MapFixedLengthMapper::class.java))

            sut.write(mapOf("recordName" to RecordType.HEADER, "header" to mapOf("id" to 1, "field" to "test")))
            assertThat(stream.toString(), Matchers.`is`("1test   \r\n"))

            sut.write(mapOf("recordName" to RecordType.DATA, "data" to mapOf("id" to 2, "name" to "aaa", "age" to 12)))
            assertThat(stream.toString(), Matchers.`is`("1test   \r\n2aaa 012\r\n"))

            sut.write(mapOf("recordName" to RecordType.DATA, "data" to mapOf("id" to 2, "name" to "bb", "age" to 345)))
            assertThat(stream.toString(), Matchers.`is`("1test   \r\n2aaa 012\r\n2bb  345\r\n"))
            sut.close()
        }
    }

    @Test
    fun `マルチレイアウトでレコード名に紐づくデータがMapに存在しない場合に例外が送出されること`() {

        val stream = ByteArrayOutputStream()
        val config = FixedLengthDataBindConfigBuilder
                .newBuilder()
                .charset(MS932())
                .length(8)
                .lineSeparator("\r\n")
                .addRecord(RecordBuilder()
                        .recordName("header")
                        .addField("id", 1, 1)
                        .addField("field", 2, 7, Rpad.RpadConverter(' '))
                        .build())
                .addRecord(RecordBuilder()
                        .recordName("data")
                        .addField("id", 1, 1)
                        .addField("name", 2, 4, Rpad.RpadConverter(' '))
                        .addField("age", 6, 3, Lpad.LpadConverter('0'))
                        .build())
                .multiLayout(MultiLayoutConfig(MultiLayoutConfig.RecordIdentifier {
                    if (it.first().toInt() == 0x31) {
                        RecordType.HEADER
                    } else {
                        RecordType.DATA
                    }
                }))
                .build()

        ObjectMapperFactory.create(Map::class.java, stream, config).use { sut ->
            assertThat(sut, Matchers.instanceOf(MapFixedLengthMapper::class.java))

            expectedException.expect(IllegalArgumentException::class.java)
            expectedException.expectMessage("record data is not found. record_name:header")
            sut.write(mapOf("recordName" to RecordType.HEADER, "data" to mapOf("id" to 2, "name" to "aaa", "age" to 12)))
        }
    }

    @Test
    fun `レコード長がオーバーしている場合に例外が発生すること`() {

        val stream = ByteArrayOutputStream()
        val recordConfig = RecordBuilder()
                .addField("name", 1, 8, CustomConverter())
                .addField("text", 9, 8, CustomConverter())
                .addField("age", 17, 3, CustomConverter())
                .build()

        val config = FixedLengthDataBindConfigBuilder
                .newBuilder()
                .charset(MS932())
                .length(19)
                .lineSeparator("\r\n")
                .addRecord(recordConfig)
                .build()

        ObjectMapperFactory.create(Map::class.java, stream, config).use { sut ->
            assertThat(sut, Matchers.instanceOf(MapFixedLengthMapper::class.java))

            expectedException.expect(IllegalArgumentException::class.java)
            expectedException.expectMessage("record length is invalid. expected_length:19, actual_length:20")
            expectedException.expectCause(Matchers.instanceOf(BufferOverflowException::class.java))
            sut.write(mapOf("name" to "testname", "text" to "testtext", "age" to 1000))

        }
    }

    @Test
    fun `readメソッドは使用できないこと`() {

        val stream = ByteArrayOutputStream()
        val recordConfig = RecordBuilder()
                .addField("name", 1, 8)
                .build()

        val config = FixedLengthDataBindConfigBuilder
                .newBuilder()
                .charset(MS932())
                .length(8)
                .lineSeparator("\r\n")
                .addRecord(recordConfig)
                .build()

        ObjectMapperFactory.create(Map::class.java, stream, config).use { sut ->
            assertThat(sut, Matchers.instanceOf(MapFixedLengthMapper::class.java))
            expectedException.expect(UnsupportedOperationException::class.java)
            expectedException.expectMessage("unsupported read method.")
            sut.read()
        }
    }

    @FieldConvert(CustomConverter::class)
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    annotation class Custom

    class CustomConverter : FieldConvert.FieldConverter<Custom> {
        override fun initialize(annotation: Custom?) {
        }

        override fun convertOfRead(fixedLengthDataBindConfig: FixedLengthDataBindConfig, fieldConfig: FieldConfig, input: ByteArray): Any {
            return input
        }

        override fun convertOfWrite(fixedLengthDataBindConfig: FixedLengthDataBindConfig, fieldConfig: FieldConfig, output: Any): ByteArray {
            return output.toString().toByteArray()
        }
    }

    enum class RecordType : MultiLayoutConfig.RecordName {
        HEADER {
            override fun getRecordName(): String = "header"
        },
        DATA {
            override fun getRecordName(): String = "data"
        };
    }
}

