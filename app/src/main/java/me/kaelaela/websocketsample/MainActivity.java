package me.kaelaela.websocketsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private WebSocketClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new WebSocketClient();
        final EditText editText = (EditText) findViewById(R.id.message);
        findViewById(R.id.emit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    client.sendMessage(editText.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
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
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MessageListAdapter adapter = new MessageListAdapter();
        recyclerView.setAdapter(adapter);
        client.setAdapter(adapter);
    }
}
