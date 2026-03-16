package me.huidoudour.event.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.huidoudour.event.R;
import me.huidoudour.event.data.Event;

public class EventAdapter extends ListAdapter<Event, EventAdapter.EventViewHolder> {
    private final OnItemClickListener listener;
    private final OnItemLongClickListener longListener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Event event, View view);
    }

    public EventAdapter(OnItemClickListener listener, OnItemLongClickListener longListener) {
        super(new EventDiffCallback());
        this.listener = listener;
        this.longListener = longListener;
    }

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

        EventViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textEventTitle);
            textDescription = itemView.findViewById(R.id.textEventDescription);
            textTime = itemView.findViewById(R.id.textEventTime);
            cardView = (MaterialCardView) itemView;
        }

        void bind(Event event) {
            textTitle.setText(event.getTitle());

            // 显示描述文本
            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                textDescription.setText(event.getDescription());
                textDescription.setVisibility(View.VISIBLE);
            } else {
                textDescription.setVisibility(View.GONE);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            textTime.setText(dateFormat.format(new Date(event.getEventTime())));

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(event);
                }
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
