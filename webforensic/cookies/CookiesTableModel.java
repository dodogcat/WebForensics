package cookies;

import gui.TableSelectionDemo;
import urls.UrlsDAO;
import urls.UrlsDTO;

import javax.swing.table.AbstractTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

public class CookiesTableModel extends AbstractTableModel {
    private static CookiesTableModel instance = new CookiesTableModel();

    private CookiesTableModel() {
    }

    public static CookiesTableModel getInstance() {
        return instance;
    }

    public static String[] getColumnNames() {
        return columnNames;
    }

    public static void setColumnNames(String[] columnNames) {
        CookiesTableModel.columnNames = columnNames;
    }

    private static String[] columnNames = {"creation_utc", " top_frame_site_key", " host_key", " name", " value", " encrypted_value", " path", " expires_utc", " is_secure", " is_httponly", " last_access_utc", " has_expires", " is_persistent", " priority", " samesite", " source_scheme", " source_port", " is_same_party"};
    private ArrayList<CookiesDTO> records;


    public String getColumnName(int column) {
        return columnNames[column];
    }

    public void setRecords(ArrayList<CookiesDTO> records) {
        this.records = records;
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        int column_n=0;
        for(int i=0; i < columnNames.length; i++){
            if(TableSelectionDemo.getInstance().filter_On.get(2)[i] == true){
                column_n++;
            }
        }

        return column_n;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int filter = 0;
        int max = columnIndex;
        for(int i=0; i < max + 1; i++){
            if(TableSelectionDemo.getInstance().filter_On.get(2)[i] == false){
                filter++;
                max++;
            }
        }

        Object result = null;
        CookiesDTO to = records.get(rowIndex);
        switch (columnIndex + filter) {
            case 0:
                result = to.getCreation_utc();
                break;
            case 1:
                result = to.getTop_frame_site_key();
                break;
            case 2:
                result = to.getHost_key();
                break;
            case 3:
                result = to.getName();
                break;
            case 4:
                result = to.getValue();
                break;
            case 5:
                result = to.getEncrypted_value();
                break;
            case 6:
                result = to.getPath();
                break;
            case 7:
                result = to.getExpires_utc();
                break;
            case 8:
                result = to.getIs_secure();
                break;
            case 9:
                result = to.getIs_httponly();
                break;
            case 10:
                result = to.getLast_access_utc();
                break;
            case 11:
                result = to.getHas_expires();
                break;
            case 12:
                result = to.getIs_persistent();
                break;
            case 13:
                result = to.getPriority();
                break;
            case 14:
                result = to.getSamesite();
                break;
            case 15:
                result = to.getSource_scheme();
                break;
            case 16:
                result = to.getSource_port();
                break;
            case 17:
                result = to.getIs_same_party();
                break;
        }
        return result;
    }

    public String[] detail(String url, String date) {
        int i;
        for (i = 0; i < records.size(); i++) {
            if (url.equals(records.get(i).getUrl()) && date.equals(records.get(i).getLast_access_utc())) {
                break;
            }
        }
        int column = columnNames.length;
        String[] ret = new String[column];
        for (int j = 0; j < column; j++) {
            String result = null;
            CookiesDTO to = records.get(i);
            switch (j) {
                case 0:
                    result = to.getCreation_utc();
                    break;
                case 1:
                    result = to.getTop_frame_site_key();
                    break;
                case 2:
                    result = to.getHost_key();
                    break;
                case 3:
                    result = to.getName();
                    break;
                case 4:
                    result = to.getValue();
                    break;
                case 5:
                    result = to.getEncrypted_value();
                    break;
                case 6:
                    result = to.getPath();
                    break;
                case 7:
                    result = to.getExpires_utc();
                    break;
                case 8:
                    result = String.valueOf(to.getIs_secure());
                    break;
                case 9:
                    result = String.valueOf(to.getIs_httponly());
                    break;
                case 10:
                    result = to.getLast_access_utc();
                    break;
                case 11:
                    result = String.valueOf(to.getHas_expires());
                    break;
                case 12:
                    result = String.valueOf(to.getIs_persistent());
                    break;
                case 13:
                    result = String.valueOf(to.getPriority());
                    break;
                case 14:
                    result = String.valueOf(to.getSamesite());
                    break;
                case 15:
                    result = String.valueOf(to.getSource_scheme());
                    break;
                case 16:
                    result = String.valueOf(to.getSource_port());
                    break;
                case 17:
                    result = String.valueOf(to.getIs_same_party());
                    break;
            }

            ret[j] = result;
        }

        return ret;
    }
}


