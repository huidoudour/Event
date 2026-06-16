package me.huidoudour.event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import me.huidoudour.event.data.Event;
import me.huidoudour.event.ui.EventListFragment;
import me.huidoudour.event.ui.EventTableFragment;
import me.huidoudour.event.ui.EventViewModel;
import me.huidoudour.event.ui.SettingsActivity;
import me.huidoudour.event.utils.BaseActivity;
import me.huidoudour.event.utils.IconColorHelper;
import me.huidoudour.event.utils.ViewModeHelper;

public class MainActivity extends BaseActivity {

    private EventViewModel viewModel;
    
    // Toolbar按钮
    private ImageButton btnRefresh;
    private ImageButton btnClearAll;
    private ImageButton btnMultiSelect;
    private ImageButton btnSettings;
    
    // FAB和批量操作
    private FloatingActionButton fabAddEvent;
    private LinearLayout batchActionContainer;
    private ImageButton btnSelectAll;
    private ImageButton btnDeleteSelected;
    
    // 当前Fragment
    private Fragment currentFragment;
    private EventListFragment eventListFragment;
    private EventTableFragment eventTableFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 恢复图标颜色状态（确保alias与SharedPreferences一致）
        IconColorHelper.applyIconColor(this, IconColorHelper.getIconColor(this));
        
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupButtons();
        setupViewModel();
        setupFAB();
        
        // 根据视图模式加载对应的Fragment
        loadFragmentByViewMode();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupButtons() {
        btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(v -> {
            refreshCurrentFragment();
            Toast.makeText(MainActivity.this, R.string.refreshed, Toast.LENGTH_SHORT).show();
        });

        btnClearAll = findViewById(R.id.btnClearAll);
        btnClearAll.setOnLongClickListener(v -> {
            showClearAllConfirmDialog();
            return true;
        });

        btnMultiSelect = findViewById(R.id.btnMultiSelect);
        btnMultiSelect.setOnClickListener(v -> {
            toggleMultiSelectOnCurrentFragment();
        });

        // 批量操作按钮
        batchActionContainer = findViewById(R.id.batchActionContainer);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);

        btnSelectAll.setOnClickListener(v -> {
            selectAllOnCurrentFragment();
        });

        btnDeleteSelected.setOnClickListener(v -> {
            deleteSelectedOnCurrentFragment();
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, new EventViewModel.Factory(getApplication()))
                .get(EventViewModel.class);
    }

    private void setupFAB() {
        fabAddEvent = findViewById(R.id.fabAddEvent);
        fabAddEvent.setOnClickListener(v -> {
            if (isMultiSelectModeOnCurrentFragment()) {
                Toast.makeText(this, R.string.exit_multi_select_first, Toast.LENGTH_SHORT).show();
            } else {
                showAddDialog();
            }
        });
    }

    private void loadFragmentByViewMode() {
        int viewMode = ViewModeHelper.getViewMode(this);
        
        if (viewMode == ViewModeHelper.VIEW_MODE_LIST) {
            // 列表视图模式 - 加载表格Fragment
            loadEventTableFragment();
        } else {
            // 条目视图模式 - 加载列表Fragment
            loadEventListFragment();
        }
    }

    private void loadEventListFragment() {
        if (eventListFragment == null) {
            eventListFragment = EventListFragment.newInstance();
        }
        currentFragment = eventListFragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, eventListFragment)
                .commit();
        
