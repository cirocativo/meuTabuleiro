package com.example.mysmartchess;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ConnectTestActivity extends AppCompatActivity {

    private ClientTest cliente;
    private TextView respostaTextView;
    private EditText mensagemEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_test);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        cliente = new ClientTest();
        respostaTextView = findViewById(R.id.respostaTextView);
        mensagemEditText = findViewById(R.id.mensagemEditText);

    }

    public void onConnectClicked(View view) {
        if (!cliente.isConnected()) {
            Log.d("ciro","conectandoow");
            cliente = new ClientTest();
            cliente.execute();
        } else Log.d("ciro", "ainda ta conectado, meu querido");

    }

    public void onSendClicked(View view) {
        try {

            cliente.sendMessage(mensagemEditText.getText().toString());
            respostaTextView.setText("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDisconnectClicked(View view) {
        cliente.disconnect();
        cliente.cancel(true);
    }

}
