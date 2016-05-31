package su.moy.chernihov.dictaphonev2app;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;


public class AudioPlayer  {
    private MediaPlayer mediaPlayer;
    private Context mContext;
    private static final String TAG = "Media_Player";
    private static final int MS_IN_SECOND = 1000;
    private Uri mUri;
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public AudioPlayer(Context context,Uri uri) {
    mUri = uri;
    mContext = context;
    }

    public void play () {
        stop();
        init();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
        mediaPlayer.start();
    }
    public void init() {
        mediaPlayer = MediaPlayer.create(mContext, mUri);
    }


    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            return;
        }
        if (mediaPlayer != null && !mediaPlayer.isPlaying())
            mediaPlayer.start();
    }



    public int getCurrentPosition() {
        if (mediaPlayer == null) {
            //Log.d(TAG," not create. Method: getCurrentPosition");
            return 0;
        }
        return mediaPlayer.getCurrentPosition()/MS_IN_SECOND;
    }
    public int getDuration() {
        if (mediaPlayer == null) {
            //Log.d(TAG," not create. Method: getDuration");
            return 0;
        }
        return mediaPlayer.getDuration()/MS_IN_SECOND;
    }
    public boolean isPlaying() {
        if (mediaPlayer == null) {
            //Log.d(TAG," not create. Method: isPlaying");
            return false;
        }
        return mediaPlayer.isPlaying();
    }
    public void setPosition(int position) {
        if (mediaPlayer == null) {
            //Log.d(TAG," not create. Method: setPosition");
            return;
        }
        mediaPlayer.seekTo(position);
    }
}

