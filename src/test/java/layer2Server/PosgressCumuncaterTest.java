package layer2Server;

import org.example.MacroDef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.Driver;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class PosgressCumuncaterTest {
    PosgressCumuncater pc;
    String user1= "plony";
    String pswrd1 = "password1";
    String user2= "Binyamin";
    String pswrd2 = "password2";
    String user3 = "avraham";
    String pswrd3 = "password3";
    String Np1 = "npassword1";
    Connection conn;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");
        pc=new PosgressCumuncater(1);
        String password = System.getenv("psw");
         conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/simplechatdb",
                "postgres",
                password
        );

    }

    @After
    public void tearDown() throws Exception {
        System.out.println("tearDown");
        conn.close();


    }

    @Test
    public void login() throws SQLException {
        long[] res= pc.login(user1,pswrd1);
        assertEquals(res[0],MacroDef.ok);
        assertEquals(res[1],1);
        res= pc.login(user2,pswrd2);
        assertEquals(res[0],MacroDef.ok);
        assertEquals(res[1],2);
        res= pc.login(user3,pswrd3);
        assertEquals(res[0],MacroDef.ok);
        assertEquals(res[1],3);
        res= pc.login("bad",pswrd1);
        assertEquals(res[0],MacroDef.fail);
        res= pc.login(user1,"bad");
        assertEquals(res[0],MacroDef.fail);
        res= pc.login("bad","bad");
        assertEquals(res[0],MacroDef.fail);
        res= pc.login(user1,pswrd3);



    }

    @Test
    public void getmfd() {
    }

    @Test
    public void getQuue() {
    }

    @Test
    public void removeBQ() {
    }

    @Test
    public void getMessges() throws SQLException {
        long[] get = pc.login(user2, pswrd2);
        pc.getmfd(get[1], Timestamp.valueOf(LocalDateTime.now().minus(1, java.time.temporal.ChronoUnit.DAYS)));
        PosgressCumuncater.Messege m = pc.getMessges(get[1], Timestamp.valueOf(LocalDateTime.now().minus(1, java.time.temporal.ChronoUnit.DAYS)));
        System.out.println(m.groupName);
        System.out.println(m.mc);
        System.out.println(m.sender);
    }

    @Test
    public void insertMessage() throws SQLException {
        PosgressCumuncater.Messege m =new PosgressCumuncater.Messege("m1",  Timestamp.valueOf(LocalDateTime.now()),"1",user1);
        long res =pc.insertMessage(m,1);
        assertEquals(res,MacroDef.ok);

    }

    @Test
    public void createGroup() throws SQLException {
        long[] id = pc.login(user1,pswrd1);

        long res = pc.createGroup("groups1",id[1]);
        assertEquals(res,MacroDef.ok);

        String sql = "SELECT * FROM groups";
        java.sql.Statement stmt = conn.createStatement();
        java.sql.ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.print(rs.getString(MacroDef.Sql.Groups.name ));
            System.out.println(rs.getString(MacroDef.Sql.Groups.id));
        }
        String sql2 = "SELECT * FROM groupmembers";
        java.sql.Statement stmt2 = conn.createStatement();
        java.sql.ResultSet rs2 = stmt2.executeQuery(sql2);
        while (rs2.next()) {
            System.out.print(rs2.getString(MacroDef.Sql.GroupMembers.Gid)+"\t");
            System.out.println(rs2.getString(MacroDef.Sql.GroupMembers.Uid));
        }

    }

    @Test
    public void addUserToGroup() throws SQLException {
        long[] id1 = pc.login(user1,pswrd1);
        long[] id2 = pc.login(user2,pswrd2);
        long[] id3 = pc.login(user3,pswrd3);
        long res = pc.addUserToGroup(id1[1],1,id2[1],0);
        assertEquals(res,MacroDef.ok);
        res = pc.addUserToGroup(id2[1],1,id3[1],2);
        assertEquals(res,1);

        res = pc.changPermissions(id1[1],1,id2[1],MacroDef.admin );
        assertEquals(res,MacroDef.ok);
        res = pc.addUserToGroup(id2[1],1,id3[1],MacroDef.admin );
        assertEquals(res,MacroDef.ok);
        String sql = "SELECT * FROM groupmembers";
        java.sql.Statement stmt = conn.createStatement();
        java.sql.ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.print(rs.getString(MacroDef.Sql.GroupMembers.Gid) + "\t");
            System.out.println(rs.getString(MacroDef.Sql.GroupMembers.Uid));
        }

    }

    @Test
    public void removeUserFromGroup() {
    }

    @Test
    public void leaveGroup() {
    }

    @Test
    public void deleteGroup() {
    }

    @Test
    public void changPermissions() {
    }

    @Test
    public void nwUser() throws SQLException {
        System.out.println("nwUser");

        System.out.println("Make passwords");
        long res = pc.nwUser(user1,pswrd1);
        assertEquals(res, MacroDef.ok);
        res = pc.nwUser(user2,pswrd2);
        assertEquals(res, MacroDef.ok);
        res = pc.nwUser(user3,pswrd3);
        assertEquals(res, MacroDef.ok);
        System.out.println("Passwords made");


        System.out.println("sql query");
        String sql = "SELECT * FROM users";
        java.sql.Statement stmt = conn.createStatement();
        java.sql.ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getString(MacroDef.Sql.Users.name));
            System.out.println(rs.getString(MacroDef.Sql.Users.hashPassword));
            System.out.println(rs.getString(MacroDef.Sql.Users.id));
        }
        System.out.println("sql query done");



    }

    @Test
    public void changPswrd() throws SQLException {
        System.out.println("changPswrd");
        long res = pc.changPswrd(user1,pswrd1,Np1);
        assertEquals(res,MacroDef.ok);
        res = pc.changPswrd(user2,pswrd2+"bad",Np1);
        assertEquals(res,MacroDef.fail);

        long[] id = pc.login(user1,Np1);
        assertEquals(id[0],MacroDef.ok);
        assertEquals(id[1],1);
        id = pc.login(user1,pswrd1);
        assertEquals(id[0],MacroDef.fail);


    }

    @Test
    public void deletUser() {
    }

    @Test
    public void getGroupMembers() {
    }

    @Test
    public void getUid() {
    }

    @Test
    public void myGroups() {
    }
}