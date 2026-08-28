package layer2Server;

import org.example.MacroDef;

import java.sql.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.example.MacroDef.users;

public class PosgressCumuncater {
    BlockingQueue<Connection> conns=new LinkedBlockingDeque<>();
    ConcurrentHashMap<Long, BlockingQueue<Messege>>dataPulls=new ConcurrentHashMap<>();


    public PosgressCumuncater(long cons) throws SQLException {
        if (cons < 1) throw new IllegalArgumentException("Connection count must be at least 1");
        for (long i = 0; i < cons; i++) {
            conns.add(DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/mydb",
                    "postgres",
                    "password"
            ));
        }
    }

    private void returnConnection(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.isClosed()) conns.offer(conn);
        } catch (SQLException ignored) {
        }
    }

    public long[] login(String username, String password) throws SQLException {
        long[] vlues = new long[]{MacroDef.fail, 0};
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength, TimeUnit.MILLISECONDS);
            if(conn==null){
                vlues[0]= MacroDef.error;
                vlues[1]=MacroDef.timeout;
                return vlues;
            }
        PreparedStatement stmt = conn.prepareStatement(MacroDef.Sql.Prep.Hashedlogin());
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            vlues[1] = rs.getLong(MacroDef.Sql.Users.id);
            vlues[0] = MacroDef.ok;
        }
        rs.close();
        stmt.close();
        } catch (InterruptedException e) {
            vlues[0] = MacroDef.error;
        } finally {
            returnConnection(conn);
        }
        return vlues;
    }

    public void getmfd(long id, Timestamp dt) throws SQLException {
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength, TimeUnit.MILLISECONDS);
            if(conn==null)return;

        PreparedStatement statement = conn.prepareStatement(MacroDef.Sql.Prep.getMessenger());
        statement.setLong(1,id);
        statement.setTimestamp(2,dt);


        ResultSet rs = statement.executeQuery();
        BlockingQueue<Messege> bq= getQuue(id);

        while (rs.next()){
           bq.add(new Messege(rs.getString(MacroDef.messges.contents),
                   rs.getTimestamp(MacroDef.messges.time),
                   rs.getString(MacroDef.messges.groupID),
                   rs.getString(MacroDef.groups.name),
                   rs.getString(MacroDef.messges.SenderName)));

        }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            returnConnection(conn);
        }


    }
    public synchronized BlockingQueue<Messege>getQuue(long id){
        if(dataPulls.get(id)==null){
            dataPulls.put(id,new LinkedBlockingDeque<>());
        }
        return dataPulls.get(id);
    }
    public synchronized void removeBQ(long id){
        dataPulls.remove(id);

    }

    public Messege getMessges(long id, Timestamp tm){

        BlockingQueue<Messege> bq=getQuue(id);

        while(true) {
            Messege m = bq.poll();
            if (m == null) return null;
            if (m.tm.after(tm)||m.tm.equals(tm))return m;


        }
    }



    public long insertMessage(Messege m, long id) throws SQLException {
        if (m.tm == null) {
            m.tm = new Timestamp(System.currentTimeMillis());
        }
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength, TimeUnit.MILLISECONDS);
            if(conn==null)return MacroDef.timeout;
        PreparedStatement stmt = conn.prepareStatement(MacroDef.Sql.Prep.insertMessege());
        stmt.setString(1, m.sender);
        stmt.setString(2, m.mc);
        stmt.setTimestamp(3, m.tm);
        stmt.setString(4, m.groupID);
        stmt.setLong(5, id);
        stmt.setString(6, m.groupID);

        int rows = stmt.executeUpdate();
        stmt.close();

        if (rows > 0) {
            PreparedStatement memberStmt = conn.prepareStatement(MacroDef.Sql.Prep.getGroupMembers());
            memberStmt.setString(1, m.groupID);
            ResultSet rs = memberStmt.executeQuery();
            while (rs.next()) {
                long uid = rs.getLong(MacroDef.Sql.GroupMembers.Uid);
                BlockingQueue<Messege> bq = dataPulls.get(uid);
                if (bq != null) {
                    bq.add(m);
                }
            }
            rs.close();
            memberStmt.close();
            return MacroDef.ok;
        }else {
            return MacroDef.permssionsFail;

        }

        }catch (InterruptedException e) {
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }



    }
    public long createGroup(String name, long id) throws SQLException {
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if(conn==null)
                return MacroDef.timeout;
            PreparedStatement stmt =conn.prepareStatement(MacroDef.Sql.Prep. makeGroup());
            stmt.setString(1, name);
            stmt.setLong(2, id);
            int rows = stmt.executeUpdate();
            stmt.close();
            if(rows>0)return MacroDef.ok;
            else return MacroDef.fail;


        } catch (Exception e) {
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }

    }
    public long addUserToGroup(long myid, long gid,long uid,long perm) throws SQLException {
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if (conn == null)return MacroDef.timeout;
            PreparedStatement stmt = conn.prepareCall(MacroDef.Sql.Prep.adduserTG());
            stmt.setLong(1, uid);
            stmt.setLong(2, gid);
            stmt.setLong(3, perm);
            stmt.setLong(4, myid);
            stmt.setLong(5, gid);

            int rows = stmt.executeUpdate();
            stmt.close();
            if(rows>0)return MacroDef.ok;
            else return MacroDef.fail;
        }catch (Exception e){
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }
    }
    public long removeUserFromGroup(long myid, long gid,long uid) throws SQLException {
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if (conn == null)return MacroDef.timeout;
            PreparedStatement stmt = conn.prepareCall(MacroDef.Sql.Prep.kickUser());
            stmt.setLong(1, uid);
            stmt.setLong(2, gid);
            stmt.setLong(3, myid);
            stmt.setLong(4, gid);

            int rows = stmt.executeUpdate();

            stmt.close();
            if(rows>0)return MacroDef.ok;
            else return MacroDef.permssionsFail;

        } catch (Exception e) {
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }

    }
    public long leaveGroup(long myid, long gid) throws SQLException {
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if (conn == null)return MacroDef.timeout;
            PreparedStatement stmt =conn.prepareStatement(MacroDef.Sql.Prep.leave());
            stmt.setLong(1, myid);
            stmt.setLong(2, gid);
            int rows = stmt.executeUpdate();
            if(rows==0)return MacroDef.fail;
            stmt.close();
            stmt =conn.prepareStatement(MacroDef.Sql.Prep.dlEmtyChat());
            stmt.setLong(1, gid);

            stmt.executeUpdate();
            stmt.close();
            return MacroDef.ok;

    }catch (Exception e){
        return MacroDef.error;
    } finally {
        returnConnection(conn);
    }
    }

    public long deleteGroup(long uid,long gid) throws SQLException{
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if (conn == null) return MacroDef.timeout;
            PreparedStatement stmt=conn.prepareStatement(MacroDef.Sql.Prep.dlChat());
            stmt.setLong(1,gid);
            stmt.setLong(2,uid);
            stmt.setLong(3,uid);
            stmt.setLong(4,gid);
            int rows=stmt.executeUpdate();
            stmt.close();
            if(rows>0)return MacroDef.ok;
            else return MacroDef.permssionsFail;

        } catch (Exception e) {
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }
    }
    public long changPermissions(long myuid,long gid,long uid,long perm) throws SQLException{
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if (conn == null) return MacroDef.timeout;
            PreparedStatement stmt = conn.prepareStatement(MacroDef.Sql.Prep.ChPrm());
            stmt.setLong(1, perm);
            stmt.setLong(2, uid);
            stmt.setLong(3, gid);
            stmt.setLong(4, myuid);
            stmt.setLong(5, gid);
            int rows = stmt.executeUpdate();
            stmt.close();
            if(rows>0)return MacroDef.ok;
            else return MacroDef.permssionsFail;
        }catch (Exception e){
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }
    }
    public long nwUser(String username, String password) throws SQLException{
        if(username.length()<MacroDef.minUserNameL||
                password.length()<MacroDef.minPaswordL)
            return MacroDef.fail;
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if(conn==null)return MacroDef.timeout;
            PreparedStatement stmt = conn.prepareCall(MacroDef.Sql.Prep.createUser());
            stmt.setString(1, username);
            stmt.setString(2, password);
            int rows = stmt.executeUpdate();
            stmt.close();
            if(rows>0)return MacroDef.ok;
            else return MacroDef.fail;
        }catch (Exception e){
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }
    }
    public long changPswrd(String name,String oldpsword,String newpasword) throws SQLException{
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if(conn==null)return MacroDef.timeout;
            PreparedStatement stmt = conn.prepareCall(MacroDef.Sql.Prep.chngPswrd());
            stmt.setString(1, newpasword);
            stmt.setString(2, name);
            stmt.setString(3, oldpsword);


            int rows = stmt.executeUpdate();
            stmt.close();
            if(rows>0)return MacroDef.ok;
            else return MacroDef.fail;
        }catch (Exception e){
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }
    }
    public long deletUser(String username,String password){
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if(conn==null)return MacroDef.timeout;
            PreparedStatement stmt= conn.prepareCall(MacroDef.Sql.Prep.dlUser());
            stmt.setString(1,username);
            stmt.setString(2,password);
            int rows=stmt.executeUpdate();
            stmt.close();
            if(rows>0)return MacroDef.ok;
            else return MacroDef.fail;
        }catch (Exception e){
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }
    }
 public long getGroupMembers(long myuid,long gid,List<Long> uid,List<String> names) throws SQLException{
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if(conn==null)return MacroDef.timeout;
            PreparedStatement stmt = conn.prepareStatement(MacroDef.Sql.Prep.getMyGroupMembers());
            stmt.setLong(1,gid);
            stmt.setLong(2,myuid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                uid.add(rs.getLong(users.c(users.id)));
                names.add(rs.getString(users.c(users.name)));
            }
            rs.close();
            stmt.close();
            return MacroDef.ok;
        }catch (Exception e){
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }
 }

