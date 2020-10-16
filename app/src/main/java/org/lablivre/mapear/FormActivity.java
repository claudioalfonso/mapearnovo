package org.lablivre.mapear;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.lablivre.mapear.Ferramentas.Tools;

public class FormActivity extends AppCompatActivity {

    TextView messageText;
    Button uploadButton;
    int serverResponseCode = 0;
    String cod, urlfile = "";
    ProgressDialog dialog = null;
    String path = android.os.Environment.getExternalStorageDirectory() + File.separator + "mapear";
    String upLoadServerUri = "http://lablivre.org/educar/index.php/component/requestapp?format=raw";

    protected static final int REQUEST_CAMERA = 1;
    protected static final int SELECT_FILE = 2;
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
    ImageView ivImage;
    Button btEnviar;
    EditText detalhes;
    String lat="", lng="";
    ProgressBar uploadProgressBar;
    boolean isok, gpsok = false;
    Location newLocation = null;//add thiss
    SharedPreferences sharedPref;
    SharedPreferences.Editor pref;
    String filename;
    Tools con = new Tools();


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener mlocListener = new NewLocationListener();
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        filename = randomIdentifier() + ".jpg";

        detalhes = (EditText) findViewById(R.id.editTextDesc);
        ivImage = (ImageView)findViewById(R.id.ivImage);
        btEnviar = (Button) findViewById(R.id.button1);
        uploadProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        if(urlfile == ""){
            selectImage();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public void btSelectphoto(View view){
        selectImage();
    }

    public void btSubmit(View view){

        try {
            lng = newLocation.getLongitude() + "";
            lat = newLocation.getLatitude() + "";
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Nao foi possivel localizar. Sua foto foi salva, tente novamente.",
                    Toast.LENGTH_LONG).show();
        }

        isok = con.verificaConexao(FormActivity.this);

        // if (isok == false) {
        //     Denuncias den = new Denuncias(lat, lng, detalhes.getText().toString(), urlfile);
        //     den.save();
        //   sucess(FormActivity.this, "Sua denuncia foi cadastrada com sucesso, assim que você estiver conectado sua denuncia estará efetivada.");
        // }

        if (isok && !urlfile.equals("") && !lng.equals("") && !lat.equals("")) {
            cod = randInt(100000000, 999999999) + "";
            btEnviar.setEnabled(false);
            btEnviar.setText("Aguarde...");
            //uploadFile(urlfile,"http://ckoala.com/up.php","temp-1.jpg");
            Ion.with(FormActivity.this, upLoadServerUri)
                    .uploadProgressBar(uploadProgressBar)
                    .setMultipartParameter("text_registro", "Registro N.")
                    .setMultipartParameter("gera_numm", cod)
                    .setMultipartParameter("imagem_hidden", "images/")
                    .setMultipartParameter("pdi", "[" + lat + "," + lng + "]")
                    .setMultipartParameter("lng", lng)
                    .setMultipartParameter("lat", lat)
                    .setMultipartParameter("task", "save")
                    .setMultipartParameter("id", "0")
                    .setMultipartParameter("fm_", "1")
                    .setMultipartParameter("nome_do_monumento", detalhes.getText() + "")
                    .setMultipartParameter("config[type]", "cadastro_de_pois")
                    .setMultipartParameter("config[stage]", "-1")
                    .setMultipartParameter("config[skip]", "0")
                    .setMultipartParameter("config[url]", "http://lablivre.org/educar/index.php/registro-app")
                    .setMultipartParameter("config[id]", "0")
                    .setMultipartParameter("config[itemId]", "105")
                    .setMultipartParameter("config[unique]", "seblod_form_cadastro_de_pois")
                    .setMultipartFile("imagem", new File(urlfile)).
                    asString().withResponse().setCallback(
                    new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception arg0,
                                                Response<String> r) {
                            if (r.getResult().equalsIgnoreCase("ok")) {
                                sucess(FormActivity.this, "Registrado com sucesso!"); // protocolo nº: " + "#" + cod
                                Log.i("DADOS", r.getResult());
                            }
                        }
                    }
            );
        } else {
            Toast.makeText(getApplicationContext(), "Nao foi possivel realizar a conexão com servidor.",
                    Toast.LENGTH_LONG).show();
        }

    }
    @Override
    public void onPause(){
        super.onPause();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("ONCRESULT","RESULT");
        //sharedPref = getSharedPreferences("data_saves",Registrar.MODE_PRIVATE);
        super.onActivityResult(requestCode, resultCode, data);

        File file = new File(path, filename);

        Bitmap bm,bm2;
        OutputStream fOut = null;
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                File f = new File(path);
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals(filename)) {
                        f = temp;
                        Log.i("CAMERA", f.getAbsolutePath());
                        urlfile = f.getAbsolutePath();

                        btEnviar.setEnabled(true);
                        break;
                    }
                }
                try {

                    BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                    bm = BitmapFactory.decodeFile(f.getAbsolutePath(),btmapOptions);
                    bm2 = Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() * 0.5), (int) (bm.getHeight()*0.5), true);
                    ivImage.setImageBitmap(bm2);
                    f.delete();
                    try {
                        fOut = new FileOutputStream(file);
                        bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                        fOut.flush();
                        fOut.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_FILE) {

                Uri selectedImageUri = data.getData();
                String tempPath = con.getPath(selectedImageUri, FormActivity.this);
                BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
                bm2 = Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() * 0.5), (int) (bm.getHeight()*0.5), true);
                ivImage.setImageBitmap(bm2);

                try {
                    fOut = new FileOutputStream(file);
                    bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("CAMERA",file.getAbsolutePath());
                btEnviar.setEnabled(true);
                urlfile = file.getAbsolutePath();

            }
        }

    }

    // tirar foto
    private void selectImage() {
        final CharSequence[] items = { "Tirar foto", "Escolher foto",
                "Cancelar" };

        AlertDialog.Builder builder = new AlertDialog.Builder(FormActivity.this);
        builder.setTitle("Adicionar foto!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Tirar foto")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(path, filename);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Escolher foto")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Selecione a uma foto"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public void sucess(final Activity activity,String message){
        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        //final String message = "Registrado com sucesso, protocolo nº: "+"#"+cod;
        builder.setMessage(message)
                .setPositiveButton("Fechar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                finish();
                                d.dismiss();
                            }
                        });
        builder.create().show();

    }

    // class variable
    final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345674890";

    final java.util.Random rand = new java.util.Random();

    // consider using a Map<String,Boolean> to say whether the identifier is being used or not
    final Set<String> identifiers = new HashSet<>();

    public String randomIdentifier() {
        StringBuilder builder = new StringBuilder();
        while(builder.toString().length() == 0) {
            int length = 15;
            for(int i = 0; i < length; i++)
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            if(identifiers.contains(builder.toString()))
                builder = new StringBuilder();
        }
        return builder.toString();
    }
    public class NewLocationListener implements LocationListener {

        public void onLocationChanged(Location loc) {

            //save the new location
            newLocation = loc;

        }

        public void onProviderDisabled(String provider) {
            gpsok =false;
            Tools.displayPromptForEnablingGPS(FormActivity.this);
        }

        public void onProviderEnabled(String provider) {
            gpsok = true;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

}
