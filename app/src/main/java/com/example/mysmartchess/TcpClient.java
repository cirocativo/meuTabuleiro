package com.example.mysmartchess;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {

    private String TAG = TcpClient.class.getSimpleName();
    private String SERVER_IP = "192.168.0.19"; //server IP address
    //private String SERVER_IP = "192.168.100.119"; //server IP address
    private int SERVER_PORT = 70;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private volatile BufferedReader mBufferIn;

    private Socket socket = null;

    //Variáveis relacionadas a teste de conexão
    private volatile boolean isConnected = false;
    private volatile boolean isTestingConnection = false;
    private int contagemDeConexoes = 0;

    private boolean isTryingToConnect = false;
    private boolean isReceivingMessage = false;

    private Thread threadReceiveMessage;
    private Thread threadCreateConnection;


    private String messageConnection = "";
    private long startTime = SystemClock.elapsedRealtime();
    private long timeInterval;
    private long startTimeReceiveMessage;


    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */

    public void stopClient() {
        mRun = false;
        mMessageListener = null;
        stopClientInterno();
    }


    private void stopClientInterno() {
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        //mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }


    public void run() {

        mRun = true;
        //Log.d("ciro", "vai entrar em createconnection");
        //cria uma conexão com o servidor
        contagemDeConexoes++;
        //createConnection(contagemDeConexoes);
        tryToConnect(contagemDeConexoes);
        //Log.d("ciro", "vai criar thread de teste de conexão");
        //cria uma thread que fica testando conexão
        createConnectionTestThread();
        //Log.d("ciro", "vai criar thread de receber mensagem");

    }

    private void createConnectionTestThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (mRun) {
                    timeInterval = SystemClock.elapsedRealtime() - startTime;

                    //A cada 5 segundos manda msg pro servidor, e espera um "ok". Se não chegar em 5 segundos, cria uma nova conexão
                    if (timeInterval >= 4000) {
                        if (isConnected) {
                            if (isTestingConnection) {
                                if (!messageConnection.equals("ok")) {
                                    Log.d("ciro", "Não estamos conectados");
                                    isConnected = false;
                                    //threadCreateConnection.interrupt();
                                    stopClientInterno();
                                    contagemDeConexoes++;
                                    //createConnection(contagemDeConexoes);
                                    tryToConnect(contagemDeConexoes);
                                    startTime = SystemClock.elapsedRealtime();
                                } else {
                                    //Log.d("ciro", "conexão normal");
                                    isConnected = true;
                                    messageConnection = "";
                                }
                                isTestingConnection = false;

                            } else {
                                //Log.d("ciro", "enviando teste");
                                sendMessage("{\"Connection Test\": true}");
                                startTime = SystemClock.elapsedRealtime();
                                isTestingConnection = true;
                            }
                        } else {
                            //Não está conectado
                            //visto a demora, finaliza processo e tenta nova conexão
                            //Log.d("ciro", "Demorou, estamos interrompendo e criando nova conexão");
                            //threadCreateConnection.interrupt();
                            contagemDeConexoes++;
                            //createConnection(contagemDeConexoes);
                            tryToConnect(contagemDeConexoes);
                            startTime = SystemClock.elapsedRealtime();
                        }
                    }

                }
            }
        };
        Thread threadConnectionTest = new Thread(runnable);
        threadConnectionTest.start();
    }

    private void receiveMessage(int conexao) {
        threadReceiveMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("ciro", "iniciou threadReceive numero " + conexao);
                while (mRun) {
                    if (isConnected) {
                        //finaliza thread antiga e mantém somente a nova
                        if (contagemDeConexoes != conexao)
                            break;
                        try {
                            if (SystemClock.elapsedRealtime() - startTimeReceiveMessage > 10000) {
                                //Log.d("ciro", "Receiving, firme e forte");
                                startTimeReceiveMessage = SystemClock.elapsedRealtime();
                            }

                            mServerMessage = mBufferIn.readLine();
                            if (mServerMessage != null && mMessageListener != null) {
                                //call the method messageReceived from MyActivity class
                                mMessageListener.messageReceived(mServerMessage);
                                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
                                if (isTestingConnection) {
                                    if (getMoveFromJson(mServerMessage).equals("ok"))
                                        messageConnection = getMoveFromJson(mServerMessage);
                                    Log.d("ciro", "mensagem de teste recebida: " + messageConnection);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d("ciro", "finalizou threadReceive numero " + conexao);
            }
        });
        threadReceiveMessage.start();
    }

    private boolean testConnection(int conexao) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + SERVER_IP);
            int mExitValue = mIpAddrProcess.waitFor();
            // Log.d("ciro", " mExitValue " + mExitValue);
            if (mExitValue == 0) {
                Log.d("ciro", "resposta isConnected " + conexao + ": true");
                return true;
            } else {
                Log.d("ciro", "resposta isConnected " + conexao + ": false");
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("ciro", " Exception:" + e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ciro", " Exception:" + e);
        }
        return false;
    }

    private void tryToConnect(int conexao) {
        Log.d("ciro","Tentando conectar");
        if (testConnection(conexao)) {
            try {
                Log.d("ciro", "conexão bem sucedida, vai instanciar o socket");
                //here you must put your computer's IP address.
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);


                //create a socket to make the connection with the server
                socket = new Socket(serverAddr, SERVER_PORT);
                //sends the message to the server
                if (conexao == contagemDeConexoes) {
                    Log.d("ciro", "instanciou o socket " + conexao + " principal");
                    mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    //receives the message which the server sends back
                    mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    //Log.d("ciro", "tentando conectar-se à ESP");
                    isConnected = true;

                    //thread que fica recebendo mensagens do servidor
                    receiveMessage(conexao);
                    startTimeReceiveMessage = SystemClock.elapsedRealtime();
                } else {
                    Log.d("ciro", "instanciou o socket " + conexao + ", mas não vale de nada");
                }
            } catch (IOException e) {
                Log.d("ciro", "Não deu certo, jovem");
                e.printStackTrace();
            }
        }
    }

    private String getMoveFromJson(String jsonMessage) {
        JSONObject comandoJson;
        String comando = "";
        try {
            comandoJson = new JSONObject(jsonMessage);
            comando = comandoJson.getString("comando");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return comando;
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
//class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}