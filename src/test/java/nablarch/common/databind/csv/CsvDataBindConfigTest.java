package nablarch.common.databind.csv;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.array;
import static org.hamcrest.Matchers.emptyArray;
import static org.junit.Assert.assertThat;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

import nablarch.common.databind.csv.CsvDataBindConfig.QuoteMode;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@link CsvDataBindConfig}のテスト。
 */
public class CsvDataBindConfigTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void withQuote() throws Exception {
        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT
                .withEmptyToNull(false);

        sut = sut.withQuote('a');
        assertThat(sut.getQuote(), is('a'));
        assertThat(sut.getFieldSeparator(), is(CsvDataBindConfig.DEFAULT.getFieldSeparator()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getCharset(), is(CsvDataBindConfig.DEFAULT.getCharset()));
        assertThat(sut.getHeaderTitles(), is(CsvDataBindConfig.DEFAULT.getHeaderTitles()));
        assertThat(sut.getQuotedColumnNames(), is(CsvDataBindConfig.DEFAULT.getQuotedColumnNames()));
        assertThat(sut.getQuoteMode(), is(CsvDataBindConfig.DEFAULT.getQuoteMode()));
        assertThat(sut.isIgnoreEmptyLine(), is(CsvDataBindConfig.DEFAULT.isIgnoreEmptyLine()));
        assertThat(sut.isRequiredHeader(), is(CsvDataBindConfig.DEFAULT.isRequiredHeader()));
        assertThat(sut.isEmptyToNull(), is(false));
    }

    @Test
    public void withFieldSeparator() throws Exception {
        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT;

        sut = sut.withFieldSeparator('b');
        assertThat(sut.getFieldSeparator(), is('b'));

        assertThat(sut.getQuote(), is(CsvDataBindConfig.DEFAULT.getQuote()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getCharset(), is(CsvDataBindConfig.DEFAULT.getCharset()));
        assertThat(sut.getHeaderTitles(), is(CsvDataBindConfig.DEFAULT.getHeaderTitles()));
        assertThat(sut.getQuotedColumnNames(), is(CsvDataBindConfig.DEFAULT.getQuotedColumnNames()));
        assertThat(sut.getQuoteMode(), is(CsvDataBindConfig.DEFAULT.getQuoteMode()));
        assertThat(sut.isIgnoreEmptyLine(), is(CsvDataBindConfig.DEFAULT.isIgnoreEmptyLine()));
        assertThat(sut.isRequiredHeader(), is(CsvDataBindConfig.DEFAULT.isRequiredHeader()));
        assertThat(sut.isEmptyToNull(), is(CsvDataBindConfig.DEFAULT.isEmptyToNull()));
    }

    @Test
    public void withLineSeparator() throws Exception {
        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT.withEmptyToNull(false);

        sut = sut.withLineSeparator("\r");
        assertThat(sut.getLineSeparator(), is("\r"));
        sut = sut.withLineSeparator("\n");
        assertThat(sut.getLineSeparator(), is("\n"));
        sut = sut.withLineSeparator("\r\n");
        assertThat(sut.getLineSeparator(), is("\r\n"));

        assertThat(sut.getQuote(), is(CsvDataBindConfig.DEFAULT.getQuote()));
        assertThat(sut.getFieldSeparator(), is(CsvDataBindConfig.DEFAULT.getFieldSeparator()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getCharset(), is(CsvDataBindConfig.DEFAULT.getCharset()));
        assertThat(sut.getHeaderTitles(), is(CsvDataBindConfig.DEFAULT.getHeaderTitles()));
        assertThat(sut.getQuotedColumnNames(), is(CsvDataBindConfig.DEFAULT.getQuotedColumnNames()));
        assertThat(sut.getQuoteMode(), is(CsvDataBindConfig.DEFAULT.getQuoteMode()));
        assertThat(sut.isIgnoreEmptyLine(), is(CsvDataBindConfig.DEFAULT.isIgnoreEmptyLine()));
        assertThat(sut.isRequiredHeader(), is(CsvDataBindConfig.DEFAULT.isRequiredHeader()));
        assertThat(sut.isEmptyToNull(), is(false));
    }

    @Test
    public void withLineSeparator_invalid() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("invalid line separator. must be set '\\r\\n or \\n or \\r'");

        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT;
        sut.withLineSeparator("\r\n\n");
    }

    @Test
    public void constructor_lineSeparator_invalid() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("invalid line separator. must be set '\\r\\n or \\n or \\r'");

        CsvDataBindConfig sut = new CsvDataBindConfig(
                ',',
                "\r\r\n",
                '"',
                true,
                true,
                new String[0],
                Charset.forName("UTF-8"),
                true,
                QuoteMode.NORMAL,
                Collections.<String>emptyList()
        );
    }

