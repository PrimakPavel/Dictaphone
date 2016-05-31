package su.moy.chernihov.dictaphonev2app;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class RecorderFileAdapter extends ArrayAdapter<File> {
    private Context mContext;
    private LayoutInflater inflater;

    public RecorderFileAdapter(Context context, ArrayList<File> recordFilesList) {
        super(context, 0, recordFilesList);
        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_record_file, null);
        }

        File recordFile = getItem(position);
        String fileName = recordFile.getName();
        // file name
        TextView recordNameTextView = (TextView) convertView.findViewById(R.id.record_file_item_list_file_name);
        recordNameTextView.setText(fileName.replace(".3ga", ""));
        // file date
        TextView recordDateTextView = (TextView) convertView.findViewById(R.id.record_file_item_list_file_date);
        SharedPreferences prefs = DictaphoneFragment.getPrefs();

        recordDateTextView.setText(prefs.getString(fileName + "date", "not Date"));
        TextView recordDurationTextView = (TextView) convertView.findViewById(R.id.record_file_item_list_file_duration);
        int duration = prefs.getInt(fileName + "duration", 0);
        recordDurationTextView.setText(String.format("%02d:%02d", (duration % 3600) / 60, duration % 60));
        return convertView;
    }
}



