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


    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent()!=null) {
            if (getIntent().getSerializableExtra("Options") != null) {
                options = (Options) getIntent().getSerializableExtra("Options");
            }

            if (options.getDLNADevice()!=null) {
                TextView sourceLable = (TextView) findViewById(R.id.sourceLabel);
                sourceLable.setText(options.getDLNADevice()+"("+options.getDLNADeviceUDN()+")");
            }
        }
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

