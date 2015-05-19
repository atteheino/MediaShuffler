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
