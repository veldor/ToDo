package net.veldor.todo.ui.incoming;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import net.veldor.todo.R;
import net.veldor.todo.ui.outgoing.OutgoingViewModel;
import net.veldor.todo.utils.Preferences;


public class IncomingFragment extends Fragment {

    private FragmentActivity mActivity;
    private OutgoingViewModel mViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_incoming_tickets, container, false);
        mViewModel = new ViewModelProvider(this).get(OutgoingViewModel.class);
        mActivity = getActivity();
        setupUI(root);
        setupObservers();
        return root;
    }

    private void setupObservers() {
        // буду отслеживать изменение периода, если он меняется- перезапущу таймер
    }

    private void setupUI(View root) {

    }


    @Override
    public void onResume() {
        super.onResume();
        if (!Preferences.getInstance().isUserUnknown()) {
            mViewModel.updateTaskList();
        }
    }
}
