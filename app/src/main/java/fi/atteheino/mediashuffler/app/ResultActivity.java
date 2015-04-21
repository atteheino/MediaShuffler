package fi.atteheino.mediashuffler.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;


public class ResultActivity extends Activity {

    private ArrayAdapter<SerializableMusicTrack> arrayAdapter;
    private Options options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get the options needed
        options = (Options) getIntent().getSerializableExtra("Options");
        arrayAdapter = new ArrayAdapter<SerializableMusicTrack>(this, android.R.layout.simple_list_item_1, options.getMusicTrackList());
        ListView resultListView = (ListView) findViewById(R.id.resultListView);
        resultListView.setAdapter(arrayAdapter);
        resultListView.setOnItemClickListener(resultListOnClickListener);
    }

    public void playMedia(Uri file) {
        Intent intent = new Intent();
        /*ComponentName comp = new ComponentName("com.android.music", "com.android.music.MediaPlaybackActivity");
        intent.setComponent(comp);
        */
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(file, "audio/*");
        startActivity(intent);
    }

    private ListView.OnItemClickListener resultListOnClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String filename = options.getMusicTrackList().get(i).getFullFilename();
            File file = new File(options.getTargetFolderName(), filename);
            playMedia(Uri.fromFile(file));
        }
    };

    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