    @Test
    public void withCharset() throws Exception {
        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT;

        sut = sut.withCharset("Windows-31J");
        assertThat(sut.getCharset(), is(Charset.forName("Windows-31J")));
        sut = sut.withCharset(Charset.forName("EUC-JP"));
        assertThat(sut.getCharset(), is(Charset.forName("EUC-JP")));

        assertThat(sut.getFieldSeparator(), is(CsvDataBindConfig.DEFAULT.getFieldSeparator()));
        assertThat(sut.getQuote(), is(CsvDataBindConfig.DEFAULT.getQuote()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getHeaderTitles(), is(CsvDataBindConfig.DEFAULT.getHeaderTitles()));
        assertThat(sut.getQuotedColumnNames(), is(CsvDataBindConfig.DEFAULT.getQuotedColumnNames()));
        assertThat(sut.getQuoteMode(), is(CsvDataBindConfig.DEFAULT.getQuoteMode()));
        assertThat(sut.isIgnoreEmptyLine(), is(CsvDataBindConfig.DEFAULT.isIgnoreEmptyLine()));
        assertThat(sut.isRequiredHeader(), is(CsvDataBindConfig.DEFAULT.isRequiredHeader()));
    }

    @Test
    public void withEmptyToNull() throws Exception {
        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT;

        sut = sut.withEmptyToNull(true);
        assertThat(sut.isEmptyToNull(), is(true));
        sut = sut.withEmptyToNull(false);
        assertThat(sut.isEmptyToNull(), is(false));

        assertThat(sut.getFieldSeparator(), is(CsvDataBindConfig.DEFAULT.getFieldSeparator()));
        assertThat(sut.getQuote(), is(CsvDataBindConfig.DEFAULT.getQuote()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getHeaderTitles(), is(CsvDataBindConfig.DEFAULT.getHeaderTitles()));
        assertThat(sut.getQuotedColumnNames(), is(CsvDataBindConfig.DEFAULT.getQuotedColumnNames()));
        assertThat(sut.getQuoteMode(), is(CsvDataBindConfig.DEFAULT.getQuoteMode()));
        assertThat(sut.isIgnoreEmptyLine(), is(CsvDataBindConfig.DEFAULT.isIgnoreEmptyLine()));
        assertThat(sut.isRequiredHeader(), is(CsvDataBindConfig.DEFAULT.isRequiredHeader()));
    }

    @Test
    public void withHeaderTitles() throws Exception {
        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT;

        sut = sut.withHeaderTitles("hoge", "fuga");
        assertThat(sut.getHeaderTitles(), is(new String[] {"hoge", "fuga"}));

        assertThat(sut.getQuote(), is(CsvDataBindConfig.DEFAULT.getQuote()));
        assertThat(sut.getFieldSeparator(), is(CsvDataBindConfig.DEFAULT.getFieldSeparator()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getCharset(), is(CsvDataBindConfig.DEFAULT.getCharset()));
        assertThat(sut.getQuotedColumnNames(), is(CsvDataBindConfig.DEFAULT.getQuotedColumnNames()));
        assertThat(sut.getQuoteMode(), is(CsvDataBindConfig.DEFAULT.getQuoteMode()));
        assertThat(sut.isIgnoreEmptyLine(), is(CsvDataBindConfig.DEFAULT.isIgnoreEmptyLine()));
        assertThat(sut.isRequiredHeader(), is(CsvDataBindConfig.DEFAULT.isRequiredHeader()));
        assertThat(sut.isEmptyToNull(), is(CsvDataBindConfig.DEFAULT.isEmptyToNull()));
    }

    @Test
    public void withRequiredHeader() throws Exception {
        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT;
        sut = sut.withRequiredHeader();
        assertThat(sut.isRequiredHeader(), is(true));
        sut = sut.withRequiredHeader(false);
        assertThat(sut.isRequiredHeader(), is(false));

        assertThat(sut.getQuote(), is(CsvDataBindConfig.DEFAULT.getQuote()));
        assertThat(sut.getFieldSeparator(), is(CsvDataBindConfig.DEFAULT.getFieldSeparator()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getCharset(), is(CsvDataBindConfig.DEFAULT.getCharset()));
        assertThat(sut.getHeaderTitles(), is(CsvDataBindConfig.DEFAULT.getHeaderTitles()));
        assertThat(sut.getQuotedColumnNames(), is(CsvDataBindConfig.DEFAULT.getQuotedColumnNames()));
        assertThat(sut.getQuoteMode(), is(CsvDataBindConfig.DEFAULT.getQuoteMode()));
        assertThat(sut.isIgnoreEmptyLine(), is(CsvDataBindConfig.DEFAULT.isIgnoreEmptyLine()));
        assertThat(sut.isEmptyToNull(), is(CsvDataBindConfig.DEFAULT.isEmptyToNull()));
    }

    @Test
    public void withQuoteMode() throws Exception {

        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT;

        sut = sut.withQuoteMode(QuoteMode.NOT_NUMERIC);
        assertThat(sut.getQuoteMode(), is(QuoteMode.NOT_NUMERIC));

        assertThat(sut.getQuote(), is(CsvDataBindConfig.DEFAULT.getQuote()));
        assertThat(sut.getFieldSeparator(), is(CsvDataBindConfig.DEFAULT.getFieldSeparator()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getCharset(), is(CsvDataBindConfig.DEFAULT.getCharset()));
        assertThat(sut.getHeaderTitles(), is(CsvDataBindConfig.DEFAULT.getHeaderTitles()));
        assertThat(sut.isRequiredHeader(), is(CsvDataBindConfig.DEFAULT.isRequiredHeader()));
        assertThat(sut.getQuotedColumnNames(), is(CsvDataBindConfig.DEFAULT.getQuotedColumnNames()));
        assertThat(sut.isIgnoreEmptyLine(), is(CsvDataBindConfig.DEFAULT.isIgnoreEmptyLine()));
        assertThat(sut.isEmptyToNull(), is(CsvDataBindConfig.DEFAULT.isEmptyToNull()));
    }

    @Test
    public void withQuotedColumnNames() throws Exception {
        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT;
        sut = sut.withQuotedColumnNames("col1", "col3");
        assertThat(sut.getQuotedColumnNames(), is(Arrays.asList("col1", "col3")));

        assertThat(sut.getQuote(), is(CsvDataBindConfig.DEFAULT.getQuote()));
        assertThat(sut.getFieldSeparator(), is(CsvDataBindConfig.DEFAULT.getFieldSeparator()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getCharset(), is(CsvDataBindConfig.DEFAULT.getCharset()));
        assertThat(sut.getHeaderTitles(), is(CsvDataBindConfig.DEFAULT.getHeaderTitles()));
        assertThat(sut.isRequiredHeader(), is(CsvDataBindConfig.DEFAULT.isRequiredHeader()));
        assertThat(sut.getQuoteMode(), is(CsvDataBindConfig.DEFAULT.getQuoteMode()));
        assertThat(sut.isIgnoreEmptyLine(), is(CsvDataBindConfig.DEFAULT.isIgnoreEmptyLine()));
        assertThat(sut.isEmptyToNull(), is(CsvDataBindConfig.DEFAULT.isEmptyToNull()));
    }

    @Test
    public void withIgnoreEmptyLine() throws Exception {

        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT;

        sut = sut.withIgnoreEmptyLine();
        assertThat(sut.isIgnoreEmptyLine(), is(true));
        sut = sut.withIgnoreEmptyLine(false);
        assertThat(sut.isIgnoreEmptyLine(), is(false));

        assertThat(sut.getQuote(), is(CsvDataBindConfig.DEFAULT.getQuote()));
        assertThat(sut.getFieldSeparator(), is(CsvDataBindConfig.DEFAULT.getFieldSeparator()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getCharset(), is(CsvDataBindConfig.DEFAULT.getCharset()));
        assertThat(sut.getHeaderTitles(), is(CsvDataBindConfig.DEFAULT.getHeaderTitles()));
        assertThat(sut.isRequiredHeader(), is(CsvDataBindConfig.DEFAULT.isRequiredHeader()));
        assertThat(sut.getQuoteMode(), is(CsvDataBindConfig.DEFAULT.getQuoteMode()));
        assertThat(sut.getQuotedColumnNames(), is(CsvDataBindConfig.DEFAULT.getQuotedColumnNames()));
        assertThat(sut.isEmptyToNull(), is(CsvDataBindConfig.DEFAULT.isEmptyToNull()));
    }

    @Test
    public void withProperties() throws Exception {
        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT;
        assertThat(sut.getProperties(), is(emptyArray()));

        sut = sut.withProperties("test1", "test2");
        assertThat(sut.getProperties(), array(is("test1"), is("test2")));

        assertThat(sut.getFieldSeparator(), is(CsvDataBindConfig.DEFAULT.getFieldSeparator()));
        assertThat(sut.getLineSeparator(), is(CsvDataBindConfig.DEFAULT.getLineSeparator()));
        assertThat(sut.getQuote(), is(CsvDataBindConfig.DEFAULT.getQuote()));
        assertThat(sut.isIgnoreEmptyLine(), is(CsvDataBindConfig.DEFAULT.isIgnoreEmptyLine()));
        assertThat(sut.isRequiredHeader(), is(CsvDataBindConfig.DEFAULT.isRequiredHeader()));
        assertThat(sut.getHeaderTitles(), is(CsvDataBindConfig.DEFAULT.getHeaderTitles()));
        assertThat(sut.getCharset(), is(CsvDataBindConfig.DEFAULT.getCharset()));
        assertThat(sut.isEmptyToNull(), is(CsvDataBindConfig.DEFAULT.isEmptyToNull()));
        assertThat(sut.getQuoteMode(), is(CsvDataBindConfig.DEFAULT.getQuoteMode()));
        assertThat(sut.getQuotedColumnNames(), is(CsvDataBindConfig.DEFAULT.getQuotedColumnNames()));
    }

    @Test
    public void getKeys() throws Exception {
        CsvDataBindConfig sut = CsvDataBindConfig.DEFAULT.withHeaderTitles("header1", "header2");
        assertThat(sut.getHeaderTitles(), array(is("header1"), is("header2")));
        assertThat(sut.getProperties(), is(emptyArray()));
        assertThat(sut.getKeys(), array(is("header1"), is("header2")));

        sut = sut.withProperties("prop1", "prop2");
        assertThat(sut.getHeaderTitles(), array(is("header1"), is("header2")));
        assertThat(sut.getProperties(), array(is("prop1"), is("prop2")));
        assertThat(sut.getKeys(), array(is("prop1"), is("prop2")));
    }
}
