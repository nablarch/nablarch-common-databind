package nablarch.common.databind.fixedlength

import nablarch.common.databind.InvalidDataFormatException
import nablarch.common.databind.ObjectMapperFactory
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import sun.nio.cs.ext.MS932
import java.io.*

/**
 * {@link FixedLengthBeanMapper}のテストクラス
 */
class FixedLengthBeanMapperTest {

    @get:Rule
    var expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun `シンプルな固定長をBeanに変換できること`() {

        @FixedLength(length = 11, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
            @get:Field(offset = 1, length = 4)
            var name: String? = null,
            @get:Field(offset = 5, length = 4)
            var text: String? = null,
            @get:Field(offset = 9, length = 3)
            var age: Int? = null
        ) {
            constructor() : this(null, null, null)
        }

        val inputStream = listOf(
            "abcdあい123",
            "efghかき234"
        ).joinToString("\r\n").byteInputStream(MS932())

        ObjectMapperFactory.create<TestBean>(TestBean::class.java, inputStream).use { sut ->
            assertThat(sut, instanceOf<Any>(FixedLengthBeanMapper::class.java))
            assertThat(sut.read(), `is`(TestBean("abcd", "あい", 123)))
            assertThat(sut.read(), `is`(TestBean("efgh", "かき", 234)))
            assertThat(sut.read(), `is`(nullValue()))
        }
    }

    @Test
    fun `改行コードの存在しないデータでも読み取れること`() {

        @FixedLength(length = 5, charset = "MS932", lineSeparator = "")
        data class TestBean(
            @get:Field(offset = 1, length = 4)
            var name: String? = null,
            @get:Field(offset = 5, length = 1)
            var text: String? = null
        ) {
            constructor() : this(null, null)
        }

        val inputStream = listOf("1234a", "4321b").joinToString("").byteInputStream(MS932())

        ObjectMapperFactory.create<TestBean>(TestBean::class.java, inputStream).use { sut ->
            assertThat(sut, instanceOf<Any>(FixedLengthBeanMapper::class.java))
            assertThat(sut.read(), `is`(TestBean("1234", "a")))
            assertThat(sut.read(), `is`(TestBean("4321", "b")))
            assertThat(sut.read(), `is`(nullValue()))
        }
    }

    @Test
    fun `空ファイルを読み込むことができること`() {
        @FixedLength(length = 8, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
            @get:Field(offset = 1, length = 8)
            var name: String? = null
        ) {
            constructor() : this(null)
        }

        val inputStream = ByteArrayInputStream(byteArrayOf())
        ObjectMapperFactory.create<TestBean>(TestBean::class.java, inputStream).use { sut ->
            assertThat(sut.read(), `is`(nullValue()))
        }
    }

    @Test
    fun `末尾に改行コードがあっても読み込むことができること`() {
        @FixedLength(length = 8, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
            @get:Field(offset = 1, length = 8)
            var name: String? = null
        ) {
            constructor() : this(null)
        }

        val inputStream = "testname\r\n".byteInputStream(MS932())
        ObjectMapperFactory.create<TestBean>(TestBean::class.java, inputStream).use { sut ->
            assertThat(sut.read(), `is`(TestBean("testname")))
            assertThat(sut.read(), `is`(nullValue()))
        }
    }

    @Test
    fun `レコードの途中でEOFになる場合に例外が発生すること`() {

        @FixedLength(length = 11, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
            @get:Field(offset = 1, length = 8)
            var name: String? = null,
            @get:Field(offset = 9, length = 3)
            var age: Int? = null
        ) {
            constructor() : this(null, null)
        }

        val inputStream = listOf(
            "testname123",
            "invalid").joinToString("\r\n").byteInputStream(MS932())

        ObjectMapperFactory.create<TestBean>(TestBean::class.java, inputStream).use { sut ->
            sut.read()
            expectedException.expect(InvalidDataFormatException::class.java)
            expectedException.expectMessage(`is`("data format is invalid. last record is short. line number = [2]"))
            sut.read()
        }
    }

    @Test
    fun `実際の改行コードが設定と異なる場合は例外が送出されること`() {

        @FixedLength(length = 2, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
            @get:Field(offset = 1, length = 2)
            var name: String? = null
        ) {
            constructor() : this(null)
        }

        val inputStream = listOf(
            "12",
            "34"
        ).joinToString("\r\n")
            .plus("ab45")       // 改行コード部に\r\nではなく「ab」を設定
            .byteInputStream(MS932())

        ObjectMapperFactory.create<TestBean>(TestBean::class.java, inputStream).use { sut ->
            assertThat("最初のレコードは正しく読み込める", sut.read(), `is`(TestBean("12")))
            
            expectedException.expect(InvalidDataFormatException::class.java)
            expectedException.expectMessage(`is`("data format is invalid. line separator is invalid. line number = [2]"))
            sut.read()
        }
    }
    
    @Test
    fun `最終レコードの改行コードが実際より短い場合例外が送出されること`() {

        @FixedLength(length = 2, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
            @get:Field(offset = 1, length = 2)
            var name: String? = null
        ) {
            constructor() : this(null)
        }

        val inputStream = listOf(
            "12",
            "34"
        ).joinToString("\r\n")
            .plus("\r")       // 最終レコードが\rで終わる
            .byteInputStream(MS932())

        ObjectMapperFactory.create<TestBean>(TestBean::class.java, inputStream).use { sut ->
            assertThat("最初のレコードは正しく読み込める", sut.read(), `is`(TestBean("12")))

            expectedException.expect(InvalidDataFormatException::class.java)
            expectedException.expectMessage(`is`("data format is invalid. line separator is invalid. line number = [2]"))
            sut.read()
        }
    }

    @Test
    fun データ読み込み中に例外が送出されてた場合は原因例外を持つ実行時例外が送出されること() {
        @FixedLength(length = 2, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
            @get:Field(offset = 1, length = 2)
            var name: String? = null
        ) {
            constructor() : this(null)
        }

        ObjectMapperFactory.create(TestBean::class.java, object : ByteArrayInputStream(byteArrayOf()) {
            override fun read(): Int {
                throw IOException("io error")
            }

            override fun read(b: ByteArray?, off: Int, len: Int): Int {
                throw IOException("io error")
            }
        }).use { 
            it.read()
        }
    }

    @Test
    fun writeメソッドはサポートしない例外が送出されること() {

        @FixedLength(length = 2, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
            @get:Field(offset = 1, length = 2)
            var name: String? = null
        ) {
            constructor() : this(null)
        }
        
        ObjectMapperFactory.create(TestBean::class.java, byteArrayOf().inputStream()).use {
            expectedException.expect(UnsupportedOperationException::class.java)
            expectedException.expectMessage("unsupported write method.")
            it.write(null)
        }
    }
}
