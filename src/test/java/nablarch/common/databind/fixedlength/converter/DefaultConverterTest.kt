package nablarch.common.databind.fixedlength.converter

import nablarch.common.databind.fixedlength.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.*

/**
 * [nablarch.common.databind.fixedlength.converter.DefaultConverter]のテスト
 */
class DefaultConverterTest {

    @get:Rule
    var expectedException: ExpectedException = ExpectedException.none()
    
    val sut = DefaultConverter()

    private val fixedLengthDataBindConfig = FixedLengthDataBindConfig(5, charset("MS932"), "", ' ', mutableMapOf())

    @Test
    fun 読み込み時は文字列に変換されること() {
        val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "123".toByteArray(charset("MS932")))
        assertThat(actual, `is`<Any>("123"))
    }
    
    @Test
    fun 出力時はそのままバイト配列に変換されること() {
        val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "12345")
        assertThat(actual, `is`("12345".toByteArray(charset("MS932"))))
    }

    @Test
    fun 出力時にnullの場合は空文字を表すバイト配列に変換されること() {
        val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 0, sut), null)
        assertThat(actual, `is`("".toByteArray(charset("MS932"))))
    }

    @Test
    fun 出力値の長さが設定よりも長い場合は例外が送出されること() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage("length is invalid. expected length 5 but was actual length 6. field_name: name output value: 123456")
        sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "123456")
    }
}