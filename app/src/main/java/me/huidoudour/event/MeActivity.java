package me.huidoudour.event;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.core.view.WindowCompat;

import me.huidoudour.event.util.ActionMonitor;
import me.huidoudour.event.util.BaseActivity;

public class MeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        setupStatusBar();

        Button btnWebsite = findViewById(R.id.btn_website);
        btnWebsite.setOnClickListener(v -> {
            ActionMonitor.log("BTN_CLICK", "点击访问GitHub按钮", 0);
            String url = "https://github.com/huidoudour";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(true);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}
