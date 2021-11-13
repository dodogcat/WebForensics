package cache;

import util.CopyFile;
import util.Time;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CacheDAO {
    private static CacheDAO instance = new CacheDAO();
    private CacheDAO(){}

    public static CacheDAO getInstance(){
        return instance;
    }

    private ArrayList<CacheDTO> records = new ArrayList<CacheDTO>();

    public ArrayList<CacheDTO> searchRecord(int days) throws IOException {
        try {
            String username = System.getProperty("user.name");

            CopyFile copy = CopyFile.getInstance();
            copy.makeCache("data_0");
            copy.makeCache("data_1");

            RandomAccessFile data_0 = new RandomAccessFile("C:\\Users\\" + username + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Cache\\new_data_0", "r");
            RandomAccessFile data_1 = new RandomAccessFile("C:\\Users\\" + username + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Cache\\new_data_1", "r");

            byte[] block_header = new byte[0x2000];
            byte[] index_block = new byte[0x24];

            int entry_count = 0;
            data_0.read(block_header);
            entry_count = convertHexToDec(block_header, 0x13, 0x10);

            int[] entry_addr = new int[entry_count];
            for (int i = 0; i < entry_count; i++) {
                data_0.read(index_block);
                entry_addr[i] = getEntryOffset(index_block);
            }

            int i = 0;
            while (entry_addr[i] == -1) i++; // skip until not -1

            Time time = Time.getInstance();
            String time_str = time.subDays(days);
            System.out.println("time_str: " + time_str);

            // parse each entry's data
            for (; i < entry_count; i++) {
                if (entry_addr[i] < 0) continue;

                CacheDTO cache = new CacheDTO();
                byte entry_header[] = new byte[0x60];

                data_1.seek(entry_addr[i]);
                data_1.read(entry_header);

                // get create time
                String time_test = getTimeString(entry_header, time_str);
                System.out.println("time_test: " + time_test);
                if (time_test.equals("-1")) continue;
                cache.setCreate_time(time_test);

                // ready to parse url from key_data
                byte test[] = new byte[1]; // check whether the key data exists.
                data_1.read(test);
                if (test[0] == 0) {
                    // case of no key_data -> check metadata then can get url
                    continue;
                }

                // - parse detail url from key_data (detail url is located in third part)
                int key_len = convertHexToDec(entry_header, 0x23, 0x20);
                byte key_data[] = new byte[key_len - 1];
                data_1.read(key_data);
                String key_str = new String(key_data, "UTF-8");
                int j = 0;
                for (int flag = 0; flag < 2 && j < key_str.length(); j++) {
                    if (key_str.charAt(j) == ' ') flag += 1;
                }
                char url[] = new char[key_str.length() - j];
                key_str.getChars(j, key_str.length(), url, 0);
                cache.setUrl(String.valueOf(url));

                // cache data size
                int file_size = convertHexToDec(entry_header, 0x2F, 0x2C);
                cache.setData_size(String.valueOf(file_size));

                records.add(cache);
            }

            data_0.close();
            data_1.close();
        } catch(IOException | SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    // convert hexdecimal address(length) to decimal address(length) (little endian ver.)
    private static int convertHexToDec(byte[] header, int start, int end) {
        int num = 0;
        for (int i = start; i >= end; i--) {
            num = num << 8;
            num += Byte.toUnsignedInt(header[i]);
        }
        return num;
    }

    // convert Chrome Timestamp to readable time string
    private static String getTimeString(byte[] header, String days) throws SQLException {
        long webKitTimestamp = 0;
        for(int i = 31; i >= 24; i--){
            webKitTimestamp = webKitTimestamp << 8;
            webKitTimestamp += Byte.toUnsignedInt(header[i]);
        }

        if(String.valueOf(webKitTimestamp).compareTo(days) < 0){
            return "-1";
        }

        long epochStart = LocalDateTime
                .from(LocalDateTime.of(1601, Month.JANUARY, 1, 0, 0))
                .until(LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0), ChronoUnit.MICROS);
        long delta = webKitTimestamp - epochStart;
        LocalDateTime localdatetime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(TimeUnit.MICROSECONDS.toMillis(delta)), ZoneId.systemDefault());
        return localdatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // convert cache address to offset in file(integer)
    private static int getEntryOffset(byte[] index){
        // 0x19(25), 0x18(24) index --> offset
        // 0x20(16) index --> file number
        // offset * block size + 0x2000(8192) --> offset

        int address = 8192;
        int fileNum = index[26];
        int offset = Byte.toUnsignedInt(index[25]);
        offset = offset << 8;
        offset += Byte.toUnsignedInt(index[24]);

        if(fileNum == 1) address += offset * 256; // 0x100
        else if(fileNum == 2) address += offset * 1024; // 0x400
        else if(fileNum == 3) address += offset * 4096; // 0x1000
        else address = -1; // none..

        return address;
    }

    public String getUrl(int idx){
        return records.get(idx).getUrl();
    }

    public String getCreate_time(int idx){
        return records.get(idx).getCreate_time();
    }

    public int getRecordCnt(){
        return records.size();
    }

}
