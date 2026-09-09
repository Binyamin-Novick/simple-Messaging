package layer2Server;

import org.example.MacroDef;

import static org.junit.jupiter.api.Assertions.*;

class TokenHolderTest {


    @org.junit.jupiter.api.Test
    void creatToken() {
        TokenHolder tokenHolder = new TokenHolder();
        long id=1;
        String token=tokenHolder.creatToken(id);
        assertEquals(tokenHolder.ids.get(token).id,id);

    }

    @org.junit.jupiter.api.Test
    void login() {
        TokenHolder tokenHolder = new TokenHolder();
        String token=tokenHolder.creatToken(1);
        long[] result=tokenHolder.login(token);
        assertEquals(result[0], MacroDef.ok);
        assertEquals(result[1],1);
        assertEquals(tokenHolder.ids.get(token).id,1);
        String badToken=token+"bad";
        result=tokenHolder.login(badToken);
        assertEquals(result[0], MacroDef.fail);
        assertEquals(result[1],0);


        long id3=3;
        String token3=tokenHolder.creatToken(id3);
        result=tokenHolder.login(token3);
        assertEquals(result[0], MacroDef.ok);
        assertEquals(result[1],3);
        assertEquals(tokenHolder.ids.get(token3).id,3);
        assertEquals(tokenHolder.login(token)[0],MacroDef.ok);
        assertEquals(tokenHolder.login(token)[1],1);
    }

    @org.junit.jupiter.api.Test
    void clean() {
        TokenHolder tokenHolder = new TokenHolder();
        long id=1;
        String token=tokenHolder.creatToken(id);
        assertEquals(tokenHolder.clean().size(),0);
        tokenHolder.login(token);
        assertEquals(tokenHolder.clean().size(),0);

    }

    @org.junit.jupiter.api.Test
    void getActiveUserIds() {
        TokenHolder tokenHolder = new TokenHolder();
        long id=1;
        String token=tokenHolder.creatToken(id);
        assertEquals(tokenHolder.getActiveUserIds().size(),1);
        long id2=2;
        String token2=tokenHolder.creatToken(id2);
        assertEquals(tokenHolder.getActiveUserIds().size(),2);
    }
}