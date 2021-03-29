package fi.ese.tv.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import fi.ese.tv.R;

public class EpgActivity extends LeanbackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.epg_fragment);
        getWindow().setBackgroundDrawableResource(R.drawable.grid_bg);
    }
}
