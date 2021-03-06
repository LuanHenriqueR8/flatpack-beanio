package net.sf.flatpack.rfc4180;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.sf.flatpack.structure.ColumnMetaData;
import net.sf.flatpack.util.FPConstants;
import net.sf.flatpack.util.FlatpackWriterUtil;
import net.sf.flatpack.util.ParserUtils;
import net.sf.flatpack.writer.*;

public class CsvWriterFormatDefintionTest extends Rfc4180TestCase {
    private final char FIELD_DELIMITER = ',';
    private final char FIELD_QUALIFIER = '"';
    private final String LINE_SEPARATOR = "\r\n";

    public static void main(String... args) throws IOException {
        final StringWriter out = new StringWriter();

        List<ColumnMetaData> columnMetaDataList = Arrays.asList(
                columnMetaData("nome", 50),
                columnMetaData("idade", 3),
                columnMetaData("email", 50),
                columnMetaData("datanascimento", 10));

        Map<String, Object> mapping = ImmutableMap.of(
                FPConstants.DETAIL_ID, columnMetaDataList,
                FPConstants.COL_IDX, ParserUtils.buidColumnIndexMap(columnMetaDataList, null)
        );

        final FixedWriterFactory factory = new FixedWriterFactory(mapping);
        try (Writer writer = factory.createWriter(out)) {

            writer
                    .addRecordEntry("nome", "Diego Alves Oliveira da Silva")
                    .addRecordEntry("idade", "30")
                    .addRecordEntry("email", "isdiegoalves@gmail.com")
                    .addRecordEntry("datanascimento", LocalDate.of(1987, 12, 12))
                    .nextRecord();

            writer
                    .addRecordEntry("nome", "Camila Estefani Neto do Carmo Lima")
                    .addRecordEntry("idade", "25")
                    .addRecordEntry("email", "camilaestefanilima@gmail.com")
                    .addRecordEntry("datanascimento", LocalDate.of(1993, 10, 3))
                    .nextRecord();

            writer
                    .addRecordEntry("nome", "Isabela Oliveira de Melo")
                    .addRecordEntry("idade", "12")
                    .addRecordEntry("email", "isabelaom2006@gmail.com")
                    .addRecordEntry("datanascimento", LocalDate.of(2006, 10, 1))
                    .nextRecord();

            writer.flush();

        }

        final String result = out.toString();

        System.out.println(result);
    }

    private static ColumnMetaData columnMetaData(String nome, int length) {
        ColumnMetaData column = new ColumnMetaData();
        column.setColName(nome);
        column.setColLength(length);
        return column;
    }

    /*
     * 2.1 Each record is located on a separate line, delimited by a line break (CRLF).  For example:
     *
     * aaa,bbb,ccc CRLF
     * zzz,yyy,xxx CRLF
     */
    public void testLineSeparation() throws IOException {
        final StringWriter out = new StringWriter();

        try (Writer writer = getWriterForRfc4180(out, false)) {
            // write one line of data ... not in the correct order of fields
            writer.addRecordEntry("col1", "aaa").addRecordEntry("col2", "bbb").addRecordEntry("col3", "ccc").nextRecord();
            writer.addRecordEntry("col3", "xxx").addRecordEntry("col2", "yyy").addRecordEntry("col1", "zzz").nextRecord();

            writer.flush();
        }

        final String result = out.toString();
        assertThat(result, is(notNullValue()));
        assertThat(result.length(), is(26));

        final String[] lines = result.split(LINE_SEPARATOR);
        assertThat(lines, is(notNullValue()));
        assertThat(lines.length, is(2));
        assertThat(lines[0], is("aaa,bbb,ccc"));
        assertThat(lines[1], is("zzz,yyy,xxx"));
    }

