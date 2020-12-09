package websocket;

import com.google.gson.Gson;
import models.Data;
import tcp.Server;
import utils.MedianFilter;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;


@ServerEndpoint(value = "/hello")
public class MainEndpoint {

    private static final Gson GSON = new Gson();

    public MainEndpoint() {
        Server.getInstance();
    }

    @OnOpen
    public void onOpen(Session session) {
    }

    // Когда с клиента приходит какое-то сообщение, то срабатывает этот метод.
    @OnMessage(maxMessageSize = 10 * 1024 * 1024)
    public void onMessage(Session session, String s) throws IOException {
        Data data = GSON.fromJson(s, Data.class);
        // MedianFilter.filter(data);
        Server.getInstance().enqueue(new Server.BufferItem(data, (filteredImage) -> {
            try {
                session.getBasicRemote().sendText(filteredImage.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
       // session.getBasicRemote().sendText(data.toString());
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {

    }
}
