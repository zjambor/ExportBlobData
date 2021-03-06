package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;

public class Main {

    private static int ind = 0;
    private static String userHome = System.getProperty("user.home");

    public static void main(String[] args) throws IOException {

        long start = System.currentTimeMillis();
        var connectionstring = "";

        var path = Paths.get(userHome + "/eles_conf.txt");
        try {
            connectionstring = Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        path = Paths.get(userHome + "/photos.txt");
        var content = "";
        try {
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(connectionstring);

            Worker(con, "20190926000000", "20191101000000");
            Worker(con, "20191101000000", "20191201000000");

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Computation lasted " + (System.currentTimeMillis() - start) / 1000 / 60 + " minutes.");
    }

    /*
    ID	NUMBER(10,0)	Yes
    URL	NVARCHAR2(100 CHAR)	Yes
    FOTO	BLOB	Yes
    CREATE_USER_ID	NUMBER(10,0)	Yes
    CREATE_USER_DATE	NUMBER(14,0)	Yes
     */
    private static void Worker(Connection con, String d1, String d2) throws SQLException, IOException {
        var id = 0;
        var CREATE_USER_ID = 0;
        long CREATE_USER_DATE = 0;
        var url = "";

        Path path = Paths.get(userHome + "/photos.txt");
        var content = "";

        PreparedStatement ps = con.prepareStatement("select id, url, foto, CREATE_USER_ID, CREATE_USER_DATE from lksz.fotok where CREATE_USER_DATE>='" + d1 + "' and CREATE_USER_DATE<'" + d2 + "' order by id");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            id = rs.getInt(1);
            url = rs.getString(2);
            CREATE_USER_ID = rs.getInt(4);
            CREATE_USER_DATE = rs.getLong(5);

            try {
                Blob b = rs.getBlob(3);        //2 means 2nd column data
                if (b != null) {
                    byte barr[] = b.getBytes(1, (int) b.length());    //1 means first image

                    FileOutputStream fout = new FileOutputStream("/export/exp/" + url);
                    fout.write(barr);

                    fout.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            System.out.println(ind + ": " + "/export/exp/" + url);
            ind++;
            content = id + "|" + url + "|" + CREATE_USER_ID + "|" + CREATE_USER_DATE + "\n";
            try {
                Files.write(path, content.getBytes(), StandardOpenOption.APPEND);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        rs.close();
        ps.close();
    }
}
