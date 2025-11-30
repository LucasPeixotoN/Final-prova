package com.example.afinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class MedicamentoAdapter extends FirestoreRecyclerAdapter<Medicamento, MedicamentoAdapter.MedicamentoViewHolder> {

    private OnItemClickListener listener;

    public MedicamentoAdapter(@NonNull FirestoreRecyclerOptions<Medicamento> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position, @NonNull Medicamento model) {
        holder.txtNome.setText(model.getNome());
        holder.txtDescricao.setText(model.getDescricao());
        holder.txtHorario.setText(model.getHorario());
        holder.checkTomado.setChecked(model.isTomado());
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class MedicamentoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        TextView txtDescricao;
        TextView txtHorario;
        CheckBox checkTomado;

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtHorario = itemView.findViewById(R.id.txtHorario);
            checkTomado = itemView.findViewById(R.id.checkTomado);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        deleteItem(position);
                        Toast.makeText(itemView.getContext(), "Medicamento excluído", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
