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

    public static void main(String[] args) throws IOException {

        long start = System.currentTimeMillis();
        var connectionstring = "";

        var path = Paths.get("d:\\blob_conf.txt");
        try {
            connectionstring = Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        path = Paths.get("d:\\photos.txt");
        var content = "";
        Files.write(path, content.getBytes(), StandardOpenOption.CREATE);

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(connectionstring);

            Worker(con, "20190201", "20190201");
            Worker(con, "20190301", "20190301");
            Worker(con, "20190401", "20190401");
            Worker(con, "20190501", "20190501");

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Computation lasted " + (System.currentTimeMillis() - start) + " milliseconds.");
    }

    private static void Worker(Connection con, String d1, String d2) throws SQLException, IOException {
        var id = 0;
        var createdate = "";

        Path path = Paths.get("d:\\photos.txt");
        var content = "";

        PreparedStatement ps = con.prepareStatement("select id, blob_data, createdate from tab2 where createdate>='" + d1 + "' and createdate<='" + d2 + "' order by id");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            id = rs.getInt(1);
            createdate = rs.getString(3);

            Blob b = rs.getBlob(2);        //2 means 2nd column data
            byte barr[] = b.getBytes(1, (int) b.length());    //1 means first image

            FileOutputStream fout = new FileOutputStream("d:\\photos\\" + id);
            fout.write(barr);

            fout.close();
            System.out.println(ind + ": d:\\photos\\" + id);
            ind++;
            content = id + "|" + createdate + "\n";
            try {
                Files.write(path, content.getBytes(), StandardOpenOption.APPEND);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
