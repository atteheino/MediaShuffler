package fi.atteheino.mediashuffler.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import fi.atteheino.mediashuffler.app.Folder;
import fi.atteheino.mediashuffler.app.R;

/**
 * Created by Atte on 18.10.2014.
 */
public class FolderSelectArrayAdapter extends ArrayAdapter<Folder> {
    public FolderSelectArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.folder_select_array_adapter, null);
        }
        final Folder folder = getItem(position);
        TextView textView = (TextView) convertView.findViewById(R.id.folderNameTextView);
        textView.setText(folder.getName());
        TextView folderIdTextView = (TextView) convertView.findViewById(R.id.folderIdTextView);
        folderIdTextView.setText(folder.getId());
        return convertView;
    }
}
