package tcp;

import com.google.gson.Gson;
import models.Data;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class Server {

    public static Server server = null;

    public static Server getInstance() {
        if (server == null) {
            System.out.println("Новый TCP Сервер запущен");
            server = new Server();
        }
        System.out.println("TCP Сервер запущен");
        return server;
    }

    public static void main(String[] args) {
        Server.getInstance();
    }

    public static final int PORT = 8088;
    private static final Gson GSON = new Gson();

    private ServerSocket serverSocket;
    private final List<Socket> clients = Collections.synchronizedList(new ArrayList<>());
    // буфер данных полученных от вебсервера
    private final List<BufferItem> buffer = Collections.synchronizedList(new ArrayList<>());
    // переменная для защиты работы клиентов
    private volatile boolean working = false;

    private final Thread connectingThread = new Thread(this::connecting);
    private final Thread sendingThread = new Thread(this::sending);

    private Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            connectingThread.setDaemon(true);
            connectingThread.start();
            sendingThread.setDaemon(true);
            sendingThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void connecting() {
        while (true) {
            try {
                Socket client = serverSocket.accept();
                clients.add(client);
                System.out.println("Клиент подключен");
                System.out.println("Колво клиентов " + clients.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sending() {
        while (true) {
            while (working || buffer.isEmpty() || clients.isEmpty());
            working = true;
            List<DataInputStream> ins = new ArrayList<>();
            List<DataOutputStream> outs = new ArrayList<>();
            Iterator<Socket> iterator = clients.iterator();
            while (iterator.hasNext()) {
                Socket client = iterator.next();
                try {
                    DataInputStream in = new DataInputStream(client.getInputStream());
                    DataOutputStream out = new DataOutputStream(client.getOutputStream());
                     ins.add(in);
                     outs.add(out);
                } catch (IOException e) {
                    System.out.println( e.getMessage());
                    iterator.remove();
                }
            }
            Data data = buffer.get(0).data;
            System.out.println("Данные от вебсервера получены");
            int count = outs.size();
            for (int i = 0; i < count; i++) {
                int h = data.getPixels().length / count;
                int numlines = h;
                if (h * i + numlines < data.getPixels().length - h) numlines = h + data.getPixels().length - h * i;
                //Data d = new Data(data.getFilterSize(), data.partOfImage(h * i, numlines));
                DataOutputStream outputStream = outs.get(i);
                try {
                    // отправка клиенту его части изображения
                    byte[] bytes = GSON.toJson(data).getBytes();
                    outputStream.writeInt(bytes.length);
                    outputStream.write(bytes);
                    System.out.println("Данные отправлены кластеру/клиентк");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < count; i++) {
                try {

                    // ответ клиента отфильтрованная картинка
                    int size = ins.get(i).readInt();
                    byte[] bytes = new byte[size];
                    ins.get(i).readFully(bytes);
                    Data filteredData = GSON.fromJson(new String(bytes), Data.class);
                    int h = data.getPixels().length / count;
                    int numlines = h;
                    if (h * i + numlines < data.getPixels().length - h) numlines = h + data.getPixels().length - h * i;
                    // замена части исходнного изображения на ответ клента
                    data.changeData(h * i, h, filteredData);
                    // замена в исходнном
                    //data.change(filteredData);
                    System.out.println("Данные получены");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            buffer.get(0).callback.call(data);
            buffer.remove(0);
            working = false;
        }
    }

    public void enqueue(BufferItem bufferItem) {
        buffer.add(bufferItem);
    }


    @FunctionalInterface
    public interface SessionCallback {
        void call(Data result);
    }


    public static class BufferItem {
        public final Data data;
        public final SessionCallback callback;

        public BufferItem(Data data, SessionCallback callback) {
            this.data = data;
            this.callback = callback;
        }
    }
}

