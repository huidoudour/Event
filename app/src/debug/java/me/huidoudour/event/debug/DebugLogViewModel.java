package me.huidoudour.event.debug;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebugLogViewModel extends AndroidViewModel {

    private final DebugLogDao dao;
    private final LiveData<List<DebugLogEntry>> allLogs;
    private final LiveData<Integer> logCount;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DebugLogViewModel(@NonNull Application application) {
        super(application);
        DebugLogDatabase db = DebugLogDatabase.getDatabase(application);
        dao = db.debugLogDao();
        allLogs = dao.getAllLogs();
        logCount = dao.getLogCount();
    }

    public LiveData<List<DebugLogEntry>> getAllLogs() {
        return allLogs;
    }

    public LiveData<Integer> getLogCount() {
        return logCount;
    }

    public List<DebugLogEntry> getAllLogsSync() {
        return dao.getAllLogsSync();
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public void refresh() {
        // LiveData 自动更新，无需手动刷新
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(DebugLogViewModel.class)) {
                return (T) new DebugLogViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
