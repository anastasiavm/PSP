package websocket;

import com.google.gson.Gson;
import models.Data;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class MessageDecoder implements Decoder.Text {
    private static final Gson gson = new Gson();


    public Data decode(String s) throws DecodeException {
        //System.out.println("sdfdsfsdfsd");
        return gson.fromJson(s, Data.class);
    }

    public boolean willDecode(String s) {
        return false;
    }

    public void init(EndpointConfig endpointConfig) {

    }

    public void destroy() {

    }
}
