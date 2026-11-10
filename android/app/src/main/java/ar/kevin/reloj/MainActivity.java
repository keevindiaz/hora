package ar.kevin.reloj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Muestra la página del reloj a pantalla completa (sin barra de direcciones).
 * De día mantiene la pantalla prendida; de noche la deja apagarse sola.
 */
public class MainActivity extends Activity {

    static final String EXTRA_RECARGAR = "recargar";

    private static final long CHEQUEO_MS = 30 * 1000L;   // revisa el horario cada 30 s
    private static final long REINTENTO_MS = 30 * 1000L; // reintenta si no hay internet

    private final Handler handler = new Handler();
    private WebView web;
    private Boolean nocheAplicada = null;

    private final Runnable chequeo = new Runnable() {
        @Override
        public void run() {
            aplicarHorario();
            handler.postDelayed(this, CHEQUEO_MS);
        }
    };

    private final Runnable reintento = new Runnable() {
        @Override
        public void run() {
            if (web != null) web.loadUrl(Config.URL);
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#111111"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);       // para recordar el modo día/noche
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setTextZoom(100);
        s.setBuiltInZoomControls(false);
        s.setSupportZoom(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false; // todo se abre adentro
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler h, SslError error) {
                String host = null;
                try {
                    host = Uri.parse(error.getUrl()).getHost();
                } catch (Exception ignored) {
                }
                if (Config.hostConfiable(host)) {
                    h.proceed();   // Android 4.4 no reconoce el certificado, pero es nuestro sitio
                } else {
                    h.cancel();
                }
            }

            @Override
            public void onReceivedError(WebView view, int code, String desc, String failingUrl) {
                // Sin internet (por ejemplo recién prendida): pantalla oscura y reintento.
                view.loadData("<html><body style='background:#111'></body></html>",
                        "text/html", "utf-8");
                handler.removeCallbacks(reintento);
                handler.postDelayed(reintento, REINTENTO_MS);
            }
        });

        setContentView(web);
        web.loadUrl(Config.URL);

        Config.programarDespertador(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getBooleanExtra(EXTRA_RECARGAR, false) && web != null) {
            web.loadUrl(Config.URL); // a la mañana recarga por si hubo cambios en la página
        }
        nocheAplicada = null;
        aplicarHorario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ocultarBarras();
        nocheAplicada = null;
        handler.removeCallbacks(chequeo);
        handler.post(chequeo);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) ocultarBarras();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (web != null) {
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Modo kiosko: "atrás" no cierra el reloj. Para salir, usar el botón de inicio.
    }

    /** De día: pantalla siempre prendida. De noche: brillo mínimo y se apaga sola. */
    private void aplicarHorario() {
        boolean noche = Config.esHoraDeApagar();
        if (nocheAplicada != null && nocheAplicada == noche) return;
        nocheAplicada = noche;

        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        if (noche) {
            w.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            lp.screenBrightness = 0.02f;
        } else {
            w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        }
        w.setAttributes(lp);
    }

    private void ocultarBarras() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}
