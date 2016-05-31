package su.moy.chernihov.dictaphonev2app;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class LabRecordFiles {
    public static final String DICTAPHONE_DIR = Environment.DIRECTORY_MUSIC + "/Dictaphone";
    private static LabRecordFiles mLabs;
    private ArrayList<File> mAudioFilesList;


    private LabRecordFiles() {
        mAudioFilesList = new ArrayList<>();
        refreshFileList();
    }

    public static LabRecordFiles getInstance() {
        if (mLabs == null) {
            mLabs = new LabRecordFiles();
        }
        return mLabs;
    }
    public static int returnMaxNumOfRecord (){
        File dir = Environment.getExternalStoragePublicDirectory(DICTAPHONE_DIR);
        int maxNum = 0;
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                String tempStr = file.getName().replace("Запись ", "").replace(".3ga", "");
                try {
                    int temp = Integer.parseInt(tempStr);
                    if (maxNum < temp)
                        maxNum = temp;
                }
                catch (NumberFormatException e){}

            }
        }
        return maxNum;
    }


    public ArrayList<File> getAudioFilesList() {
        refreshFileList();
        return mAudioFilesList;
    }


    private void refreshFileList() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File filesDirectory = Environment.getExternalStoragePublicDirectory(DICTAPHONE_DIR);
            listFilesFromDir(filesDirectory);
        }
    }

    private void listFilesFromDir(File dir) {
        mAudioFilesList.clear();
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                mAudioFilesList.add(file);
            }
        }
    }

}




