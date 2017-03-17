package nablarch.common.databind.csv;

import static org.eclipse.persistence.jpa.jpql.Assert.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.beans.HasPropertyWithValue.*;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertThat;

import nablarch.common.databind.InvalidDataFormatException;
import nablarch.common.databind.LineNumber;
import nablarch.common.databind.ObjectMapper;
import nablarch.common.databind.ObjectMapperFactory;
import nablarch.core.beans.BeansException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * {@link CsvBeanMapper}のテストクラス。
 */
public class CsvBeanMapperTest {

    @Rule
    public CsvResource resource = new CsvResource("test.csv", "utf-8", "\r\n");

    /**
     * {@link CsvBeanMapper}のコンストラクタに{@link java.io.InputStream}を指定した場合、
     * Beanの{@link Csv#type()}の設定が適用されて、CSVのレコードを1件読み込めること
     *
     * @throws Exception
     */
    @Test
    public void testRead_basic_inputstream() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("");
        resource.writeLine("20,山田太郎");
        resource.writeLine("");
        resource.writeLine("30,");
        resource.close();

        final ObjectMapper<PersonDefault> mapper = ObjectMapperFactory.create(PersonDefault.class, resource.createInputStream());
        PersonDefault person1 = mapper.read();
        PersonDefault person2 = mapper.read();
        PersonDefault person3 = mapper.read();
        mapper.close();

        assertThat(person1.getAge(), is(20));
        assertThat(person1.getName(), is("山田太郎"));

        assertThat(person2.getAge(), is(30));
        assertThat(person2.getName(), nullValue());

