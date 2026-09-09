package org.example;
public class MacroDef {

     public static final long ok=0;
     public static final long fail = 1;
     public static final long error = -1;
     public static final long timeout = 2;
     public static final long invalidtoken = 3;
     public static final long permssionsFail = 4;
     public static final long timeoutLength = 500;
    public static final int minPaswordL = 8;
    public static final int minUserNameL = 4;
     public static final int post = 1;
    public static final int admin = 2;

     public static Sql.Users users = new MacroDef.Sql.Users();
    public static Sql.GroupMembers gm =new MacroDef.Sql.GroupMembers();
    public static Sql.Messges messges = new Sql.Messges();
    public static Sql.Groups groups = new Sql.Groups();
    public static class http{
        public static final String url="http://localhost:8080";
        public static final String login="/login";
        public static final String register="/register";
        public static final String sendMessege="/sendMessege";
        public static final String chngpswrd="/ChangePassword";
        public static final String getMesseges="/MyMesseges";
        public static final String addUserToGroup="/addUserToGroup";
        public static final String removeUserFromGroup="/removeUserFromGroup";
        public static final String leaveGroup="/leaveGroup";
        public static final String createGroup="/createGroup";
        public static final String chngPermissions="/changePermissions";
        public static final String deleteGroup="/deleteGroup";
        //public static final String deleteMessege="/deleteMessege";
        public static final String getGroupMembers="/getGroupMembers";
        public static final String deleteUser="/deleteUser";
        public static final String getMyGroups="/getMyGroups";
        public static final String getUsers="/getUsers";
        public class Res{
            public static final String ok="ok";
            public static final String fail="fail";
            public static final String error="error";
            public static final String timeout="timeout";
            public static final String invalidtoken="invalidtoken";
            public static final String permssionsFail="permssionsFail";
        }
        public static class Reqfield{
            public static final String username="username";
            public static final String password="password";
            public static final String token="token";
            public static final String newPassword="newPassword";
            public static final String oldPassword="oldPassword";
            public static final String groupName="groupName";
            public static final String messege="messege";
            public static final String gid="gid";
            public static final String uid="uid";
            public static final String perm="perm";
            public static final String id="id";
            public static final String myid="myid";
            public static final String timestamp="timestamp";


        }

    }

     public static class Sql{

         public static class Table{
             public final String Name;
             public Table(String name) {
                 this.Name = name;
             }
             public String c(String e){
                 return Name + "."+e;
             }
         }
         public static class Messges extends Table{
             public Messges() {
                 super("Messages");
             }
             public String SenderName = "Uname";
             public String contents = "contents";
             public String time = "time";
             public String groupID = "groupid";
         }
         public static class Users extends Table {
             public Users() {
                 super("users");
             }
             public static String id = "id";

             public static String name = "name";

             public static String hashPassword = "HashPassword";
         }
         public static class GroupMembers extends Table{
             public GroupMembers() {
                 super("groupMembers");
             }
             public static String Uid = "Uid";
             public static String perm = "perm";
             public static String Gid = "Gid";

         }
         public static class Groups extends Table{
             public Groups() {
                 super("Groups");
             }
             public static String id = "id";
             public static String name = "name";

         }
        public static String JoinnOn(Table t1,String v1,Table t2,String v2 ){
             return " JOIN "+ t2.Name + " ON "+ t1.c(v1)+" = "+ t2.c(v2)+" ";
        }






         public static class Prep{

