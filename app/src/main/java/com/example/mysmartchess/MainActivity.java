package com.example.mysmartchess;

import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView response;
    private EditText editTextAddress, editTextPort, editTextMessage;
    private Button buttonConnect, buttonClear, buttonSend, buttonDisconnect;
    private int displayX, displayY;

    private String resultado = "";

    TcpClient mTcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //
        //Abaixo, o código que já veio
        //
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        //
        //Agora, Código da conexão
        //
        setContentView(R.layout.activity_main);
        ;
        editTextAddress = (EditText) findViewById(R.id.addressEditText);
        editTextPort = (EditText) findViewById(R.id.portEditText);
        editTextMessage = (EditText) findViewById(R.id.messageEditText);
        buttonSend = (Button) findViewById(R.id.sendButton);
        buttonConnect = (Button) findViewById(R.id.connectButton);
        buttonDisconnect = (Button) findViewById(R.id.disconnectButton);
        buttonClear = (Button) findViewById(R.id.clearButton);
        response = (TextView) findViewById(R.id.responseTextView);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayX = size.x;
        displayY = size.y;


    }

    public void onConnectClicked(View v) {
        //new ConnectTask().execute("");
        //mostrarToast("Conectando..");
        //Intent intent = new Intent(getApplicationContext(), VoujaexcluirActivity.class);
        //startActivity(intent);

        //new conecta().execute();

        Intent intent = new Intent(getApplicationContext(), AppActivity.class);
        // Intent intent = new Intent(getApplicationContext(), ConnectTestActivity.class);
        startActivity(intent);
    }

    public void onClearClicked(View v) {
        response.setText("");
        //PAra teste apenas, apagar depois

    }

    public void onDisconnectClicked(View v) {
        if (mTcpClient != null) {
            mTcpClient.stopClient();
            mTcpClient = null;
            mostrarToast("Desconectado");
        }
    }

    public void onSendClicked(View v) {
        String message = editTextMessage.getText().toString();
        if (mTcpClient != null)
            mTcpClient.sendMessage(message);
        else mostrarToast("Não conectado");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class conecta extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            response.setText(resultado);
        }

        private StringBuilder createStringBuilder(HashMap<String, String> params) {
            StringBuilder sbParams = new StringBuilder();
            int i = 0;
            for (String key : params.keySet()) {
                try {
                    if (i != 0) {
                        sbParams.append("&");
                    }
                    sbParams.append(key).append("=")
                            .append(URLEncoder.encode(params.get(key), "UTF-8"));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                i++;
            }
            return sbParams;
        }

        @Override
        protected String doInBackground(String... strings) {
            String lichess = "lichess.org";
            String ip = "192.168.0.19";
            //String file = "?P2=2";
            String gameId = "7U1TAm2b";
            String move = "e5c6";
            String file = "api/challenge/" + gameId + "/accept";
            String token = "A2d9ZnMNkBuACKO5";

            HashMap<String, String> stringHashMap = new HashMap<>();
            stringHashMap.put("rated", "false");
            StringBuilder infoToSend = createStringBuilder(stringHashMap);
            try {
                URL url = new URL("https", lichess, file);
                HttpURLConnection client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Authorization", "Bearer " + token);
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("User-Agent", "Mozilla/5.0");
                //client.setRequestProperty("username", "cyrus_the_vyrus");

                //client.setRequestProperty("move", move);
                client.setDoOutput(true);

                DataOutputStream writer = new DataOutputStream(client.getOutputStream());
                writer.writeBytes(infoToSend.toString());

                client.connect();

                Log.d("ciro", "here 1");

                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                StringBuilder responseOutput = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

                Log.d("ciro", "mensagem: " + responseOutput.toString());
                resultado = responseOutput.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("ciro", "deu certo não, véi!");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("ciro", "deu certo não, véi!!");
            }

            return null;
        }
    }

    public void mostrarToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
