package net.sf.flatpack;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.util.Arrays;

import junit.framework.TestCase;
import net.sf.flatpack.util.FPConstants;

/**
 * Test methods in the DataSet
 *
 * @author Paul Zepernick
 */
public class DataSetFunctionalityTest extends TestCase {

    public void testContains() {
        DataSet ds;
        final String cols = "column1,column2,column3\r\n value1,value2,value3";
        final Parser p = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', FPConstants.NO_QUALIFIER);
        ds = p.parse();
        ds.next();
        assertEquals("column should NOT be found...", false, ds.contains("shouldnotcontain"));

        assertEquals("column should be found...", true, ds.contains("column1"));
    }

    public void testContainsForStream() {
        final String cols = "column1,column2,column3\r\n value1  ,value2,value3";
        final Parser p = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', FPConstants.NO_QUALIFIER);
        final StreamingDataSet ds = p.parseAsStream();
        assertTrue(ds.next());
        assertEquals("column should NOT be found...", false, ds.getRecord().get().contains("shouldnotcontain"));
        assertEquals("column should be found...", true, ds.getRecord().get().contains("column1"));
        assertEquals("column should be found...", " value1", ds.getRecord().get().getString("column1"));
    }

    public void testDoNotPreserveSpace() {
        final String cols = "column1,column2,column3\r\n value1  , value2,value3   ";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', FPConstants.NO_QUALIFIER);
        p1.setPreserveLeadingWhitespace(false);
        final StreamingDataSet ds = p1.parseAsStream();
        ds.next();
        assertEquals("do not preserve leading space value1...", "value1", ds.getRecord().get().getString("column1"));
        assertEquals("do not preserve leading space value2...", "value2", ds.getRecord().get().getString("column2"));
        assertEquals("do not preserve leading space value3...", "value3", ds.getRecord().get().getString("column3"));
    }

    public void testDoPreserveLeadingSpace() {
        final String cols = "column1,column2,column3\r\n value1  , value2,value3   ";
        final Parser p = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', FPConstants.NO_QUALIFIER);
        final StreamingDataSet ds = p.parseAsStream();
        ds.next();
        assertEquals("value1...", " value1", ds.getRecord().get().getString("column1"));
        assertEquals("value2...", " value2", ds.getRecord().get().getString("column2"));
        assertEquals("value3...", "value3", ds.getRecord().get().getString("column3"));
    }

    public void testDoPreserveTrailingSpace() {
        final String cols = "column1,column2,column3\r\n value1  , value2,value3   ";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', FPConstants.NO_QUALIFIER);
        p1.setPreserveLeadingWhitespace(false);
        p1.setPreserveTrailingWhitespace(true);
        final StreamingDataSet ds = p1.parseAsStream();
        ds.next();
        assertEquals("do preserve trailing space value1...", "value1  ", ds.getRecord().get().getString("column1"));
        assertEquals("do preserve trailing space value2...", "value2", ds.getRecord().get().getString("column2"));
        assertEquals("do preserve trailing space value3...", "value3   ", ds.getRecord().get().getString("column3"));
    }

    public void testDoPreserveBothSpace() {
        final String cols = "column1,column2,column3\r\n value1  , value2,value3   ";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', FPConstants.NO_QUALIFIER);
        p1.setPreserveLeadingWhitespace(true);
        p1.setPreserveTrailingWhitespace(true);
        final StreamingDataSet ds = p1.parseAsStream();
        ds.next();
        assertEquals("do preserve both space value1...", " value1  ", ds.getRecord().get().getString("column1"));
        assertEquals("do preserve both space value2...", " value2", ds.getRecord().get().getString("column2"));
        assertEquals("do preserve both space value3...", "value3   ", ds.getRecord().get().getString("column3"));
    }

    public void testDoubleQuote() {
        final String cols = "column1,column2,column3\r\n\"val1\",\"val2\",\"val3\"";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', '\"');
        final StreamingDataSet ds = p1.parseAsStream();
        ds.next();
        assertEquals("double Quoted val1...", "val1", ds.getRecord().get().getString("column1"));
        assertEquals("double Quoted val2...", "val2", ds.getRecord().get().getString("column2"));
        assertEquals("double Quoted val3...", "val3", ds.getRecord().get().getString("column3"));
    }

    public void testMixedDoubleQuote() {
        final String cols = "column1,column2,column3\r\nval1,\"val2\",\"val3\"";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', '\"');
        final StreamingDataSet ds = p1.parseAsStream();
        ds.next();
        assertEquals("double Quoted val1...", "val1", ds.getRecord().get().getString("column1"));
        assertEquals("double Quoted val2...", "val2", ds.getRecord().get().getString("column2"));
        assertEquals("double Quoted val3...", "val3", ds.getRecord().get().getString("column3"));
    }

