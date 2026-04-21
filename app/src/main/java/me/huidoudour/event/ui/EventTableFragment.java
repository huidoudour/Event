package me.huidoudour.event.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.huidoudour.event.R;
import me.huidoudour.event.data.Event;

public class EventTableFragment extends Fragment {

    private EventViewModel viewModel;
    private TableLayout tableLayout;
    private View emptyView;
    private View scrollView;

    public static EventTableFragment newInstance() {
        return new EventTableFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_table, container, false);

        tableLayout = rootView.findViewById(R.id.tableLayout);
        emptyView = rootView.findViewById(R.id.emptyView);
        scrollView = rootView.findViewById(R.id.scrollView);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(requireActivity(), new EventViewModel.Factory(requireActivity().getApplication()))
                .get(EventViewModel.class);

        // 观察事件列表
        viewModel.getSortedEvents().observe(getViewLifecycleOwner(), events -> {
            if (events == null || events.isEmpty()) {
                scrollView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                scrollView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                renderTable(events);
            }
        });

        return rootView;
    }

    /**
     * 渲染表格（参考 CodeScan 的 StartupRecordsActivity）
     */
    private void renderTable(List<Event> events) {
        tableLayout.removeAllViews();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        // 添加表头
        TableRow headerRow = (TableRow) LayoutInflater.from(getContext())
                .inflate(R.layout.item_table_row, tableLayout, false);
        
        TextView tvIdHeader = headerRow.findViewById(R.id.tvId);
        TextView tvTitleHeader = headerRow.findViewById(R.id.tvTime);
        TextView tvDescHeader = headerRow.findViewById(R.id.tvPage);
        TextView tvTimeHeader = headerRow.findViewById(R.id.tvExtra);
        
        tvIdHeader.setText(getString(R.string.table_id));
        tvTitleHeader.setText(getString(R.string.event_title));
        tvDescHeader.setText(getString(R.string.event_description));
        tvTimeHeader.setText(getString(R.string.event_time));
        
        // 设置表头样式 - 所有列都居中
        tvIdHeader.setGravity(android.view.Gravity.CENTER);
        tvTitleHeader.setGravity(android.view.Gravity.CENTER);
        tvDescHeader.setGravity(android.view.Gravity.CENTER);
        tvTimeHeader.setGravity(android.view.Gravity.CENTER);
        
        tvIdHeader.setTextColor(requireContext().getColor(android.R.color.darker_gray));
        tvTitleHeader.setTextColor(requireContext().getColor(android.R.color.darker_gray));
        tvDescHeader.setTextColor(requireContext().getColor(android.R.color.darker_gray));
        tvTimeHeader.setTextColor(requireContext().getColor(android.R.color.darker_gray));
        
        tvIdHeader.setTextSize(14);
        tvTitleHeader.setTextSize(14);
        tvDescHeader.setTextSize(14);
        tvTimeHeader.setTextSize(14);
        
        tableLayout.addView(headerRow);

        // 添加数据行
        for (Event event : events) {
            TableRow dataRow = (TableRow) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_table_row, tableLayout, false);

            TextView tvId = dataRow.findViewById(R.id.tvId);
            TextView tvTitle = dataRow.findViewById(R.id.tvTime);
            TextView tvDescription = dataRow.findViewById(R.id.tvPage);
            TextView tvTime = dataRow.findViewById(R.id.tvExtra);

            tvId.setText(String.valueOf(event.getId()));
            tvTitle.setText(event.getTitle());
            
            String description = event.getDescription();
            tvDescription.setText(description != null && !description.isEmpty() ? description : "-");
            
            tvTime.setText(dateFormat.format(new Date(event.getEventTime())));

            tableLayout.addView(dataRow);
        }
    }
}
