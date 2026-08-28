package layer2Server;

import org.example.MacroDef;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Serverlogic {
    PosgressCumuncater pc;
    TokenHolder th = new TokenHolder();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "cleanup-thread");
        t.setDaemon(true);
        return t;
    });

    public Serverlogic(long tcount) throws SQLException {
        pc=new PosgressCumuncater(tcount);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Set<Long> inactiveUsers = th.clean();
                for (Long uid : inactiveUsers) {
                    pc.removeBQ(uid);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.DAYS);
    }

    public long[] login(String username, String password,String[]token) throws SQLException {
        long[] result = pc.login(username, password);
        String[] response = new String[2];

        if (result[0] == org.example.MacroDef.ok) {
             token[0] = th.creatToken(result[1]);
            return result;
        }

        return result;
    }
    public long[] isTokenValid(String token){
        return th.login(token);
    }

    public long[] nwUser(String username, String password,String[]token) throws SQLException {
       long[]res= pc.login(username,password);
       // user already exists
       if(res[0]==MacroDef.ok)return new long[]{MacroDef.fail,0};
       long results=pc.nwUser(username,password);
       if(results==MacroDef.fail)return new long[]{MacroDef.fail,0};
       res[0]=MacroDef.ok;

       long[]lres =login(username,password,token);
       long id =isTokenValid(token[0])[1];
       res[1]=id;
       return res;
    }
    public PosgressCumuncater.Messege Getmessge(String token, Timestamp dt, long[] feedback) throws SQLException {
        long[] tknr = isTokenValid(token);
        if (tknr[0] == MacroDef.fail) {
            feedback[0] = MacroDef.fail;
            feedback[1] = MacroDef.invalidtoken;
            return null;
        }
        PosgressCumuncater.Messege mes = pc.getMessges(tknr[1], dt);
        if(mes==null){
            pc.getmfd(tknr[1],dt);
            mes = pc.getMessges(tknr[1], dt);
        }
        feedback[0] = MacroDef.ok;
        return mes;
    }
    public long[] insertMessage(String token, PosgressCumuncater.Messege messege) throws SQLException {
        long[] tkr=isTokenValid(token);
        if(tkr[0]==MacroDef.fail){
            return new long[]{MacroDef.fail,MacroDef.invalidtoken};
        }
        long feedback = pc.insertMessage(messege,tkr[1]);
        if(feedback==MacroDef.timeout)return new long[]{MacroDef.fail,MacroDef.timeout};
        if(feedback==MacroDef.permssionsFail)return new long[]{MacroDef.fail,MacroDef.permssionsFail};
        return new long[]{MacroDef.ok,feedback};
    }
    public long[] createGroup(String token, String name) throws SQLException {
        long[] tkr=isTokenValid(token);
        if(tkr[0]==MacroDef.fail){
            return new long[]{MacroDef.fail,MacroDef.invalidtoken};
        }
       long res= pc.createGroup(name,tkr[1]);
        return new long[]{res,0};
    }
public long[] addUserToGroup(String token, long groupid, long Uid,long perm) throws SQLException {
        long[] tkr=isTokenValid(token);
    if(tkr[0]==MacroDef.fail){
        return new long[]{MacroDef.fail,MacroDef.invalidtoken};
    }
    long res =pc.addUserToGroup(tkr[1],groupid,Uid,perm);
    return new long[]{res,0};
}
public long[] removeUserFromGroup(String token, long groupid, long Uid) throws SQLException {
        long[] tkr=isTokenValid(token);
        if(tkr[0]==MacroDef.fail){
        return new long[]{MacroDef.fail,MacroDef.invalidtoken};
        }
        long res =pc.removeUserFromGroup(tkr[1],groupid,Uid);
        return new long[]{res,0};

}
public long[] leaveGroup(String token, long groupid) throws SQLException {
        long[] tkr=isTokenValid(token);
        if(tkr[0]==MacroDef.fail){
        return new long[]{MacroDef.fail,MacroDef.invalidtoken};
        }
        long res =pc.leaveGroup(tkr[1],groupid);
        return new long[]{res,0};
}
public long[] deleteGroup(String token, long groupid) throws SQLException {
        long[] tkr=isTokenValid(token);
        if(tkr[0]==MacroDef.fail){
        return new long[]{MacroDef.fail,MacroDef.invalidtoken};
        }
        long res =pc.deleteGroup(tkr[1],groupid);
        return new long[]{res,0};
}
public long[] changePerms(String token,long groupid,long Uid,long perm) throws SQLException {
        long[] tkr=isTokenValid(token);
        if(tkr[0]==MacroDef.fail){
        return new long[]{MacroDef.fail,MacroDef.invalidtoken};
        }
        long res =pc.changPermissions(tkr[1],groupid,Uid,perm);
        return new long[]{res,0};


}
public long[]ChangPassword(String token,String oldPasword,String username,String newpasword) throws SQLException {
        long[] tkr=isTokenValid(token);
        if(tkr[0]==org.example.MacroDef.fail){
            return new long[]{MacroDef.fail,MacroDef.invalidtoken};
        }
    long[]res= pc.login(username,newpasword);
        if(res[0]==MacroDef.ok)return new long[]{MacroDef.fail,0};
        long feedback=pc.changPswrd(username,oldPasword,newpasword);
        return new long[]{feedback,0};
    }
public long[]deleteUser(String token, String username,String password) throws SQLException{
        long[] tkr=isTokenValid(token);
        long[]res= pc.login(username,password);
        if(tkr[0]==MacroDef.fail){
            return new long[]{MacroDef.fail,MacroDef.invalidtoken};
        }
        if(res[0]==MacroDef.ok)return new long[]{MacroDef.fail,0};
        if(res[1]!=tkr[1])return new long[]{MacroDef.fail,0};
        res[0]=pc.deletUser(username,password);
        res[1]=0;
        return res;

    }

public long[] getGroups(String token, List<String>gns,List<Long>gids ) throws SQLException{
        long[] tkr=isTokenValid(token);
        if(tkr[0]==MacroDef.fail){
            return new long[]{MacroDef.fail,MacroDef.invalidtoken};
        }
        long res =pc.myGroups(tkr[1],gids,gns );
        return new long[]{res,0};
}
public long[] getMyGusers(String token,Long gid,List<Long>usrids,List<String>usernames) throws SQLException{
        long[] tkr = isTokenValid(token);
        if(tkr[0]==MacroDef.fail){
            return new long[]{MacroDef.fail,MacroDef.invalidtoken};
        }
        long res = pc.getGroupMembers(tkr[1],gid,usrids,usernames);
        return new long[]{res,0};
}
public  long[]getUID(String username) throws SQLException {
        long[]feedback=new long[1];
        long res = pc.getUid(username,feedback);
        return new long[]{feedback[0],res};
}




}
