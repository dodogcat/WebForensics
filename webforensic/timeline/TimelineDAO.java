package timeline;

import cache.CacheDAO;
import cookies.CookiesDAO;
import downloads.DownloadsDAO;
import org.json.simple.parser.ParseException;
import urls.*;
import util.Time;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class TimelineDAO{
    private static TimelineDAO instance = new TimelineDAO();
    private TimelineDAO() {}

    public static TimelineDAO getInstance(){
        return instance;
    }

    private ArrayList<TimelineDTO> records = new ArrayList<TimelineDTO>();
    private Connection conn = null;
    Statement stmt;

    public ArrayList<TimelineDTO> searchRecord(int days){
        Time time = Time.getInstance();

        UrlsDAO urlsDAO = UrlsDAO.getInstance();
        CacheDAO cacheDAO = CacheDAO.getInstance();
        CookiesDAO cookieDAO = CookiesDAO.getInstance();
        DownloadsDAO downloadsDAO = DownloadsDAO.getInstance();

        urlsDAO.searchRecord(days);
        try{
            cacheDAO.searchRecord(days);
            downloadsDAO.searchRecord(days);
            cookieDAO.searchRecord(days);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int total_index = 0;
        int sz = urlsDAO.getRecordCnt();
        for(int i=0; i<sz; i++){
            TimelineDTO record = new TimelineDTO();

            record.setTable_type("urls");
            record.setUrl(urlsDAO.getUrl(i));
            record.setAccess_time(urlsDAO.getLast_visit_time(i));

            records.add(record);
        }
        total_index += sz;

        sz = cacheDAO.getRecordCnt();
        for(int i=0; i<sz; i++){
            TimelineDTO record = new TimelineDTO();

            record.setTable_type("cache");
            record.setUrl(cacheDAO.getUrl(i));
            record.setAccess_time(cacheDAO.getCreate_time(i));

            records.add(record);
        }
        total_index += sz;

        sz = downloadsDAO.getRecordCnt();
        for(int i=0; i<sz; i++){
            TimelineDTO record = new TimelineDTO();

            record.setId(Integer.toString(total_index + i + 1));
            record.setTable_type("downloads");
            record.setUrl(downloadsDAO.getUrl(i));
            record.setAccess_time(downloadsDAO.getLast_access_time(i));

            records.add(record);
        }
        total_index += sz;

        sz = cookieDAO.getRecordCnt();
        for(int i=0; i<sz; i++){
            TimelineDTO record = new TimelineDTO();

            record.setTable_type("cookies");
            record.setUrl(cookieDAO.getUrl(i));
            record.setAccess_time(cookieDAO.getLast_access_time(i));

            records.add(record);
        }
        total_index += sz;

        Collections.sort(records, new TimelineDTOCompator());
        for(int i=0; i<total_index; i++){
            records.get(i).setId(Integer.toString(i+1));
        }

        return records;
    }


}