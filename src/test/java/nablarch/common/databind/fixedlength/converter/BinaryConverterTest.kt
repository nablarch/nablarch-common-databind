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
 * [nablarch.common.databind.fixedlength.converter.Binary.BinaryConverter]のテスト
 */
class BinaryConverterTest {

    @get:Rule
    var expectedException: ExpectedException = ExpectedException.none()
    
    val sut = Binary.BinaryConverter()

    private val fixedLengthDataBindConfig = FixedLengthDataBindConfig(5, MS932(), "", mutableMapOf())

    @Test
    fun 入力時の変換は何も行われないこと() {
        val actual = sut.convertOfRead(null, null, byteArrayOf(0x01, 0x02, 0x03))
        assertThat(actual, `is`<Any>(byteArrayOf(0x01, 0x02, 0x03)))
    }
    
    @Test
    fun 出力時の変換は何も行われないこと() {
        val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 3, sut), byteArrayOf(0x01, 0x02, 0x03))
        assertThat(actual, `is`(byteArrayOf(0x01, 0x02, 0x03)))
    }

    @Test
    fun 出力値の長さが設定よりも長い場合は例外が送出されること() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage("length is invalid. expected length 5 but was actual length 6. field_name: name")
        sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06))
    }
}