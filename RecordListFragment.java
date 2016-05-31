package su.moy.chernihov.dictaphonev2app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class RecordListFragment extends Fragment {
    private static final String TAG = "RecordListActivityTag";
    private static final String FRAGMENT_TAG = "RecordListFragmentTag";
    private LabRecordFiles mLabRecordFiles;
    private ArrayList<File> mFilesList;
    private RecorderFileAdapter mAdapter;
    private ListView mListView;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        // библиотека файлов.
        mLabRecordFiles = LabRecordFiles.getInstance();

        // получаю список всех файлов
        mFilesList = mLabRecordFiles.getAudioFilesList();

        // настраиваю адаптер, передаю в него список файлов
        mAdapter = new RecorderFileAdapter(getContext(), mFilesList);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_record, container, false);
        mListView = (ListView) v.findViewById(R.id.record_fragment_list_view);
        // устанавливаю адаптер
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File recordFile = mAdapter.getItem(position);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                MediaPlayerFragment mediaPlayerFragment = (MediaPlayerFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
                if (mediaPlayerFragment == null) {
                    mediaPlayerFragment = MediaPlayerFragment.getInstance(recordFile);
                    fragmentTransaction.add(R.id.mediaPlayerFragmentContainer, mediaPlayerFragment, FRAGMENT_TAG)
                            .commit();
                } else {
                    mediaPlayerFragment.release();
                    fragmentTransaction.remove(mediaPlayerFragment);
                    mediaPlayerFragment = MediaPlayerFragment.getInstance(recordFile);
                    fragmentTransaction.add(R.id.mediaPlayerFragmentContainer, mediaPlayerFragment, FRAGMENT_TAG)
                            .commit();
                }
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                File recordFile = mAdapter.getItem(position);
                createDialogDelete(recordFile);
                return false;
            }
        });


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getActivity().getLayoutInflater().inflate(R.layout.empty_list_record, null);
        ((ViewGroup) mListView.getParent()).addView(view);
        mListView.setEmptyView(view);
        SharedPreferences prefs = DictaphoneFragment.getPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        if (mFilesList.size() == 0) {
            editor.clear();
        }
        editor.commit();
    }




    @Override
    public void onResume() {
        mFilesList = mLabRecordFiles.getAudioFilesList();
        mAdapter.notifyDataSetChanged();
        super.onResume();
        Log.d(TAG, "OnResume" + mFilesList.size());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause" + mFilesList.size());
    }

    private void createDialogDelete(final File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.title_dialog_delete))
                .setIcon(R.drawable.microphone_icon12)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.btn_yes_dialog_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                file.delete();
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                MediaPlayerFragment mediaPlayerFragment = (MediaPlayerFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
                                if (mediaPlayerFragment != null) {
                                    fragmentTransaction.remove(mediaPlayerFragment)
                                            .commit();
                                }
                                onResume();
                            }
                        }
                )
                .setNegativeButton(getString(R.string.btn_no_dialog_delete),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }




}
