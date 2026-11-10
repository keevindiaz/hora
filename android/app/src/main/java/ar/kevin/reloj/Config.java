package ar.kevin.reloj;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/** Todo lo configurable de la app está acá. */
final class Config {

    /** Página que se muestra. */
    static final String URL = "https://keevindiaz.github.io/hora/";

    /** Hora en que la pantalla se apaga (0-23). */
    static final int HORA_APAGAR = 23;

    /** Hora en que la pantalla se vuelve a prender (0-23). */
    static final int HORA_PRENDER = 10;

    /**
     * Sitios a los que se les acepta el certificado aunque Android 4.4 no lo reconozca.
     * Solo los de la página; cualquier otro sitio sigue bloqueado.
     */
    static final String[] HOSTS_CONFIABLES = {
            "keevindiaz.github.io",
            "fonts.googleapis.com",
            "fonts.gstatic.com"
    };

    private Config() {}

    /** true entre HORA_APAGAR y HORA_PRENDER (cruza la medianoche si hace falta). */
    static boolean esHoraDeApagar() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (HORA_APAGAR == HORA_PRENDER) return false;
        if (HORA_APAGAR < HORA_PRENDER) return h >= HORA_APAGAR && h < HORA_PRENDER;
        return h >= HORA_APAGAR || h < HORA_PRENDER;
    }

    static boolean hostConfiable(String host) {
        if (host == null) return false;
        for (String h : HOSTS_CONFIABLES) {
            if (h.equalsIgnoreCase(host)) return true;
        }
        return false;
    }

    /** Programa la alarma que despierta la pantalla a la próxima HORA_PRENDER. */
    static void programarDespertador(Context ctx) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, HORA_PRENDER);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if (c.getTimeInMillis() <= System.currentTimeMillis() + 1000) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0,
                new Intent(ctx, DespertarReceiver.class), flags);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        long t = c.getTimeInMillis();
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, t, pi);
            }
        } catch (SecurityException e) {
            // Android 12+ sin permiso de alarma exacta: usamos una aproximada.
            am.set(AlarmManager.RTC_WAKEUP, t, pi);
        }
    }

    static Intent intentAbrirApp(Context ctx) {
        Intent i = new Intent(ctx, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return i;
    }
}
