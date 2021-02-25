package net.veldor.todo.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import net.veldor.todo.R;
import net.veldor.todo.utils.Preferences;
import net.veldor.todo.utils.TimePickerDialog;
import net.veldor.todo.utils.TimePreference;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Log.d("surprise", "SettingsActivity onCreate 25: enable action bar");
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState == null) {
            PreferenceFragment preferenceFragment = new PreferenceFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.pref_container, preferenceFragment);
            ft.commit();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // And override this method
    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }

    public static class PreferenceFragment extends PreferenceFragmentCompat {

        private static final String DIALOG_FRAGMENT_TAG = "TimePickerDialog";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                PreferenceScreen rootScreen = getPreferenceManager().createPreferenceScreen(getActivity());
                setPreferenceScreen(rootScreen);

                SwitchPreference doNotDisturbPref = new SwitchPreference(activity);
                doNotDisturbPref.setKey(Preferences.KEY_DO_NOT_DISTURB);
                doNotDisturbPref.setSummary("Отключает все уведомления и периодические проверки новых и непринятых задач. Не рекомендуется использовать");
                doNotDisturbPref.setTitle("Не беспокоить");
                doNotDisturbPref.setChecked(Preferences.getInstance().isNotDisturb());
                rootScreen.addPreference(doNotDisturbPref);

                SwitchPreference doNotDisturbHolidaysPref = new SwitchPreference(activity);
                doNotDisturbHolidaysPref.setKey(Preferences.KEY_DO_NOT_DISTURB_HOLIDAYS);
                doNotDisturbHolidaysPref.setTitle("Не беспокоить по выходным");
                doNotDisturbHolidaysPref.setSummary("По выходным уведомления будут приходить без звука");
                rootScreen.addPreference(doNotDisturbHolidaysPref);

                SwitchPreference isSilentPref = new SwitchPreference(activity);
                isSilentPref.setKey(Preferences.KEY_SILENT);
                isSilentPref.setSummary("Уведомления без звука и вибрации");
                isSilentPref.setTitle("Тихий режим");
                rootScreen.addPreference(isSilentPref);

                SwitchPreference periodicCheckPref = new SwitchPreference(activity);
                periodicCheckPref.setKey(Preferences.KEY_SHOW_WAITING_ACCEPT);
                periodicCheckPref.setSummary("Периодически проверять наличие новых задач");
                periodicCheckPref.setTitle("Проверять новые задачи");
                periodicCheckPref.setChecked(Preferences.getInstance().isShowWaitingAccept());
                rootScreen.addPreference(periodicCheckPref);

                SwitchPreference showNewTaskWindowPref = new SwitchPreference(activity);
                showNewTaskWindowPref.setKey(Preferences.KEY_SHOW_NEW_TASK_WINDOW);
                showNewTaskWindowPref.setSummary("Отображает окно подтверждения задачи при её получении");
                showNewTaskWindowPref.setTitle("Показывать окно новой задачи");
                showNewTaskWindowPref.setChecked(Preferences.getInstance().isCreateNewTaskWindow());
                rootScreen.addPreference(showNewTaskWindowPref);

                SwitchPreference notifyTaskCreatedPref = new SwitchPreference(activity);
                notifyTaskCreatedPref.setKey(Preferences.KEY_SHOW_TASK_CREATED);
                notifyTaskCreatedPref.setSummary("Получать уведомления о созданных задачах");
                notifyTaskCreatedPref.setTitle("Созданные задачи");
                notifyTaskCreatedPref.setChecked(Preferences.getInstance().isShowTaskCreated());
                rootScreen.addPreference(notifyTaskCreatedPref);

                SwitchPreference notifyTaskAcceptedPref = new SwitchPreference(activity);
                notifyTaskAcceptedPref.setKey(Preferences.KEY_SHOW_TASK_ACCEPTED);
                notifyTaskAcceptedPref.setSummary("Получать уведомления о подтверждении задачи исполнителем");
                notifyTaskAcceptedPref.setTitle("Принятные задачи");
                notifyTaskAcceptedPref.setChecked(Preferences.getInstance().isShowTaskAccepted());
                rootScreen.addPreference(notifyTaskAcceptedPref);

                SwitchPreference notifyTaskCancelledPref = new SwitchPreference(activity);
                notifyTaskCancelledPref.setKey(Preferences.KEY_SHOW_TASK_CANCELLED);
                notifyTaskCancelledPref.setSummary("Получать уведомления о отменённых задачах");
                notifyTaskCancelledPref.setTitle("Отменённые задачи");
                notifyTaskCancelledPref.setChecked(Preferences.getInstance().isShowTaskCancelled());
                rootScreen.addPreference(notifyTaskCancelledPref);

                SwitchPreference notifyTaskDismissedPref = new SwitchPreference(activity);
                notifyTaskDismissedPref.setKey(Preferences.KEY_SHOW_TASK_DISMISSED);
                notifyTaskDismissedPref.setSummary("Получать уведомления о задачах, отменённых исполнителем");
                notifyTaskDismissedPref.setTitle("Отказ от задачи");
                notifyTaskDismissedPref.setChecked(Preferences.getInstance().isShowTaskDismissed());
                rootScreen.addPreference(notifyTaskDismissedPref);

                SwitchPreference notifyTaskFinishedPref = new SwitchPreference(activity);
                notifyTaskFinishedPref.setKey(Preferences.KEY_SHOW_TASK_FINISHED);
                notifyTaskFinishedPref.setSummary("Получать уведомления о завершённых задачах");
                notifyTaskFinishedPref.setTitle("Завершённые задачи");
                notifyTaskFinishedPref.setChecked(Preferences.getInstance().isShowTaskFinished());
                rootScreen.addPreference(notifyTaskFinishedPref);

                TimePreference tPref = new TimePreference(getContext(), null);
                tPref.setTitle("Время начала активного режима");
                tPref.setKey(Preferences.KEY_START_ACTIVE_TIME);
                tPref.setDefaultValue(Preferences.getInstance().getActiveStartTime());
                rootScreen.addPreference(tPref);

                TimePreference tOffPref = new TimePreference(getContext(), null);
                tOffPref.setTitle("Время завершения активного режима");
                tOffPref.setKey(Preferences.KEY_FINISH_ACTIVE_TIME);
                tOffPref.setDefaultValue(Preferences.getInstance().getActiveFinishTime());
                rootScreen.addPreference(tOffPref);

                setHasOptionsMenu(true);
            }
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            Log.d("surprise", "PreferenceFragment onDisplayPreferenceDialog 138: show");
            if (preference instanceof TimePreference) {
                TimePickerDialog timePicker = new TimePickerDialog(preference.getKey());
                timePicker.setTargetFragment(this, 0);
                timePicker.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }
}