        assertThat(person3, is(nullValue()));
    }

    /**
     * {@link CsvBeanMapper}のコンストラクタに{@link java.io.InputStream}を指定した場合、
     * Beanの{@link CsvFormat}の設定が適用されて、CSVのレコードを1件読み込めること
     *
     * @throws Exception
     */
    @Test
    public void testRead_custom_inputstream() throws Exception {
        resource.writeLine("'20'\t'山田太郎'");
        resource.close();

        final ObjectMapper<PersonCustom> mapper = ObjectMapperFactory.create(PersonCustom.class, resource.createInputStream());
        PersonCustom person = mapper.read();
        mapper.close();

        assertThat(person.getAge(), is(20));
        assertThat(person.getName(), is("山田太郎"));
    }

    /**
     * {@link CsvBeanMapper}のコンストラクタに{@link java.io.Reader}を指定した場合、
     * Beanの{@link Csv#type()}の設定が適用されて、CSVのレコードを1件読み込めること
     *
     * @throws Exception
     */
    @Test
    public void testRead_basic_reader() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.close();

        final ObjectMapper<PersonDefault> mapper = ObjectMapperFactory.create(PersonDefault.class, resource.createReader());
        PersonDefault person = mapper.read();
        mapper.close();

        assertThat(person.getAge(), is(20));
        assertThat(person.getName(), is("山田太郎"));
    }

    /**
     * {@link CsvBeanMapper}のコンストラクタに{@link java.io.Reader}を指定した場合、
     * Beanの{@link CsvFormat}の設定が適用されて、CSVのレコードを1件読み込めること
     *
     * @throws Exception
     */
    @Test
    public void testRead_custom_reader() throws Exception {
        resource.writeLine("'20'\t'山田太郎'");
        resource.close();

        final ObjectMapper<PersonCustom> mapper = ObjectMapperFactory.create(PersonCustom.class, resource.createReader());
        PersonCustom person = mapper.read();
        mapper.close();

        assertThat(person.getAge(), is(20));
        assertThat(person.getName(), is("山田太郎"));
    }

    /**
     * 既にクローズされている場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testRead_closed() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.close();

        // ファイルが存在する場合
        final ObjectMapper<PersonDefault> mapper = ObjectMapperFactory.create(PersonDefault.class, resource.createInputStream());
        PersonDefault person = mapper.read();

        // クローズ前であれば読み込みできること
        assertThat(person.getAge(), is(20));
        assertThat(person.getName(), is("山田太郎"));

        mapper.close();
        try {
            mapper.read();
            fail("既にクローズされているため、読み込みできないこと");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("failed to read file."));
        }
    }

    /**
     * プロパティ名配列とレコードの項目数に差異がある場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testRead_mismatch_property_size() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎,mismatch");
        resource.close();

        final ObjectMapper<PersonDefault> mapper = ObjectMapperFactory.create(PersonDefault.class, resource.createInputStream());

        try {
            mapper.read();
            fail("項目数が異なるため、読み込みできないこと");
        } catch (InvalidDataFormatException e) {
            assertThat(e.getMessage(), containsString("property size does not match."
                    + " expected field count = [2], actual field count = [3]. line number = [2]"));
        } finally {
            mapper.close();
        }
    }

    /**
     * 単一項目の空行無視も読み込めること
     */
    @Test
    public void testRead_singleField() throws Exception {
        resource.writeLine("title");
        resource.writeLine("name");
        resource.writeLine("");
        resource.close();

        final ObjectMapper<PersonSub> mapper = ObjectMapperFactory.create(PersonSub.class, resource.createReader());
        final PersonSub person = mapper.read();
        assertThat(person.getName(), is("name"));
        assertThat(mapper.read(), is(nullValue()));
        mapper.close();
    }

    /**
     * CSVのレコードを複数レコード読み込めること
     *
     * @throws Exception
     */
    @Test
    public void testRead_multi() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.writeLine("25,田中次郎");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        final ObjectMapper<PersonDefault> mapper = ObjectMapperFactory.create(PersonDefault.class, resource.createReader());
        PersonDefault person = mapper.read();
        assertThat(person.getAge(), is(20));
        assertThat(person.getName(), is("山田太郎"));

        person = mapper.read();
        assertThat(person.getAge(), is(25));
        assertThat(person.getName(), is("田中次郎"));

        person = mapper.read();
        assertThat(person.getAge(), is(30));
        assertThat(person.getName(), is("鈴木三郎"));

        person = mapper.read();
        assertThat(person, is(nullValue()));

        mapper.close();
    }

    /**
     * 空行を無視する設定で、空行を含むCSVを読み込んだ場合、空行をスキップすること
     *
     * @throws Exception
     */
    @Test
    public void testRead_ignore_empty_line() throws Exception {
        resource.writeLine("");
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.writeLine("");
        resource.writeLine("");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        final ObjectMapper<PersonDefault> mapper = ObjectMapperFactory.create(PersonDefault.class, resource.createInputStream());
        PersonDefault person = mapper.read();
        assertThat(person.getAge(), is(20));
        assertThat(person.getName(), is("山田太郎"));

        person = mapper.read();
        assertThat(person.getAge(), is(30));
        assertThat(person.getName(), is("鈴木三郎"));

        mapper.close();
    }

    /**
     * 空行を無視しない設定で、空行を含むCSVを読み込んだ場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testRead_not_ignore_empty_line() throws Exception {
        resource.writeLine("20,山田太郎");
        resource.writeLine("");
        resource.writeLine("");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        final ObjectMapper<PersonRfc4180> mapper = ObjectMapperFactory.create(PersonRfc4180.class, resource.createInputStream());
        PersonRfc4180 person = mapper.read();
        assertThat(person.getAge(), is(20));
        assertThat(person.getName(), is("山田太郎"));
        assertThat(person.getName(), is("山田太郎"));

        try {
            mapper.read();
            fail("空行をバインドしようとして例外が発生");
        } catch (InvalidDataFormatException e) {
            assertThat(e.getMessage(), containsString("property size does not match."
                    + " expected field count = [2], actual field count = [1]. line number = [2]"));
        }

        mapper.close();
    }

    /**
     * ヘッダありの設定で、ヘッダなしのCSVを読み込んだ場合、先頭の1レコードをスキップすること
     *
     * @throws Exception
     */
    @Test
    public void testRead_exists_header() throws Exception {
        resource.writeLine("20,山田太郎");
        resource.writeLine("25,田中次郎");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        final ObjectMapper<PersonDefault> mapper = ObjectMapperFactory.create(PersonDefault.class, resource.createInputStream());
        PersonDefault person = mapper.read();
        assertThat(person.getAge(), is(25));
        assertThat(person.getName(), is("田中次郎"));

        person = mapper.read();
        assertThat(person.getAge(), is(30));
        assertThat(person.getName(), is("鈴木三郎"));

        mapper.close();
    }

    /**
     * ヘッダなしの設定で、ヘッダありのCSVを読み込んだ場合、例外を送出すること
     *
     * @throws Exception
     */
    @Test
    public void testRead_not_exists_header() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.writeLine("25,田中次郎");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        final ObjectMapper<PersonRfc4180> mapper = ObjectMapperFactory.create(PersonRfc4180.class, resource.createInputStream());
        try {
            PersonRfc4180 person = mapper.read();
        } catch (Exception e) {
            assertThat(e, instanceOf(BeansException.class));
        } finally {
            mapper.close();
        }
    }


    /**
     * ヘッダなしで行番号を保持する設定で行番号を取得できること
     *
     * @throws Exception
     */
    @Test
    public void testRead_with_linenumber_property() throws Exception {
        resource.writeLine("10,山田太郎");
        resource.close();

        final ObjectMapper<PersonWithLineNumber> mapper = ObjectMapperFactory.create(PersonWithLineNumber.class, resource.createInputStream());
        PersonWithLineNumber personWithLineNumber = mapper.read();

        assertThat(personWithLineNumber.getLineNumber(), is(1L));
        assertThat(personWithLineNumber.getAge(), is(10));
        assertThat(personWithLineNumber.getName(), is("山田太郎"));

        mapper.close();
    }

    /**
     * ヘッダありで行番号を保持する設定で行番号を取得できること
     *
     * @throws Exception
     */
    @Test
    public void testRead_with_header_and_linenumber_property() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        final ObjectMapper<PersonWithHeaderAndLineNumber> mapper = ObjectMapperFactory.create(PersonWithHeaderAndLineNumber.class, resource.createInputStream());
        PersonWithHeaderAndLineNumber personWithHeaderAndLineNumber = mapper.read();

        assertThat(personWithHeaderAndLineNumber.getLineNumber(), is(2L));
        assertThat(personWithHeaderAndLineNumber.getAge(), is(20));
        assertThat(personWithHeaderAndLineNumber.getName(), is("山田太郎"));

        personWithHeaderAndLineNumber = mapper.read();
        assertThat(personWithHeaderAndLineNumber.getLineNumber(), is(3L));
        assertThat(personWithHeaderAndLineNumber.getAge(), is(30));
        assertThat(personWithHeaderAndLineNumber.getName(), is("鈴木三郎"));

        mapper.close();
    }

    /**
     * 行番号を保持する設定で空行がある場合に空行込みの行番号を取得できること
     *
     * @throws Exception
     */
    @Test
    public void testRead_with_linenumber_property_and_empty_line() throws Exception {
        resource.writeLine("");
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.writeLine("");
        resource.writeLine("");
        resource.writeLine("30,鈴木三郎");
        resource.close();

        final ObjectMapper<PersonWithHeaderAndLineNumber> mapper = ObjectMapperFactory.create(PersonWithHeaderAndLineNumber.class, resource.createInputStream());
        PersonWithHeaderAndLineNumber personWithHeaderAndLineNumber = mapper.read();

        assertThat(personWithHeaderAndLineNumber.getLineNumber(), is(3L));
        assertThat(personWithHeaderAndLineNumber.getAge(), is(20));
        assertThat(personWithHeaderAndLineNumber.getName(), is("山田太郎"));

        personWithHeaderAndLineNumber = mapper.read();
        assertThat(personWithHeaderAndLineNumber.getLineNumber(), is(6L));
        assertThat(personWithHeaderAndLineNumber.getAge(), is(30));
        assertThat(personWithHeaderAndLineNumber.getName(), is("鈴木三郎"));

        mapper.close();
    }


    /**
     * BeanにIDカラムが複数定義されている場合に例外を送出すること。
     *
     * @throws Exception 例外
     */
    @Test
    public void testRead_multi_defined_lineNumber() throws Exception {
        resource.writeLine("年齢,氏名");
        resource.writeLine("20,山田太郎");
        resource.close();

        ObjectMapper<PersonWithDuplicateLineNumber> mapper = null;

        try {
            mapper = ObjectMapperFactory.create(PersonWithDuplicateLineNumber.class, resource.createInputStream());
            Assert.fail("BeanにLineNumberカラムが複数定義されているため、例外が発生。");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("line number column should be defined only one. class = [nablarch.common.databind.csv.CsvBeanMapperTest$PersonWithDuplicateLineNumber]"));

        } finally {
            if(mapper != null){
                mapper.close();
            }
        }
    }

    /**
     * 空のフィールドはnullとして読み込めること
     */
    @Test
    public void testEmptyToNull() throws Exception {
        resource.writeLine("年齢,名前");
        resource.writeLine("20,山田太郎");
        resource.writeLine(",山田太郎");
        resource.writeLine(",");
        resource.close();

        final ObjectMapper<PersonDefault> mapper = ObjectMapperFactory.create(
                PersonDefault.class, resource.createInputStream());

        assertThat(mapper.read(), allOf(
                hasProperty("name", is("山田太郎")),
                hasProperty("age", is(20))
        ));
        
        assertThat(mapper.read(), allOf(
                hasProperty("name", is("山田太郎")),
                hasProperty("age", is(nullValue()))
        ));
        
        assertThat(mapper.read(), allOf(
                hasProperty("name", is(nullValue())),
                hasProperty("age", is(nullValue()))
        ));
    }
    
    /**
     * 空のフィールドは空のまま読み込めること
     */
    @Test
    public void testEmptyToEmpty() throws Exception {
        resource.writeLine("20\t山田太郎");
        resource.writeLine("\t山田太郎");
        resource.writeLine("\t");
        resource.writeLine(" \t 　 ");
        resource.close();

        final ObjectMapper<PersonCustomAllString> mapper = ObjectMapperFactory.create(
                PersonCustomAllString.class, resource.createInputStream());

        assertThat(mapper.read(), allOf(
                hasProperty("name", is("山田太郎")),
                hasProperty("age", is("20"))
        ));

        assertThat(mapper.read(), allOf(
                hasProperty("name", is("山田太郎")),
                hasProperty("age", isEmptyString())
        ));

        assertThat(mapper.read(), allOf(
                hasProperty("name", isEmptyString()),
                hasProperty("age", isEmptyString())
        ));
        
        assertThat("スペースはそのまま読み込まれること", mapper.read(), allOf(
                hasProperty("name", is(" 　 ")),
                hasProperty("age", is(" "))
        ));
        mapper.close();
    }

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"age", "name"})
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

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"name"}, headers = "名前")
    public static class PersonSub extends Person {
    }

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"age", "name"})
    public static class PersonNotProperty {
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

    @Csv(type = Csv.CsvType.RFC4180, properties = {"age", "name"}, headers = {"年齢", "氏名"})
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

    @Csv(type = Csv.CsvType.CUSTOM, properties = {"age", "name"})
    @CsvFormat(fieldSeparator = '\t',
            lineSeparator = "\r\n",
            quote = '\'',
            ignoreEmptyLine = false,
            requiredHeader = false,
            charset = "UTF-8",
            quoteMode = CsvDataBindConfig.QuoteMode.CUSTOM,
            emptyToNull = false)
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
    
    @Csv(type = Csv.CsvType.CUSTOM, properties = {"age", "name"})
    @CsvFormat(fieldSeparator = '\t',
            lineSeparator = "\r\n",
            quote = '\'',
            ignoreEmptyLine = false,
            requiredHeader = false,
            charset = "UTF-8",
            quoteMode = CsvDataBindConfig.QuoteMode.CUSTOM,
            emptyToNull = false)
    public static class PersonCustomAllString {
        private String age;
        private String name;

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
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

    @Csv(type = Csv.CsvType.RFC4180, properties = {"age", "name"}, headers = {"年齢", "氏名"})
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

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"age", "name"}, headers = {"年齢", "氏名"})
    public static class PersonWithHeaderAndLineNumber {
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

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"age", "name"}, headers = {"年齢", "氏名"})
    public static class PersonWithDuplicateLineNumber {
        private Long lineNumber;
        private Long lineNumberDuplicate;
        private Integer age;
        private String name;

        @LineNumber
        public Long getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(Long lineNumber) {
            this.lineNumber = lineNumber;
        }

        @LineNumber
        public Long getLineNumberDuplicate() {
            return lineNumberDuplicate;
        }

        public void setLineNumberDuplicate(Long lineNumberDuplicate) {
            this.lineNumberDuplicate = lineNumberDuplicate;
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