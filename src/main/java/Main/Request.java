package Main;

import com.google.gson.Gson;

import java.util.HashMap;

public class Request {
    private static String userToken;
    private final String token;
    public GameRequest gameRequest;
    public NormalRequest normalRequest;
    public HashMap<String, String> argument;



    public static void setUserToken(String userToken) {
        Request.userToken = userToken;
    }



    public Request(GameRequest gameRequest, NormalRequest normalRequest) {
        token = userToken;
        this.gameRequest = gameRequest;
        this.normalRequest = normalRequest;
        argument = new HashMap<>();
    }



    public boolean verify(String token) {
        if(this.token==null) return false;
        //TODO: FIX AND CHANGE
        return this.token.equals(token);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Request fromJson(String json) {
        return new Gson().fromJson(json, Request.class);
    }
}
