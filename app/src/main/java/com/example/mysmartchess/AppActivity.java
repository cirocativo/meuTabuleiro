package com.example.mysmartchess;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.example.mysmartchess.EngineS22k.ChessBoard;
import com.example.mysmartchess.EngineS22k.ChessBoardUtil;
import com.example.mysmartchess.EngineS22k.move.MoveWrapper;
import com.example.mysmartchess.EngineS22k.move.PV;
import com.example.mysmartchess.EngineS22k.search.NegamaxUtil;
import com.example.mysmartchess.EngineS22k.search.TimeUtil;
import com.example.mysmartchess.Jogo.TabuleiroDeXadrez;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppActivity extends AppCompatActivity {

    final static boolean ENGINE_TURN = false;
    final static boolean USER_TURN = true;

    final static char[] letras = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

    private TcpClient mTcpClient = null;

    private int[] notacaoRecebida = new int[4];
    private int[] notacaoClasseInterna = new int[4];
    private String notacaoOficialRecebida;
    private String mensagemRecebidaDoESP = "";
    private List<String> listaStringsEnviadas = new ArrayList<>();
    private Thread threadSendMessage;


    //Relativos aos movimentos especiais
    private boolean rockGrandeAconteceu = false;
    private boolean rockPequenoAconteceu = false;
    private boolean enPassantAconteceu = false;
    private boolean pecaFoiTomada = false;

    private List<ImageView> listaImageView = new ArrayList<>();
    private TabuleiroDeXadrez tabuleiroDeXadrez;

    private ChessBoard cb;

    //Relativos à Engine
    private boolean engineTurn = false;
    private ThreadEngineMove threadEngineMove;
    private MyEngine myengine;
    private Handler handler;

    //Relativo ao Lichess
    private LichessInfo lichessInfo = new LichessInfo();
    private static String tokenLichess = "A2d9ZnMNkBuACKO5";
    private String gameIdLichess = "";
    private boolean lichessAtivado = false; //ativado se o jogador estiver jogando online
    boolean updateTextViews = false; //pra colocar os nomes dos jogadores, avisando o handler
    volatile String username = "";// pra salver o nome do jogador

    //Relativo à persistência de dados
    SharedPreferences sharedPreferencesTabuleiro;
    SharedPreferences sharedPreferencesLichess;

    private EditText lanceEditText;

    private TextView textViewJogadorBrancas, textViewJogadorNegras;

    public ImageView imageViewTabuleiro, imageViewTorreBranco1, imageViewTorreBranco2, imageViewBispoBranco1,
            imageViewBispoBranco2, imageViewCavaloBranco1, imageViewCavaloBranco2, imageViewDamaBranco, imageViewReiBranco,
            imageViewPeaoBranco1, imageViewPeaoBranco2, imageViewPeaoBranco3, imageViewPeaoBranco4, imageViewPeaoBranco5,
            imageViewPeaoBranco6, imageViewPeaoBranco7, imageViewPeaoBranco8,

    imageViewTorrePreto1, imageViewTorrePreto2, imageViewBispoPreto1, imageViewBispoPreto2, imageViewCavaloPreto1,
            imageViewCavaloPreto2, imageViewDamaPreto, imageViewReiPreto, imageViewPeaoPreto1, imageViewPeaoPreto2,
            imageViewPeaoPreto3, imageViewPeaoPreto4, imageViewPeaoPreto5, imageViewPeaoPreto6, imageViewPeaoPreto7,
            imageViewPeaoPreto8;

    public ImageView imageViewDamaPromocao, imageViewCavaloPromocao, imageViewBispoPromocao, imageViewTorrePromocao;//imagens para promoção

    public ImageView imageViewRetangulo;

    public ImageView imageViewQuadrado1; //dá a indicacação do movimento das peças
    public ImageView imageViewQuadrado2;
    public int displayX, displayY; //tamanho da tela do celular
    public int tabuleiroTamanho; //tamanho do tabuleiro em pixels
    public int retanguloX, retanguloY;
    private boolean promocao;

    private boolean check = true;//para dar mensagem de conexão bem sucedida

    private LoadingDialog loadingDialog; //responsavel por fazer esperar conexão ao tabuleiro

    @Override
    protected void onStop() {
        //mTcpClient.stopClient();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Log.d("ciro", "voltar pressionado");
        //loadingDialog.setCancelable(true);
        loadingDialog.dismissDialog();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mTcpClient != null)
            mTcpClient.stopClient();
        super.onDestroy();
    }

    public void onConnectClicked(View view) {
        if (mTcpClient == null)
            iniciaConexao();
        else mostrarToast("Conexão já foi iniciada");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lanceEditText = findViewById(R.id.lanceEditText);

        iniciaConexao();

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        loadingDialog = new LoadingDialog(AppActivity.this);

        loadingDialog.startLoadingDialog();

        sharedPreferencesTabuleiro = getSharedPreferences("tabuleiro", 0);
        String gameStatus = sharedPreferencesTabuleiro.getString("status", "nada aqui /2");
        if (gameStatus != null && gameStatus.equals("start")){
            Log.d("ciro","precisa atualizar a tela pro jogo em questao");
        }else{
            Log.d("ciro", "pode começar do zero");
        }


        textViewJogadorBrancas = findViewById(R.id.textViewJogadorBrancas);
        textViewJogadorNegras = findViewById(R.id.textViewJogadorNegras);

        imageViewTabuleiro = findViewById(R.id.tabuleiro);
        imageViewTorreBranco1 = findViewById(R.id.pecatorrebranco1);
        imageViewTorreBranco2 = findViewById(R.id.pecatorrebranco2);
        imageViewCavaloBranco1 = findViewById(R.id.pecacavalobranco1);
        imageViewCavaloBranco2 = findViewById(R.id.pecacavalobranco2);
        imageViewBispoBranco1 = findViewById(R.id.pecabispobranco1);
        imageViewBispoBranco2 = findViewById(R.id.pecabispobranco2);
        imageViewDamaBranco = findViewById(R.id.pecadamabranco);
        imageViewReiBranco = findViewById(R.id.pecareibranco);
        imageViewPeaoBranco1 = findViewById(R.id.pecapeaobranco1);
        imageViewPeaoBranco2 = findViewById(R.id.pecapeaobranco2);
        imageViewPeaoBranco3 = findViewById(R.id.pecapeaobranco3);
        imageViewPeaoBranco4 = findViewById(R.id.pecapeaobranco4);
        imageViewPeaoBranco5 = findViewById(R.id.pecapeaobranco5);
        imageViewPeaoBranco6 = findViewById(R.id.pecapeaobranco6);
        imageViewPeaoBranco7 = findViewById(R.id.pecapeaobranco7);
        imageViewPeaoBranco8 = findViewById(R.id.pecapeaobranco8);

        imageViewTorrePreto1 = findViewById(R.id.pecatorrepreto1);
        imageViewTorrePreto2 = findViewById(R.id.pecatorrepreto2);
        imageViewCavaloPreto1 = findViewById(R.id.pecacavalopreto1);
        imageViewCavaloPreto2 = findViewById(R.id.pecacavalopreto2);
        imageViewBispoPreto1 = findViewById(R.id.pecabispopreto1);
        imageViewBispoPreto2 = findViewById(R.id.pecabispopreto2);
        imageViewDamaPreto = findViewById(R.id.pecadamapreto);
        imageViewReiPreto = findViewById(R.id.pecareipreto);
        imageViewPeaoPreto1 = findViewById(R.id.pecapeaopreto1);
        imageViewPeaoPreto2 = findViewById(R.id.pecapeaopreto2);
        imageViewPeaoPreto3 = findViewById(R.id.pecapeaopreto3);
        imageViewPeaoPreto4 = findViewById(R.id.pecapeaopreto4);
        imageViewPeaoPreto5 = findViewById(R.id.pecapeaopreto5);
        imageViewPeaoPreto6 = findViewById(R.id.pecapeaopreto6);
        imageViewPeaoPreto7 = findViewById(R.id.pecapeaopreto7);
        imageViewPeaoPreto8 = findViewById(R.id.pecapeaopreto8);

        criaListaImageView();

        imageViewQuadrado1 = findViewById(R.id.quadrado1);
        imageViewQuadrado2 = findViewById(R.id.quadrado2);

        //Quadrado só aparece quando alguém faz algum lance
        imageViewQuadrado1.setVisibility(View.INVISIBLE);
        imageViewQuadrado2.setVisibility(View.INVISIBLE);

        //Pega o tamanho da tela
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayX = size.x;
        displayY = size.y;
        tabuleiroTamanho = displayX;

        ajustarImagens(); //ajusta tamanho do tabuleiro e das pecas ao celular

        arrumarTabuleiro(); //Coloca as peças em seus devidos lugares

        tabuleiroDeXadrez = new TabuleiroDeXadrez(listaImageView);

        //Cria handle para lidar com o retorno da classe ThreadEngineMove

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                engineMoveReceived(msg);
            }
        };

        myengine = new MyEngine(handler);


        imageViewBispoPromocao = findViewById(R.id.bispoButton);
        imageViewCavaloPromocao = findViewById(R.id.cavaloButton);
        imageViewDamaPromocao = findViewById(R.id.damaButton);
        imageViewTorrePromocao = findViewById(R.id.torreButton);
        imageViewRetangulo = findViewById(R.id.retangulo);


        //Cálculos baseados em pura geometria, para funcionar em qualquer tamanho de dispositivo móvel
        //Posiciona o retângulo e os botões de promoção no centro do tabuleiro

        retanguloY = imageViewRetangulo.getLayoutParams().height = tabuleiroTamanho / 3;
        retanguloX = imageViewRetangulo.getLayoutParams().width = 862 * tabuleiroTamanho / (360 * 3);

        float posRetX = (float) (tabuleiroTamanho - retanguloX) / 2;
        float posRetY = (float) (tabuleiroTamanho - retanguloY) / 2;

        imageViewRetangulo.setX(posRetX);
        imageViewRetangulo.setY(posRetY);

        ajustaImagemsPromocao(imageViewBispoPromocao);
        ajustaImagemsPromocao(imageViewCavaloPromocao);
        ajustaImagemsPromocao(imageViewTorrePromocao);
        ajustaImagemsPromocao(imageViewDamaPromocao);

        float posX = (float) (retanguloX - 2 * retanguloY) / 5;
        float posY = (float) retanguloY / 4 + posRetY;
        float imageX = (float) retanguloY / 2;
        imageViewDamaPromocao.setX(posX + posRetX);
        imageViewTorrePromocao.setX(posX * 2 + imageX + posRetX);
        imageViewBispoPromocao.setX(posX * 3 + 2 * imageX + posRetX);
        imageViewCavaloPromocao.setX(posX * 4 + 3 * imageX + posRetX);
        imageViewDamaPromocao.setY(posY);
        imageViewTorrePromocao.setY(posY);
        imageViewBispoPromocao.setY(posY);
        imageViewCavaloPromocao.setY(posY);

        imageViewRetangulo.setVisibility(View.INVISIBLE);
        imageViewBispoPromocao.setVisibility(View.INVISIBLE);
        imageViewCavaloPromocao.setVisibility(View.INVISIBLE);
        imageViewDamaPromocao.setVisibility(View.INVISIBLE);
        imageViewTorrePromocao.setVisibility(View.INVISIBLE);
    }

    public void ajustaImagemsPromocao(ImageView imageView) {
        imageView.getLayoutParams().width = retanguloY / 2;
        imageView.getLayoutParams().height = retanguloY / 2;
        //imageView.setBackgroundColor(200);
    }

    private void criaListaImageView() {
//Essa lista é usada para poder linkar cada imagem à peça, quando for criado o objeto TabuleiroDeXadrez
        listaImageView.add(imageViewTorreBranco1);
        listaImageView.add(imageViewCavaloBranco1);
        listaImageView.add(imageViewBispoBranco1);
        listaImageView.add(imageViewDamaBranco);
        listaImageView.add(imageViewReiBranco);
        listaImageView.add(imageViewBispoBranco2);
        listaImageView.add(imageViewCavaloBranco2);
        listaImageView.add(imageViewTorreBranco2);
        listaImageView.add(imageViewPeaoBranco1);
        listaImageView.add(imageViewPeaoBranco2);
        listaImageView.add(imageViewPeaoBranco3);
        listaImageView.add(imageViewPeaoBranco4);
        listaImageView.add(imageViewPeaoBranco5);
        listaImageView.add(imageViewPeaoBranco6);
        listaImageView.add(imageViewPeaoBranco7);
        listaImageView.add(imageViewPeaoBranco8);

        listaImageView.add(imageViewTorrePreto1);
        listaImageView.add(imageViewCavaloPreto1);
        listaImageView.add(imageViewBispoPreto1);
        listaImageView.add(imageViewDamaPreto);
        listaImageView.add(imageViewReiPreto);
        listaImageView.add(imageViewBispoPreto2);
        listaImageView.add(imageViewCavaloPreto2);
        listaImageView.add(imageViewTorrePreto2);
        listaImageView.add(imageViewPeaoPreto1);
        listaImageView.add(imageViewPeaoPreto2);
        listaImageView.add(imageViewPeaoPreto3);
        listaImageView.add(imageViewPeaoPreto4);
        listaImageView.add(imageViewPeaoPreto5);
        listaImageView.add(imageViewPeaoPreto6);
        listaImageView.add(imageViewPeaoPreto7);
        listaImageView.add(imageViewPeaoPreto8);
    }

    private int[] converteLetraPraNumero(String palavra) {

        int inicioX = 0, finalX = 0;

        char charInicioX = palavra.charAt(0);
        int inicioY = Integer.parseInt(palavra.charAt(1) + "");
        char charFinalX = palavra.charAt(2);
        int finalY = Integer.parseInt(palavra.charAt(3) + "");

        //a=0, b=1, etc...
        for (int i = 0; i < 8; i++) {
            if (charInicioX == letras[i])
                inicioX = i;
            if (charFinalX == letras[i])
                finalX = i;
        }
        //fica x de 0 a 7, e y 7 a 0, notacao da do tabuleiro gráfico do celular
        int[] retorno = {inicioX, 8 - inicioY, finalX, 8 - finalY};
        return retorno;
    }

    /*
       -----------------notacao do tabuleiro do celular
       0
       1
       2
       3
       4
       5
       6
       7,0 1 2 3 4 5 6 7

       -----------------notação real
       8
       7
       6
       5
       4
       3
       2
       1,a b c d e f g h

       -----------------notação da classe TabuleiroDeXadrez
       7
       6
       5
       4
       3
       2
       1
       0,0 1 2 3 4 5 6 7
     */

    public void iniciaConexao() {
        new AppActivity.ConnectTask().execute("");
    }

    //Arruma as peças no tabuleiro
    public void arrumarTabuleiro() {
        setarPosicaoPeca(imageViewTorreBranco1, 0, 7);
        setarPosicaoPeca(imageViewTorreBranco2, 7, 7);
        setarPosicaoPeca(imageViewCavaloBranco1, 1, 7);
        setarPosicaoPeca(imageViewCavaloBranco2, 6, 7);
        setarPosicaoPeca(imageViewDamaBranco, 3, 7);
        setarPosicaoPeca(imageViewReiBranco, 4, 7);
        setarPosicaoPeca(imageViewBispoBranco1, 2, 7);
        setarPosicaoPeca(imageViewBispoBranco2, 5, 7);
        setarPosicaoPeca(imageViewPeaoBranco1, 0, 6);
        setarPosicaoPeca(imageViewPeaoBranco2, 1, 6);
        setarPosicaoPeca(imageViewPeaoBranco3, 2, 6);
        setarPosicaoPeca(imageViewPeaoBranco4, 3, 6);
        setarPosicaoPeca(imageViewPeaoBranco5, 4, 6);
        setarPosicaoPeca(imageViewPeaoBranco6, 5, 6);
        setarPosicaoPeca(imageViewPeaoBranco7, 6, 6);
        setarPosicaoPeca(imageViewPeaoBranco8, 7, 6);
        setarPosicaoPeca(imageViewTorrePreto1, 0, 0);
        setarPosicaoPeca(imageViewTorrePreto2, 7, 0);
        setarPosicaoPeca(imageViewCavaloPreto1, 1, 0);
        setarPosicaoPeca(imageViewCavaloPreto2, 6, 0);
        setarPosicaoPeca(imageViewDamaPreto, 3, 0);
        setarPosicaoPeca(imageViewReiPreto, 4, 0);
        setarPosicaoPeca(imageViewBispoPreto1, 2, 0);
        setarPosicaoPeca(imageViewBispoPreto2, 5, 0);
        setarPosicaoPeca(imageViewPeaoPreto1, 0, 1);
        setarPosicaoPeca(imageViewPeaoPreto2, 1, 1);
        setarPosicaoPeca(imageViewPeaoPreto3, 2, 1);
        setarPosicaoPeca(imageViewPeaoPreto4, 3, 1);
        setarPosicaoPeca(imageViewPeaoPreto5, 4, 1);
        setarPosicaoPeca(imageViewPeaoPreto6, 5, 1);
        setarPosicaoPeca(imageViewPeaoPreto7, 6, 1);
        setarPosicaoPeca(imageViewPeaoPreto8, 7, 1);

        //setarPosicaoPeca(imageViewQuadrado1,-28,7);
        //setarPosicaoPeca(imageViewQuadrado2,-29,7);
    }


    //A notação da classe Tabuleiro de xadrez não foi bem pensada e troquei x pelo y, então tem que fazer isso
    private void converteParaClasseInterna(String string) {
        notacaoRecebida = converteLetraPraNumero(string);
        notacaoOficialRecebida = string;

        notacaoClasseInterna[0] = 7 - notacaoRecebida[1];
        notacaoClasseInterna[1] = notacaoRecebida[0];
        notacaoClasseInterna[2] = 7 - notacaoRecebida[3];
        notacaoClasseInterna[3] = notacaoRecebida[2];
    }

    private boolean isRock(int origemX, int finalX) {
        ImageView imageViewDestino = tabuleiroDeXadrez.getImageView(notacaoClasseInterna[2], notacaoClasseInterna[3]);
        //Se for um rei, e tiver andado duas ou mais casas, é roque
        if (imageViewDestino == imageViewReiBranco || imageViewDestino == imageViewReiPreto) {
            return Math.abs(origemX - finalX) > 1;
        } else {
            return false;
        }
    }

    public void onVaiClicked(View view) {
        String lance = lanceEditText.getText().toString();
        moverPeca(lance);
    }


    public boolean moverPeca(String string) {
        //moverPeca(notacaoRecebida[0], notacaoRecebida[1], notacaoRecebida[2], notacaoRecebida[3]);

        //faz as conversões necessárias
        converteParaClasseInterna(string);

        int origemX = notacaoRecebida[0];
        int origemY = notacaoRecebida[1];
        int finalX = notacaoRecebida[2];
        int finalY = notacaoRecebida[3];

        //Peça a ser tomada, se existir
        ImageView imageViewInvisible = tabuleiroDeXadrez.getImageView(notacaoClasseInterna[2], notacaoClasseInterna[3]);

        //Salva imagem do peao do lado caso seja en passant
        ImageView imageViewPeao = tabuleiroDeXadrez.getImageView(notacaoClasseInterna[0], notacaoClasseInterna[3]);

        if (tabuleiroDeXadrez.moverPeca(notacaoClasseInterna[0], notacaoClasseInterna[1], notacaoClasseInterna[2], notacaoClasseInterna[3])) {

            ImageView imageView = tabuleiroDeXadrez.getImageView(notacaoClasseInterna[2], notacaoClasseInterna[3]);

            //no lichess, o rock é  zuado, é a única situação em que o destino fica desocupado
            if (tabuleiroDeXadrez.isCasaDesocupada(notacaoClasseInterna[2], notacaoClasseInterna[3])) {
                Log.d("ciro", "\n\nMALUQUICEEEEEEEEEEEEE\nAAAAAAAAAAAAAAAAAHHHHH\n\n");
                //Temos que deixar do jeito CERTO, e não do jeito lixess... to de brinks, lichess é top
                if (notacaoClasseInterna[3] == 7) {//é roque pequeno

                    finalX = 6;
                    notacaoOficialRecebida.replace('h', 'g');
                    notacaoClasseInterna[3] = 6;

                }
                if (notacaoClasseInterna[3] == 0) { // é roque grande

                    finalX = 2;
                    notacaoOficialRecebida.replace('a', 'c');
                    notacaoClasseInterna[3] = 2;
                }
            }

            if (imageViewInvisible != null) {
                imageViewInvisible.setVisibility(View.INVISIBLE);
                pecaFoiTomada = true;
            }

            //Se for enPassant, tem que apagar a imagem do peão tomado
            if (tabuleiroDeXadrez.isEnPassant()) {
                imageViewPeao.setVisibility(View.GONE);
                enPassantAconteceu = true;
            }

            //Se for roque, deve mexer na imagem da torre também
            if (isRock(origemX, finalX)) {
                int torreXInicio, torreXDestino;

                //roque grande
                if (finalX == 2) {
                    torreXDestino = 3;
                    torreXInicio = 0;
                    rockGrandeAconteceu = true;
                }
                //roque pequeno
                else {
                    torreXDestino = 5;
                    torreXInicio = 7;
                    rockPequenoAconteceu = true;
                }

                ImageView torreRock = tabuleiroDeXadrez.getImageView(notacaoClasseInterna[0], torreXDestino);
                moverImagem(torreRock, torreXInicio, origemY, torreXDestino, origemY, 200);
            }

            //Se for promoção, faz o devido tratamento
            if (tabuleiroDeXadrez.isPromocao()) {
                trataPromocao(View.VISIBLE);
                promocao = true;

            }

            moverImagem(imageView, origemX, origemY, finalX, finalY, 200);
            setarPosicaoPeca(imageViewQuadrado1, origemX, origemY);
            setarPosicaoPeca(imageViewQuadrado2, finalX, finalY);

            imageViewQuadrado1.setVisibility(View.VISIBLE);
            imageViewQuadrado2.setVisibility(View.VISIBLE);
/*
            if (!promocao)
                if (engineTurn) {
                    //Log.d("mano", "ta chamando a maquina pra jogar");
                    if (lichessAtivado) {
                        Log.d("ciro", "Enviando lance ao lichess: " + notacaoOficialRecebida);
                        sendToLichessThread("lance", handler);
                    } else {
                        myengine.setComando(notacaoOficialRecebida);
                        threadEngineMove = new ThreadEngineMove(myengine);
                        threadEngineMove.start();
                    }
                } else engineTurn = true;
                */
            if (promocao && engineTurn) {
                Log.d("mano", "vai promover");
                doEnginePromotion();
            }

            engineTurn = !engineTurn;
            return true;
        }
        //Log.d("mano", "saiu no moverPeca");
        return false;
    }

    public void doEnginePromotion() {
        char c = notacaoOficialRecebida.charAt(4);
        switch (c) {
            case 'q':
                onDamaClicked(this.imageViewDamaPromocao);
                break;
            case 'r':
                onTorreClicked(this.imageViewTorrePromocao);
                break;
            case 'b':
                onBispoClicked(this.imageViewBispoPromocao);
                break;
            case 'n':
                onCavaloClicked(this.imageViewCavaloPromocao);
                break;
        }
    }

    public void onDamaClicked(View view) {
        notacaoOficialRecebida += "q";
        if (tabuleiroDeXadrez.isPeaoAPromoverBranco())
            promovePeao(R.drawable.dama_branco, 0);
        else promovePeao(R.drawable.dama_preto, 0);

    }

    public void onTorreClicked(View view) {
        notacaoOficialRecebida += "r";
        if (tabuleiroDeXadrez.isPeaoAPromoverBranco())
            promovePeao(R.drawable.torre_branco, 1);
        else promovePeao(R.drawable.torre_preto, 1);

    }

    public void onBispoClicked(View view) {
        notacaoOficialRecebida += "b";
        if (tabuleiroDeXadrez.isPeaoAPromoverBranco())
            promovePeao(R.drawable.bispo_branco, 2);
        else promovePeao(R.drawable.bispo_preto, 2);

    }

    public void onCavaloClicked(View view) {
        notacaoOficialRecebida += "n";
        if (tabuleiroDeXadrez.isPeaoAPromoverBranco())
            promovePeao(R.drawable.cavalo_branco, 3);
        else promovePeao(R.drawable.cavalo_preto, 3);

    }

    public void promovePeao(int drawable, int index) {
        Log.d("ciro", "quem ta em " + notacaoClasseInterna[2] + "," + notacaoClasseInterna[3] + " será promovido, pois " + notacaoOficialRecebida);
        ImageView peaoPromovidoImageView = tabuleiroDeXadrez.getImageView(notacaoClasseInterna[2], notacaoClasseInterna[3]);
        peaoPromovidoImageView.setImageResource(drawable);
        tabuleiroDeXadrez.promovePeao(notacaoClasseInterna[2], notacaoClasseInterna[3], peaoPromovidoImageView, index);
        trataPromocao(View.INVISIBLE);
        promocao = false;

        if (lichessAtivado) {
            Log.d("ciro", "Enviando lance ao lichess: " + notacaoOficialRecebida);
            sendToLichessThread("lance", handler);
        } else {
            myengine.setComando(notacaoOficialRecebida);
            threadEngineMove = new ThreadEngineMove(myengine);
            threadEngineMove.start();
        }
    }

    public void trataPromocao(int visibilidade) {
        if (tabuleiroDeXadrez.isPeaoAPromoverBranco()) {
            imageViewBispoPromocao.setImageResource(R.drawable.bispo_branco);
            imageViewCavaloPromocao.setImageResource(R.drawable.cavalo_branco);
            imageViewDamaPromocao.setImageResource(R.drawable.dama_branco);
            imageViewTorrePromocao.setImageResource(R.drawable.torre_branco);
        } else {
            imageViewBispoPromocao.setImageResource(R.drawable.bispo_preto);
            imageViewCavaloPromocao.setImageResource(R.drawable.cavalo_preto);
            imageViewDamaPromocao.setImageResource(R.drawable.dama_preto);
            imageViewTorrePromocao.setImageResource(R.drawable.torre_preto);
        }
        imageViewRetangulo.setVisibility(visibilidade);
        imageViewBispoPromocao.setVisibility(visibilidade);
        imageViewCavaloPromocao.setVisibility(visibilidade);
        imageViewDamaPromocao.setVisibility(visibilidade);
        imageViewTorrePromocao.setVisibility(visibilidade);
    }

    public void moverImagem(ImageView imageView, int origemX, int origemY, int finalX, int finalY, int tempo) {

        int origemXConv = origemX * tabuleiroTamanho / 8;
        int origemYConv = origemY * tabuleiroTamanho / 8;
        int finalXConv = finalX * tabuleiroTamanho / 8;
        int finalYConv = finalY * tabuleiroTamanho / 8;

        AnimatorSet animSetXY = new AnimatorSet();

        ObjectAnimator x = ObjectAnimator.ofFloat(imageView,
                "translationX", origemXConv, finalXConv);

        ObjectAnimator y = ObjectAnimator.ofFloat(imageView,
                "translationY", origemYConv, finalYConv);

        animSetXY.playTogether(x, y);
        animSetXY.setInterpolator(new LinearInterpolator());
        animSetXY.setDuration(tempo);
        animSetXY.start();
    }

    //Move a peça pelo tabuleiro
    public void setarPosicaoPeca(ImageView imageView, int valorX, int valorY) {
        moverImagem(imageView, valorX, valorY, valorX, valorY, 0);
    }

    public void ajustarImagens() {
        //Ajusta tamanho do tabuleiro para o celular
        imageViewTabuleiro.getLayoutParams().width = tabuleiroTamanho;
        imageViewTabuleiro.getLayoutParams().height = tabuleiroTamanho;
        ajustarTamanhoDaPeca(imageViewTorreBranco1);
        ajustarTamanhoDaPeca(imageViewTorreBranco2);
        ajustarTamanhoDaPeca(imageViewCavaloBranco1);
        ajustarTamanhoDaPeca(imageViewCavaloBranco2);
        ajustarTamanhoDaPeca(imageViewBispoBranco1);
        ajustarTamanhoDaPeca(imageViewBispoBranco2);
        ajustarTamanhoDaPeca(imageViewReiBranco);
        ajustarTamanhoDaPeca(imageViewDamaBranco);
        ajustarTamanhoDaPeca(imageViewPeaoBranco1);
        ajustarTamanhoDaPeca(imageViewPeaoBranco2);
        ajustarTamanhoDaPeca(imageViewPeaoBranco3);
        ajustarTamanhoDaPeca(imageViewPeaoBranco4);
        ajustarTamanhoDaPeca(imageViewPeaoBranco5);
        ajustarTamanhoDaPeca(imageViewPeaoBranco6);
        ajustarTamanhoDaPeca(imageViewPeaoBranco7);
        ajustarTamanhoDaPeca(imageViewPeaoBranco8);
        ajustarTamanhoDaPeca(imageViewTorrePreto1);
        ajustarTamanhoDaPeca(imageViewTorrePreto2);
        ajustarTamanhoDaPeca(imageViewCavaloPreto1);
        ajustarTamanhoDaPeca(imageViewCavaloPreto2);
        ajustarTamanhoDaPeca(imageViewBispoPreto1);
        ajustarTamanhoDaPeca(imageViewBispoPreto2);
        ajustarTamanhoDaPeca(imageViewReiPreto);
        ajustarTamanhoDaPeca(imageViewDamaPreto);
        ajustarTamanhoDaPeca(imageViewPeaoPreto1);
        ajustarTamanhoDaPeca(imageViewPeaoPreto2);
        ajustarTamanhoDaPeca(imageViewPeaoPreto3);
        ajustarTamanhoDaPeca(imageViewPeaoPreto4);
        ajustarTamanhoDaPeca(imageViewPeaoPreto5);
        ajustarTamanhoDaPeca(imageViewPeaoPreto6);
        ajustarTamanhoDaPeca(imageViewPeaoPreto7);
        ajustarTamanhoDaPeca(imageViewPeaoPreto8);
        ajustarTamanhoDaPeca(imageViewQuadrado1);
        ajustarTamanhoDaPeca(imageViewQuadrado2);
    }

    //Ajusta tamanho das peças para o tabuleiro
    public void ajustarTamanhoDaPeca(ImageView imageView) {
        imageView.getLayoutParams().width = tabuleiroTamanho / 8;
        imageView.getLayoutParams().height = tabuleiroTamanho / 8;
    }


    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {
            //we create a TCPClient object
            //here the messageReceived method is implemented
            //this method calls the onProgressUpdate
            try {
                mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                    @Override
                    //here the messageReceived method is implemented
                    public void messageReceived(String message) {
                        //this method calls the onProgressUpdate
                        publishProgress(message);

                    }
                });

                mTcpClient.run();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            Log.d("ciro", "CANCELADO");
            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("cyrus", "resposta aqui no app:  " + values[0]);
            //process server response here....

            String respostaJsonESP = values[0].toLowerCase();
            handleReceivedMessage(respostaJsonESP);

            if (check) {
                loadingDialog.dismissDialog();

                mostrarToast("Conectado ao tabuleiro!");

                check = false;
            }
            //mostrarToast(comandoESP);

        }

    }

    private void handleReceivedMessage(String respostaJsonESP) {

        //guarda numa string o comando feito pelo jogador
        String comandoESP = getMoveFromJson(respostaJsonESP);

        if (!isFeedbackMessage(respostaJsonESP)) {//Se for só mensagem de feedback, não precisa fazer nada
            //Log.d("cyrus", tabuleiroDeXadrez.toString());
            //Verifica se é um comando válido
            if (checaComando(comandoESP) && !promocao) {
                notacaoOficialRecebida = comandoESP;
                boolean moveu = moverPeca(comandoESP);
                if (moveu) {
                    Log.d("ciro", "Movimento legal");
                    //movimento válido, ativa engine, ou envia pro lichess se estiver jogando online
                    if (!promocao) {//só vai enviar algo depois que o usuario escolher a peça pra promover
                        if (lichessAtivado) {
                            Log.d("ciro", "Enviando lance ao lichess: " + notacaoOficialRecebida);
                            sendToLichessThread("lance", handler);
                        } else {
                            myengine.setComando(notacaoOficialRecebida);
                            threadEngineMove = new ThreadEngineMove(myengine);
                            threadEngineMove.start();
                        }
                    }
                } else {
                    Log.d("ciro", "movimento ilegal, reprovou no moverPeca");
                    //movimento inválido
                    //mostrarToast("erro... -> (" + notacaoClasseInterna[0] + "," + notacaoClasseInterna[1] + ") (" + notacaoClasseInterna[2] + ", " + notacaoClasseInterna[3] + ")");

                }
                String info;
                //if (notacaoOficialRecebida.length() > 4)
                info = gerarJSON(tabuleiroDeXadrez.getTabuleiro(), notacaoOficialRecebida.substring(0, 4), moveu, !engineTurn);
                //else
                //  info = gerarJSON(tabuleiroDeXadrez.getTabuleiro(), notacaoOficialRecebida, moveu, !engineTurn);
                enviarProServidor(info);
            }
        }
    }

    private boolean checaComando(String comando) {
        char[] valores = {'1', '2', '3', '4', '5', '6', '7', '8'};
        for (int i = 0; i < 8; i++)
            if (comando.charAt(1) == valores[i])
                for (int j = 0; j < 8; j++)
                    if (comando.charAt(3) == valores[j])
                        return true;
        return false;
    }

    private boolean getFeedbackFromJson(String jsonMessage) {
        JSONObject comandoJson;
        boolean feedback = false;
        try {
            comandoJson = new JSONObject(jsonMessage);
            feedback = comandoJson.getBoolean("feedback");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return feedback;
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

    private boolean isFeedbackMessage(String msg) {
        Log.d("threadSend", "Tá analisando se é feedback, função isFeedbackMessage");
        if (!listaStringsEnviadas.isEmpty()) {

            String comandoEnviadoAnteriormente = getMoveFromJson(listaStringsEnviadas.get(0));
            String comandoESP = getMoveFromJson(msg);

            if (comandoEnviadoAnteriormente.equals(comandoESP)) {//Feedback recebido com sucesso
                Log.d("threadSend", "essa mensagem '" + comandoESP + "' é um feedback");
                mensagemRecebidaDoESP = comandoESP;
                return true;
            }
        }
        return getFeedbackFromJson(msg);
    }

    public void engineMoveReceived(Message msg) {
        String move = (String) msg.obj;
        //engineTurn = false;

        if (updateTextViews) {
            if (msg.arg1 == 1) {
                textViewJogadorBrancas.setText(move);
                textViewJogadorNegras.setText(username);
            } else {
                textViewJogadorNegras.setText(move);
                textViewJogadorBrancas.setText(username);
            }
            updateTextViews = false;

        } else {
            moverPeca(move);
            String info = gerarJSON(tabuleiroDeXadrez.getTabuleiro(), move, true, !engineTurn);

            enviarProServidor(info);
        }
    }

    public void enviarProServidor(String message) {
        listaStringsEnviadas.add(message);
        if (threadSendMessage == null || !threadSendMessage.isAlive()) {
            threadSendMessage();
        }
        /*
        if (listaStringsEnviadas.isEmpty()) {
            mTcpClient.sendMessage(message);
            listaStringsEnviadas.add(message);
        } else if (message.equals(listaStringsEnviadas.get(0)))//solicitaçao de reenvio de mensagem

           mTcpClient.sendMessage(message);
           */
    }

    public void onLichessClicked(View view) {
        if (!lichessAtivado) {
            mostrarToast("Procurando jogo no lichess");
            sendToLichessThread("profile", handler);
            sendToLichessThread("iniciar", handler);
        } else {
            sendToLichessThread("stream", handler);
            mostrarToast("Jogo já iniciado: " + gameIdLichess);
        }
        

    }

    public void onEngineClicked(View view) {
        myengine.setComando(notacaoOficialRecebida);
        threadEngineMove = new ThreadEngineMove(myengine);
        threadEngineMove.start();
    }

    private Object getFromJson(String jsonMessage, String parameter) {
        JSONObject comandoJson;
        Object message = new Object();
        try {
            comandoJson = new JSONObject(jsonMessage);
            JSONArray array = comandoJson.getJSONArray("nowPlaying");
            if (array.length() > 0) {
                comandoJson = array.getJSONObject(0);
                message = comandoJson.get(parameter);
            } else {
                Log.d("ciro", "Não há nenhum jogo agora");
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }


    public void sendToLichessThread(String comando, Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String lichess = "lichess.org";
                String file = "";
                String requestMethodString = "";
                //String move = "e5c6";
                switch (comando) {
                    case "iniciar":
                        file = "api/account/playing";
                        requestMethodString = "GET";
                        break;
                    case "profile":
                        file = "api/account";
                        requestMethodString = "GET";
                        break;
                    case "stream":
                        file = "api/bot/game/stream/" + gameIdLichess;
                        requestMethodString = "GET";
                        break;
                    case "lance":
                        file = "api/bot/game/" + gameIdLichess + "/move/" + notacaoOficialRecebida;
                        requestMethodString = "POST";
                        break;
                }

                try {
                    URL url = new URL("https", lichess, file);
                    HttpURLConnection client = (HttpURLConnection) url.openConnection();
                    client.setRequestMethod(requestMethodString);
                    client.setRequestProperty("Authorization", "Bearer " + tokenLichess);
                    client.setRequestProperty("Content-Type", "application/json");
                    client.setRequestProperty("User-Agent", "Mozilla/5.0");

                    //client.setRequestProperty("move", move);
                    //client.setDoOutput(true);

                    //DataOutputStream writer = new DataOutputStream(client.getOutputStream());
                    //writer.writeBytes(infoToSend.toString());

                    client.connect();

                    Log.d("ciro", "vamos ver o que o lichess tem pra nois");
                    BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String line;
                    StringBuilder responseOutput = new StringBuilder();

                    if (!comando.equals("stream")) {
                        while ((line = br.readLine()) != null /*&& !line.equals("")*/) {
                            responseOutput.append(line);
                            //Log.d("ciro", line);
                        }
                        br.close();

                        Log.d("ciro", "mensagem do lichess: " + responseOutput.toString());
                    }
                    switch (comando) {
                        case "iniciar":

                            gameIdLichess = (String) getFromJson(responseOutput.toString(), "gameId");
                            if (gameIdLichess != null && !gameIdLichess.isEmpty()) {

                                Log.d("ciro", "gameId: " + gameIdLichess);
                                lichessAtivado = true;
                                String lm = (String) getFromJson(responseOutput.toString(), "lastMove");
                                boolean opponentMoved = !lm.isEmpty();

                                if(opponentMoved)
                                    engineTurn = true;//para que a ESP saiba que há um movimento inicial feito pelo adversário, e ela ainda vai processá-lo, mesmo sendo a vez do usuário
                                else
                                    engineTurn = !((boolean) getFromJson(responseOutput.toString(), "isMyTurn"));

                                //mostra pra esp que o jogo começou
                                String msg = gerarJSON(tabuleiroDeXadrez.getTabuleiro(), "start", true, !engineTurn);
                                //notacaoOficialRecebida = "start";
                                enviarProServidor(msg);

                                sendToLichessThread("stream", handler);

                                String minhaCor = (String) getFromJson(responseOutput.toString(), "color");

                                try {
                                    JSONObject adversarioJson = (JSONObject) getFromJson(responseOutput.toString(), "opponent");
                                    String adversario = (String) adversarioJson.get("username");
                                    //uma gambs pra pegar o nome do adversario
/*
                                int pos1 = adversarioJson.lastIndexOf("username") + 11;
                                int pos2=pos1;

                                while (adversarioJson.charAt(pos2) != '\"'){
                                    pos2++;
                                }
                                adversarioJson = adversarioJson.substring(pos1, pos2);
                                */
                                    if (minhaCor != null) {
                                        Message m = new Message();
                                        m.obj = adversario;
                                        updateTextViews = true;
                                        if (minhaCor.equals("black")) {//aqui estamos de negras
                                            //textViewJogadorBrancas.setText(adversario);

                                            m.arg1 = 1;

                                        } else {//aqui estamos de brancas
                                            //textViewJogadorNegras.setText(adversario);

                                            m.arg1 = 2;

                                        }
                                        int i = 0;
                                        while (username.isEmpty()) {
                                            i++;
                                        }
                                        handler.sendMessage(m);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mostrarToastLichess("Jogo online iniciado!");


                            } else {
                                mostrarToastLichess("Não há jogo iniciado");
                            }
                            break;
                        case "profile":
                            try {
                                JSONObject jsonObject = new JSONObject(responseOutput.toString());

                                username = jsonObject.getString("username");

                                Log.d("ciro", "username: " + username);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "lance"://foi feito um lance, tem que mandar um get esperando o lance do lichess

                            Log.d("ciro", "estamos esperando lance vindo do lichess");
                            //sendToLichessThread("stream", handler);

                            break;

                        case "stream":
                            Log.d("ciro", "Entramos aqui na consulta infinita");
                            while ((line = br.readLine()) != null /*&& !line.equals("")*/) {

                                //responseOutput.append(line);

                                if (!line.isEmpty()) {

                                    Log.d("ciro", "stream do lichess: " + line);

                                    String lastmove = getLastMoveFromNDJson(line);
                                    if (!lastmove.isEmpty()) {

                                        //já temos o último movimento, falta ver se é um movimento do adversário

                                        String status = getGameStatus(line);
                                        if (!lastmove.substring(0, 4).equals(notacaoOficialRecebida)) {
                                            Log.d("ciro", "Last Move was: " + lastmove + ", diferente de " + notacaoOficialRecebida);
                                            Message m = new Message();
                                            m.obj = lastmove;

                                            handler.sendMessage(m);
                                            if (status.equals("mate")) {
                                                Log.d("ciro", "Vitória do adversário");
                                                mostrarToastLichess("Vitória do adversário");

                                                lichessAtivado = false;
                                            }
                                        } else {//não é movimento do adversário
                                            if (status.equals("mate")) {
                                                Log.d("ciro", "Você venceu!");
                                                mostrarToastLichess("Você venceu!");

                                                lichessAtivado = false;
                                            }

                                        }
                                        if (status.equals("resign")) {

                                            String vencedor = getVencedor(line);
                                            Log.d("ciro", vencedor + " venceram");
                                            mostrarToastLichess(vencedor + " venceram");

                                            lichessAtivado = false;

                                        }
                                    } else {
                                        Log.d("ciro", "Jogo ainda nem começou");
                                    }
                                }

                            }
                            br.close();
                            Log.d("ciro", "Stream finalizado");
                            break;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.d("ciro", "Erro na conexão com lichess! MalformedURLException. Vamos enviar comando novamente");
                    sendToLichessThread(comando, handler);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("ciro", "Erro na conexão com lichess! IOException. Vamos enviar comando novamente");
                    sendToLichessThread(comando, handler);
                }
            }
        }).start();
    }

    private void mostrarToastLichess(String message) {
        Looper.prepare();
        mostrarToast(message);
        Looper.loop();
    }

    private String getLastMoveFromNDJson(String responseOutput) {
        int pos = responseOutput.lastIndexOf("moves") + 8;//vai direto pro que interessa, sem frescura
        StringBuilder moves = new StringBuilder();
        StringBuilder lastmove = new StringBuilder();
        while (responseOutput.charAt(pos) != '\"') {
            moves.append(responseOutput.charAt(pos));
            pos++;
        }

        //agora que já temos todos os movimentos, capturamos o último realizado

        pos = moves.lastIndexOf(" ") + 1;
        if (pos != -1) {
            while (pos < moves.length()) {
                lastmove.append(moves.charAt(pos));
                pos++;
            }
            return lastmove.toString();
        } else
            //não há nenhum movimento
            return "";
    }

    private String getGameStatus(String cmd) {
        int pos = cmd.lastIndexOf("status") + 9;//vai direto pro que interessa, sem frescura
        StringBuilder status = new StringBuilder();

        while (cmd.charAt(pos) != '\"') {
            status.append(cmd.charAt(pos));
            pos++;
        }
        return status.toString();
    }

    private String getVencedor(String cmd) {
        int pos = cmd.lastIndexOf("winner") + 9;//vai direto pro que interessa, sem frescura
        StringBuilder vencedor = new StringBuilder();

        while (cmd.charAt(pos) != '\"') {
            vencedor.append(cmd.charAt(pos));
            pos++;
        }
        switch (vencedor.toString()) {
            case "white":
                return "Brancas";
            case "black":
                return "Negras";
            default:
                return null;
        }
    }

    public void threadSendMessage() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long startTempo = SystemClock.elapsedRealtime();
                long atualTempo;
                String message = listaStringsEnviadas.get(0);
                Log.d("ciro", "vamos enviar: " + message);
                mensagemRecebidaDoESP = "";
                mTcpClient.sendMessage(message);

                while (!listaStringsEnviadas.isEmpty()) {//até enviar todas as mensagens pendentes
                    atualTempo = SystemClock.elapsedRealtime() - startTempo;
                    Log.d("threadSend", "mensagemRecebidaDoESP: " + mensagemRecebidaDoESP);
                    if (mensagemRecebidaDoESP.equals(getMoveFromJson(message))) {//feedback recebido

                        Log.d("threadSend", "feedback de " + mensagemRecebidaDoESP + " recebido com sucesso");
                        listaStringsEnviadas.remove(0);
                        mensagemRecebidaDoESP = "";

                        if (listaStringsEnviadas.isEmpty()) {   //enviou todas as mensagens pendentes
                            Log.d("threadSend", "Nao há mensagens pendentes");
                            break;
                        } else {    //ainda tem mensagem pendente a ser enviada

                            message = listaStringsEnviadas.get(0);
                            Log.d("threadSend", "enviando mensagem pendente: " + message);
                            mTcpClient.sendMessage(message);
                            startTempo = SystemClock.elapsedRealtime();

                        }
                    }

                    if (atualTempo >= 2000) {//tempo estourou sem receber o feedback
                        Log.d("threadSend", "tempo estourou, reenviando mensagem: " + message);
                        mTcpClient.sendMessage(message);
                        startTempo = SystemClock.elapsedRealtime();
                    }
                }
            }
        };
        threadSendMessage = new Thread(runnable);
        threadSendMessage.start();

    }

    public void mostrarToast(String msg) {
        Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

    }

    private String gerarJSON(int[][] tab, String move, boolean status, boolean turn) {
        JSONObject jo = new JSONObject();
        boolean rock = false;
        try {
            jo.put("comando", move);
            JSONArray ja = new JSONArray(tab);
            jo.put("tabuleiro", ja);
            jo.put("status", status);
            jo.put("minhaVez", turn);

            //Trata casos especiais
            String move2 = "0";
            if (rockPequenoAconteceu) {
                move2 = "h" + move.charAt(1) + "f" + move.charAt(3);
                rock = true;
                rockPequenoAconteceu = false;
            }
            if (rockGrandeAconteceu) {
                move2 = "a" + move.charAt(1) + "d" + move.charAt(3);
                rock = true;
                rockGrandeAconteceu = false;
            }
            if (enPassantAconteceu) {//avisa pra retirar o peao do lugar
                move2 = "" + move.charAt(2) + move.charAt(0) + "i4";//por enquanto, todas as peças vão pro mesmo lugar
                enPassantAconteceu = false;

            } else if (pecaFoiTomada) {
                move2 = "" + move.charAt(2) + move.charAt(3) + "i4";
                pecaFoiTomada = false;
            }

            //Pra informar ao tabuleiro que outra peça precisa ser movida
            jo.put("comando2", move2);

            //Pra informar se houve rock ou não
            jo.put("rock", rock);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo.toString();
    }

}
