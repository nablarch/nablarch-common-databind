package nablarch.common.databind;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.nio.charset.Charset;
import java.util.Collections;

import nablarch.common.databind.csv.Csv;
import nablarch.common.databind.csv.CsvDataBindConfig;
import nablarch.common.databind.csv.CsvFormat;
import nablarch.common.databind.csv.Quoted;
import nablarch.core.beans.BeansException;

import org.junit.Test;

/**
 * {@link DataBindUtil}のテストクラス。
 */
public class DataBindUtilTest {

    /**
     * Beanに{@link Csv}の設定が正しく適用されること
     *
     * @throws Exception
     */
    @Test
    public void testFindPreference_default() throws Exception {

        // DEFAULT
        CsvDataBindConfig actual = (CsvDataBindConfig) DataBindUtil.createDataBindConfig(PersonDefault.class);
        CsvDataBindConfig expected = CsvDataBindConfig.DEFAULT;

        assertThat(actual.getFieldSeparator(), is(expected.getFieldSeparator()));
        assertThat(actual.getLineSeparator(), is(expected.getLineSeparator()));
        assertThat(actual.getQuote(), is(expected.getQuote()));
        assertThat(actual.isIgnoreEmptyLine(), is(expected.isIgnoreEmptyLine()));
        assertThat(actual.isRequiredHeader(), is(expected.isRequiredHeader()));
        assertThat(actual.getCharset(), is(expected.getCharset()));
        assertThat(actual.getQuoteMode(), is(expected.getQuoteMode()));

        // RFC4180
        actual = (CsvDataBindConfig) DataBindUtil.createDataBindConfig(PersonRfc4180.class);
        expected = CsvDataBindConfig.RFC4180;

        assertThat(actual.getFieldSeparator(), is(expected.getFieldSeparator()));
        assertThat(actual.getLineSeparator(), is(expected.getLineSeparator()));
        assertThat(actual.getQuote(), is(expected.getQuote()));
        assertThat(actual.isIgnoreEmptyLine(), is(expected.isIgnoreEmptyLine()));
        assertThat(actual.isRequiredHeader(), is(expected.isRequiredHeader()));
        assertThat(actual.getCharset(), is(expected.getCharset()));
        assertThat(actual.getQuoteMode(), is(expected.getQuoteMode()));

        // EXCEL
        actual = (CsvDataBindConfig) DataBindUtil.createDataBindConfig(PersonExcel.class);
        expected = CsvDataBindConfig.EXCEL;

        assertThat(actual.getFieldSeparator(), is(expected.getFieldSeparator()));
        assertThat(actual.getLineSeparator(), is(expected.getLineSeparator()));
        assertThat(actual.getQuote(), is(expected.getQuote()));
        assertThat(actual.isIgnoreEmptyLine(), is(expected.isIgnoreEmptyLine()));
        assertThat(actual.isRequiredHeader(), is(expected.isRequiredHeader()));
        assertThat(actual.getCharset(), is(expected.getCharset()));
        assertThat(actual.getQuoteMode(), is(expected.getQuoteMode()));

        // TSV
        actual = (CsvDataBindConfig) DataBindUtil.createDataBindConfig(PersonTsv.class);
        expected = CsvDataBindConfig.TSV;

        assertThat(actual.getFieldSeparator(), is(expected.getFieldSeparator()));
        assertThat(actual.getLineSeparator(), is(expected.getLineSeparator()));
        assertThat(actual.getQuote(), is(expected.getQuote()));
        assertThat(actual.isIgnoreEmptyLine(), is(expected.isIgnoreEmptyLine()));
        assertThat(actual.isRequiredHeader(), is(expected.isRequiredHeader()));
        assertThat(actual.getCharset(), is(expected.getCharset()));
        assertThat(actual.getQuoteMode(), is(expected.getQuoteMode()));
    }

