package me.kaelaela.websocketsample;

import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import okio.ByteString;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class WebSocketClient implements WebSocketListener {

    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();
    private WebSocket webSocket;
    private MessageListAdapter adapter;

    public WebSocketClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        String url = "wss://echo.websocket.org";
        Request request = new Request.Builder().url(url).build();
        WebSocketCall.create(client, request).enqueue(this);
        client.dispatcher().executorService().shutdown();
    }

    public void setAdapter(MessageListAdapter adapter) {
        this.adapter = adapter;
    }

    public void sendMessage(final String text) throws IOException {
        adapter.setMessage(text);
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    webSocket.sendMessage(RequestBody.create(WebSocket.TEXT, text));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void close() {
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    webSocket.close(1000, "Goodbye!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void onOpen(final WebSocket webSocket, Response response) {
        this.webSocket = webSocket;
        writeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    webSocket.sendMessage(RequestBody.create(WebSocket.TEXT, "Hello..."));
                    webSocket.sendMessage(RequestBody.create(WebSocket.TEXT, "...World!"));
                    webSocket.sendMessage(RequestBody.create(WebSocket.BINARY, ByteString.decodeHex("deadbeef")));
                } catch (IOException e) {
                    System.err.println("Unable to send messages: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onFailure(IOException e, Response response) {
        e.printStackTrace();
        writeExecutor.shutdown();
    }

    @Override
    public void onMessage(ResponseBody message) throws IOException {
        if (message.contentType() == WebSocket.TEXT) {
            setMessageOnUiThread(message.string());
        } else {
            System.out.println("MESSAGE: " + message.source().readByteString().hex());
        }
        message.close();
    }

    @Override
    public void onPong(Buffer payload) {
        System.out.println("PONG: " + payload.readUtf8());
    }

    @Override
    public void onClose(int code, String reason) {
        setResponseOnUiThread("CLOSE: " + code + " " + reason);
        System.out.println("CLOSE: " + code + " " + reason);
        writeExecutor.shutdown();
    }

    private void setMessageOnUiThread(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adapter.setMessage(message);
            }
        });
    }

    private void setResponseOnUiThread(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adapter.setResponse("Echo >" + message);
            }
        });
    }
}
