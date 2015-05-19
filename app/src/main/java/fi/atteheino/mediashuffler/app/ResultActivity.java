/*
 * Copyright 2015 Atte Heino
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package fi.atteheino.mediashuffler.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
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
    private NotificationManager mNotifyManager;

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

        // Hide the notification if user hasn't clicked it
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.cancelAll();
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
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
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
