package edu.ucsd.cse110.sharednotes.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimeService {
    // LiveData variable which contains the latest real time value.
    private final MutableLiveData<Long> realTimeData;

    // LiveData variable which contains the mocked time value (if there is one).
    private LiveData<Long> mockTimeData = null;

    // MediatorLiveData which we return to clients of this service.
    private final MediatorLiveData<Long> timeData;
    // ScheduledFuture result of scheduling a task with a ScheduledExecutorService
    private ScheduledFuture<?> clockFuture;
    // Singleton instance of TimeService
    private static TimeService instance;

    /**
     * @return a singleton instance of the TimeService class
     */
    public static TimeService singleton() {
        if (instance == null) {
            instance = new TimeService();
        }
        return instance;
    }

    /**
     * Constructor for TimeService
     */
    protected TimeService() {
        // Set up the real time value.
        realTimeData = new MutableLiveData<>();
        registerTimeListener();

        // Wrap it in a MediatorLiveData, which forwards the updates (for now).
        timeData = new MediatorLiveData<>();
        timeData.addSource(realTimeData, timeData::postValue);
    }

    /**
     * Registers a time listener using ScheduledExecutorService
     * which runs at an interval and updates the time value.
     */
    public void registerTimeListener() {
        var executor = Executors.newSingleThreadScheduledExecutor();
        clockFuture = executor.scheduleAtFixedRate(() -> {
            realTimeData.postValue(System.currentTimeMillis());
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Unregisters the time listener
     */
    public void unregisterTimeListener() {
        clockFuture.cancel(true);
    }

    /**
     * Mocks the time source instead of using the time listener
     */
    public void setMockTimeSource(MutableLiveData<Long> mockTimeData) {
        this.mockTimeData = mockTimeData;
        unregisterTimeListener();
        timeData.removeSource(realTimeData);
        timeData.addSource(mockTimeData, timeData::postValue);
    }

    /** Undoes the mock, restoring the original behavior. */
    public void clearMockTimeSource() {
        registerTimeListener();
        timeData.removeSource(mockTimeData);
        timeData.addSource(realTimeData, timeData::postValue);
    }

    public LiveData<Long> getTimeData() {
        return this.timeData;
    }
}