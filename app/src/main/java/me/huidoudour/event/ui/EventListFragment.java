package me.huidoudour.event.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Set;

import me.huidoudour.event.MainActivity;
import me.huidoudour.event.R;
import me.huidoudour.event.data.Event;
import me.huidoudour.event.util.ActionMonitor;
import me.huidoudour.event.util.LocaleHelper;

public class EventListFragment extends Fragment {

    private EventViewModel viewModel;
    private RecyclerView recyclerView;
    private View emptyView;
    private EventAdapter adapter;

    // 多选模式相关
    private boolean isMultiSelectMode = false;

    public static EventListFragment newInstance() {
        return new EventListFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 应用语言设置
        Context localizedContext = LocaleHelper.applyLanguage(context);
        // 这里可以做一些初始化工作
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        emptyView = rootView.findViewById(R.id.emptyView);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(requireActivity(), new EventViewModel.Factory(requireActivity().getApplication()))
                .get(EventViewModel.class);

        // 初始化适配器
        adapter = new EventAdapter(
            event -> showEventDetail(event),
            (event, view) -> showLongClickMenu(event, view)
        );
        adapter.setOnSelectionChangedListener(count -> updateSelectionBar());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 观察事件列表
        viewModel.getSortedEvents().observe(getViewLifecycleOwner(), events -> {
            adapter.submitList(events);
            if (events.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        return rootView;
    }

    // ─────────────────────────────────────────────
    // 单条事件操作
    // ─────────────────────────────────────────────

    private void showEventDetail(Event event) {
        ActionMonitor.log("ITEM_CLICK", "查看事件详情: " + event.getTitle(), event.getId());
        ((MainActivity) requireActivity()).showEventDetail(event);
    }

    private void showLongClickMenu(Event event, View view) {
        ActionMonitor.log("ITEM_LONG_CLICK", "长按事件条目: " + event.getTitle(), event.getId());
        ((MainActivity) requireActivity()).showLongClickMenu(event, view);
    }

    // ─────────────────────────────────────────────
    // 多选模式相关方法
    // ─────────────────────────────────────────────

    public void toggleMultiSelectMode() {
        if (adapter.isMultiSelectMode()) {
            exitMultiSelectMode();
        } else {
            enterMultiSelectMode();
        }
    }

    public void enterMultiSelectMode() {
        isMultiSelectMode = true;
        adapter.enterMultiSelectMode();
        ActionMonitor.log("MULTI_SELECT", "进入多选模式", 0);
        ((MainActivity) requireActivity()).updateBatchActionContainerVisibility(true);
    }

    public void exitMultiSelectMode() {
        isMultiSelectMode = false;
        adapter.exitMultiSelectMode();
        ActionMonitor.log("MULTI_SELECT", "退出多选模式", 0);
        ((MainActivity) requireActivity()).updateBatchActionContainerVisibility(false);
    }

    public void selectAll() {
        if (adapter.getSelectedCount() == adapter.getItemCount()) {
            adapter.clearSelection();
            ActionMonitor.log("MULTI_SELECT", "取消全选", 0);
        } else {
            adapter.selectAll();
            ActionMonitor.log("MULTI_SELECT", "全选所有条目", 0);
        }
    }

    public void deleteSelected() {
        Set<Long> selectedIds = adapter.getSelectedIds();
        if (selectedIds.isEmpty()) {
            // 提示用户没有选择项目
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), R.string.selected_count_0, android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }
        // 使用MainActivity中的删除确认对话框
        ((MainActivity) requireActivity()).showDeleteSelectedConfirmDialog(selectedIds);
    }

    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    public int getSelectedCount() {
        return adapter.getSelectedCount();
    }

    private void updateSelectionBar() {
        ((MainActivity) requireActivity()).updateSelectionBar(adapter.getSelectedCount(), adapter.getItemCount());
    }

    // ─────────────────────────────────────────────
    // 公共方法供MainActivity调用
    // ─────────────────────────────────────────────

    public void refresh() {
        adapter.notifyDataSetChanged();
    }
}