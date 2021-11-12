package timeline;

import base.*;
import cache.CacheDAO;
import urls.*;
import util.Time;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

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

        urlsDAO.searchRecord(days);
        try{
            cacheDAO.searchRecord(days);
        }catch (IOException e) {
            e.printStackTrace();
        }

        int total_index = 0;
        int sz = urlsDAO.getRecordCnt();
        for(int i=0; i<sz; i++){
            TimelineDTO record = new TimelineDTO();

            record.setId(Integer.toString(total_index + i + 1));
            record.setTable_type("urls");
            record.setUrl(urlsDAO.getUrl(i));
            record.setAccess_time(urlsDAO.getLast_visit_time(i));

            records.add(record);
        }
        total_index += sz;

        sz = cacheDAO.getRecordCnt();
        for(int i=0; i<sz; i++){
            TimelineDTO record = new TimelineDTO();

            record.setId(Integer.toString(total_index + i + 1));
            record.setTable_type("cache");
            record.setUrl(cacheDAO.getUrl(i));
            record.setAccess_time(cacheDAO.getCreate_time(i));

            records.add(record);
        }
        total_index += sz;

        return records;
    }


}