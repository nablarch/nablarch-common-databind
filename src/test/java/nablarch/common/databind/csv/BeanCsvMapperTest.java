package nablarch.common.databind.csv;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import nablarch.common.databind.ObjectMapper;
import nablarch.common.databind.ObjectMapperFactory;
import nablarch.common.databind.csv.Csv.CsvType;
import nablarch.common.databind.csv.CsvDataBindConfig.QuoteMode;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

/**
 * {@link BeanCsvMapper}のテスト。
 */
public class BeanCsvMapperTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * 1レコード書き込めること。
     */
    @Test
    public void testWriteSingleRecord() throws Exception {
        StringWriter writer = new StringWriter();
        final ObjectMapper<Person> mapper = ObjectMapperFactory.create(Person.class, writer);
        mapper.write(new Person("csv", "nablarch", "20100101", 10));
        mapper.close();

        assertThat("CSVが書き込まれていること", readFile(new StringReader(writer.toString())),
                is("csv,nablarch,10,20100101\r\n"));
    }

    /**
     * 複数レコードが書き込めること。
     */
    @Test
    public void testWriteMultiRecord() throws Exception {
        final File file = folder.newFile();

        final ObjectMapper<Person> mapper = ObjectMapperFactory.create(Person.class, new FileOutputStream(file));
        mapper.write(new Person("あいうえお", "かきくけこ", "20100101", 9));
        mapper.write(new Person(null, null, "19800101", 5));
        mapper.close();

        assertThat("CSVが書き込まれていること", readFile(file, "utf-8"),
                is("あいうえお,かきくけこ,9,20100101\r\n"
                        + ",,5,19800101\r\n"));
    }

    /**
     * Getterで値を編集している場合その値が書き込まれること
     */
    @Test
    public void testGetterTest() throws Exception {
        StringWriter writer = new StringWriter();
        final ObjectMapper<CustomGetterPerson> mapper = ObjectMapperFactory.create(CustomGetterPerson.class, writer);
        mapper.write(new CustomGetterPerson("first", "last", "20150101", 1));
        mapper.close();

        assertThat("Getterで編集した値が出力されていること", readFile(new StringReader(writer.toString())),
                is("last first,20150101\r\n"));
    }

    /**
     * ヘッダー行が必須のオブジェクトの場合、ヘッダー行が出力されること。
     */
    @Test
    public void testRequiredHeader() throws Exception {
        StringWriter writer = new StringWriter();

        final ObjectMapper<HeaderPerson> mapper = ObjectMapperFactory.create(HeaderPerson.class, new BufferedWriter(writer));
        mapper.write(new HeaderPerson("たろう", "なぶらーく", "20100101", 5));
        mapper.write(new HeaderPerson("CSV", "まっぱー", "20150605", 1));
        mapper.close();

        assertThat("Beanのヘッダー情報が出力されること", readFile(new StringReader(writer.toString())),
                is("性,名,家族人数,誕生日\r\n"
                        + "なぶらーく,たろう,5,20100101\r\n"
                        + "まっぱー,CSV,1,20150605\r\n"));
    }

    /**
     * TSVファイルが書き込めること
     */
    @Test
    public void testTsvFile() throws Exception {
        StringWriter writer = new StringWriter();

        final ObjectMapper<TsvPerson> mapper = ObjectMapperFactory.create(TsvPerson.class, writer);
        mapper.write(new TsvPerson("たろう", "なぶらーく", "20100101", 5));
        mapper.write(new TsvPerson("CSV", "まっぱー", "20150605", 1));
        mapper.close();

        assertThat("Beanのヘッダー情報が出力されること", readFile(new StringReader(writer.toString())),
                is("なぶらーく\tたろう\t5\t20100101\r\n"
                        + "まっぱー\tCSV\t1\t20150605\r\n"));
    }

    /**
     * ヘッダー有りのTSVファイルの出力ができること。
     */
    @Test
    public void testTsvFileWithHeader() throws Exception {
        StringWriter writer = new StringWriter();

        final ObjectMapper<TsvWithHeaderPerson> mapper = ObjectMapperFactory.create(TsvWithHeaderPerson.class, writer);
        mapper.write(new TsvWithHeaderPerson("たろう", "なぶらーく", "20100101", 5));
        mapper.write(new TsvWithHeaderPerson("CSV", "まっぱー", "20150605", 1));
        mapper.close();

        assertThat("Beanのヘッダー情報が出力されること", readFile(new StringReader(writer.toString())),
                is("'性'\t'名'\t'家族の人数'\t'誕生日'\r\n"
                        + "'なぶらーく'\t'たろう'\t5\t'20100101'\r\n"
                        + "'まっぱー'\t'CSV'\t1\t'20150605'\r\n"));
    }

    /**
     * 読み取り専用のMapperに対して書き込みを行った場合エラーとなること。
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testReadOnlyMapper() throws Exception {
        final ObjectMapper<Person> mapper = ObjectMapperFactory.create(Person.class, "12345,12345");
        mapper.write(new Person("1", "2", "3", 0));
    }

    /**
     * ファイル書き込み時にIOExceptionが発生するケース。
     */
    @Test
    public void testWriteError(@Mocked final BufferedWriter mockWriter) throws Exception {
        final IOException exception = new IOException("io error!");
        new Expectations() {{
            mockWriter.write(anyInt);
            result = exception;
        }};
        final ObjectMapper<Person> mapper = ObjectMapperFactory.create(Person.class, new BufferedWriter(mockWriter));
        try {
            mapper.write(new Person("1", "2", "3", 0));
            fail("ここはとおらない");
        } catch (Exception e) {
            assertThat((IOException) e.getCause(), is(sameInstance(exception)));
        }
    }

    /**
     * ヘッダー部の書き込み時にIOExceptionが発生するケース
     */
    @Test
    public void testWriteHeaderError(@Mocked final BufferedWriter mockWriter) throws Exception {
        final IOException exception = new IOException("header write error!!!  ");
        new Expectations() {{
            mockWriter.write(anyString);
            result = exception;
        }};

        try {
            ObjectMapperFactory.create(HeaderPerson.class, mockWriter);
        } catch (Exception e) {
            assertThat((IOException) e.getCause(), is(exception));
        }

    }

    /**
     * 書き込み用オブジェクトなのでリードは失敗すること。
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testRead(@Injectable final Writer mockWriter) throws Exception {
        final ObjectMapper<Person> mapper = ObjectMapperFactory.create(Person.class, mockWriter);
        mapper.read();
    }

    private String readFile(File file, String charset) throws Exception {
        return readFile(new BufferedReader(new InputStreamReader(new FileInputStream(file), charset)));
    }

    /**
     * テストで出力されたファイルを読み込む。
     *
     * @param reader リソース
     * @return 読み込んだ結果
     */
    private String readFile(Reader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        int read;
        while ((read = reader.read()) != -1) {
            sb.append((char) read);
        }
        reader.close();
        return sb.toString();
    }

    @Csv(
            type = CsvType.RFC4180,
            properties = {"firstName", "lastName", "familySize", "birthday"}
    )
    public static class Person {

        private String firstName;

        private String lastName;

        private String birthday;

        private int familySize;

        public Person() {
        }

        public Person(String firstName, String lastName, String birthday, int familySize) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthday = birthday;
            this.familySize = familySize;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getBirthday() {
            return birthday;
        }

        public int getFamilySize() {
            return familySize;
        }
    }

    @Csv(
            type = CsvType.EXCEL,
            properties = {"name", "birthday"}
    )
    public static class CustomGetterPerson extends Person {

        public CustomGetterPerson() {
            super();
        }

        public CustomGetterPerson(String firstName, String lastName, String birthday, int familySize) {
            super(firstName, lastName, birthday, familySize);
        }

        public String getName() {
            return getLastName() + ' ' + getFirstName();
        }
    }

    @Csv(
            type = CsvType.DEFAULT,
            properties = {"lastName", "firstName", "familySize", "birthday"},
            headers = {"性", "名", "家族人数", "誕生日"}
    )
    public static class HeaderPerson extends Person {

        public HeaderPerson() {
            super();
        }

        public HeaderPerson(String firstName, String lastName, String birthday, int familySize) {
            super(firstName, lastName, birthday, familySize);
        }
    }

    @Csv(
            type = CsvType.TSV,
            properties = {"lastName", "firstName", "familySize", "birthday"}
    )
    public static class TsvPerson extends Person {

        public TsvPerson() {
            super();
        }

        public TsvPerson(String firstName, String lastName, String birthday, int familySize) {
            super(firstName, lastName, birthday, familySize);
        }
    }

    @Csv(
            type = CsvType.CUSTOM,
            properties = {"lastName", "firstName", "familySize", "birthday"},
            headers = {"性", "名", "家族の人数", "誕生日"}
    )
    @CsvFormat(
            fieldSeparator = '\t',
            charset = "Windows-31j",
            ignoreEmptyLine = false,
            lineSeparator = "\r\n",
            quote = '\'',
            quoteMode = QuoteMode.NOT_NUMERIC,
            requiredHeader = true,
            emptyToNull = false
    )
    public static class TsvWithHeaderPerson extends Person {

        public TsvWithHeaderPerson() {
            super();
        }

        public TsvWithHeaderPerson(String firstName, String lastName, String birthday, int familySize) {
            super(firstName, lastName, birthday, familySize);
        }
    }
}
