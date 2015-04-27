package fi.atteheino.mediashuffler.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TargetFolderSelectActivity extends Activity {

    private Options options;
    private String mCurrentRootFolder = "";
    private List<String> mFolders;
    private ArrayAdapter<String> mAdapter;
    private final String sdRoot = Environment.getExternalStorageDirectory().getAbsolutePath();

    //Return root directory or currently selected directory
    public String getmCurrentRootFolder() {
        return mCurrentRootFolder.equals("") ? sdRoot : mCurrentRootFolder;
    }

    public void setmCurrentRootFolder(String mCurrentRootFolder) {
        this.mCurrentRootFolder = mCurrentRootFolder;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_folder_select);
        // Get the options needed
        options = (Options) getIntent().getSerializableExtra("Options");
        mFolders = getFolders(getmCurrentRootFolder());
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mFolders);

        ListView listView = (ListView) findViewById(R.id.targetFolderListView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mMessageClickedHandler);
        listView.setOnItemLongClickListener(itemLongClickListener);

    }


    // Create a message handling object as an anonymous class.
    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {

            TextView idView = (TextView) v.findViewById(android.R.id.text1);
            setmCurrentRootFolder(getmCurrentRootFolder() + "/" + idView.getText());
            updateDirectories();

        }
    };

    private void updateDirectories() {
        mFolders.clear();
        mFolders.addAll(getFolders(getmCurrentRootFolder()));
        mAdapter.notifyDataSetChanged();
    }

    private AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            final TextView textView = (TextView) view.findViewById(android.R.id.text1);
            options.setTargetFolderName(getmCurrentRootFolder() + "/" + textView.getText());
            mainActivityIntent.putExtra("Options", options);
            startActivity(mainActivityIntent);
            return true;
        }
    };


    private List<String> getFolders(String rootFolder) {
        List<String> dirs = new ArrayList<String>();

        try {
            File dirFile = new File(rootFolder);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            for (File file : dirFile.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file.getName());
                }
            }
        } catch (Exception e) {
        }

        Collections.sort(dirs, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return dirs;

    }

    /**
     * Boolean method to control up navigation. We check that if parent is same as the folder that has been requested
     * to be navigated to, then we actually navigate back to home screen. Else we navigate up to the parent directory
     *
     * @return true if should navigate up to main activity
     */

    private boolean shouldNavigateUp() {
        final String parent = new File(getmCurrentRootFolder()).getParent();
        //If the current folder equals to External Storage Directory, then we must back out to main activity
        if (sdRoot.equals(getmCurrentRootFolder())) {
            return true;
        } else {
            setmCurrentRootFolder(parent);
            updateDirectories();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (shouldNavigateUp()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigateUp() {
        if (shouldNavigateUp()) {
            return super.onNavigateUp();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_target_folder_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