    public void testDoubleQuoteWithComma() {
        final String cols = "column1,column2,column3\r\n\"val1,\",\"val,2\",\"va,,l3\"";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', '\"');
        final StreamingDataSet ds = p1.parseAsStream();
        ds.next();
        assertEquals("double Quoted val1...", "val1,", ds.getRecord().get().getString("column1"));
        assertEquals("double Quoted val2...", "val,2", ds.getRecord().get().getString("column2"));
        assertEquals("double Quoted val3...", "va,,l3", ds.getRecord().get().getString("column3"));
    }

    public void testDoubleQuoteWithDoubleQuoteInsideWithStream() {
        final String cols = "column1\r\n\"val\"\"1\"";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', '\"');

        p1.setStoreRawDataToDataSet(true);

        final StreamingDataSet ds = p1.parseAsStream();
        ds.next();
        System.out.println("1/" + ds.getRecord().get().getRawData());
        System.out.println("2/" + Arrays.asList(ds.getRecord().get().getColumns()));
        System.out.println("3/" + ds.getRecord().get().getString("column1"));
        assertEquals("Special | Quoted val\"1...", "val\"1", ds.getRecord().get().getString("column1"));

        // assertTrue("Contains val\"1", p1.stream().allMatch(t -> t.getString("column1").equalsIgnoreCase("val\"1")));
    }

    public void testDoubleQuoteWithDoubleQuoteInsideWithStream2() {
        final String cols = "column1,column2\r\n\"val\"\"1\",val2";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', '\"');
        p1.setStoreRawDataToDataSet(true);

        final StreamingDataSet ds = p1.parseAsStream();
        ds.next();
        System.out.println("4/" + ds.getRecord().get().getRawData());
        System.out.println("5/" + Arrays.asList(ds.getRecord().get().getColumns()));
        System.out.println("6/" + ds.getRecord().get().getString("column1"));
        assertEquals("Special | Quoted val\"1...", "val\"1", ds.getRecord().get().getString("column1"));
    }

    public void testSpecialQualifier() {
        final String cols = "column1,column2\r\n|val1|,val2";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', '|');
        p1.setStoreRawDataToDataSet(true);
        final StreamingDataSet ds = p1.parseAsStream();
        ds.next();
        System.out.println(ds.getRecord().get().getRawData());
        System.out.println(Arrays.asList(ds.getRecord().get().getColumns()));

        assertEquals("Special | Quoted val1...", "val1", ds.getRecord().get().getString("column1"));
        assertEquals("Special | Quoted val2...", "val2", ds.getRecord().get().getString("column2"));
    }

    public void testInvalidLine() {
        final String cols = "column1,column2\r\n|val1,val2";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', '|');
        p1.setStoreRawDataToDataSet(true);
        final StreamingDataSet ds = p1.parseAsStream();
        ds.next();
        assertThat(ds.getRecord()).isEmpty();
        assertThat(ds.getErrorCount()).isEqualTo(1);
        assertThat(ds.getErrors()).extracting("errorDesc").containsExactly("Odd number of Qualifier characters");
    }

    public void testInvalidLineAsStreamOfRecord() {
        final String cols = "column1,column2\r\n|val1,val2";
        final Parser p1 = DefaultParserFactory.getInstance().newDelimitedParser(new StringReader(cols), ',', '|');
        p1.setStoreRawDataToDataSet(true);
        final Stream<Record> ds = p1.stream();
        assertThat(ds.count()).isZero();
    }

    public void testContainsWithStream() {
        final String cols = "column1,column2,column3\r\nvalue1,value2,value3\r\nvalue1a,value2a,value3a";
        final Parser p = DefaultParserFactory.newCsvParser(new StringReader(cols));
        final List<Record> collect = p.stream().collect(Collectors.toList());
        assertEquals("Size", 2, collect.size());
        assertEquals("column should NOT be found...", false, collect.get(0).contains("shouldnotcontain"));
        assertEquals("column should be found...", true, collect.get(0).contains("column1"));
        assertEquals("column should NOT be found...", false, collect.get(0).contains("shouldnotcontain"));
        assertEquals("column should be found...", true, collect.get(0).contains("column1"));
        assertEquals("column should be found val1...", "value1", collect.get(0).getString("column1"));
        assertEquals("column should be found 1a...", "value1a", collect.get(1).getString("column1"));
        // p.stream().forEach(
        // t -> {
        // assertEquals("column should NOT be found...", false,
        // t.contains("shouldnotcontain"));
        // assertEquals("column should be found...", true,
        // t.contains("column1"));
        // });
    }
}
