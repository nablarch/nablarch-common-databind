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
 * [nablarch.common.databind.fixedlength.converter.Lpad.LpadConverter]
 */
@RunWith(Enclosed::class)
class LpadConverterTest {

    /**
     * 読み込み時の変換のテスト
     */
    class ConvertOfRead {

        private val fixedLengthDataBindConfig = FixedLengthDataBindConfig(5, MS932(), "", mutableMapOf())

        @Test
        fun トリム対象の文字がない場合はそのまま戻されること() {
            val sut = Lpad.LpadConverter(AnnotationConfigs.getLpad("default"))
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 4, sut), "1234".toByteArray(MS932()))
            assertThat(actual, `is`<Any>("1234"))
        }

        @Test
        fun デフォルトの場合半角の0が先頭からトリムされること() {
            val sut = Lpad.LpadConverter(AnnotationConfigs.getLpad("default"))
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 4, sut), "0034".toByteArray(MS932()))

            assertThat(actual, `is`<Any>("34"))
        }

        @Test
        fun カスタムの場合指定した文字が先頭からトリムされること() {
            val sut = Lpad.LpadConverter(AnnotationConfigs.getLpad("custom"))
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 6, sut), "　　あ".toByteArray(MS932()))
            assertThat(actual, `is`<Any>("あ"))
        }

        @Test
        fun 全てトリム対象の場合長さゼロの文字列となること() {
            val sut = Lpad.LpadConverter(AnnotationConfigs.getLpad("default"))
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 6, sut), "000000".toByteArray(MS932()))
            assertThat(actual, `is`<Any>(""))
        }

        @Test
        fun アノテーション情報が無い場合でも指定した値が先頭からトリムされること() {
            val sut = Lpad.LpadConverter('　')
            val actual = sut.convertOfRead(fixedLengthDataBindConfig, FieldConfig("name", 1, 6, sut), "　　あ".toByteArray(MS932()))
            assertThat(actual, `is`<Any>("あ"))
        }
    }
    
    class ConvertOfWrite {

        @get:Rule
        val expectedException: ExpectedException = ExpectedException.none()
        private val fixedLengthDataBindConfig = FixedLengthDataBindConfig(5, MS932(), "", mutableMapOf())
        
        @Test
        fun パディングの必要が無い場合値がそのまま返されること() {
            val sut = Lpad.LpadConverter(AnnotationConfigs.getLpad("default"))
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut),"12345")
            assertThat(actual, `is`("12345".toByteArray(MS932())))
        }
        
        @Test
        fun デフォルトの設定の場合半角0がパディングされること() {
            val sut = Lpad.LpadConverter(AnnotationConfigs.getLpad("default"))
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut),"1")
            assertThat(actual, `is`("00001".toByteArray(MS932())))
        }


        @Test
        fun カスタムの設定の場合指定した値がパディングされること() {
            val sut = Lpad.LpadConverter(AnnotationConfigs.getLpad("custom"))
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 6, sut),"あい")
            assertThat(actual, `is`("　あい".toByteArray(MS932())))
        }

        @Test
        fun パディング時にサイズを超えた場合は例外が送出されること() {
            val sut = Lpad.LpadConverter(AnnotationConfigs.getLpad("custom"))

            expectedException.expect(IllegalArgumentException::class.java)
            expectedException.expectMessage("length after padding is invalid. expected length 5 but was actual length 6. field_name: name output value: あい padding_char: 　")
            sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut), "あい")
        }

        @Test
        fun `nullの場合、空文字に変換してパディングされること`() {
            val sut = Lpad.LpadConverter(AnnotationConfigs.getLpad("default"))
            val actual = sut.convertOfWrite(fixedLengthDataBindConfig, FieldConfig("name", 1, 5, sut),null)
            assertThat(actual, `is`("00000".toByteArray(MS932())))
        }
    }

    private object AnnotationConfigs {
        @Lpad
        fun default() {
        }

        @Lpad('　')
        fun custom() {
        }

        fun getLpad(methodName: String): Lpad {
            return AnnotationConfigs::class.members.filter {
                it.name == methodName
            }.firstOrNull()?.annotations?.firstOrNull()!! as Lpad
        }
    }

}