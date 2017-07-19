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
                @get:Field(offset = 1, length = 8)
                @get:Rpad
                var name: String? = null,
                @get:Field(offset = 9, length = 8)
                @get:Rpad('a')
                var text: String? = null,
                @get:Field(offset = 17, length = 3)
                @get:Lpad
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
    fun `レコード長がオーバーしている場合に例外が発生すること`() {

        val stream = ByteArrayOutputStream()

        @FixedLength(length = 19, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
                @get:Field(offset = 1, length = 8)
                var name: String? = null,
                @get:Field(offset = 9, length = 8)
                var text: String? = null,
                @get:Field(offset = 17, length = 3)
                var age: Int? = null
        ) {
            constructor() : this(null, null, null)
        }

        ObjectMapperFactory.create(TestBean::class.java, stream).use { sut ->
            assertThat(sut, Matchers.instanceOf(BeanFixedLengthMapper::class.java))

            expectedException.expect(IllegalArgumentException::class.java)
            expectedException.expectMessage("record length is invalid. expected_length:19, actual_length:20")
            sut.write(TestBean("testname", "testtext", 1000))

        }
    }

    @Test
    fun `レコード長が足りない場合に例外が発生すること`() {

        val stream = ByteArrayOutputStream()

        @FixedLength(length = 19, charset = "MS932", lineSeparator = "\r\n")
        data class TestBean(
                @get:Field(offset = 1, length = 8)
                var name: String? = null,
                @get:Field(offset = 9, length = 8)
                var text: String? = null,
                @get:Field(offset = 17, length = 3)
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
                @get:Field(offset = 1, length = 8)
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
}
