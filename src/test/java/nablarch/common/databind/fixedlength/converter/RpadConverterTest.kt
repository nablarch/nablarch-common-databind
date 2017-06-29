package nablarch.common.databind.fixedlength.converter

import nablarch.common.databind.fixedlength.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.Assert.*
import org.junit.experimental.runners.*
import org.junit.rules.*
import org.junit.runner.*
import sun.nio.cs.ext.*

/**
 * [Rpad.RpadConverter]のテスト。
 */
@RunWith(Enclosed::class)
class RpadConverterTest {

    /**
     * 読み込み時の変換のテスト
     */
    class ConvertOfRead {

        val sut = Rpad.RpadConverter()
        private val fixedLengthDataBindConfig = FixedLengthDataBindConfig(5, MS932(), "", mutableMapOf())

        @Test
        fun トリム対象の文字がない場合はそのまま戻されること() {
            val rpad = AnnotationConfigs.getRpad("default")
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, null), rpad, "12345".toByteArray(MS932()))
            assertThat(actual, `is`("12345"))
        }

        @Test
        fun デフォルトでは半角スペースがトリムされること() {
            val rpad = AnnotationConfigs.getRpad("default")

            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, null), rpad, "12   ".toByteArray(MS932()))
            assertThat(actual, `is`("12"))
        }

        @Test
        fun カスタムな値を設定した場合その値がトリムされること() {
            val rpad = AnnotationConfigs.getRpad("custom")
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 10, null), rpad, "あいう　　".toByteArray(MS932()))
            assertThat(actual, `is`("あいう"))
        }

        @Test
        fun 全てトリム対象の場合長さゼロの文字列となること() {
            val rpad = AnnotationConfigs.getRpad("default")

            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 10, null), rpad, "     ".toByteArray(MS932()))
            assertThat(actual, isEmptyString())
        }
    }

    /**
     * 書き込み時の変換のテスト
     */
    class ConvertOfWrite {

        @get:Rule
        val expectedException: ExpectedException = ExpectedException.none()

        val sut = Rpad.RpadConverter()
        private val fixedLengthDataBindConfig = FixedLengthDataBindConfig(5, MS932(), "", mutableMapOf())

        @Test
        fun パディングの必要が無い場合値がそのまま返されること() {
            val rpad = AnnotationConfigs.getRpad("default")
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, null), rpad, "12345")
            assertThat(actual, `is`("12345".toByteArray(MS932())))
        }

        @Test
        fun デフォルトの設定の場合半角スペースがパディングされること() {
            val rpad = AnnotationConfigs.getRpad("default")
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, null), rpad, "1")
            assertThat(actual, `is`("1    ".toByteArray(MS932())))
        }

        @Test
        fun カスタムの設定の場合指定した値がパディングされること() {
            val rpad = AnnotationConfigs.getRpad("custom")
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 6, null), rpad, "あい")
            assertThat(actual, `is`("あい　".toByteArray(MS932())))
        }

        @Test
        fun パディング時にサイズを超えた場合は例外が送出されること() {
            val rpad = AnnotationConfigs.getRpad("custom")

            expectedException.expect(IllegalArgumentException::class.java)
            expectedException.expectMessage("length after padding is invalid. expected length 5 but was actual length 6. field_name: name output value: あい padding_char: 　")
            sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, null), rpad, "あい")
        }
    }

    private object AnnotationConfigs {
        @Rpad
        fun default() {
        }

        @Rpad('　')
        fun custom() {
        }

        fun getRpad(methodName: String): Rpad {
            return AnnotationConfigs::class.members.filter {
                it.name == methodName
            }.firstOrNull()?.annotations?.firstOrNull()!! as Rpad
        }
    }

}