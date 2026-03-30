package me.huidoudour.event.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Set;

import me.huidoudour.event.R;
import me.huidoudour.event.data.Event;

public class HomeActivity extends AppCompatActivity {

    private EventViewModel viewModel;
    private RecyclerView recyclerView;
    private View emptyView;
    private EventAdapter adapter;

    // 多选模式相关
    private LinearLayout batchActionContainer;
    private ImageButton btnSelectAll;
    private ImageButton btnDeleteSelected;
    private FloatingActionButton fabAddEvent;
    private ImageButton btnMultiSelect;
    private ImageButton btnClearAll;
    private boolean isMultiSelectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置按钮
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // 排序按钮
        ImageButton btnSortOrder = findViewById(R.id.btnSortOrder);
        btnSortOrder.setOnClickListener(v -> {
            viewModel.toggleSortOrder();
            String sortOrder = viewModel.isAscending()
                    ? getString(R.string.sort_ascending)
                    : getString(R.string.sort_descending);
            Toast.makeText(HomeActivity.this, sortOrder, Toast.LENGTH_SHORT).show();
        });

        // 刷新按钮
        ImageButton btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(v -> {
            adapter.notifyDataSetChanged();
            Toast.makeText(HomeActivity.this, R.string.refreshed, Toast.LENGTH_SHORT).show();
        });

        // 清除按钮（长按触发）
        btnClearAll = findViewById(R.id.btnClearAll);
        btnClearAll.setOnLongClickListener(v -> {
            showClearAllConfirmDialog();
            return true;
        });

        // 多选按钮
        btnMultiSelect = findViewById(R.id.btnMultiSelect);
        btnMultiSelect.setOnClickListener(v -> toggleMultiSelectMode());

        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        fabAddEvent = findViewById(R.id.fabAddEvent);

