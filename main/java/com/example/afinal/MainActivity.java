package com.example.afinal;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MedicamentoAdapter.OnItemClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference medicamentosRef = db.collection("medicamentos");

    private MedicamentoAdapter adapter;

    private EditText editMedicamento;
    private EditText editDescricao;
    private EditText editTHorario;
    private CheckBox checkTomado;
    private Button btnSalvar;
    private Button btnPokemon;

    private DocumentSnapshot snapshotParaEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editMedicamento = findViewById(R.id.editMedicamento);
        editDescricao = findViewById(R.id.editDescricao);
        editTHorario = findViewById(R.id.editTHorario);
        checkTomado = findViewById(R.id.checkTomado);
        btnSalvar = findViewById(R.id.novoMed);
        btnPokemon = findViewById(R.id.pokemon);

        solicitarPermissoes();

        setUpRecyclerView();

        editTHorario.setOnClickListener(v -> showTimePickerDialog());
        btnSalvar.setOnClickListener(v -> salvarMedicamento());

        btnPokemon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ApiPokemonActivity.class);
            startActivity(intent);
        });
    }

    private void solicitarPermissoes() {
    
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Por favor, ative alarmes exatos nas configurações", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showTimePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    editTHorario.setText(formattedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void setUpRecyclerView() {
        Query query = medicamentosRef.orderBy("nome", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Medicamento> options = new FirestoreRecyclerOptions.Builder<Medicamento>()
                .setQuery(query, Medicamento.class)
                .build();

        adapter = new MedicamentoAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private void salvarMedicamento() {
        String nome = editMedicamento.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();
        String horario = editTHorario.getText().toString().trim();
        boolean tomado = checkTomado.isChecked();

        if (nome.isEmpty()) {
            Toast.makeText(this, R.string.medication_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (horario.isEmpty()) {
            Toast.makeText(this, R.string.medication_time_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (snapshotParaEditar != null) {
       
            String documentId = snapshotParaEditar.getId();
            snapshotParaEditar.getReference().set(new Medicamento(nome, descricao, horario, tomado));
            Toast.makeText(this, "Medicamento atualizado", Toast.LENGTH_SHORT).show();

            agendarNotificacao(nome, descricao, horario, documentId);

            snapshotParaEditar = null;
            btnSalvar.setText("Cadastrar");

        } else {
    
            medicamentosRef.add(new Medicamento(nome, descricao, horario, tomado))
                    .addOnSuccessListener(documentReference -> {
                        String documentId = documentReference.getId();
           
                        agendarNotificacao(nome, descricao, horario, documentId);
                        Toast.makeText(this, R.string.medication_added, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

       
        editMedicamento.setText("");
        editDescricao.setText("");
        editTHorario.setText("");
        checkTomado.setChecked(false);
    }

    private void agendarNotificacao(String nome, String descricao, String horario, String documentId) {
        try {
            
            String[] timeParts = horario.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("medicamento_nome", nome);
            intent.putExtra("medicamento_descricao", descricao);
            intent.putExtra("medicamento_id", documentId);

            int requestCode = documentId.hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
            
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
      
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }

                Toast.makeText(this, "Lembrete agendado para " + horario, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao agendar notificação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void cancelarNotificacao(String documentId) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        int requestCode = documentId.hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
        Medicamento medicamento = documentSnapshot.toObject(Medicamento.class);
        if (medicamento != null) {
            editMedicamento.setText(medicamento.getNome());
            editDescricao.setText(medicamento.getDescricao());
            editTHorario.setText(medicamento.getHorario());
            checkTomado.setChecked(medicamento.isTomado());

            snapshotParaEditar = documentSnapshot;
            btnSalvar.setText("Atualizar");
        }
    }
}
