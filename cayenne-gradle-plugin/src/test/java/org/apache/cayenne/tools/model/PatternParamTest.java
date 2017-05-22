package org.apache.cayenne.tools.model;

import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeColumn;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeColumn;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeProcedure;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @since 4.0
 */
public class PatternParamTest {

    private PatternParam param;

    @Before
    public void createNewPatternParam() {
        param = new PatternParam("test");
    }

    @Test
    public void toExcludeTable() throws Exception {
        ExcludeTable table = param.toExcludeTable();
        assertNotNull(table);
        assertEquals("test", table.getPattern());
    }

    @Test
    public void toIncludeColumn() throws Exception {
        IncludeColumn table = param.toIncludeColumn();
        assertNotNull(table);
        assertEquals("test", table.getPattern());
    }

    @Test
    public void toExcludeColumn() throws Exception {
        ExcludeColumn table = param.toExcludeColumn();
        assertNotNull(table);
        assertEquals("test", table.getPattern());
    }

    @Test
    public void toIncludeProcedure() throws Exception {
        IncludeProcedure table = param.toIncludeProcedure();
        assertNotNull(table);
        assertEquals("test", table.getPattern());
    }

    @Test
    public void toExcludeProcedure() throws Exception {
        ExcludeProcedure table = param.toExcludeProcedure();
        assertNotNull(table);
        assertEquals("test", table.getPattern());
    }

}
