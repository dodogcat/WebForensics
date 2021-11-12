package cookies;

import com.sun.jna.platform.win32.Crypt32Util;
import netscape.javascript.JSObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import urls.UrlsDTO;
import util.CopyFile;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class CookiesDAO {
    private ArrayList<CookiesDTO> records = new ArrayList<CookiesDTO>();
    private Connection conn = null;
    Statement stmt;

    public ArrayList<CookiesDTO> searchRecord(int period) throws ClassNotFoundException, SQLException{
        File file = new File(System.getenv("USERPROFILE")+"\\AppData\\Local\\google\\chrome\\user data\\default\\cookies");
        File Nfile = new File(System.getenv("USERPROFILE")+"\\AppData\\Local\\google\\chrome\\user data\\default\\new_cookies");

        try {
            Files.copy(file.toPath(), Nfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //db 연결 정보
        String url = "jdbc:sqlite:" + System.getenv("USERPROFILE") + "\\AppData\\Local\\google\\chrome\\user data\\default\\new_cookies";

        //db 연결 정보
        CopyFile copy = CopyFile.getInstance();
        copy.makeNewFile("cookies");

        //String url = "jdbc:sqlite:" + System.getenv("USERPROFILE") + "\\AppData\\Local\\google\\chrome\\user data\\default\\cookies";
//        String url = "jdbc:sqlite:" + System.getenv("USERPROFILE") + "\\files\\cookies";


        //db 드라이버 로딩
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch(ClassNotFoundException e)  {
            System.out.println("org.sqlite.JDBC를 찾지못했습니다.");
        }

        try{
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
//            String sql = "SELECT creation_utc, top_frame_site_key, host_key, name, value, encrypted_value, path, expires_utc, is_secure, is_httponly, last_access_utc, has_expires, is_persistent, priority, samesite, source_scheme, source_port, is_same_party FROM cookies WHERE name = 'SSID'";
            String sql = "SELECT creation_utc, top_frame_site_key, host_key, name, value, encrypted_value, path, expires_utc, is_secure, is_httponly, last_access_utc, has_expires, is_persistent, priority, samesite, source_scheme, source_port, is_same_party FROM cookies where last_access_utc >= " + subDays(period);

            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()){
                CookiesDTO record = new CookiesDTO();
                record.setCreation_utc(datetoDefault(chromeToUNIX(rs.getString(1))));
                record.setTop_frame_site_key(rs.getString(2));
                record.setHost_key(rs.getString(3));
                record.setName(rs.getString(4));
                record.setValue(rs.getString(5));
                record.setEncrypted_value(decrypted(rs.getBytes(6)));
                record.setPath(rs.getString(7));
                record.setExpires_utc(datetoDefault(chromeToUNIX(rs.getString(8))));
                record.setIs_secure(rs.getInt(9));
                record.setIs_httponly(rs.getInt(10));
                record.setLast_access_utc(datetoDefault(chromeToUNIX(rs.getString(11))));
                record.setHas_expires(rs.getInt(12));
                record.setIs_persistent(rs.getInt(13));
                record.setPriority(rs.getInt(14));
                record.setSamesite(rs.getInt(15));
                record.setSource_scheme(rs.getInt(16));
                record.setSource_port(rs.getInt(17));
                record.setIs_same_party(rs.getInt(18));

                records.add(record);
            }

            if(rs != null) rs.close();
            if(stmt != null) stmt.close();
            if(conn != null) conn.close();

        } catch(SQLException e){
            System.out.println("Connection failed");
        }

        return records;
    }

    public String decrypted (byte[] encryptedValue){
        byte[] nonce = Arrays.copyOfRange(encryptedValue, 3, 3 + 12);
        byte[] ciphertextTag = Arrays.copyOfRange(encryptedValue, 3 + 12,
                encryptedValue.length);
        byte[] decryptedBytes = null;

        byte[] windowsMasterKey;
        String pathLocalState = System.getProperty("user.home") + "/AppData/Local/Google/Chrome/User Data/Local State";
        File localStateFile = new File(pathLocalState);

        // json 데이터 추출
        JSONParser parser = new JSONParser();
        Reader reader = null;
        try {
             reader = new FileReader(pathLocalState);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JSONObject j = null;
        try {
            JSONObject json = (JSONObject) parser.parse(reader);
            j = ((JSONObject) json.get("os_crypt"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String encryptedMasterKeyWithPrefixB64 = (String) j.get("encrypted_key");
//        String encryptedMasterKeyWithPrefixB64 = "RFBBUEkBAAAA0Iyd3wEV0RGMegDAT8KX6wEAAADdTm77nf/BQ6M99yhpHK6EAAAAAAIAAAAAABBmAAAAAQAAIAAAANBiWQNIdgiE47WM23XkYA+yzkF+nNzdGbna4PwzB3v+AAAAAA6AAAAAAgAAIAAAADc3gu+f13rRKqMMLVZVYYqHRTxgQKx7xFIXlk98U8U7MAAAAK7Nie13x3S5MY51s9TMI/WJOjh5nza5+O828qQCHsJkbUpQy/hOiA8b4ewqLE65xkAAAAAZd54YhiE1laYIL0/QSI8dDAsoVvmgbpnKyKYQdxm2ptlNpQoar5N82oQKgXIBIsv9mE70+XgT3qDDSozQ8Jvx";

        // Remove prefix (DPAPI)
        byte[] encryptedMasterKeyWithPrefix = Base64.getDecoder().decode(encryptedMasterKeyWithPrefixB64);
        byte[] encryptedMasterKey = Arrays.copyOfRange(encryptedMasterKeyWithPrefix, 5, encryptedMasterKeyWithPrefix.length);

        // Decrypt and store the master key for use later
        windowsMasterKey = Crypt32Util.cryptUnprotectData(encryptedMasterKey);

        // Decrypt
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(windowsMasterKey, "AES");

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            decryptedBytes = cipher.doFinal(ciphertextTag);
        }
        catch (Exception e) {
            throw new IllegalStateException("Error decrypting", e);
        }

        return new String(decryptedBytes);
    }

    //현재 시간 - day 한 크롬 날짜를 반환합니다
    public static String subDays(int day) throws SQLException {
        long before = 11644473600L;
        long now = Long.valueOf(nowDateString());
        now = (now+before)*1000000L;
        long sub = day*24*60*60*1000000L;
        long sum = now-(sub);

        return String.valueOf(sum);
    }

//    public static void main(String[] args ) throws SQLException {
//        System.out.println(datetoDefault(String.valueOf(chromeToUNIX(13315125879249347L))));
//    }

    //현재 시간을 유닉스 시간으로 반환힙니다
    public static String nowDateString() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch(ClassNotFoundException e)  {
            System.out.println("org.sqlite.JDBC를 찾지못했습니다.");
        }
        Connection con = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmte = con.createStatement();
        ResultSet rs = stmte.executeQuery("SELECT strftime('%s','now', 'localtime');");
        rs.next();
        String ret = rs.getString(1);

        rs.close();
        stmte.close();
        con.close();

        return ret;
    }

    //날짜를 국룰 시간으로 반환합니다
    public static String datetoDefault(String date) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch(ClassNotFoundException e)  {
            System.out.println("org.sqlite.JDBC를 찾지못했습니다.");
        }
        Connection con = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmte = con.createStatement();
        ResultSet rs = stmte.executeQuery("SELECT datetime("+ date +", 'unixepoch')");
        rs.next();
        String ret = rs.getString(1);

        rs.close();
        stmte.close();
        con.close();

        return ret;
    }

    //크롬시간을 유닉스 시간으로 바꿔줍니다
    public static String chromeToUNIX(String chrome) {
        long c = Long.valueOf(chrome);
        String ret = String.valueOf(c/1000000L - 11644473600L);
        return ret;
    }
}