            public static String getMessenger(){
                return "SELECT * FROM " + messges.Name+ JoinnOn(messges,messges.groupID,gm,gm.Gid) +
                        JoinnOn(gm,gm.Gid,groups,groups.id) +
                         " Where "+ gm.c(gm.Uid) + " = ? AND "+ messges.c(messges.time) +" >= ?"  ;

            }
            public static String getGroupMembers(){
                return "SELECT " + gm.Uid + " FROM " + gm.Name + " WHERE " + gm.Gid + " = ?";
            }
            public static String getMyGroups(){
                return "SELECT " + groups.c(groups.id) +" , "+groups.c(groups.name)+ " " +
                        "FROM " + gm.Name + JoinnOn(gm,gm.Gid,groups,groups.id)+" WHERE " + gm.c(gm.Uid) + " = ?";
            }
            public static String getMyGroupMembers(){
                return "SELECT " + users.c(users.id) +","+ users.c(users.name) +
                        " FROM " + users.Name+ JoinnOn(users,users.id,gm,gm.Uid)+
                        " JOIN " + gm.Name + " x "+" ON " + gm.c(gm.Gid) + " = " + " x."+ gm.Gid
                        + " WHERE " + " x."+gm.Gid + " = ? AND x." +gm.Uid  + " = ?";
            }
            public static String getMemberId(){
                return "SELECT " + users.id + " FROM " + users.Name + " WHERE " + users.name + " = ?";

            }


             public static String makeGroup(){
                return "WITH g AS (INSERT INTO " + groups.Name + " (" + groups.name + ") VALUES (?) RETURNING " + groups.id + ") " +
                        "INSERT INTO " + gm.Name + " (" + gm.Uid + ", " + gm.perm + ", " + gm.Gid + ") " +
                        "SELECT ?, " + admin + ", " + groups.id + " FROM g;";
             }

             public static String premcheck(int perm){
                return " FROM " +
                gm.Name + " WHERE " +
                        gm.c(gm.Uid) + " = ? AND " +
                        gm.c(gm.Gid) + " = ? AND " +
                        gm.c(gm.perm) + " >= " + perm;
            }
            public static String adduserTG(){
                return "INSERT INTO " + gm.Name + " ("+
                        gm.Uid +","+
                         gm.Gid + ","+
                        gm.perm+") SELECT ?, ?, ? " + premcheck(admin);
            }
            public static String kickUser(){
                return "DELETE FROM " + gm.Name + " WHERE " +
                        gm.Uid + " = ? AND " +
                        gm.Gid + " = ? AND EXISTS (SELECT 1" + premcheck(admin) + ")";
            }
            public static String leave(){
                return "DELETE FROM " + gm.Name + " WHERE " +
                        gm.Uid + " = ? AND " +
                        gm.Gid + " = ?";
            }
             public static String ChPrm(){
                 return "UPDATE " + gm.Name + " SET " +
                         gm.perm + " = ? WHERE " +
                         gm.Uid + " = ? AND " +
                         gm.Gid + " = ? AND EXISTS (SELECT 1" + premcheck(admin) + ")";
             }
             public static String dlChat(){
                 return "DELETE FROM " + groups.Name + " WHERE " +
                         groups.id + " = ? AND EXISTS (SELECT 1" + premcheck(admin) + ")";
             }
             public static String dlEmtyChat(){
                return "DELETE FROM "+ groups.Name + " WHERE "+ groups.id +
                        " = ? AND NOT EXISTS ( SELECT 1 FROM "
                        + gm.Name+" WHERE " + gm.Gid + " = ? )";
             }



            public static String insertMessege(){
                return "INSERT INTO " + messges.Name + " (" +
                        messges.SenderName + ", " +
                        messges.contents + ", " +
                        messges.time + ", " +
                        messges.groupID + ") SELECT ?, ?, ?, ?"+ premcheck(post);
            }
            public static String pswcheck(){
                return " WHERE " + Users.name + " = ? AND " +
                        Users.hashPassword + " = crypt(?, " +
                        Users.hashPassword + ")";
            }

             public static String Hashedlogin (){return
                     "SELECT " + Users.id +
                             " FROM " + users.Name +
                             pswcheck();}
             public static String createUser(){
                 return "INSERT INTO " + users.Name + " (" +
                         Users.name + ", " +
                         Users.hashPassword + ") VALUES (?, crypt(?, gen_salt('bf')))";
             }
             public static String chngPswrd(){
                 return "UPDATE " + users.Name + " SET " +
                         Users.hashPassword + " = crypt(?, gen_salt('bf'))"+
                         pswcheck();
             }
             public static String dlUser(){
                 return "DELETE FROM " + users.Name + pswcheck();
             }

         }
     }
}
