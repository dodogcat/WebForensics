package gui;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Vector;

public class TableSorter extends TableMap implements TableModelListener {
    private static TableSorter instance = new TableSorter();

    private TableSorter() {
    }

    public static TableSorter getInstance() {
        return instance;
    }

    int indexes[] = new int[0];
    Vector sortingColumns = new Vector();
    boolean ascending = true;
    public String current_table;

    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
        sortByColumn(0);
        fireTableDataChanged();
        current_table = model.getClass().getName();
    }


    public int compareRowsByColumn(int row1, int row2, int column) {
        Class type = model.getColumnClass(column);
        TableModel data = model;

        // Check for nulls

        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);

        // If both values are null return 0
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) { // Define null less than everything.
            return -1;
        } else if (o2 == null) {
            return 1;
        }

        if (type.getSuperclass() == Number.class) {
            Number n1 = (Number) data.getValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number) data.getValueAt(row2, column);
            double d2 = n2.doubleValue();

            if (d1 < d2)
                return -1;
            else if (d1 > d2)
                return 1;
            else
                return 0;
        } else if (type == String.class) {
            String s1 = (String) data.getValueAt(row1, column);
            String s2 = (String) data.getValueAt(row2, column);

            int sz1 = s1.length();
            int sz2 = s2.length();
            if (sz1 != sz2) {
                if (sz1 < sz2) return -1;
                else return 1;
            }

            int result = s1.compareTo(s2);

            if (result < 0)
                return -1;
            else if (result > 0)
                return 1;
            else
                return 0;
        } else if (type == java.util.Date.class) {
            Date d1 = (Date) data.getValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date) data.getValueAt(row2, column);
            long n2 = d2.getTime();

            if (n1 < n2)
                return -1;
            else if (n1 > n2)
                return 1;
            else
                return 0;
        } else if (type == Boolean.class) {
            Boolean bool1 = (Boolean) data.getValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) data.getValueAt(row2, column);
            boolean b2 = bool2.booleanValue();

            if (b1 == b2)
                return 0;
            else if (b1) // Define false < true
                return 1;
            else
                return -1;
        } else {
            Object v1 = data.getValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString();

            int sz1 = s1.length();
            int sz2 = s2.length();
            if (sz1 != sz2) {
                if (sz1 < sz2) return -1;
                else return 1;
            }

            int result = s1.compareTo(s2);

            if (result < 0)
                return -1;
            else if (result > 0)
                return 1;
            else
                return 0;
        }
    }

    public int compare(int row1, int row2) {
        for (int level = 0, n = sortingColumns.size(); level < n; level++) {
            Integer column = (Integer) sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0) {
                return (ascending ? result : -result);
            }
        }
        return 0;
    }

    public void reallocateIndexes() {
        int rowCount = model.getRowCount();
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    public void tableChanged(TableModelEvent tableModelEvent) {
        super.tableChanged(tableModelEvent);
        reallocateIndexes();
        sortByColumn(0);
        fireTableStructureChanged();
    }

    public void checkModel() {
        if (indexes.length != model.getRowCount()) {
            System.err.println("Sorter not informed of a change in model.");
        }
    }

    public void sort() {
        checkModel();
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
        fireTableDataChanged();
    }

    public void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    }

    private void swap(int first, int second) {
        int temp = indexes[first];
        indexes[first] = indexes[second];
        indexes[second] = temp;
    }

    public Object getValueAt(int row, int column) {
        checkModel();
        return model.getValueAt(indexes[row], column);
    }

    public void setValueAt(Object aValue, int row, int column) {
        checkModel();
        model.setValueAt(aValue, indexes[row], column);
    }

    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    public void sortByColumn(int column, boolean ascending) {
        this.ascending = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort();
        super.tableChanged(new TableModelEvent(this));
    }
}


class TableMap extends AbstractTableModel implements TableModelListener {

    TableModel model;

    public TableModel getModel() {
        return model;
    }

    public void setModel(TableModel model) {
        if (this.model != null) {
            this.model.removeTableModelListener(this);
        }
        this.model = model;
        if (this.model != null) {
            this.model.addTableModelListener(this);
        }
    }


    public Class getColumnClass(int column) {
        return model.getColumnClass(column);
    }

    public int getColumnCount() {
        return ((model == null) ? 0 : model.getColumnCount());
    }

    public String getColumnName(int column) {
        int filter = 0;
        int max = column;
        for (int i = 0; i < max + 1; i++) {
            String current = TableSorter.getInstance().current_table;
//                System.out.printf(current);
            if (current.equals("urls.UrlsTableModel")) {
                if (TableSelectionDemo.getInstance().filter_On.get(0)[i] == false) {
                    filter++;
                    max++;
                }
            }
            if (current.equals("downloads.DownloadsTableModel")) {
                if (TableSelectionDemo.getInstance().filter_On.get(1)[i] == false) {
                    filter++;
                    max++;
                }
            }
            if (current.equals("cookies.CookiesTableModel")) {
                if (TableSelectionDemo.getInstance().filter_On.get(2)[i] == false) {
                    filter++;
                    max++;
                }
            }
            if (current.equals("cache.CacheTableModel")) {
                if (TableSelectionDemo.getInstance().filter_On.get(3)[i] == false) {
                    filter++;
                    max++;
                }
            }
        }
        return model.getColumnName(column + filter);
    }

    public int getRowCount() {
        return ((model == null) ? 0 : model.getRowCount());
    }

    public Object getValueAt(int row, int column) {
        return model.getValueAt(row, column);
    }

    public void setValueAt(Object value, int row, int column) {
        model.setValueAt(value, row, column);
    }

    public boolean isCellEditable(int row, int column) {
        return model.isCellEditable(row, column);
    }

    public void tableChanged(TableModelEvent tableModelEvent) {
        fireTableChanged(tableModelEvent);
    }
}