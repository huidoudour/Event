package me.huidoudour.event.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.huidoudour.event.R;
import me.huidoudour.event.data.Event;
import me.huidoudour.event.utils.LocaleHelper;
import me.huidoudour.event.utils.ThemeHelper;
import me.huidoudour.event.utils.ViewModeHelper;

public class ListViewActivity extends AppCompatActivity {

    private EventViewModel viewModel;
    private RecyclerView recyclerView;
    private View emptyView;
    private ListAdapter adapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.initTheme(this);
        
        // 检查当前视图模式，如果是条目视图则跳转回MainActivity
        int viewMode = ViewModeHelper.getViewMode(this);
        if (viewMode == ViewModeHelper.VIEW_MODE_CARD) {
            Intent intent = new Intent(ListViewActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 关闭ListViewActivity
            return;
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 设置按钮
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ListViewActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recyclerViewList);
        emptyView = findViewById(R.id.emptyView);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this, new EventViewModel.Factory(getApplication()))
                .get(EventViewModel.class);

        // 初始化适配器
        adapter = new ListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 观察事件列表
        viewModel.getSortedEvents().observe(this, events -> {
            adapter.submitList(events);
            if (events == null || events.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });
    }

    // 列表视图适配器
    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
        private List<Event> events;

        public void submitList(List<Event> eventList) {
            this.events = eventList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_view, parent, false);
            return new ListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
            holder.bind(events.get(position));
        }

        @Override
        public int getItemCount() {
            return events == null ? 0 : events.size();
        }

        class ListViewHolder extends RecyclerView.ViewHolder {
            private final TextView textId;
            private final TextView textTitle;
            private final TextView textDescription;
            private final TextView textTime;

            ListViewHolder(View itemView) {
                super(itemView);
                textId = itemView.findViewById(R.id.textId);
                textTitle = itemView.findViewById(R.id.textTitle);
                textDescription = itemView.findViewById(R.id.textDescription);
                textTime = itemView.findViewById(R.id.textTime);
            }

            void bind(Event event) {
                textId.setText(String.valueOf(event.getId()));
                textTitle.setText(event.getTitle());
                
                if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                    textDescription.setText(event.getDescription());
                    textDescription.setVisibility(View.VISIBLE);
                } else {
                    textDescription.setText("-");
                    textDescription.setVisibility(View.VISIBLE);
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                textTime.setText(dateFormat.format(new Date(event.getEventTime())));
            }
        }
    }
}
