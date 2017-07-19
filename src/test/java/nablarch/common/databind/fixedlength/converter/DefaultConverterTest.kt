package nablarch.common.databind.fixedlength.converter

import nablarch.common.databind.fixedlength.FieldConfig
import nablarch.common.databind.fixedlength.FixedLengthDataBindConfig
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import sun.nio.cs.ext.MS932

/**
 * [nablarch.common.databind.fixedlength.converter.DefaultConverter]のテスト
 */
class DefaultConverterTest {
    
    val sut = DefaultConverter()

    private val fixedLengthDataBindConfig = FixedLengthDataBindConfig(5, MS932(), "", mutableMapOf())

    @Test
    fun 読み込み時は文字列に変換されること() {
        val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "123".toByteArray(MS932()))
        assertThat(actual, `is`<Any>("123"))
    }
    
    @Test
    fun 出力時はそのままバイト配列に変換されること() {
        val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "123")
        assertThat(actual, `is`("123".toByteArray(MS932())))
    }

    @Test
    fun 出力時にnullの場合は空文字を表すバイト配列に変換されること() {
        val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), null)
        assertThat(actual, `is`("".toByteArray(MS932())))
    }
}