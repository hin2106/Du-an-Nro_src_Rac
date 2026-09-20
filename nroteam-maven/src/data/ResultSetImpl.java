package data;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class ResultSetImpl implements AlyraResultSet {

    private Map<String, Object>[] data;
    private Object[][] values;
    private int indexData = -1;

    @SuppressWarnings("unchecked") // bắt buộc vì tạo mảng generic
    public ResultSetImpl(final ResultSet rs) throws Exception {
        try {
            rs.last();
            int nRow = rs.getRow();
            rs.beforeFirst();

            ResultSetMetaData rsmd = rs.getMetaData();
            int nColumn = rsmd.getColumnCount();

            this.data = (Map<String, Object>[]) new HashMap[nRow];
            for (int i = 0; i < nRow; i++) {
                this.data[i] = new HashMap<>();
            }

            this.values = new Object[nRow][nColumn];

            int index = 0;
            while (rs.next()) {
                for (int j = 1; j <= nColumn; j++) {
                    String tableName = rsmd.getTableName(j);
                    String columnName = rsmd.getColumnName(j);
                    Object columnValue = rs.getObject(j);

                    this.data[index].put(columnName.toLowerCase(), columnValue);
                    if (!tableName.isEmpty()) {
                        this.data[index].put((tableName + "." + columnName).toLowerCase(), columnValue);
                    }
                    this.values[index][j - 1] = columnValue;
                }
                index++;
            }
        } finally {
            if (rs != null) {
                try {
                    if (rs.getStatement() != null) rs.getStatement().close();
                    rs.close();
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void dispose() {
        if (this.data != null) {
            for (Map<String, Object> map : this.data) {
                if (map != null) map.clear();
            }
            this.data = null;
        }
        this.values = null;
        this.indexData = -1;
    }

    @Override
    public boolean next() throws Exception {
        if (data == null) throw new Exception("No data available");
        indexData++;
        return indexData < data.length;
    }

    @Override
    public boolean first() throws Exception {
        if (data == null) throw new Exception("No data available");
        indexData = 0;
        return data.length > 0;
    }

    @Override
    public boolean gotoResult(int index) throws Exception {
        if (data == null) throw new Exception("No data available");
        if (index < 0 || index >= data.length) throw new Exception("Index out of bound");
        indexData = index;
        return true;
    }

    @Override
    public boolean gotoFirst() throws Exception {
        if (data == null || data.length == 0) throw new Exception("No data available");
        indexData = 0;
        return true;
    }

    @Override
    public void gotoBeforeFirst() {
        indexData = -1;
    }

    @Override
    public boolean gotoLast() throws Exception {
        if (data == null) throw new Exception("No data available");
        indexData = data.length - 1;
        return true;
    }

    @Override
    public int getRows() throws Exception {
        if (data == null) throw new Exception("No data available");
        return data.length;
    }

    // ========== Getter chung ==========
    private Object getValByIndex(int column) throws Exception {
        if (values == null) throw new Exception("No data available");
        if (indexData < 0) throw new Exception("Results need to be prepared in advance");
        return values[indexData][column - 1];
    }

    private Object getValByName(String column) throws Exception {
        if (data == null) throw new Exception("No data available");
        if (indexData < 0) throw new Exception("Results need to be prepared in advance");
        return data[indexData].get(column.toLowerCase());
    }

    // ========== GETTERS ==========
    @Override public byte getByte(int column) throws Exception {
        Object val = getValByIndex(column);
        return val instanceof Number ? ((Number) val).byteValue() : 0;
    }

    @Override public byte getByte(String column) throws Exception {
        Object val = getValByName(column);
        return val instanceof Number ? ((Number) val).byteValue() : 0;
    }

    @Override public int getInt(int column) throws Exception {
        Object val = getValByIndex(column);
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    @Override public int getInt(String column) throws Exception {
        Object val = getValByName(column);
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    @Override public float getFloat(int column) throws Exception {
        Object val = getValByIndex(column);
        return val instanceof Number ? ((Number) val).floatValue() : 0f;
    }

    @Override public float getFloat(String column) throws Exception {
        Object val = getValByName(column);
        return val instanceof Number ? ((Number) val).floatValue() : 0f;
    }

    @Override public double getDouble(int column) throws Exception {
        Object val = getValByIndex(column);
        return val instanceof Number ? ((Number) val).doubleValue() : 0d;
    }

    @Override public double getDouble(String column) throws Exception {
        Object val = getValByName(column);
        return val instanceof Number ? ((Number) val).doubleValue() : 0d;
    }

    @Override public long getLong(int column) throws Exception {
        Object val = getValByIndex(column);
        return val instanceof Number ? ((Number) val).longValue() : 0L;
    }

    @Override public long getLong(String column) throws Exception {
        Object val = getValByName(column);
        return val instanceof Number ? ((Number) val).longValue() : 0L;
    }

    @Override public short getShort(int column) throws Exception {
        Object val = getValByIndex(column);
        return val instanceof Number ? ((Number) val).shortValue() : 0;
    }

    @Override public short getShort(String column) throws Exception {
        Object val = getValByName(column);
        return val instanceof Number ? ((Number) val).shortValue() : 0;
    }

    @Override public boolean getBoolean(int column) throws Exception {
        Object val = getValByIndex(column);
        if (val instanceof Boolean b) return b;
        if (val instanceof Number n) return n.intValue() == 1;
        return false;
    }

    @Override public boolean getBoolean(String column) throws Exception {
        Object val = getValByName(column);
        if (val instanceof Boolean b) return b;
        if (val instanceof Number n) return n.intValue() == 1;
        return false;
    }

    @Override public String getString(int column) throws Exception {
        Object val = getValByIndex(column);
        return val != null ? val.toString() : null;
    }

    @Override public String getString(String column) throws Exception {
        Object val = getValByName(column);
        return val != null ? val.toString() : null;
    }

    @Override public Object getObject(int column) throws Exception {
        return getValByIndex(column);
    }

    @Override public Object getObject(String column) throws Exception {
        return getValByName(column);
    }

    @Override public Timestamp getTimestamp(int column) throws Exception {
        Object val = getValByIndex(column);
        return val instanceof Timestamp ? (Timestamp) val : null;
    }

    @Override public Timestamp getTimestamp(String column) throws Exception {
        Object val = getValByName(column);
        return val instanceof Timestamp ? (Timestamp) val : null;
    }
}
