package me.huidoudour.event.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import me.huidoudour.event.R;
import me.huidoudour.event.data.Event;

public class HomeActivity extends AppCompatActivity {
    private EventViewModel viewModel;
    private RecyclerView recyclerView;
    private View emptyView;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        FloatingActionButton fabAddEvent = findViewById(R.id.fabAddEvent);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, new EventViewModel.Factory(getApplication()))
                .get(EventViewModel.class);

        // Setup RecyclerView
        adapter = new EventAdapter(
            event -> { /* Click handled by ripple effect */ },
            (event, view) -> showLongClickMenu(event, view)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Observe events
        viewModel.getAllEvents().observe(this, events -> {
            adapter.submitList(events);
            if (events.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        fabAddEvent.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event, null);
        TextInputEditText editTitle = dialogView.findViewById(R.id.editEventTitle);
        TextInputEditText editDescription = dialogView.findViewById(R.id.editEventDescription);

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_event)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
                String description = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";

                if (!title.isEmpty()) {
                    viewModel.addEvent(title, description, System.currentTimeMillis());
                    Toast.makeText(this, R.string.event_saved, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showEventDetail(Event event) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event, null);
        TextInputEditText editTitle = dialogView.findViewById(R.id.editEventTitle);
        TextInputEditText editDescription = dialogView.findViewById(R.id.editEventDescription);

        editTitle.setText(event.getTitle());
        editDescription.setText(event.getDescription() != null ? event.getDescription() : "");
        
        // Disable editing for detail view
        editTitle.setEnabled(false);
        editDescription.setEnabled(false);

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.event_detail)
            .setView(dialogView)
            .setPositiveButton(R.string.ok, null)
            .setNeutralButton(R.string.edit, (dialog, which) -> showEditDialog(event))
            .setNegativeButton(R.string.delete, (dialog, which) -> showDeleteConfirmDialog(event))
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
                String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
                String description = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";

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
                    // 查看详情
                    showEventDetail(event);
                } else if (which == 1) {
                    // 编辑
                    showEditDialog(event);
                } else if (which == 2) {
                    // 删除
                    showDeleteConfirmDialog(event);
                }
            })
            .show();
    }

    private void showDeleteConfirmDialog(Event event) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null);
        TextInputEditText editInput = dialogView.findViewById(R.id.editConfirmInput);

        new MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                String input = editInput.getText() != null ? editInput.getText().toString().trim() : "";
                if ("del".equals(input)) {
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_clear, null);
        TextInputEditText editInput = dialogView.findViewById(R.id.editClearInput);

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clear_all)
            .setView(dialogView)
            .setPositiveButton(R.string.clear_all, (dialog, which) -> {
                String input = editInput.getText() != null ? editInput.getText().toString().trim() : "";
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
