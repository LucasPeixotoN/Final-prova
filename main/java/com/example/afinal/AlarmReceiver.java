package com.example.afinal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "medicamento_channel";
    private static final String CHANNEL_NAME = "Lembretes de Medicamentos";
    private static final String CHANNEL_DESCRIPTION = "Notificações para lembrar de tomar medicamentos";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicamentoNome = intent.getStringExtra("medicamento_nome");
        String medicamentoDescricao = intent.getStringExtra("medicamento_descricao");
        String medicamentoId = intent.getStringExtra("medicamento_id");

        criarCanalNotificacao(context);
        mostrarNotificacao(context, medicamentoNome, medicamentoDescricao, medicamentoId);
    }

    private void criarCanalNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void mostrarNotificacao(Context context, String titulo, String descricao, String medicamentoId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ Hora do Medicamento: " + titulo)
                .setContentText(descricao.isEmpty() ? "Lembre-se de tomar seu medicamento" : descricao)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = medicamentoId != null ? medicamentoId.hashCode() : (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());
        }
    }
}