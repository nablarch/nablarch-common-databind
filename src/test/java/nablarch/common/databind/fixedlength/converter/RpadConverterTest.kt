package nablarch.common.databind.fixedlength.converter

import nablarch.common.databind.fixedlength.FieldConfig
import nablarch.common.databind.fixedlength.FixedLengthDataBindConfig
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import sun.nio.cs.ext.MS932

/**
 * [Rpad.RpadConverter]のテスト。
 */
@RunWith(Enclosed::class)
class RpadConverterTest {

    /**
     * 読み込み時の変換のテスト
     */
    class ConvertOfRead {

        private val fixedLengthDataBindConfig = FixedLengthDataBindConfig(5, MS932(), "", mutableMapOf())

        @Test
        fun トリム対象の文字がない場合はそのまま戻されること() {
            val sut = Rpad.RpadConverter(AnnotationConfigs.getRpad("default").value)
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "12345".toByteArray(MS932()))
            assertThat(actual, `is`<Any>("12345"))
        }

        @Test
        fun デフォルトでは半角スペースがトリムされること() {
            val sut = Rpad.RpadConverter(AnnotationConfigs.getRpad("default").value)
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "12   ".toByteArray(MS932()))
            assertThat(actual, `is`<Any>("12"))
        }

        @Test
        fun カスタムな値を設定した場合その値がトリムされること() {
            val sut = Rpad.RpadConverter(AnnotationConfigs.getRpad("custom").value)
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 10, sut), "あいう　　".toByteArray(MS932()))
            assertThat(actual, `is`<Any>("あいう"))
        }

        @Test
        fun 全てトリム対象の場合長さゼロの文字列となること() {
            val sut = Rpad.RpadConverter(AnnotationConfigs.getRpad("default").value)
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 10, sut), "     ".toByteArray(MS932()))
            assertThat(actual, `is`<Any>(""))
        }

        @Test
        fun アノテーション情報が無い場合でも指定した値でトリムされること() {
            val sut = Rpad.RpadConverter('　')
            val actual = sut.convertOfRead(fixedLengthDataBindConfig,FieldConfig("name", 1, 10,sut), "あいう　　".toByteArray(MS932()))
            assertThat(actual, `is`<Any>("あいう"))
        }
    }

    /**
     * 書き込み時の変換のテスト
     */
    class ConvertOfWrite {

        @get:Rule
        val expectedException: ExpectedException = ExpectedException.none()

        private val fixedLengthDataBindConfig = FixedLengthDataBindConfig(5, MS932(), "", mutableMapOf())

        @Test
        fun パディングの必要が無い場合値がそのまま返されること() {
            val sut = Rpad.RpadConverter(AnnotationConfigs.getRpad("default").value)
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "12345")
            assertThat(actual, `is`("12345".toByteArray(MS932())))
        }

        @Test
        fun デフォルトの設定の場合半角スペースがパディングされること() {
            val sut = Rpad.RpadConverter(AnnotationConfigs.getRpad("default").value)
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "1")
            assertThat(actual, `is`("1    ".toByteArray(MS932())))
        }

        @Test
        fun カスタムの設定の場合指定した値がパディングされること() {
            val sut = Rpad.RpadConverter(AnnotationConfigs.getRpad("custom").value)
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 6, sut), "あい")
            assertThat(actual, `is`("あい　".toByteArray(MS932())))
        }

        @Test
        fun パディング時にサイズを超えた場合は例外が送出されること() {
            val sut = Rpad.RpadConverter(AnnotationConfigs.getRpad("custom").value)
            expectedException.expect(IllegalArgumentException::class.java)
            expectedException.expectMessage("length after padding is invalid. expected length 5 but was actual length 6. field_name: name output value: あい padding_char: 　")
            sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "あい")
        }

        @Test
        fun `nullの場合、空文字に変換してパディングされること`() {
            val sut = Rpad.RpadConverter(AnnotationConfigs.getRpad("default").value)
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut),null)
            assertThat(actual, `is`("     ".toByteArray(MS932())))
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