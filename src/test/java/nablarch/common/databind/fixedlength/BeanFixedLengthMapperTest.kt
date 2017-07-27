package nablarch.common.databind.fixedlength

import nablarch.common.databind.ObjectMapperFactory
import nablarch.common.databind.fixedlength.converter.Lpad
import nablarch.common.databind.fixedlength.converter.Rpad
import org.hamcrest.Matchers
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.io.ByteArrayOutputStream
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.nio.BufferOverflowException

/**
 * {@link BeanFixedLengthMapper}のテスト
 */
class BeanFixedLengthMapperTest {

    @get:Rule
    var expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun `シンプルなBeanを固定長に変換できること`() {

        val stream = ByteArrayOutputStream()

        @FixedLength(length = 19, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
                @field:Field(offset = 1, length = 8)
                @field:Rpad
                var name: String? = null,
                @field:Field(offset = 9, length = 8)
                @field:Rpad('a')
                var text: String? = null,
                @field:Field(offset = 17, length = 3)
                @field:Lpad
                var age: Int? = null
        ) {
            constructor() : this(null, null, null)
        }

        ObjectMapperFactory.create(TestBean::class.java, stream).use { sut ->
            assertThat(sut, Matchers.instanceOf(BeanFixedLengthMapper::class.java))
            sut.write(TestBean("testname", "testtext", 100))
            assertThat(stream.toString(), Matchers.`is`("testnametesttext100\r\n"))

            sut.write(TestBean("name", "text", 12))
            assertThat(stream.toString(), Matchers.`is`("testnametesttext100\r\nname    textaaaa012\r\n"))
            sut.close()
        }
    }

    @Test
    fun `マルチレイアウトなBeanを固定長に変換できること`() {

        val stream = ByteArrayOutputStream()

        data class Header (
                @field:Field(offset = 1, length = 1)
                var id: Int? = null,
                @field:Field(offset = 2, length = 7)
                @field:Rpad
                var field: String? = null
        ) {
            constructor() : this(null, null)
        }

        data class Data(
                @field:Field(offset = 1, length = 1)
                var id: Int? = null,
                @field:Field(offset = 2, length = 4)
                @field:Rpad
                var name: String? = null,
                @field:Field(offset = 6, length = 3)
                @field:Lpad
                var age: Int? = null
        ) {
            constructor() : this(null, null, null)
        }

        @FixedLength(length = 8, charset = "MS932", lineSeparator = "\r\n", multiLayout = true)
        class Multi : MultiLayout() {
            override fun getRecordIdentifier(): MultiLayoutConfig.RecordIdentifier {
                return MultiLayoutConfig.RecordIdentifier {
                    if (it.first().toInt() == 0x31) {
                        RecordType.HEADER
                    } else {
                        RecordType.DATA
                    }
                }
            }
            @field:Record
            var header: Header? = null

            @field:Record
            var data: Data? = null
        }

        ObjectMapperFactory.create(Multi::class.java, stream).use { sut ->
            assertThat(sut, Matchers.instanceOf(BeanFixedLengthMapper::class.java))

            val header = Multi()
            header.recordName = RecordType.HEADER
            header.header = Header(1, "test")
            sut.write(header)
            assertThat(stream.toString(), Matchers.`is`("1test   \r\n"))

            var data1 = Multi()
            data1.recordName = RecordType.DATA
            data1.data = Data(2, "aaa", 12)
            sut.write(data1)
            assertThat(stream.toString(), Matchers.`is`("1test   \r\n2aaa 012\r\n"))

            var data2 = Multi()
            data2.recordName = RecordType.DATA
            data2.data = Data(2, "bb", 345)
            sut.write(data2)
            assertThat(stream.toString(), Matchers.`is`("1test   \r\n2aaa 012\r\n2bb  345\r\n"))
            sut.close()
        }
    }

