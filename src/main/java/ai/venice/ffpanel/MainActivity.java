package ai.venice.ffpanel;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {

    // ===== ഫീച്ചറുകൾ ഇവിടെ തിരുത്തുക =====
    static final String[] NAMES = {
        "Speed 2x", "Speed 5x", "Speed reset", "Headshot", "ESP"
    };
    static final String[] SCRIPTS = {
        "pcall(gg.setSpeed,2) gg.toast('Speed 2x')",
        "pcall(gg.setSpeed,5) gg.toast('Speed 5x')",
        "pcall(gg.setSpeed,1) gg.toast('Speed normal')",
        "local S='h 00 00 80 3F 00 00 00 00'\n" +
        "local R='h 00 00 00 00 00 00 00 00'\n" +
        "gg.setRanges(gg.REGION_C_ALLOC|gg.REGION_ANONYMOUS) gg.clearResults()\n" +
        "gg.searchNumber(S,gg.TYPE_BYTE) gg.sleep(200)\n" +
        "if gg.getResultsCount()<1 then gg.toast('Headshot: no match') else\n" +
        "gg.getResults(500) gg.editAll(R,gg.TYPE_BYTE) gg.clearResults()\n" +
        "gg.toast('Headshot applied') end",
        "local S='h 01 00 00 00 00 00 00 00'\n" +
        "local R='h 00 00 00 00 00 00 00 00'\n" +
        "gg.setRanges(gg.REGION_C_ALLOC|gg.REGION_ANONYMOUS) gg.clearResults()\n" +
        "gg.searchNumber(S,gg.TYPE_BYTE) gg.sleep(200)\n" +
        "if gg.getResultsCount()<1 then gg.toast('ESP: no match') else\n" +
        "gg.getResults(500) gg.editAll(R,gg.TYPE_BYTE) gg.clearResults()\n" +
        "gg.toast('ESP applied') end"
    };
    // =====================================

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Build.VERSION.SDK_INT >= 24)
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 30, 30, 30);

        TextView tv = new TextView(this);
        tv.setText("FFPanel — OB55");
        tv.setTextSize(22);
        root.addView(tv);

        Button gg = new Button(this);
        gg.setText("Open GameGuardian");
        gg.setOnClickListener(v -> {
            Intent i = getPackageManager().getLaunchIntentForPackage("com.gameguardian");
            if (i != null) startActivity(i); else say("GG not installed");
        });
        root.addView(gg);

        ListView lv = new ListView(this);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, NAMES));
        lv.setOnItemClickListener((p, v, pos, id) -> run(pos));
        lv.setOnItemLongClickListener((p, v, pos, id) -> { copy(pos); return true; });
        root.addView(lv);

        setContentView(root);
        say("Tap = run • Hold = copy");
    }

    void run(int i) {
        try {
            File f = new File(getCacheDir(), "f" + i + ".lua");
            FileOutputStream o = new FileOutputStream(f);
            o.write(SCRIPTS[i].getBytes("UTF-8"));
            o.close();

            Intent in = new Intent(Intent.ACTION_VIEW);
            in.setDataAndType(Uri.fromFile(f), "text/plain");
            in.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            in.setPackage("com.gameguardian");
            try { startActivity(in); }
            catch (ActivityNotFoundException e) {
                Intent s = new Intent(Intent.ACTION_SEND);
                s.setType("text/plain");
                s.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                startActivity(Intent.createChooser(s, "Open with"));
            }
        } catch (Exception e) { say("Error: " + e.getMessage()); }
    }

    void copy(int i) {
        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
            .setPrimaryClip(ClipData.newPlainText("lua", SCRIPTS[i]));
        say("Copied");
    }

    void say(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
  }