    /**
     * Beanに{@link CsvFormat}が設定されている場合、その設定が適用されること
     *
     * @throws Exception
     */
    @Test
    public void testFindPreference_custom() throws Exception {

        // クォートモードがNORMAL
        CsvDataBindConfig actual = (CsvDataBindConfig) DataBindUtil.createDataBindConfig(PersonCustomNormal.class);
        CsvDataBindConfig expected = new CsvDataBindConfig('\t',"\n",'\'',false, false, new String[]{}, Charset.forName("MS932"),
                false, CsvDataBindConfig.QuoteMode.NORMAL, Collections.<String>emptyList()
        );
        assertThat(actual.getFieldSeparator(), is(expected.getFieldSeparator()));
        assertThat(actual.getLineSeparator(), is(expected.getLineSeparator()));
        assertThat(actual.getQuote(), is(expected.getQuote()));
        assertThat(actual.isIgnoreEmptyLine(), is(expected.isIgnoreEmptyLine()));
        assertThat(actual.isRequiredHeader(), is(expected.isRequiredHeader()));
        assertThat(actual.getCharset(), is(expected.getCharset()));
        assertThat(actual.getQuoteMode(), is(expected.getQuoteMode()));
        assertThat(actual.getQuotedColumnNames().size(), is(0));

        // クォートモードがALL
        actual = (CsvDataBindConfig) DataBindUtil.createDataBindConfig(PersonCustomAll.class);
        expected = new CsvDataBindConfig('\t',"\n",'\'',false,false, new String[]{}, Charset.forName("MS932"), false,
                CsvDataBindConfig.QuoteMode.ALL, Collections.<String>emptyList()
        );
        assertThat(actual.getFieldSeparator(), is(expected.getFieldSeparator()));
        assertThat(actual.getLineSeparator(), is(expected.getLineSeparator()));
        assertThat(actual.getQuote(), is(expected.getQuote()));
        assertThat(actual.isIgnoreEmptyLine(), is(expected.isIgnoreEmptyLine()));
        assertThat(actual.isRequiredHeader(), is(expected.isRequiredHeader()));
        assertThat(actual.getCharset(), is(expected.getCharset()));
        assertThat(actual.getQuoteMode(), is(expected.getQuoteMode()));
        assertThat(actual.getQuotedColumnNames().size(), is(0));

        // クォートモードがNOT_NUMERIC
        actual = (CsvDataBindConfig) DataBindUtil.createDataBindConfig(PersonCustomNotNumeric.class);
        expected = new CsvDataBindConfig('\t',"\n",'\'',false,false, new String[]{}, Charset.forName("MS932"), false,
                CsvDataBindConfig.QuoteMode.NOT_NUMERIC, Collections.<String>emptyList()
        );
        assertThat(actual.getFieldSeparator(), is(expected.getFieldSeparator()));
        assertThat(actual.getLineSeparator(), is(expected.getLineSeparator()));
        assertThat(actual.getQuote(), is(expected.getQuote()));
        assertThat(actual.isIgnoreEmptyLine(), is(expected.isIgnoreEmptyLine()));
        assertThat(actual.isRequiredHeader(), is(expected.isRequiredHeader()));
        assertThat(actual.getCharset(), is(expected.getCharset()));
        assertThat(actual.getQuoteMode(), is(expected.getQuoteMode()));
        assertThat(actual.getQuotedColumnNames().size(), is(0));

        // クォートモードがCUSTOM
        actual = (CsvDataBindConfig) DataBindUtil.createDataBindConfig(PersonCustom.class);
        expected = new CsvDataBindConfig('\t',"\n",'\'',false,false, new String[]{}, Charset.forName("MS932"), false,
                CsvDataBindConfig.QuoteMode.CUSTOM, Collections.<String>emptyList()
        );
        assertThat(actual.getFieldSeparator(), is(expected.getFieldSeparator()));
        assertThat(actual.getLineSeparator(), is(expected.getLineSeparator()));
        assertThat(actual.getQuote(), is(expected.getQuote()));
        assertThat(actual.isIgnoreEmptyLine(), is(expected.isIgnoreEmptyLine()));
        assertThat(actual.isRequiredHeader(), is(expected.isRequiredHeader()));
        assertThat(actual.getCharset(), is(expected.getCharset()));
        assertThat(actual.getQuoteMode(), is(expected.getQuoteMode()));
        assertThat(actual.getQuotedColumnNames().size(), is(1));
        assertThat(actual.getQuotedColumnNames().get(0), is("name"));
    }

