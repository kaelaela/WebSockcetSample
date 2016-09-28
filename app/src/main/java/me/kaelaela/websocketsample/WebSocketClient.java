package me.kaelaela.websocketsample;

import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class WebSocketClient implements WebSocketListener {

    private WebSocket webSocket;
    private MessageListAdapter adapter;
    private Callback callback;

    public WebSocketClient(Callback callback) {
        this.callback = callback;
        initSocket();
    }

    private void initSocket() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        String url = "wss://echo.websocket.org";
        Request request = new Request.Builder().url(url).build();
        client.newWebSocketCall(request).enqueue(this);
        client.dispatcher().executorService().shutdown();
    }

    public void setAdapter(MessageListAdapter adapter) {
        this.adapter = adapter;
    }

    public void open() {
        initSocket();
    }

    public void sendMessage(final String text) {
        adapter.setMessage(text);
        Observable.just(text).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                try {
                    webSocket.sendMessage(RequestBody.create(WebSocket.TEXT, s));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return s;
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                //nop
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(String s) {
                System.out.println("send message:" + s);
            }
        });
    }

    public void close() {
        Observable.just(null).map(new Func1<Object, Void>() {
            @Override
            public Void call(Object o) {
                try {
                    webSocket.close(1000, "Goodbye!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                //nop
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Void aVoid) {
                System.out.println("closed WebSocket.");
            }
        });
    }

    @Override
    public void onOpen(final WebSocket webSocket, Response response) {
        this.webSocket = webSocket;
        setMessageOnUiThread("OPEN: Hello World!");
        callback.onOpen();
    }

    @Override
    public void onMessage(ResponseBody message) throws IOException {
        if (message.contentType() == WebSocket.TEXT) {
            setResponseOnUiThread(message.string());
        } else {
            System.out.println("MESSAGE: " + message.source().readByteString().hex());
        }
        message.close();
        callback.onMessage();
    }

    @Override
    public void onPong(ByteString byteString) {
        System.out.println("PONG: " + byteString);
    }

    @Override
    public void onClose(int code, String reason) {
        setMessageOnUiThread("CLOSE: " + reason);
        callback.onClose();
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        throwable.printStackTrace();
        setMessageOnUiThread("ERROR: " + throwable.getMessage());
        callback.onError();
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
                adapter.setResponse("Echo >>> " + message);
            }
        });
    }

    interface Callback {
        void onOpen();

        void onMessage();

        void onClose();

        void onError();
    }
}
