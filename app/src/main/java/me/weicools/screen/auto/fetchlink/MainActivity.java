package me.weicools.screen.auto.fetchlink;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        textView = findViewById(R.id.url_text);

        findViewById(R.id.clean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
            }
        });
    }

    @Subscribe
    public void onEvent(MessageEvent.FetchedLinkEvent event) {
        String text = "Type: " + event.type +
                ", PackageName: " + event.packageName +
                ", ClassName: " + event.className +
                ", Url: " + event.url +
                "\n\n";
        textView.append(text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
