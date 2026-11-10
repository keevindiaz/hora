package ar.kevin.reloj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Al encender la tablet: programa el despertador y abre el reloj. */
public class ArranqueReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Config.programarDespertador(ctx);
        try {
            ctx.startActivity(Config.intentAbrirApp(ctx));
        } catch (Exception ignored) {
        }
    }
}
