package nablarch.common.databind.fixedlength.converter

import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * [nablarch.common.databind.fixedlength.converter.Binary.BinaryConverter]のテスト
 */
class BinaryConverterTest {
    
    val sut = Binary.BinaryConverter()

    @Test
    fun 入力時の変換は何も行われないこと() {
        val actual = sut.convertOfRead(null, null, byteArrayOf(0x01, 0x02, 0x03))
        assertThat(actual, `is`(byteArrayOf(0x01, 0x02, 0x03)))
    }
    
    @Test
    fun 出力時の変換は何も行われないこと() {
        val actual = sut.convertOfWrite(null, null, byteArrayOf(0x01, 0x02, 0x03))
        assertThat(actual, `is`(byteArrayOf(0x01, 0x02, 0x03)))
    }
}