        // 批量操作按钮组
        batchActionContainer = findViewById(R.id.batchActionContainer);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);

        btnSelectAll.setOnClickListener(v -> {
            if (adapter.getSelectedCount() == adapter.getItemCount()) {
                adapter.clearSelection();
            } else {
                adapter.selectAll();
            }
        });

        btnDeleteSelected.setOnClickListener(v -> {
            Set<Long> selectedIds = adapter.getSelectedIds();
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, R.string.selected_count_0, Toast.LENGTH_SHORT).show();
                return;
            }
            showDeleteSelectedConfirmDialog(selectedIds);
        });

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this, new EventViewModel.Factory(getApplication()))
                .get(EventViewModel.class);

        // 初始化适配器
        adapter = new EventAdapter(
            event -> showEventDetail(event),
            (event, view) -> showLongClickMenu(event, view)
        );
        adapter.setOnSelectionChangedListener(count -> updateSelectionBar());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 观察事件列表
        viewModel.getSortedEvents().observe(this, events -> {
            adapter.submitList(events);
            if (events.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        // 注册返回键处理
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (adapter.isMultiSelectMode()) {
                    exitMultiSelectMode();
                } else {
                    // 禁用回调，让系统处理返回
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        fabAddEvent.setOnClickListener(v -> {
            if (isMultiSelectMode) {
                // 批量管理模式下，点击FAB无操作（或提示）
                Toast.makeText(this, R.string.exit_multi_select_first, Toast.LENGTH_SHORT).show();
            } else {
                showAddDialog();
            }
        });
    }

    // ─────────────────────────────────────────────
    // 多选模式
    // ─────────────────────────────────────────────

    private void toggleMultiSelectMode() {
        if (adapter.isMultiSelectMode()) {
            exitMultiSelectMode();
        } else {
            enterMultiSelectMode();
        }
    }

    private void enterMultiSelectMode() {
        isMultiSelectMode = true;
        adapter.enterMultiSelectMode();
        batchActionContainer.setVisibility(View.VISIBLE);
        updateSelectionBar();
    }

    private void exitMultiSelectMode() {
        isMultiSelectMode = false;
        adapter.exitMultiSelectMode();
        batchActionContainer.setVisibility(View.GONE);
    }

    private void updateSelectionBar() {
        int count = adapter.getSelectedCount();
        // 全选按钮图标跟随状态切换
        boolean allSelected = (count == adapter.getItemCount() && count > 0);
        btnSelectAll.setImageResource(allSelected ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_agenda);
    }

    /** 批量删除确认对话框（无需输入确认文字，但弹窗二次确认） */
    private void showDeleteSelectedConfirmDialog(Set<Long> selectedIds) {
        int count = selectedIds.size();
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_delete_selected)
            .setMessage(getString(R.string.confirm_delete_selected_message, count))
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                viewModel.deleteEventsByIds(new ArrayList<>(selectedIds));
                Toast.makeText(this, R.string.deleted_selected, Toast.LENGTH_SHORT).show();
                exitMultiSelectMode();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    // ─────────────────────────────────────────────
    // 单条事件操作
    // ─────────────────────────────────────────────

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event, null);
        TextInputEditText editTitle = dialogView.findViewById(R.id.editEventTitle);
        TextInputEditText editDescription = dialogView.findViewById(R.id.editEventDescription);

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_event)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String title = editTitle.getText() != null
                        ? editTitle.getText().toString().trim() : "";
                String description = editDescription.getText() != null
                        ? editDescription.getText().toString().trim() : "";
                if (!title.isEmpty()) {
                    viewModel.addEvent(title, description, System.currentTimeMillis());
                    Toast.makeText(this, R.string.event_saved, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showEventDetail(Event event) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event_detail, null);
        TextInputEditText editTitle = dialogView.findViewById(R.id.editEventTitle);
        TextInputEditText editDescription = dialogView.findViewById(R.id.editEventDescription);

        editTitle.setText(event.getTitle());
        editDescription.setText(event.getDescription() != null ? event.getDescription() : "");

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void showEditDialog(Event event) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event, null);
        TextInputEditText editTitle = dialogView.findViewById(R.id.editEventTitle);
        TextInputEditText editDescription = dialogView.findViewById(R.id.editEventDescription);

        editTitle.setText(event.getTitle());
        editDescription.setText(event.getDescription());

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.edit)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String title = editTitle.getText() != null
                        ? editTitle.getText().toString().trim() : "";
                String description = editDescription.getText() != null
                        ? editDescription.getText().toString().trim() : "";
                if (!title.isEmpty()) {
                    event.setTitle(title);
                    event.setDescription(description);
                    viewModel.updateEvent(event);
                    Toast.makeText(this, R.string.event_saved, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showLongClickMenu(Event event, View view) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.event_options)
            .setItems(R.array.event_options_array, (dialog, which) -> {
                if (which == 0) {
                    showChangeDateTimeDialog(event);
                } else if (which == 1) {
                    showEditDialog(event);
                } else if (which == 2) {
                    showDeleteConfirmDialog(event);
                }
            })
            .show();
    }

    private void showChangeDateTimeDialog(Event event) {
        // 获取当前事件时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getEventTime());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 创建日期选择器
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                // 日期选择后，显示时间选择器
                calendar.set(selectedYear, selectedMonth, selectedDay);

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (timeView, selectedHour, selectedMinute) -> {
                        // 设置新的时间
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        // 更新事件时间
                        event.setEventTime(calendar.getTimeInMillis());
                        viewModel.updateEvent(event);

                        // 显示成功提示
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        Toast.makeText(this,
                            getString(R.string.event_datetime_changed) + ": " + sdf.format(new Date(event.getEventTime())),
                            Toast.LENGTH_LONG).show();
                    },
                    hour,
                    minute,
                    true // 24小时制
                );
                timePickerDialog.show();
            },
            year,
            month,
            day
        );
        datePickerDialog.show();
    }

    private void showDeleteConfirmDialog(Event event) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null);
        TextInputEditText editInput = dialogView.findViewById(R.id.editConfirmInput);

        new MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                String input = editInput.getText() != null
                        ? editInput.getText().toString().trim() : "";
                if ("d".equals(input)) {
                    viewModel.deleteEvent(event);
                    Toast.makeText(this, R.string.event_deleted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    // ─────────────────────────────────────────────
    // 清空全部数据功能（长按触发）
    // ─────────────────────────────────────────────

    /** 清空确认对话框：二级确认 */
    private void showClearAllConfirmDialog() {
        // 第一步:显示简单确认对话框
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clear_all)
            .setMessage(R.string.confirm_clear_all_message)
            .setPositiveButton(R.string.confirm, (dialog, which) -> {
                // 第二步:显示输入"clear"的对话框
                showClearAllInputDialog();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    /** 清空输入对话框：需输入 "clear" */
    private void showClearAllInputDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_clear, null);
        com.google.android.material.textfield.TextInputEditText editInput =
                dialogView.findViewById(R.id.editClearInput);

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clear_all)
            .setMessage(R.string.type_clear_to_confirm)
            .setView(dialogView)
            .setPositiveButton(R.string.clear_all, (dialog, which) -> {
                String input = editInput.getText() != null
                        ? editInput.getText().toString().trim() : "";
                if ("clear".equals(input)) {
                    viewModel.deleteAllEvents();
                    Toast.makeText(this, R.string.all_events_cleared, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

}
