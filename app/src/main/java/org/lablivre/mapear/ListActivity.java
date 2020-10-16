package org.lablivre.mapear;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lablivre.mapear.Ferramentas.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListActivity extends AppCompatActivity {

    ListView listview;
    ArrayAdapter<Item> adaptes;
    EditText filtro;
    Tools util = new Tools();
    List<Item> dadosLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        carregaLista();
        listview = findViewById(R.id.listpontos);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String url = Objects.requireNonNull(adaptes.getItem(position)).getLink();
                try {
                    util.loadWebPage(url, ListActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        filtro = findViewById(R.id.editTextFiltro);

        filtro.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                ListActivity.this.adaptes.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    protected List<Item> carregaLista() {

        try {
            if (Tools.verificaConexao(this)) {
                final ProgressDialog dialog = ProgressDialog.show(this, "", "Carregando lista...", true);
                //Log.i("JSON",jo.getString("results"));
                Ion.with(this, "http://lablivre.org/educar/requestapp.php")
                        .setBodyParameter("lat", "-1.4")
                        .setBodyParameter("lng", "-48.5")
                        .setBodyParameter("raio", "9999999999999").asString().withResponse()
                        .setCallback(new FutureCallback<Response<String>>() {
                            @Override
                            public void onCompleted(Exception e, Response<String> request) {
                                String result = request.getResult();
                                try {
                                    JSONObject jo = new JSONObject(result);
                                    JSONArray ja;
                                    ja = jo.getJSONArray("results");

                                    dadosLista = new ArrayList<Item>();

                                    for (int i = 0; i < ja.length(); i++) {
                                        String nome_ = ja.getJSONObject(i).getString("title");
                                        String link_ = ja.getJSONObject(i).getString("webpage");
                                        dadosLista.add(new Item(nome_, link_));

                                    }
                                    dialog.dismiss();
                                    setList();
                                } catch (Exception ejson) {
                                    ejson.printStackTrace();
                                }
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dadosLista;

    }

    public void setList() {
        adaptes = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, dadosLista);
        listview.setAdapter(adaptes);
    }

    public class Item {

        private String mName;
        private String mLink;

        public Item(String name, String link) {

            this.mName = name;
            this.mLink = link;
        }

        public String getLink() {

            return mLink;
        }

        public void setLink(String link) {

            this.mLink = link;
        }

        public String getName() {

            return mName;
        }

        public void setName(String name) {

            this.mName = name;
        }

        @Override
        public String toString() {

            return this.mName;
        }
    }

}
