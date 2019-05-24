package ca.pakdel.namak;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

class Popup {
    // private static final int veryLongDuration = 8000; // Maybe Snackbar.LENGTH_LONG ?
    private static Popup instance;
    private Context context;


    private Popup(Context context) {
        assert this.context == null;
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        this.context = context.getApplicationContext();
    }

    static synchronized void init(Context context) {
        assert instance == null;
        instance = new Popup(context);
    }

    public static void message(@NonNull CharSequence text) {
        Toast.makeText(instance.context, text, Toast.LENGTH_SHORT)
                .show();
    }

    public static void error(@NonNull Exception error) {
        error.printStackTrace();
        Log.e("Namak", "error: ", error);
        // Toast.makeText(instance.context, error.getMessage(), Toast.LENGTH_LONG).show();
        Toast.makeText(instance.context, error.toString(), Toast.LENGTH_LONG).show();
    }

/*    public static void error(@NonNull Context context, @NonNull CharSequence text, final int code, @Nullable Throwable error) {
        if (error != null) {
            Log.e("Namak", "error: ", error);
            // TODO
            // Log.e(String.format("Error Code %03d", code), error.toString(), error);
        }
        //noinspection ResourceType
        Snackbar.make(context, text, veryLongDuration)
                .setAction("Details", v -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://namak.pakdel.ca"));
                            context.startActivity(browserIntent);
                        }
                )
                .setActionTextColor(Color.GRAY)
                .show();
    }
*/
}