    /*
     * 2.4a. Each line should contain the same number of fields throughout the file
     */
    public void testSameNumberOffFields() throws IOException {
        final StringWriter out = new StringWriter();

        try (Writer writer = getWriterForRfc4180(out, false)) {
            // write one line of data ... not in the correct order of fields
            writer.addRecordEntry("col1", "aaa").addRecordEntry("col2", "bbb").addRecordEntry("col3", "ccc").nextRecord();
            writer.addRecordEntry("col3", "xxx")
                    // .addRecordEntry("col2", "yyy")
                    .addRecordEntry("col1", "zzz").nextRecord();

            writer.flush();
        }

        final String result = out.toString();
        assertThat(result, is(notNullValue()));
        assertThat(result.length(), is(23));

        final String[] lines = result.split(LINE_SEPARATOR);
        assertThat(lines, is(notNullValue()));
        assertThat(lines.length, is(2));
        assertThat(lines[0], is("aaa,bbb,ccc"));
        assertThat(lines[0].split(String.valueOf(FIELD_DELIMITER)).length, is(3));
        assertThat(lines[1], is("zzz,,xxx"));
        assertThat(lines[1].split(String.valueOf(FIELD_DELIMITER)).length, is(3));
    }

    /*
     * 2.4b. Spaces are considered part of a field and should not be ignored.
     */
    public void testSpacesArePartOfField() throws IOException {
        final StringWriter out = new StringWriter();

        try (Writer writer = getWriterForRfc4180(out, false)) {
            // write one line of data ... not in the correct order of fields
            writer.addRecordEntry("col1", "aaa ").addRecordEntry("col2", " bbb").addRecordEntry("col3", "c cc").nextRecord();
            writer.addRecordEntry("col3", "x x x").addRecordEntry("col2", "yy y").addRecordEntry("col1", " zzz ").nextRecord();

            writer.flush();
        }

        final String result = out.toString();
        assertThat(result, is(notNullValue()));
        assertThat(result.length(), is(34));

        final String[] lines = result.split(LINE_SEPARATOR);
        assertThat(lines, is(notNullValue()));
        assertThat(lines.length, is(2));
        assertThat(lines[0], is("aaa , bbb,c cc"));
        assertThat(lines[1], is(" zzz ,yy y,x x x"));
    }

    /*
     * 2.4c. The last field in the record must not be followed by a comma
     */
    public void testLastFieldShouldNotHaveDelimiter() throws IOException {
        final StringWriter out = new StringWriter();

        try (Writer writer = getWriterForRfc4180(out, true)) {
            // write one line of data ... not in the correct order of fields
            writer.addRecordEntry("col1", "aaa").addRecordEntry("col2", "bbb").addRecordEntry("col3", "ccc").nextRecord();
            writer.addRecordEntry("col3", "xxx").addRecordEntry("col2", "yyy").addRecordEntry("col1", "zzz").nextRecord();

            writer.flush();
        }

        final String result = out.toString();
        assertThat(result, is(notNullValue()));
        assertThat(result.length(), is(26));

        final String[] lines = result.split(LINE_SEPARATOR);
        assertThat(lines, is(notNullValue()));
        assertThat(lines.length, is(2));
        assertThat(lines[0].length(), is(11));
        assertThat(lines[0].charAt(lines[0].length() - 1), is(not(FIELD_DELIMITER)));
        assertThat(lines[1].length(), is(11));
        assertThat(lines[1].charAt(lines[0].length() - 1), is(not(FIELD_DELIMITER)));
    }

    private Writer getWriterForRfc4180(final java.io.Writer out, final boolean autoPrintHeader) throws IOException {
        final WriterOptions options = WriterOptions.getInstance();
        options.autoPrintHeader(autoPrintHeader);
        options.setLineSeparator(LINE_SEPARATOR);

        final DelimiterWriterFactory factory = new DelimiterWriterFactory(FIELD_DELIMITER, FIELD_QUALIFIER)//
                .addColumnTitle("col1").addColumnTitle("col2").addColumnTitle("col3");

        return factory.createWriter(out, options);
    }
}