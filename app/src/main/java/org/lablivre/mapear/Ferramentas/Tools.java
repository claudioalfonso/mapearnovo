package org.lablivre.mapear.Ferramentas;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.List;
import java.util.Random;

public class Tools {

    public static boolean verificaConexao(Context cont){
        ConnectivityManager conmag = (ConnectivityManager)cont.getSystemService(Context.CONNECTIVITY_SERVICE);

        if ( conmag != null ) {
            conmag.getActiveNetworkInfo();

            //Verifica internet pela WIFI
            if (conmag.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                return true;
            }

            //Verifica se tem internet móvel
            if (conmag.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {
                return true;
            }
        }

        return false;
    }

    public static void displayPromptForEnablingGPS(final Activity activity)
    {

        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Você deve habilitar a localização";

        builder.setMessage(message)
                .setPositiveButton("Configurações",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {

                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    public String getPath(Uri uri, Activity activity) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        float pk = (float) (180/3.14169);

        float a1 = (float) lat_a / pk;
        float a2 = (float) lng_a / pk;
        float b1 = (float) lat_b / pk;
        float b2 = (float) lng_b / pk;

        float t1 = (float) (Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2));
        float t2 = (float) (Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2));
        float t3 = (float) (Math.sin(a1)*Math.sin(b1));
        double tt = Math.acos(t1 + t2 + t3);

        return (6366000*tt)/1000;
    }

    public void loadWebPage(String url, final Context context) throws Exception {
        WebView webview = new WebView(context);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar) {
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                    this.dismiss();
                return true;
            }
        };


        webview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!processUrl(url, context)) { // if the url could not be processed by
                    // another intent
                    d.show();
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.endsWith("return")) {
                    d.dismiss();
                } else {
                    super.onPageFinished(view, url);
                }
            }

        });

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels - 60;
        int width = displaymetrics.widthPixels - 20;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = width;
        lp.height = height;

        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        int dialogHeight = d.getWindow().getAttributes().height;
        int dialogWidth = d.getWindow().getAttributes().width;
        d.getWindow().setGravity(Gravity.BOTTOM);
        d.addContentView(webview, new FrameLayout.LayoutParams(
                dialogWidth, dialogHeight,
                Gravity.BOTTOM));
        d.getWindow().setAttributes(lp);

        if (!processUrl(url, context)) { // if the url could not be processed by
            // another intent
            d.show();
            webview.loadUrl(url);
        }
    }

    public boolean processUrl(String url, Context ctx) {
        // get available packages from the given url
        List<ResolveInfo> resolveInfos = getAvailablePackagesForUrl(url, ctx);
        // filter the webbrowser > because the webview will replace it, using
        // google as simple url
        List<ResolveInfo> webBrowsers = getAvailablePackagesForUrl(
                "http://www.google.com", ctx);
        for (ResolveInfo resolveInfo : resolveInfos) {

            boolean found = false;
            for (ResolveInfo webBrowser : webBrowsers) {
                if (resolveInfo.activityInfo.packageName
                        .equals(webBrowser.activityInfo.packageName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.setClassName(resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                ctx.startActivity(intent);
                return true;
            }
        }
        return false;
    }

    private List<ResolveInfo> getAvailablePackagesForUrl(String url, Context ctx) {
        PackageManager packageManager = ctx.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        return packageManager.queryIntentActivities(intent,
                PackageManager.GET_RESOLVED_FILTER);
    }
    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
