package ru.cities.game.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.cities.game.R;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.MessageHolder> {
    private final OnItemClickListener listener;
    private final ArrayList<City> cities = new ArrayList<>();

    public CityAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(City city);
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        holder.bind(cities.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public void insertMessage(City city) {
        cities.add(city);
        notifyItemInserted(cities.size());
    }

    static class MessageHolder extends RecyclerView.ViewHolder {
        private final TextView tvOutgoing, tvIncoming;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            tvOutgoing = itemView.findViewById(R.id.tv_outgoing);
            tvIncoming = itemView.findViewById(R.id.tv_incoming);
        }

        public void bind(City city, OnItemClickListener listener) {
            TextView visibleView;
            if (city.getType()) {
                visibleView = tvOutgoing;
                tvIncoming.setVisibility(View.GONE);
            } else {
                visibleView = tvIncoming;
                tvOutgoing.setVisibility(View.GONE);
            }
            visibleView.setVisibility(View.VISIBLE);
            visibleView.setText(city.getName());
            visibleView.setCompoundDrawablesWithIntrinsicBounds(city.getFlagResource(), 0, 0, 0);
            visibleView.setOnClickListener(v -> listener.onItemClick(city));
        }
    }
}
