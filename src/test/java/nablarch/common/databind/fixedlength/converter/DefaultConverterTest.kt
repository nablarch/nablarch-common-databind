package nablarch.common.databind.fixedlength.converter

import nablarch.common.databind.fixedlength.FieldConfig
import nablarch.common.databind.fixedlength.FixedLengthDataBindConfig
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import sun.nio.cs.ext.MS932

/**
 * [nablarch.common.databind.fixedlength.converter.DefaultConverter]のテスト
 */
class DefaultConverterTest {

    @get:Rule
    var expectedException: ExpectedException = ExpectedException.none()
    
    val sut = DefaultConverter()

    private val fixedLengthDataBindConfig = FixedLengthDataBindConfig(5, MS932(), "", ' ', mutableMapOf())

    @Test
    fun 読み込み時は文字列に変換されること() {
        val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "123".toByteArray(MS932()))
        assertThat(actual, `is`<Any>("123"))
    }
    
    @Test
    fun 出力時はそのままバイト配列に変換されること() {
        val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "12345")
        assertThat(actual, `is`("12345".toByteArray(MS932())))
    }

    @Test
    fun 出力時にnullの場合は空文字を表すバイト配列に変換されること() {
        val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 0, sut), null)
        assertThat(actual, `is`("".toByteArray(MS932())))
    }

    @Test
    fun 出力値の長さが設定よりも長い場合は例外が送出されること() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage("length is invalid. expected length 5 but was actual length 6. field_name: name output value: 123456")
        sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "123456")
    }
}