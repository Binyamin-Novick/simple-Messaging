package layer2Server;

import org.example.MacroDef;

import java.security.SecureRandom;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TokenHolder {
    SecureRandom sr=new SecureRandom();
    static long days=5;
    class Token{
        LocalDateTime ts;
        long id;
    }
    ConcurrentHashMap<String,Token>ids =new ConcurrentHashMap<>();

    public String  creatToken(long id){
        Token tm=new Token();
        byte[] bytes = new byte[32];
        sr.nextBytes(bytes);
        String key =Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tm.ts=LocalDateTime.now();
        tm.id=id;
        ids.put(key,tm);
        return key;
    }
    public long[] login(String token){
        Token t=ids.get(token);
        if(t==null)return new long[]{MacroDef.fail,0};
        Duration elapsed = Duration.between(t.ts, LocalDateTime.now());
        if(elapsed.toDays()<days)return new long[]{MacroDef.ok,t.id};
        else return new long[]{MacroDef.fail,0};
    }

    public Set<Long> clean(){
        Set< Map.Entry<String,Token>> tr=
        ids.entrySet().parallelStream().
                filter((x)->
                        Duration.between(x.getValue().ts,LocalDateTime.now()).toDays()>days-1)
                .collect(Collectors.toSet());
        Set<Long> inactive = new HashSet<>();
        for(Map.Entry<String,Token> t : tr){
            ids.remove(t.getKey());
            inactive.add(t.getValue().id);
        }
        Set<Long> active = getActiveUserIds();
        inactive.removeAll(active);
        return inactive;
    }

    public Set<Long> getActiveUserIds() {
        return ids.values().stream().map(t -> t.id).collect(Collectors.toSet());
    }






}