    /**
     * Beanに{@link Csv}が設定されていない場合、例外が発生すること
     *
     * @throws Exception
     */
    @Test
    public void testFindPreference_undefined() throws Exception {

        try {
            DataBindUtil.createDataBindConfig(Person.class);
            fail("Csvが未定義のため例外が発生");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("can not find config. class = [nablarch.common.databind.DataBindUtilTest$Person]"));
        }
    }

    /**
     * Beanに{@link Csv#type()}に{@link nablarch.common.databind.csv.Csv.CsvType#CUSTOM}以外が設定されており、
     * かつ{@link CsvFormat}が設定されている場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testFindPreference_not_custom() throws Exception {

        try {
            DataBindUtil.createDataBindConfig(PersonBoth.class);
            fail("CsvTypeがCUSTOMでないのにCustomFormatを定義したため例外が発生");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("CsvFormat annotation can not defined because CsvType is not CUSTOM. class = [nablarch.common.databind.DataBindUtilTest$PersonBoth]"));
        }
    }

    /**
     * Beanに{@link CsvDataBindConfig#requiredHeader}が{@code true}かつ{@link Csv#headers()}が未設定の場合に例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testFindPreference_headers_undefined() throws Exception {

        try {
            DataBindUtil.createDataBindConfig(PersonHeader.class);
            fail("ヘッダーが設定されていないため例外が発生");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("headers and properties size does not match. class = [nablarch.common.databind.DataBindUtilTest$PersonHeader]"));
        }
    }

    /**
     * {@link Csv#properties()}に空の配列が設定されている場合に例外が送出されること
     *
     * @throws Exception
     */
    @Test
    public void testFindPreference_properties_empty() throws Exception {
        try {
            DataBindUtil.createDataBindConfig(PersonPropertyEmpty.class);
            fail("プロパティが設定されていないため例外が発生");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("properties is required. class = [nablarch.common.databind.DataBindUtilTest$PersonPropertyEmpty]"));
        }
    }

    /**
     * Beanを1件取得できること
     *
     * @throws Exception
     */
    @Test
    public void testGetInstance() throws Exception {

        Person person = DataBindUtil.getInstance(
                Person.class, new String[]{"age", "name"}, new String[]{"20", "山田太郎"});

        assertThat("ageが設定されていること", person.getAge(), is(20));
        assertThat("nameが設定されていること", person.getName(), is("山田太郎"));
    }

    /**
     * Beanにデフォルトコンストラクタが定義されていない場合に例外を送出すること
     *
     * @throws Exception
     */
    @Test(expected = BeansException.class)
    public void testGetInstance_undefined_defaultConstructor() throws Exception {
        DataBindUtil.getInstance(PersonConstructor.class, new String[]{"age", "name"}, new String[]{"20", "山田太郎"});
        fail("Beanにデフォルトコンストラクタが定義されていないため、例外が発生");
    }

    /**
     * 行番号を保持する設定のBeanを1件取得できること
     *
     * @throws Exception
     */
    @Test
    public void testGetInstanceWithLineNumber() throws Exception {

        PersonWithLineNumber personWithLineNumber = DataBindUtil.getInstanceWithLineNumber(
                PersonWithLineNumber.class, new String[]{"age", "name"}, new String[]{"20", "山田太郎"}, "lineNumber", 1);

        assertThat("ファイル行数が設定されていること", personWithLineNumber.getLineNumber(), is(1L));
        assertThat("ageが設定されていること", personWithLineNumber.getAge(), is(20));
        assertThat("nameが設定されていること", personWithLineNumber.getName(), is("山田太郎"));
    }

    /**
     * Beanにデフォルトコンストラクタが定義されていない場合に例外を送出すること
     *
     * @throws Exception
     */
    @Test(expected = BeansException.class)
    public void testGetInstanceWIthLineNumber_undefined_defaultConstructor() throws Exception {
        DataBindUtil.getInstanceWithLineNumber(PersonConstructor.class, new String[]{"age", "name"}, new String[]{"20", "山田太郎"}, "lineNumber", 1);
        fail("Beanにデフォルトコンストラクタが定義されていないため、例外が発生");
    }

    public static class Person {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"age", "name"}, headers = {"年齢", "氏名"})
    public static class PersonConstructor {
        public PersonConstructor(Integer age, String name) {
            this.age = age;
            this.name = name;
        }
        private Integer age;
        private String name;
        public Integer getAge() {
            return age;
        }
        public void setAge(Integer age) {
            this.age = age;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"age", "name"}, headers = {"年齢", "氏名"})
    public static class PersonDefault {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.RFC4180, properties = {"age", "name"})
    public static class PersonRfc4180 {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.EXCEL, properties = {"age", "name"})
    public static class PersonExcel {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.TSV, properties = {"age", "name"})
    public static class PersonTsv {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.CUSTOM, properties = {"age", "name"})
    @CsvFormat(fieldSeparator = '\t',
            lineSeparator = "\n",
            quote = '\'',
            ignoreEmptyLine = false,
            requiredHeader = false,
            charset = "MS932",
            emptyToNull = false,
            quoteMode = CsvDataBindConfig.QuoteMode.NORMAL
    )
    public static class PersonCustomNormal {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.CUSTOM, properties = {"age", "name"})
    @CsvFormat(fieldSeparator = '\t',
            lineSeparator = "\n",
            quote = '\'',
            ignoreEmptyLine = false,
            requiredHeader = false,
            charset = "MS932",
            emptyToNull = false,
            quoteMode = CsvDataBindConfig.QuoteMode.ALL)
    public static class PersonCustomAll {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.CUSTOM, properties = {"age", "name"})
    @CsvFormat(fieldSeparator = '\t',
            lineSeparator = "\n",
            quote = '\'',
            ignoreEmptyLine = false,
            requiredHeader = false,
            charset = "MS932",
            emptyToNull = false,
            quoteMode = CsvDataBindConfig.QuoteMode.NOT_NUMERIC)
    public static class PersonCustomNotNumeric {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.CUSTOM, properties = {"age", "name"})
    @CsvFormat(fieldSeparator = '\t',
            lineSeparator = "\n",
            quote = '\'',
            ignoreEmptyLine = false,
            requiredHeader = false,
            charset = "MS932",
            emptyToNull = false,
            quoteMode = CsvDataBindConfig.QuoteMode.CUSTOM)
    public static class PersonCustom {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @Quoted
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"age", "name"})
    @CsvFormat(fieldSeparator = '\t',
            lineSeparator = "\n",
            quote = '\'',
            ignoreEmptyLine = false,
            requiredHeader = false,
            charset = "MS932",
            emptyToNull = false,
            quoteMode = CsvDataBindConfig.QuoteMode.NORMAL)
    public static class PersonBoth {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.CUSTOM, properties = {"age", "name"})
    @CsvFormat(fieldSeparator = '\t',
            lineSeparator = "\n",
            quote = '\'',
            ignoreEmptyLine = false,
            requiredHeader = true,
            charset = "MS932",
            emptyToNull = false,
            quoteMode = CsvDataBindConfig.QuoteMode.NORMAL)
    public static class PersonHeader {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.DEFAULT, properties = {})
    public static class PersonPropertyEmpty {
        private Integer age;
        private String name;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class PersonWithLineNumber {

        private Long lineNumber;
        private Integer age;
        private String name;

        @LineNumber
        public Long getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(Long lineNumber) {
            this.lineNumber = lineNumber;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}