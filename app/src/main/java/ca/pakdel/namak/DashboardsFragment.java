package ca.pakdel.namak;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class DashboardsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.dashboard_list);
        assert recyclerView != null;
        recyclerView.setHasFixedSize(true);

        DashboardViewAdapter viewAdapter = new DashboardViewAdapter();
        assert getActivity() != null;
        DashboardsViewModel dashboardsViewModel = ViewModelProviders.of(getActivity()).get(DashboardsViewModel.class);
        dashboardsViewModel.getDashboards().observe(this, viewAdapter::setDashboards);
        recyclerView.setAdapter(viewAdapter);

        return view;
    }
}

class DashboardViewAdapter extends RecyclerView.Adapter<DashboardViewAdapter.DashboardItemViewHolder> {
    private static final int DISABLED_COMMAND_COLOR = Color.LTGRAY;
    private List<Dashboard> dashboards;

    static class DashboardItemViewHolder extends RecyclerView.ViewHolder {
        ViewGroup viewGroup;
        View colorView;
        TextView titleView;
        TextView summaryView;
        DashboardItemViewHolder(ViewGroup vg) {
            super(vg);
            viewGroup = vg;
            colorView = vg.findViewById(R.id.dashboard_item_color);
            titleView = vg.findViewById(R.id.dashboard_item_title_text);
            summaryView = vg.findViewById(R.id.dashboard_item_summary_text);
        }
    }

    void setDashboards(List<Dashboard> dashboardList) {
        dashboards = dashboardList;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public DashboardItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dashboard_item, parent, false);
        return new DashboardItemViewHolder(v);
    }
    
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull DashboardItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        CommandAndColor command = DashboardsViewModel.getCommandAndColor(dashboards, position);
        assert command != null;

        holder.titleView.setText(command.getTitle());
        holder.summaryView.setText(command.getSummary());

        if(command.isValid()) {
            holder.colorView.setBackgroundColor(command.color);
            final Bundle bundle = new Bundle();
            bundle.putInt(Dashboard.COMMAND_POSITION,  position);
            holder.viewGroup.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_dashboard_to_execution, bundle));
        } else {
            holder.colorView.setBackgroundColor(DISABLED_COMMAND_COLOR);
            holder.viewGroup.setEnabled(false);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dashboards.stream().mapToInt(Dashboard::count).sum();
    }
}