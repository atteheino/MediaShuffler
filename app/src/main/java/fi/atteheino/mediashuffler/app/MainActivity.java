package fi.atteheino.mediashuffler.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends Activity {
    private int selectedSizeOfCollection = 0;
    private Options options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        options = new Options();

        final TextView sizeOfCollectionText = (TextView) findViewById(R.id.sizeOfCollectionText);
        SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax(30);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                final float gigs = i / (float) 10;
                sizeOfCollectionText.setText(gigs + "GB");
                selectedSizeOfCollection = i * 1000;
                options.setTargetSizeMegaBytes(selectedSizeOfCollection);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        Button sourceButton = (Button) findViewById(R.id.selectSourceButton);
        sourceButton.setOnClickListener(sourceButtonListener);

        Button sourceFolderButton = (Button) findViewById(R.id.selectSourceFolderButton);
        sourceFolderButton.setOnClickListener(sourceFolderButtonListener);

        Button targetFolderButton = (Button) findViewById(R.id.selectTargetFolderButton);
        targetFolderButton.setOnClickListener(targetFolderButtonListener);

        Button startTransferButton = (Button) findViewById(R.id.startTransferButton);
        startTransferButton.setOnClickListener(startTransferButtonListener);

    }

    private View.OnClickListener startTransferButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent startTransferIntent = new Intent(getApplicationContext(), ShuffleActivity.class);
            startTransferIntent.putExtra("Options", options);
            startActivity(startTransferIntent);
        }
    };

    private View.OnClickListener sourceButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent browserActivityIntent = new Intent(getApplicationContext(), BrowserActivity.class);
            browserActivityIntent.putExtra("Options", options);
            startActivity(browserActivityIntent);
        }
    };

    private View.OnClickListener sourceFolderButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent sourceFolderSelectActivity = new Intent(getApplicationContext(), SourceFolderSelectActivity.class);
            sourceFolderSelectActivity.putExtra("Options", options);
            startActivity(sourceFolderSelectActivity);
        }
    };

    private View.OnClickListener targetFolderButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            Intent targetFolderSelectActivity = new Intent(getApplicationContext(), TargetFolderSelectActivity.class);
            targetFolderSelectActivity.putExtra("Options", options);
            startActivity(targetFolderSelectActivity);


        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        // If we are returning to this activity, let's load settings from Intent
        if (getIntent() != null) {

            if (getIntent().getSerializableExtra("Options") != null) {
                options = (Options) getIntent().getSerializableExtra("Options");
            } else //This is first run. Let's see if we have pre saved settings.
            {
                //Get shared preferences
                SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, 0);
                boolean isMediaserverSet = settings.getBoolean(Constants.MEDIASERVER_SET, false);
                boolean isSourceSet = settings.getBoolean(Constants.SOURCE_SET, false);
                boolean isTargetSet = settings.getBoolean(Constants.TARGET_SET, false);

                if (isMediaserverSet) {
                    options.setDLNADevice(settings.getString(Constants.MEDIASERVER_NAME, ""));
                    options.setDLNADeviceUDN(settings.getString(Constants.MEDIASERVER_UDN, ""));
                }

                if (isSourceSet) {
                    options.setSourceFolderName(settings.getString(Constants.SOURCE_FOLDER_NAME, ""));
                    options.setSourceFolderID(settings.getString(Constants.SOURCE_FOLDER_ID, ""));
                }

                if (isTargetSet) {
                    options.setTargetFolderName(settings.getString(Constants.TARGET_FOLDER, ""));
                }

            }

        }
        //Let's show the selected values to user.
        setDisplayValuesForOptions();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        if (options.getDLNADevice() != null && options.getDLNADeviceUDN() != null) {
            editor.putBoolean(Constants.MEDIASERVER_SET, true);
            editor.putString(Constants.MEDIASERVER_NAME, options.getDLNADevice());
            editor.putString(Constants.MEDIASERVER_UDN, options.getDLNADeviceUDN());
            editor.apply();
        }
        if (options.getSourceFolderID() != null && options.getSourceFolderName() != null) {
            editor.putBoolean(Constants.SOURCE_SET, true);
            editor.putString(Constants.SOURCE_FOLDER_NAME, options.getSourceFolderName());
            editor.putString(Constants.SOURCE_FOLDER_ID, options.getSourceFolderID());
            editor.apply();
        }
        if (options.getTargetFolderName() != null) {
            editor.putBoolean(Constants.TARGET_SET, true);
            editor.putString(Constants.TARGET_FOLDER, options.getTargetFolderName());
            editor.apply();
        }
    }

    private void setDisplayValuesForOptions() {
        if (options.getDLNADevice() != null) {
            TextView sourceLabel = (TextView) findViewById(R.id.sourceLabel);
            sourceLabel.setText(options.getDLNADevice());
        }

        if (options.getSourceFolderName() != null && options.getSourceFolderID() != null) {
            TextView sourceFolderLabel = (TextView) findViewById(R.id.sourcePathLabel);
            sourceFolderLabel.setText(options.getSourceFolderName());
        }

        if (options.getTargetSizeMegaBytes() > 0) {
            SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
            seekbar.setProgress(options.getTargetSizeMegaBytes() / 1000);
        }

        if (options.getTargetFolderName() != null) {
            TextView targetFolderLabel = (TextView) findViewById(R.id.targetPathLabel);
            targetFolderLabel.setText(options.getTargetFolderName());
        }
    }

    private void setTargetFolderLabel(String selectedDir) {
        TextView selectedTargetDirLabel = (TextView) findViewById(R.id.targetPathLabel);
        selectedTargetDirLabel.setText(selectedDir);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

