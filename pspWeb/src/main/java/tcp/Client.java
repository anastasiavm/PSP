package tcp;

import com.google.gson.Gson;
import models.Data;
import utils.MedianFilter;

import java.net.*;
import java.io.*;

public final class Client {
    private static final String HOST = "127.0.0.1";
    //private static final InetAddress INETHOST = InetAddress.getByName(HOST);
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        Client.startClient();
    }

    public static void startClient() {
        try (Socket socket = new Socket(HOST, Server.PORT);) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (socket.isConnected()) {

                int size = in.readInt();
                byte[] bytes = new byte[size];
                in.readFully(bytes);
                String tmp = new String(bytes);

                Data data = GSON.fromJson(tmp, Data.class);

                System.out.println("Клиент получил данные");
                MedianFilter.filter(data);
                System.out.println("Клиент отфильтровал данные");

                String result = GSON.toJson(data);
                byte[] resultBytes = result.getBytes();
                out.writeInt(resultBytes.length);
                out.write(resultBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
