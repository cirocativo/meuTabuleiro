package com.example.mysmartchess;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientTest extends AsyncTask {

    private String SERVER_IP = "192.168.0.19"; //server IP address
    private int SERVER_PORT = 70;

    private Socket cliente = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private boolean conectado = false;

    @Override
    protected Object doInBackground(Object[] objects) {
        //public void conectar(){

        while (true) {
            if (!isConnected()) {
              criaConexao();
            }
        }
    }


    private void criaConexao(){
        try {
            Log.d("ciro","vamo conectar");
            InetAddress enderecoServer = InetAddress.getByName(SERVER_IP);
            cliente = new Socket(enderecoServer, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream())), true);
        } catch (IOException e) {
            Log.d("ciro", "olha onde chegamos");
            e.printStackTrace();
        }
    }

    //Código que peguei pronto, verifica se consegue contato com o servidor
    public boolean isConnected() {
        if (cliente != null) {
            //Log.d("ciro", "executeCommand");
            Runtime runtime = Runtime.getRuntime();
            try {
                Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + SERVER_IP);
                int mExitValue = mIpAddrProcess.waitFor();
                // Log.d("ciro", " mExitValue " + mExitValue);
                if (mExitValue == 0) {
                    conectado = true;
                    return true;
                } else {
                    Log.d("ciro","desconectouu");
                    conectado = false;
                    return false;
                }
            } catch (InterruptedException ignore) {
                ignore.printStackTrace();
                Log.d("ciro", " Exception:" + ignore);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("ciro", " Exception:" + e);
            }
            conectado = false;
            return false;
        }
        conectado = false;
        return false;
    }

    public void sendMessage(String string) {
        if(conectado) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (out != null) {
                            Log.d("ciro", "Vamos enviar mensagem");
                            out.println(string);
                            out.flush();
                            Log.d("ciro", "mensagem enviada!");
                        } else {
                            Log.d("ciro", "out vazio.");
                        }

                    } catch (Exception e) {
                        Log.d("ciro", "Mensagem NÃO enviada");
                        e.printStackTrace();
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }else {
            criaConexao();
            sendMessage(string);
        }
    }

    public void disconnect() {
        try {
            cliente.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}