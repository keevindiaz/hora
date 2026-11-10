package ar.kevin.reloj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/** Se ejecuta a la HORA_PRENDER: prende la pantalla y trae el reloj al frente. */
public class DespertarReceiver extends BroadcastReceiver {

    @Override
    @SuppressWarnings("deprecation")
    public void onReceive(Context ctx, Intent intent) {
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                            | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE,
                    "reloj:despertar");
            wl.acquire(15000); // se suelta solo; después la mantiene prendida la app
        }

        Intent abrir = Config.intentAbrirApp(ctx);
        abrir.putExtra(MainActivity.EXTRA_RECARGAR, true);
        try {
            ctx.startActivity(abrir);
        } catch (Exception ignored) {
        }

        Config.programarDespertador(ctx); // la de mañana
    }
}