    @Test
    fun `マルチレイアウトでレコード名に紐づくデータが設定されていない場合に例外が送出されること`() {

        val stream = ByteArrayOutputStream()

        data class Header (
                @field:Field(offset = 1, length = 1)
                var id: Int? = null,
                @field:Field(offset = 2, length = 7)
                @field:Rpad
                var field: String? = null
        ) {
            constructor() : this(null, null)
        }

        data class Data(
                @field:Field(offset = 1, length = 1)
                var id: Int? = null,
                @field:Field(offset = 2, length = 4)
                @field:Rpad
                var name: String? = null,
                @field:Field(offset = 6, length = 3)
                @field:Lpad
                var age: Int? = null
        ) {
            constructor() : this(null, null, null)
        }

        @FixedLength(length = 8, charset = "MS932", lineSeparator = "\r\n", multiLayout = true)
        class Multi : MultiLayout() {
            override fun getRecordIdentifier(): MultiLayoutConfig.RecordIdentifier {
                return MultiLayoutConfig.RecordIdentifier {
                    if (it.first().toInt() == 0x31) {
                        RecordType.HEADER
                    } else {
                        RecordType.DATA
                    }
                }
            }
            @field:Record
            var header: Header? = null

            @field:Record
            var data: Data? = null
        }

        ObjectMapperFactory.create(Multi::class.java, stream).use { sut ->
            assertThat(sut, Matchers.instanceOf(BeanFixedLengthMapper::class.java))

            val header = Multi()
            header.recordName = RecordType.HEADER
            header.data = Data(2, "aaa", 12)

            expectedException.expect(IllegalArgumentException::class.java)
            expectedException.expectMessage("record data is not found. record_name:header")
            sut.write(header)
        }
    }

    @Test
    fun `レコード長がオーバーしている場合に例外が発生すること`() {

        val stream = ByteArrayOutputStream()

        @FixedLength(length = 19, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
                @field:Field(offset = 1, length = 8)
                @field:Custom
                var name: String? = null,
                @field:Field(offset = 9, length = 8)
                @field:Custom
                var text: String? = null,
                @field:Field(offset = 17, length = 3)
                @field:Custom
                var age: Int? = null
        ) {
            constructor() : this(null, null, null)
        }

        ObjectMapperFactory.create(TestBean::class.java, stream).use { sut ->
            assertThat(sut, Matchers.instanceOf(BeanFixedLengthMapper::class.java))

            expectedException.expect(IllegalArgumentException::class.java)
            expectedException.expectMessage("record length is invalid. expected_length:19, actual_length:20")
            expectedException.expectCause(Matchers.instanceOf(BufferOverflowException::class.java))
            sut.write(TestBean("testname", "testtext", 1000))

        }
    }

    @Test
    fun `レコード長が足りない場合に例外が発生すること`() {

        val stream = ByteArrayOutputStream()

        @FixedLength(length = 19, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
                @field:Field(offset = 1, length = 8)
                @field:Custom
                var name: String? = null,
                @field:Field(offset = 9, length = 8)
                @field:Custom
                var text: String? = null,
                @field:Field(offset = 17, length = 3)
                @field:Custom
                var age: Int? = null
        ) {
            constructor() : this(null, null, null)
        }

        ObjectMapperFactory.create(TestBean::class.java, stream).use { sut ->
            assertThat(sut, Matchers.instanceOf(BeanFixedLengthMapper::class.java))

            expectedException.expect(IllegalArgumentException::class.java)
            expectedException.expectMessage("record length is invalid. expected_length:19, actual_length:15")
            sut.write(TestBean("name", "testtext", 100))

        }
    }

    @Test
    fun `readメソッドは使用できないこと`() {

        val stream = ByteArrayOutputStream()
        @FixedLength(length = 8, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
                @field:Field(offset = 1, length = 8)
                var name: String? = null
        ) {
            constructor() : this(null)
        }

        ObjectMapperFactory.create(TestBean::class.java, stream).use { sut ->
            assertThat(sut, Matchers.instanceOf(BeanFixedLengthMapper::class.java))
            expectedException.expect(UnsupportedOperationException::class.java)
            expectedException.expectMessage("unsupported read method.")
            sut.read()
        }
    }

    @FieldConvert(CustomConverter::class)
    @Target(ElementType.FIELD)
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

