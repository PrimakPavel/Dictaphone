package su.moy.chernihov.dictaphonev2app;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;


public class DictaphoneActivity extends FragmentActivity {
    private static final String FRAGMENT_TAG = "RecordListFragmentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictaphone);


        if (savedInstanceState == null) { // очень нужная проверка!!!!! При поворотах экрана новый фрагмент не создается
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = new DictaphoneFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commit();
        }
        super.onBackPressed();


    }
}
