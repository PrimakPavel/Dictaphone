package su.moy.chernihov.dictaphonev2app;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

public class MediaPlayerFragment extends Fragment {


    private static final String FORMAT_REC = ".3ga";
    private Button btn_play, btn_stop, btn_pause;
    private TextView tv_current, tv_duration, tv_file_name;
    private AudioPlayer audioPlayer;
    private Handler handler;
    private SeekBar seekBar;
    private Thread seekBarTime;
    private static final int MS_IN_SECOND = 1000;
    private File mFile;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (mFile == null) return;
        Uri uri = Uri.parse(mFile.getAbsolutePath());
        audioPlayer = new AudioPlayer(getActivity(), uri);
        audioPlayer.init();
        //audioPlayer.play(getActivity());
        handler = new Handler();
        // Запускаю нить для обновления полосы прокрутки.
        seekBarTime = new Thread(updateSeekBar);
        seekBarTime.start();
    }

    public static MediaPlayerFragment getInstance(File file) {
        MediaPlayerFragment player = new MediaPlayerFragment();
        player.setFile(file);
        return player;
    }

    private void setFile(File mFile) {
        this.mFile = mFile;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int duration = audioPlayer.getDuration();
        int currentPosition = audioPlayer.getCurrentPosition();
        // создаю вью
        View v = inflater.inflate(R.layout.fragment_media_player, container, false);

        // создаю два текстовых поля текущее время трека и общее время

        tv_current = (TextView) v.findViewById(R.id.tv_current_audio_player_fragment);
        tv_duration = (TextView) v.findViewById(R.id.tv_duration_audio_player_fragment);
        tv_file_name = (TextView) v.findViewById(R.id.tv_file_name_audio_player_fragment);


        tv_duration.setText(String.format("%02d:%02d:%02d", duration / 3600, (duration % 3600) / 60, duration % 60));
        tv_current.setText(String.format("%02d:%02d:%02d", currentPosition / 3600, (currentPosition % 3600) / 60, currentPosition % 60));
        tv_file_name.setText(mFile.getName().replace(FORMAT_REC,""));

        // создаю полосу прокрутки трека
        seekBar = (SeekBar) v.findViewById(R.id.seekBar_audio_player_fragment);
        seekBar.setMax(duration);
        seekBar.setProgress(currentPosition);
        //Назначаю слушателя изменения положения полосы прокрутки
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioPlayer.setPosition(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // кнопки играть, пауза, стоп
        btn_play = (Button) v.findViewById(R.id.btn_play_audio_player_fragment);
        btn_pause = (Button) v.findViewById(R.id.btn_pause_audio_player_fragment);
        btn_stop = (Button) v.findViewById(R.id.btn_stop_audio_player_fragment);


        // слушатель ИГРАТЬ
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // аудио плеер запустить
                audioPlayer.play();
                // вывести в текстовое поле длительность
                int duration = audioPlayer.getDuration();
                tv_duration.setText(String.format("%02d:%02d:%02d", duration / 3600, (duration % 3600) / 60, duration % 60));
                // установить для полосы прокрутки максимальное значение равное длительности
                seekBar.setMax(audioPlayer.getDuration());
            }
        });

        // слушатель ПАУЗА
        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // поставить плеер на паузу
                audioPlayer.pause();
            }
        });

        // Слушатель СТОП
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // стоп плеер
                audioPlayer.stop();
                // установить текст длительности равным нулю
                tv_duration.setText(getString(R.string.tv_time_zero_audio_player_fragment));
                tv_current.setText(getString(R.string.tv_time_zero_audio_player_fragment));
                seekBar.setProgress(0);
            }
        });


        return v;
    }


    public Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(MS_IN_SECOND / 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (audioPlayer != null && audioPlayer.isPlaying()) {
                            int currentPosition = audioPlayer.getCurrentPosition();
                            if (seekBar != null) {
                                seekBar.setProgress(currentPosition);
                            }
                            if (tv_current != null) {
                                tv_current.setText(String.format("%02d:%02d:%02d", currentPosition / 3600, (currentPosition % 3600) / 60, currentPosition % 60));
                            }
                        }
                    }
                });

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioPlayer.stop();
    }
    public void release() {
        if (audioPlayer != null)
        audioPlayer.stop();
    }


}