        // 显示FAB和批量操作按钮
        fabAddEvent.setVisibility(View.VISIBLE);
        btnMultiSelect.setVisibility(View.VISIBLE);
    }

    private void loadEventTableFragment() {
        if (eventTableFragment == null) {
            eventTableFragment = EventTableFragment.newInstance();
        }
        currentFragment = eventTableFragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, eventTableFragment)
                .commit();
        
        // 显示FAB和多选按钮（表格视图同样支持多选）
        fabAddEvent.setVisibility(View.VISIBLE);
        btnMultiSelect.setVisibility(View.VISIBLE);
    }

    // ─────────────────────────────────────────────
    // 公共方法供Fragment调用
    // ─────────────────────────────────────────────

    public void showEventDetail(Event event) {
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

    public void showLongClickMenu(Event event, View view) {
        String title;
        if (currentFragment instanceof EventTableFragment) {
            // 表格视图：显示 ID 和标题，防止误触时不知道操作的是哪条记录
            title = "[ID: " + event.getId() + "] " + event.getTitle();
        } else {
            // 条目视图：仅显示标题
            title = event.getTitle();
        }

        new MaterialAlertDialogBuilder(this)
            .setTitle(title)
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

    public void updateBatchActionContainerVisibility(boolean visible) {
        batchActionContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void updateSelectionBar(int selectedCount, int totalCount) {
        boolean allSelected = (selectedCount == totalCount && selectedCount > 0);
        btnSelectAll.setImageResource(allSelected ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_agenda);
    }

    // ─────────────────────────────────────────────
    // 辅助方法 - 统一路由到当前Fragment
    // ─────────────────────────────────────────────

    private void toggleMultiSelectOnCurrentFragment() {
        if (currentFragment instanceof EventListFragment) {
            ((EventListFragment) currentFragment).toggleMultiSelectMode();
        } else if (currentFragment instanceof EventTableFragment) {
            ((EventTableFragment) currentFragment).toggleMultiSelectMode();
        }
    }

    private void selectAllOnCurrentFragment() {
        if (currentFragment instanceof EventListFragment) {
            ((EventListFragment) currentFragment).selectAll();
        } else if (currentFragment instanceof EventTableFragment) {
            ((EventTableFragment) currentFragment).selectAll();
        }
    }

    private void deleteSelectedOnCurrentFragment() {
        if (currentFragment instanceof EventListFragment) {
            ((EventListFragment) currentFragment).deleteSelected();
        } else if (currentFragment instanceof EventTableFragment) {
            ((EventTableFragment) currentFragment).deleteSelected();
        }
    }

    private boolean isMultiSelectModeOnCurrentFragment() {
        if (currentFragment instanceof EventListFragment) {
            return ((EventListFragment) currentFragment).isMultiSelectMode();
        } else if (currentFragment instanceof EventTableFragment) {
            return ((EventTableFragment) currentFragment).isMultiSelectMode();
        }
        return false;
    }

    private void exitMultiSelectOnCurrentFragment() {
        if (currentFragment instanceof EventListFragment) {
            ((EventListFragment) currentFragment).exitMultiSelectMode();
        } else if (currentFragment instanceof EventTableFragment) {
            ((EventTableFragment) currentFragment).exitMultiSelectMode();
        }
    }

    private void refreshCurrentFragment() {
        if (currentFragment instanceof EventListFragment) {
            ((EventListFragment) currentFragment).refresh();
        } else if (currentFragment instanceof EventTableFragment) {
            ((EventTableFragment) currentFragment).refresh();
        }
    }

    public void showDeleteSelectedConfirmDialog(Set<Long> selectedIds) {
        int count = selectedIds.size();
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_delete_selected)
            .setMessage(getString(R.string.confirm_delete_selected_message, count))
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                viewModel.deleteEventsByIds(new ArrayList<>(selectedIds));
                Toast.makeText(this, R.string.deleted_selected, Toast.LENGTH_SHORT).show();
                exitMultiSelectOnCurrentFragment();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    // ─────────────────────────────────────────────
    // 对话框方法
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

    private void showChangeDateTimeDialog(Event event) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getEventTime());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                calendar.set(selectedYear, selectedMonth, selectedDay);

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (timeView, selectedHour, selectedMinute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        event.setEventTime(calendar.getTimeInMillis());
                        viewModel.updateEvent(event);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        Toast.makeText(this,
                            getString(R.string.event_datetime_changed) + ": " + sdf.format(new Date(event.getEventTime())),
                            Toast.LENGTH_LONG).show();
                    },
                    hour,
                    minute,
                    true
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

    private void showClearAllConfirmDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clear_all)
            .setMessage(R.string.confirm_clear_all_message)
            .setPositiveButton(R.string.confirm, (dialog, which) -> {
                showClearAllInputDialog();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showClearAllInputDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_clear, null);
        TextInputEditText editInput = dialogView.findViewById(R.id.editClearInput);

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

    @Override
    protected void onResume() {
        super.onResume(); // BaseActivity会检测语言/主题变化并自动重启
        if (isRestarting) return;
        
        // 检查视图模式是否变化，如果变化则重新加载Fragment
        int viewMode = ViewModeHelper.getViewMode(this);
        boolean isListMode = (currentFragment instanceof EventListFragment);
        
        if ((viewMode == ViewModeHelper.VIEW_MODE_LIST && isListMode) ||
            (viewMode == ViewModeHelper.VIEW_MODE_CARD && !isListMode)) {
            // 视图模式已改变，重新加载
            loadFragmentByViewMode();
        }
        
        // 同步排序状态（从设置页返回时）
        if (viewModel != null) {
            viewModel.getRepository().syncSortOrder();
        }
    }
}
