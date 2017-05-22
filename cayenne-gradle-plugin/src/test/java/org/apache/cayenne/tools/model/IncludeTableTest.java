package org.apache.cayenne.tools.model;

import java.util.Collection;

import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeColumn;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeColumn;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @since 4.0
 */
public class IncludeTableTest {

    @Test
    public void includeColumn() throws Exception {
        IncludeTable table = new IncludeTable("name");
        table.includeColumn("column1");
        table.includeColumn("column2");
        table.includeColumn("column2");

        Collection<IncludeColumn> columns = table.toIncludeTable().getIncludeColumns();
        assertNotNull(columns);
        assertEquals(3, columns.size());
        assertEquals("column1", columns.iterator().next().getPattern());
    }

    @Test
    public void includeColumns() throws Exception {
        IncludeTable table = new IncludeTable("name");
        table.includeColumns("column1", "column2", "column2");

        Collection<IncludeColumn> columns = table.toIncludeTable().getIncludeColumns();
        assertNotNull(columns);
        assertEquals(3, columns.size());
        assertEquals("column1", columns.iterator().next().getPattern());
    }

    @Test
    public void excludeColumn() throws Exception {
        IncludeTable table = new IncludeTable("name");
        table.excludeColumn("column1");
        table.excludeColumn("column2");
        table.excludeColumn("column2");

        Collection<ExcludeColumn> columns = table.toIncludeTable().getExcludeColumns();
        assertNotNull(columns);
        assertEquals(3, columns.size());
        assertEquals("column1", columns.iterator().next().getPattern());
    }

    @Test
    public void excludeColumns() throws Exception {
        IncludeTable table = new IncludeTable("name");
        table.excludeColumns("column1", "column2", "column2");

        Collection<ExcludeColumn> columns = table.toIncludeTable().getExcludeColumns();
        assertNotNull(columns);
        assertEquals(3, columns.size());
        assertEquals("column1", columns.iterator().next().getPattern());
    }

    @Test
    public void toIncludeTable() throws Exception {
        IncludeTable table = new IncludeTable("name");
        table.includeColumns("column1", "column2");
        table.excludeColumns("column3", "column4", "column5");

        org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable rrTable = table.toIncludeTable();
        assertEquals("name", rrTable.getPattern());
        assertEquals(2, rrTable.getIncludeColumns().size());
        assertEquals(3, rrTable.getExcludeColumns().size());
    }

}
