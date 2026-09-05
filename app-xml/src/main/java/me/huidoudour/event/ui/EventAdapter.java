package me.huidoudour.event.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import me.huidoudour.event.R;
import me.huidoudour.event.data.Event;

public class EventAdapter extends ListAdapter<Event, EventAdapter.EventViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Event event, View view);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    private final OnItemClickListener listener;
    private final OnItemLongClickListener longListener;
    private OnSelectionChangedListener selectionChangedListener;

    // ── 多选状态 ──
    private boolean multiSelectMode = false;
    private final Set<Long> selectedIds = new HashSet<>();

    public EventAdapter(OnItemClickListener listener, OnItemLongClickListener longListener) {
        super(new EventDiffCallback());
        this.listener = listener;
        this.longListener = longListener;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener l) {
        this.selectionChangedListener = l;
    }

    // ── 多选模式切换 ──

    public void enterMultiSelectMode() {
        multiSelectMode = true;
        selectedIds.clear();
        notifyDataSetChanged();
    }

    public void exitMultiSelectMode() {
        multiSelectMode = false;
        selectedIds.clear();
        notifyDataSetChanged();
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    public void selectAll() {
        for (int i = 0; i < getItemCount(); i++) {
            selectedIds.add(getItem(i).getId());
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public Set<Long> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedIds.size());
        }
    }

    // ── RecyclerView ──

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final TextView textDescription;
        private final TextView textTime;
        private final MaterialCardView cardView;
        private final CheckBox checkBox;

        EventViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textEventTitle);
            textDescription = itemView.findViewById(R.id.textEventDescription);
            textTime = itemView.findViewById(R.id.textEventTime);
            cardView = (MaterialCardView) itemView;
            checkBox = itemView.findViewById(R.id.checkBoxSelect);
        }

        void bind(Event event) {
            textTitle.setText(event.getTitle());

            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                textDescription.setText(event.getDescription());
                textDescription.setVisibility(View.VISIBLE);
            } else {
                textDescription.setVisibility(View.GONE);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            textTime.setText(dateFormat.format(new Date(event.getEventTime())));

            // ── 多选模式下显示 CheckBox ──
            if (multiSelectMode) {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setChecked(selectedIds.contains(event.getId()));
                cardView.setChecked(selectedIds.contains(event.getId()));

                cardView.setOnClickListener(v -> {
                    long id = event.getId();
                    if (selectedIds.contains(id)) {
                        selectedIds.remove(id);
                        checkBox.setChecked(false);
                        cardView.setChecked(false);
                    } else {
                        selectedIds.add(id);
                        checkBox.setChecked(true);
                        cardView.setChecked(true);
                    }
                    notifySelectionChanged();
                });
                cardView.setOnLongClickListener(null);
            } else {
                checkBox.setVisibility(View.GONE);
                cardView.setChecked(false);

                cardView.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(event);
                });
                cardView.setOnLongClickListener(v -> {
                    if (longListener != null) {
                        longListener.onItemLongClick(event, v);
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    static class EventDiffCallback extends DiffUtil.ItemCallback<Event> {
        @Override
        public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.equals(newItem);
        }
    }
}
