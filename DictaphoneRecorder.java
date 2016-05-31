package su.moy.chernihov.dictaphonev2app;

import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;


public class DictaphoneRecorder implements Serializable {
    static final String TAG = "DictaphoneRecorder";
    static final int MS_IN_SECOND = 1000;
    private MediaRecorder mRecorder;
    private boolean isRecord = false;
    private boolean isPause = false;
    private String mFilePath;
    private String mFileName;
    private long mStartTime;
    private long mDurationMs;


    public DictaphoneRecorder(File file) {
        mFilePath = file.getAbsolutePath();
        mFileName = file.getName();
        mStartTime = 0;
        mDurationMs = 0;

    }


    public boolean isRecord() {
        return isRecord;
    }
    public boolean isPause() {
        return isPause;
    }


    public void startRecording() {

        mRecorder = new MediaRecorder();
        initRecorder();
        mRecorder.setOutputFile(mFilePath);
        try {
            mRecorder.prepare();
            mRecorder.start();

            mStartTime = System.currentTimeMillis();
            mDurationMs = 0;
            isRecord = true;
            isPause = false;
        } catch (IOException e) {
            Log.e(TAG, "Couldn't prepare and start MediaRecorder");
        }

    }

    public void pauseRecording() {
        if (mRecorder != null) {
            mDurationMs = System.currentTimeMillis() - mStartTime + mDurationMs;
            mRecorder.stop();
            mStartTime = 0;
            mRecorder.release();
            mRecorder = null;
            isRecord = false;
            isPause = true;
        }
    }

    public void unPauseRecording(File filePath) {
        mRecorder = new MediaRecorder();
        initRecorder();
        mFilePath = filePath.getAbsolutePath();
        mFileName = filePath.getName();
        mRecorder.setOutputFile(mFilePath);
        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartTime = System.currentTimeMillis();
            isRecord = true;
            isPause = false;
        } catch (IOException e) {}


    }


    // Stop recording. Release resources
    public void stopRecording(boolean isSave) {

        if (null != mRecorder) {
            mRecorder.stop();

            mDurationMs = System.currentTimeMillis() - mStartTime + mDurationMs;
            mStartTime = 0;

            if (!isSave) {
                deleteCurrentFile();
            }

            mRecorder.release();
            mRecorder = null;
            isRecord = false;
            isPause = false;
        }
        saveDurationToSharedPreferences();

    }

    private void saveDurationToSharedPreferences() {
        SharedPreferences prefs = DictaphoneFragment.getPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(mFileName + "duration", (int) mDurationMs / MS_IN_SECOND);
        editor.commit();
    }

    public boolean deleteCurrentFile() {
        File file = new File(mFilePath);
        if (file != null && file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    private void initRecorder() {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }
    public long getDurationMs() {
        if (isRecord)
            return System.currentTimeMillis() - mStartTime + mDurationMs;
        else
            return mDurationMs;
    }


}

