package me.kaelaela.websocketsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements WebSocketClient.Callback{

    private RecyclerView recyclerView;
    private WebSocketClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new WebSocketClient(this);

        findViewById(R.id.open_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.open();
            }
        });

        final EditText editText = (EditText) findViewById(R.id.message);
        findViewById(R.id.emit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    client.sendMessage(editText.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    editText.setText("");
                }
            }
        });

        findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.close();
            }
        });
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MessageListAdapter());
        client.setAdapter((MessageListAdapter) recyclerView.getAdapter());
    }

    @Override
    public void onOpen() {
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
    }

    @Override
    public void onMessage() {
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
    }

    @Override
    public void onClose() {
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
    }

    @Override
    public void onError() {
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
    }
}