public long getUid(String username,long[]feedback) throws SQLException{
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if(conn==null)return MacroDef.timeout;
            PreparedStatement stmt = conn.prepareStatement(MacroDef.Sql.Prep.getMemberId());
            stmt.setString(1,username);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
               feedback[0]=MacroDef.ok;
               long res=rs.getLong(users.c(users.id));
                return res;
            }
            else {
                feedback[0]=MacroDef.fail;
                return 0;
            }
        } catch (Exception e) {
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }
}

public long myGroups(long uid, List<Long>gids,List<String>gn){
        Connection conn = null;
        try {
            conn = conns.poll(MacroDef.timeoutLength,TimeUnit.MILLISECONDS);
            if(conn==null)return MacroDef.timeout;
            PreparedStatement stmt = conn.prepareStatement(MacroDef.Sql.Prep.getMyGroups());
            stmt.setLong(1,uid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                gids.add(rs.getLong(MacroDef.groups.id));
                gn.add(rs.getString(MacroDef.groups.name));
            }
            rs.close();
            stmt.close();
            return MacroDef.ok;

        }catch (Exception e){
            return MacroDef.error;
        } finally {
            returnConnection(conn);
        }
}




    public static class Messege{
        public String mc;
        public Timestamp tm;
        public String groupID;
        public String groupName;
        public String sender;

        public Messege(String mc, Timestamp tm, String groupID, String sender){
            this(mc, tm, groupID, null, sender);
        }

        public Messege(String mc, Timestamp tm, String groupID, String groupName, String sender){
            this.mc = mc;
            this.groupID = groupID;
            this.groupName = groupName;
            this.sender = sender;
            this.tm = tm;
        }

    }




}
