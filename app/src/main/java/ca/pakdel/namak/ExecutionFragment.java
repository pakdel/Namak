package ca.pakdel.namak;

// TODO This is ugly!

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

public class ExecutionFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_execution, container, false);

        // TODO Two pane support is not there

        assert getArguments() != null;
        int position = getArguments().getInt(Dashboard.COMMAND_POSITION);
        assert getActivity() != null;
        DashboardsViewModel dashboardsViewModel = ViewModelProviders.of(getActivity()).get(DashboardsViewModel.class);
        CommandAndColor command = dashboardsViewModel.getCommandAndColor(position);
        assert command != null;


        ((TextView) view.findViewById(R.id.execution_title)).setText(command.getTitle());
        ((TextView) view.findViewById(R.id.execution_target)).setText(command.getTarget());
        ((TextView) view.findViewById(R.id.execution_function)).setText(command.getFunction());
        // ((TextView) view.findViewById(R.id.execution_arguments)).setText(String.join(", ", command.getPositional_arguments()));
        ((TextView) view.findViewById(R.id.execution_arguments)).setText(command.getPositional_arguments().toString());


        SaltMastersViewModel saltMastersViewModel = ViewModelProviders.of(getActivity()).get(SaltMastersViewModel.class);

        MutableLiveData<String> executionResult = new MutableLiveData<>();
        executionResult.observe(getActivity(), r -> ((TextView) view.findViewById(R.id.execution_result)).setText(r));
        executionResult.setValue("Running");
        saltMastersViewModel.execute(command, executionResult);

        return view;
    }

}
