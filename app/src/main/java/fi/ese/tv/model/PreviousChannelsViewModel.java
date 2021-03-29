package fi.ese.tv.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class PreviousChannelsViewModel extends ViewModel {

    private static List<Video> list = new ArrayList<>();

    private MutableLiveData<List<Video>> channels;
    private static PreviousChannelsViewModel previousChannelsViewModel;

    public PreviousChannelsViewModel() {
        // get data
    }

    public static synchronized PreviousChannelsViewModel getInstance() {
        if ((previousChannelsViewModel == null)) {
            previousChannelsViewModel = new PreviousChannelsViewModel();
        }
        return previousChannelsViewModel;
    }

    public LiveData<List<Video>> getChannels() {
        if (channels == null)
            channels = new MutableLiveData<>();

        channels.setValue(list);
        return channels;
    }

    public void addChannel(Video video) {
        list.add(video);
    }

}