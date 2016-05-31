package su.moy.chernihov.dictaphonev2app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DictaphoneFragment extends Fragment {
    private static final String TAG = "DictaphoneFragment";
    private static final String FORMAT_REC = ".3ga";
    private static final SimpleDateFormat format = new SimpleDateFormat("yy.MM.dd' 'HH:mm");
    public static final String DICTAPHONE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()
                                                + "/"
                                                + Environment.DIRECTORY_MUSIC
                                                + "/Dictaphone";
    private static final int MS_IN_SECOND = 1000;

    private DictaphoneRecorder mRecorder;
    private String mFileName;
    private LinearLayout twoButtonLayout, threeButtonLayout;
    private Button btnList, btnStartRec;
    private Button  btnStop, btnPause, btnCancel;
    private TextView tvTime, tvName;
    private Thread mUpdateThread;
    private Handler mHandler;
    private int mRecordCount;
    private static SharedPreferences prefs;
    private ArrayList<String> mMergeList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // включаю сохранение фрагментов
        setRetainInstance(true);
        setHasOptionsMenu(true);
        // поток для вывода инфи в пользовательский интерфейс
        mHandler = new Handler();
        // объект для сохранения пар ключ-значение
        prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        // инициализирую лист имен файлов для объединения
        mMergeList = new ArrayList<>();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // получаю объект вью
        View v = inflater.inflate(R.layout.fragment_dictaphone, container, false);
        // лейауты для кнопок
        twoButtonLayout = (LinearLayout) v.findViewById(R.id.layout_two_btn_dictaphone_fragment);
        threeButtonLayout = (LinearLayout) v.findViewById(R.id.layout_three_btn_dictaphone_fragment);
        // текстовые поля
        // текущее время
        tvTime = (TextView) v.findViewById(R.id.tv_time_dictaphone_fragment);
        // текущее имя файла
        tvName = (TextView) v.findViewById(R.id.tv_name_dictaphone_fragment);

        // кнопка включения записи диктофона
        btnStartRec = (Button) v.findViewById(R.id.btn_start_rec_dictaphone_fragment);
        btnStartRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // кнопки список и старт НЕ видны
                twoButtonLayout.setVisibility(View.INVISIBLE);
                // кнопки Стоп, Пауза/Старт, Отмена видны
                threeButtonLayout.setVisibility(View.VISIBLE);
                // если запись до этого велась, то завершить запись без сохранения
                if (mRecorder != null) {
                    mRecorder.stopRecording(false);
                    mRecorder = null;
                }
                // очищаю лист имен файлов для объединения
                mMergeList.clear();

                // если можна создать директорию для диктофона на СД карте то:
                if (isFileExists(DICTAPHONE_DIR)) {

                    //старт новой записи в новый файл
                    // генерирую имя файла
                    mFileName = createFileName();
                    // сохраняю текущую дату для данного именя файла в префах
                    saveDateToSharedPreferences(mFileName);
                    // создаю файл
                    File file = new File(DICTAPHONE_DIR + "/" + mFileName);
                    // создаю рекордер
                    mRecorder = new DictaphoneRecorder(file);
                    // стартую рекордер
                    mRecorder.startRecording();
                    // вывожу название текущего файла
                    tvName.setText(mFileName.replace(FORMAT_REC,""));
                }
                // в противном случае вывожу тост
                else {
                    Toast.makeText(getContext(), getString(R.string.toast_sd_card_not_available), Toast.LENGTH_LONG).show();
                    return;
                }


            }
        });

        // кнопка вывода листа записей
        btnList = (Button) v.findViewById(R.id.btn_list_dictaphone_fragment);
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // вывожу лист записаных файлов
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment listFragment = new RecordListFragment();
                fm.beginTransaction().replace(R.id.fragmentContainer, listFragment).
                        addToBackStack(null).
                        commit();

            }
        });


        // кнопка остановки записи
        btnStop = (Button) v.findViewById(R.id.btn_stop_dictaphone_fragment);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // кнопки Стоп, Пауза/Старт, Отмена не видны
                threeButtonLayout.setVisibility(View.INVISIBLE);
                // кнопки список и старт видны
                twoButtonLayout.setVisibility(View.VISIBLE);
                // обнулить время
                tvTime.setText(getString(R.string.tv_time_zero_dictaphone_fragment));
                tvTime.setTextColor(getResources().getColor(R.color.white));
                // если запись до этого велась, , вывести тост
                if (mRecorder != null) {
                    //завершить запись, сохранить файл
                    mRecorder.stopRecording(true);
                    // если есть файлы для объединения, то делаю это
                    mergeFilesFromMergeList();
                    // очищаю лист
                    mMergeList.clear();
                    // сообщаю тостом файл сохранен
                    Toast.makeText(getContext(), getString(R.string.toast_file_save), Toast.LENGTH_LONG).show();
                    mRecorder = null;
                }
            }
        });
        // кнопка пауза
        btnPause = (Button) v.findViewById(R.id.btn_pause_start_dictaphone_fragment);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // если
                if (mRecorder == null) return;

                if (mRecorder.isRecord()) {
                    mRecorder.pauseRecording();
                    btnPause.setText(getString(R.string.btn_record_dictaphone_fragment));
                    // значок на кнопке слева от текста
                    btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fiber_manual_record_black_24dp, 0, 0, 0);
                    tvTime.setTextColor(getResources().getColor(R.color.white));
                    mMergeList.add(mFileName);
                } else {
                    mFileName = createFileName();
                    if (isFileExists(DICTAPHONE_DIR)) {
                        File file = new File(DICTAPHONE_DIR + "/" + mFileName);
                        mRecorder.unPauseRecording(file);
                    }
                    btnPause.setText(getString(R.string.btn_pause_dictaphone_fragment));
                    // значок на кнопке слева от текста
                    btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_circle_filled_black_24dp, 0, 0, 0);

                }
            }
        });

        // кнопка отмена
        btnCancel = (Button) v.findViewById(R.id.btn_cancel_dictaphone_fragment);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialogCancel();
            }
        });

        // отрабатываем повороты экрана
        if (mRecorder != null) {
            tvName.setText(mFileName);
            if (mRecorder.isRecord() || mRecorder.isPause()) {
                if (mRecorder.isPause()) {
                    btnPause.setText(getString(R.string.btn_record_dictaphone_fragment));
                    // значок на кнопке слева от текста
                    btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fiber_manual_record_black_24dp, 0, 0, 0);
                }
                else {btnPause.setText(getString(R.string.btn_pause_dictaphone_fragment));
                    // значок на кнопке слева от текста
                    btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_circle_filled_black_24dp, 0, 0, 0);}

                twoButtonLayout.setVisibility(View.INVISIBLE);
                threeButtonLayout.setVisibility(View.VISIBLE);
            } else {
                btnPause.setText(getString(R.string.btn_record_dictaphone_fragment));

                // значок на кнопке слева от текста
                btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fiber_manual_record_black_24dp, 0, 0, 0);
                twoButtonLayout.setVisibility(View.VISIBLE);
                threeButtonLayout.setVisibility(View.INVISIBLE);
            }
        }
        // создаю поток который будет отслеживать изменения tvTime
        mUpdateThread = new Thread(dataUpdate());
        mUpdateThread.start();
        return v;
    }



    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStart() {
        super.onStart();
        mRecordCount = LabRecordFiles.returnMaxNumOfRecord();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecorder) {
            mRecorder.stopRecording(false);
            mRecorder = null;
        }

    }


    protected static SharedPreferences getPrefs() {
        return prefs;
    }



    Runnable dataUpdate() {
        return new Runnable() {
            @Override
            public void run() {
                if (mRecorder != null) {
                    int seconds = (int) mRecorder.getDurationMs()/ MS_IN_SECOND;
                    tvTime.setText(String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60));
                    if (mRecorder.isRecord())
                        tvTime.setTextColor(getResources().getColor(R.color.red));
                    else
                        tvTime.setTextColor(getResources().getColor(R.color.white));

                }
                mHandler.postDelayed(this, MS_IN_SECOND / 2);
            }
        };
    }

    private void mergeFilesFromMergeList() {
        if (!mMergeList.contains(mFileName)) {
            mMergeList.add(mFileName);
        }
        if (mMergeList.size() > 1) {
            try {
                String[] fileNameArray = new String[mMergeList.size()];
                for(int i = 0; i < fileNameArray.length; i++){
                    fileNameArray[i] = DICTAPHONE_DIR  + "/" + mMergeList.get(i);
                }
                String fileName =  mergeAllFilesToOneAndDeleteOther(fileNameArray);
                saveDurationToSharedPreferences(fileName);
            } catch (IOException e) {}
        }
    }

    private void saveDurationToSharedPreferences(String fileName) {
        prefs = DictaphoneFragment.getPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(fileName + "duration", (int) mRecorder.getDurationMs() / MS_IN_SECOND);
        editor.commit();
    }
    private void saveDateToSharedPreferences(String fileName) {
        Date date = new Date(System.currentTimeMillis());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(fileName + "date", format.format(date));
        editor.commit();
    }

    private boolean isFileExists(String path) {
        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(getContext(), getString(R.string.toast_file_not_save), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private String createFileName() {

        mRecordCount++;
        return getString(R.string.btn_record_dictaphone_fragment) + " " + mRecordCount + FORMAT_REC;
    }

    private void createDialogCancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.title_dialog_cancel))
                //.setMessage(getString(R.string.question_dialog_cancel))
                .setIcon(R.drawable.microphone_icon12)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.btn_yes_dialog_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                threeButtonLayout.setVisibility(View.INVISIBLE);
                                // кнопка пауза вид по умолчанию
                                btnPause.setText(getString(R.string.btn_pause_dictaphone_fragment));
                                // значок на кнопке слева от текста
                                btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_circle_filled_black_24dp, 0, 0, 0);
                                // кнопки список и старт видны
                                twoButtonLayout.setVisibility(View.VISIBLE);

                                // обнулить время
                                tvTime.setText(getString(R.string.tv_time_zero_dictaphone_fragment));
                                // если запись до этого велась, то завершить запись без сохранения
                                if (mRecorder != null) {
                                    mRecorder.stopRecording(false);
                                    deleteAllFilesWithNameFromMergeList();
                                    mMergeList.clear();
                                    Toast.makeText(getContext(), getString(R.string.toast_file_not_save), Toast.LENGTH_LONG).show();
                                    mRecorder = null;
                                }

                            }
                        }
                )
                .setNegativeButton(getString(R.string.btn_no_dialog_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }



    private String mergeAllFilesToOneAndDeleteOther(String... filePath) throws IOException {

        List<Movie> movies = new ArrayList<>();
        for (String fileName: filePath){
            movies.add(MovieCreator.build(fileName));
        }
        final Movie finalMovie = new Movie();

        List<Track> audioTracks = new ArrayList<>();
        for (Movie movie : movies) {
            for (Track track : movie.getTracks()) {
                if (track.getHandler().equals("soun")) {
                    audioTracks.add(track);
                }
            }
        }
        finalMovie.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));

        final Container container = new DefaultMp4Builder().build(finalMovie);
        File mergedFile = new File(DICTAPHONE_DIR + "/merge" + FORMAT_REC);
        final FileOutputStream fos = new FileOutputStream(mergedFile);
        FileChannel fc = new RandomAccessFile(mergedFile, "rw").getChannel();
        container.writeContainer(fc);
        fc.close();
        fos.close();
        String firstFilePath = filePath[0];
        // записую в первый файл данные из остальных
        mergedFile.renameTo(new File(firstFilePath));
        // удаляю остальные файлы
        for (int i = 1; i < filePath.length; i++) {
            File file = new File(filePath[i]);
            if (file.exists())
                file.delete();
        }
        // возвращаю имя исходного файла (все сохранено)
        return new File(firstFilePath).getName();


    }

    private void deleteAllFilesWithNameFromMergeList() {
        String[] filePaths = new String[mMergeList.size()];
        for(int i = 0; i < filePaths.length; i++){
            filePaths[i] = DICTAPHONE_DIR  + "/" + mMergeList.get(i);
        }
        for (String filePath: filePaths) {
            File file = new File(filePath);
            if (file.exists())
                file.delete();
        }
    }


}
