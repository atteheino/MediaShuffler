package fi.atteheino.mediashuffler.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.codeproject.directorychooser.DirectoryChooserDialog;


public class MainActivity extends Activity {
    private int selectedSizeOfCollection=0;
    private Options options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        options = new Options();

        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.sizeOfCollection);
        if(numberPicker!=null){
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(100);
            numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                    selectedSizeOfCollection = i2;
                }
            });
        }

        Button sourceButton = (Button) findViewById(R.id.selectSourceButton);
        sourceButton.setOnClickListener(sourceButtonListener);

        Button sourceFolderButton = (Button) findViewById(R.id.selectSourceFolderButton);
        sourceFolderButton.setOnClickListener(sourceFolderButtonListener);

        Button targetFolderButton = (Button) findViewById(R.id.selectTargetFolderButton);
        targetFolderButton.setOnClickListener(targetFolderButtonListener);

    }

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
        private String m_chosenDir = "";
        private boolean m_newFolderEnabled = true;

        @Override
        public void onClick(View view) {
            // Create DirectoryChooserDialog and register a callback
            DirectoryChooserDialog directoryChooserDialog =
                    new DirectoryChooserDialog(MainActivity.this,
                            new DirectoryChooserDialog.ChosenDirectoryListener() {
                                @Override
                                public void onChosenDir(String chosenDir) {
                                    m_chosenDir = chosenDir;
                                    options.setTargetFolderName(chosenDir);
                                    setTargetFolderLabel(chosenDir);
                                }
                            }
                    );
            // Toggle new folder button enabling
            directoryChooserDialog.setNewFolderEnabled(m_newFolderEnabled);
            // Load directory chooser dialog for initial 'm_chosenDir' directory.
            // The registered callback will be called upon final directory selection.
            directoryChooserDialog.chooseDirectory(m_chosenDir);
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent()!=null) {
            if (getIntent().getSerializableExtra("Options") != null) {
                options = (Options) getIntent().getSerializableExtra("Options");
            }

            if (options.getDLNADevice()!=null) {
                TextView sourceLabel = (TextView) findViewById(R.id.sourceLabel);
                sourceLabel.setText(options.getDLNADevice() + "(" + options.getDLNADeviceUDN() + ")");
            }

            if (options.getSourceFolderName() != null && options.getSourceFolderID() != null) {
                TextView sourceFolderLabel = (TextView) findViewById(R.id.sourcePathLabel);
                sourceFolderLabel.setText(options.getSourceFolderName() + "(" + options.getSourceFolderID() + ")");
            }
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

