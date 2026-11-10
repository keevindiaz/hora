# App "Reloj" para la tablet vieja (Android 4.4)

Muestra https://keevindiaz.github.io/hora/ a pantalla completa, sin barra de direcciones
(así no aparece la alerta de "conexión no segura").

- De **10:00 a 23:00** la pantalla queda siempre prendida.
- A las **23:00** baja el brillo al mínimo y deja que la pantalla se apague sola.
- A las **10:00** prende la pantalla y recarga la página.
- Se abre sola al encender la tablet.
- "Atrás" no la cierra; para salir usá el botón de inicio.

## Cambiar horario o página

Todo está en `app/src/main/java/ar/kevin/reloj/Config.java`
(`HORA_APAGAR`, `HORA_PRENDER`, `URL`). Si cambiás algo, subí `versionCode` en
`app/build.gradle` (1 → 2 → 3…) para que se instale como actualización.

## Compilar

Se compila sola con GitHub Actions cada vez que hacés push de algo dentro de `android/`
(o a mano: pestaña **Actions → Compilar APK del reloj → Run workflow**).
La APK queda en la ejecución, sección **Artifacts → reloj-apk**.

## Configuración de la tablet (una sola vez)

1. Ajustes → Seguridad → **Orígenes desconocidos**: activar.
2. Ajustes → Pantalla → **Tiempo de espera de pantalla: 1 o 2 minutos**
   (es lo que tarda en apagarse a la noche; de día la app la mantiene prendida).
3. Ajustes → Pantalla de bloqueo → **Ninguno** o **Deslizar** (sin PIN), así a la mañana
   el reloj aparece directo.
4. Ajustes → Fecha y hora → **automática**.
5. Abrir la app "Reloj" una vez a mano (Android necesita eso para que arranque sola después).

La llave `app/reloj.keystore` firma la app para que las actualizaciones se instalen
encima. Es solo para esta app casera.
