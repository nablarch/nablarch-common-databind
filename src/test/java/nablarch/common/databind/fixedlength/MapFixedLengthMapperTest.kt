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
            assertThat(stream.toString(), Matchers.`is`("testnametesttext100"))

            sut.write(mapOf("name" to "name", "text" to "text", "age" to 12))
            assertThat(stream.toString(), Matchers.`is`("testnametesttext100\r\nname    text    012"))
            sut.close()
        }
    }

    @Test
    fun `レコード長がオーバーしている場合に例外が発生すること`() {

        val stream = ByteArrayOutputStream()
        val recordConfig = RecordBuilder()
                .addField("name", 1, 8)
                .addField("text", 9, 8)
                .addField("age", 17, 3)
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
    fun `レコード長が足りない場合に例外が発生すること`() {

        val stream = ByteArrayOutputStream()
        val recordConfig = RecordBuilder()
                .addField("name", 1, 8)
                .addField("text", 9, 8)
                .addField("age", 17, 3)
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
            expectedException.expectMessage("record length is invalid. expected_length:19, actual_length:15")
            sut.write(mapOf("name" to "test", "text" to "testtext", "age" to 100))

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
}

