package me.huidoudour.event.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import me.huidoudour.event.MainActivity;
import me.huidoudour.event.R;
import me.huidoudour.event.data.Event;

public class EventTableFragment extends Fragment {

    private EventViewModel viewModel;
    private TableLayout tableLayout;
    private View emptyView;
    private View scrollView;

    // 保存当前数据用于重绘
    private List<Event> currentEvents;

    // 多选模式相关
    private boolean isMultiSelectMode = false;
    private final Set<Long> selectedIds = new HashSet<>();
    private CheckBox headerCheckBox;

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
            currentEvents = events;
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
     * 渲染表格
     */
    private void renderTable(List<Event> events) {
        tableLayout.removeAllViews();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        // 添加表头
        TableRow headerRow = (TableRow) LayoutInflater.from(getContext())
                .inflate(R.layout.item_table_row, tableLayout, false);
        
        CheckBox cbHeader = headerRow.findViewById(R.id.cbSelect);
        TextView tvIdHeader = headerRow.findViewById(R.id.tvId);
        TextView tvTitleHeader = headerRow.findViewById(R.id.tvTime);
        TextView tvDescHeader = headerRow.findViewById(R.id.tvPage);
        TextView tvTimeHeader = headerRow.findViewById(R.id.tvExtra);

        // 多选模式下显示表头的全选CheckBox
        if (isMultiSelectMode) {
            headerCheckBox = cbHeader;
            cbHeader.setVisibility(View.VISIBLE);
            cbHeader.setChecked(selectedIds.size() == events.size() && !events.isEmpty());
            cbHeader.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectAll();
                } else {
                    clearSelection();
                }
            });
        } else {
            headerCheckBox = null;
            cbHeader.setVisibility(View.GONE);
        }
        
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

            CheckBox cbSelect = dataRow.findViewById(R.id.cbSelect);
            TextView tvId = dataRow.findViewById(R.id.tvId);
            TextView tvTitle = dataRow.findViewById(R.id.tvTime);
            TextView tvDescription = dataRow.findViewById(R.id.tvPage);
            TextView tvTime = dataRow.findViewById(R.id.tvExtra);

            // 多选模式：原地更新，不重建表格
            if (isMultiSelectMode) {
                cbSelect.setVisibility(View.VISIBLE);
                cbSelect.setChecked(selectedIds.contains(event.getId()));

                cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    long id = event.getId();
                    if (isChecked) {
                        selectedIds.add(id);
                    } else {
                        selectedIds.remove(id);
                    }
                    updateHeaderCheckboxState();
                    updateSelectionBar();
                });

                dataRow.setOnClickListener(v -> {
                    cbSelect.toggle();
                });
                dataRow.setOnLongClickListener(null);
            } else {
                cbSelect.setVisibility(View.GONE);

                dataRow.setOnClickListener(v -> {
                    ((MainActivity) requireActivity()).showEventDetail(event);
                });
                dataRow.setOnLongClickListener(v -> {
                    ((MainActivity) requireActivity()).showLongClickMenu(event, v);
                    return true;
                });
            }

            tvId.setText(String.valueOf(event.getId()));
            tvTitle.setText(event.getTitle());
            
            String description = event.getDescription();
            tvDescription.setText(description != null && !description.isEmpty() ? description : "-");
            
            tvTime.setText(dateFormat.format(new Date(event.getEventTime())));

            tableLayout.addView(dataRow);
        }
    }

    // ─────────────────────────────────────────────
    // 多选模式相关方法
    // ─────────────────────────────────────────────

    public void toggleMultiSelectMode() {
        if (isMultiSelectMode) {
            exitMultiSelectMode();
        } else {
            enterMultiSelectMode();
        }
    }

    public void enterMultiSelectMode() {
        isMultiSelectMode = true;
        selectedIds.clear();
        ((MainActivity) requireActivity()).updateBatchActionContainerVisibility(true);
        renderTable(currentEvents);
    }

    public void exitMultiSelectMode() {
        isMultiSelectMode = false;
        selectedIds.clear();
        ((MainActivity) requireActivity()).updateBatchActionContainerVisibility(false);
        renderTable(currentEvents);
    }

    public void selectAll() {
        if (currentEvents != null) {
            for (Event event : currentEvents) {
                selectedIds.add(event.getId());
            }
        }
        renderTable(currentEvents);
        updateSelectionBar();
    }

    public void clearSelection() {
        selectedIds.clear();
        renderTable(currentEvents);
        updateSelectionBar();
    }

    public void deleteSelected() {
        Set<Long> ids = getSelectedIds();
        if (ids.isEmpty()) {
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), R.string.selected_count_0, android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }
        ((MainActivity) requireActivity()).showDeleteSelectedConfirmDialog(ids);
    }

    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    public Set<Long> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    private void updateHeaderCheckboxState() {
        if (headerCheckBox != null && currentEvents != null) {
            boolean allSelected = selectedIds.size() == currentEvents.size() && !currentEvents.isEmpty();
            // 静默更新，不触发OnCheckedChangeListener
            headerCheckBox.setOnCheckedChangeListener(null);
            headerCheckBox.setChecked(allSelected);
            headerCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectAll();
                } else {
                    clearSelection();
                }
            });
        }
    }

    private void updateSelectionBar() {
        if (currentEvents != null) {
            ((MainActivity) requireActivity()).updateSelectionBar(selectedIds.size(), currentEvents.size());
        }
    }

    // ─────────────────────────────────────────────
    // 公共方法供MainActivity调用
    // ─────────────────────────────────────────────

    public void refresh() {
        renderTable(currentEvents);
    }
}
