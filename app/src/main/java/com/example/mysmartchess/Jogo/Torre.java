package com.example.mysmartchess.Jogo;

import android.util.Log;
import android.widget.ImageView;

import java.util.List;

public class Torre extends PecaXadrez {


    Torre(boolean pecaBranca) {
        super(pecaBranca);
    }

    Torre(boolean pecaBranca, ImageView imageView) {
        super(pecaBranca, imageView);
    }

    @Override
    public boolean isMovePossible(int inicioX, int inicioY, int destinoX, int destinoY, int[][] jogo, List<PecaXadrez> pecas) {
        //Verifica se dá pra se mover até o lugar desejado

        if (taNaVistaDaTorre(inicioX, inicioY, destinoX, destinoY) && isMovePossible(inicioX, inicioY, destinoX, destinoY, jogo)) {

            PecaXadrez pecaXadrez;
            if (jogo[destinoX][destinoY] != -1) {
                pecaXadrez = pecas.get(jogo[destinoX][destinoY]);
                if ((!pecaXadrez.isPecaBranca() || !isPecaBranca()) && (pecaXadrez.isPecaBranca() || isPecaBranca()))
                //O android studio simplificou pra mim, testei e realmente funciona :o
                {

                    setHasNotMoved(false);
                    return true;
                } else return false;
            }
            //Destino vazio, movimento legal
            else return true;
        }
        return false;
    }

    private boolean isMovePossible(int inicioX, int inicioY, int destinoX, int destinoY, int[][] jogo) {
        boolean verticalMove = analisa(inicioX, destinoX, inicioY, false, jogo);
        boolean horizontalMove = analisa(inicioY, destinoY, inicioX, true, jogo);
        Log.d("cyrus", "analisando movimento vertical");
        if (verticalMove)
            Log.d("cyrus", "movimento verticalmente possivel");
        Log.d("cyrus", "analisando movimento horizontal");
        if (horizontalMove)
            Log.d("cyrus", "movimento horizontalmente possivel");
        return verticalMove || horizontalMove;
    }

    private boolean analisa(int inicio, int destino, int estatico, boolean isXStatic, int[][] jogo) {
        int var, pecaAnalizada;
        boolean encontrouFinal = false;
        Log.d("cyrus", "vamos analisar: " + inicio + " até " + destino + ", com " + estatico + "estático. xStatic?" + isXStatic);
        for (var = 0; var < 8; var++) {
            Log.d("cyrus", "var: " + var);
            if (encontrouFinal) {
                Log.d("cyrus", "encontrou o final");
                //percorreu todo o caminho e não encontrou nada
                if (var == inicio) {
                    Log.d("cyrus", "não há peça pelo caminho");
                    return true;
                }

                //aqui vai depender se está sendo analizado a coluna ou a linha
                if (isXStatic) pecaAnalizada = jogo[estatico][var];
                else pecaAnalizada = jogo[var][estatico];

                //encontrou uma peça no meio do caminho
                if (pecaAnalizada != -1) {
                    Log.d("cyrus", "Há uma peça no caminho: " + pecaAnalizada);
                    return false;
                }
            }
            //Não encontrou o destino e chegou no inicio, vai verificar se encontra ate o final do tabuleiro
            else if (var > inicio) {

                // percorreu o caminho e não encontrou nada
                if (var == destino) {
                    Log.d("cyrus", "ta dando cheque, ou caminho livre");
                    return true;
                }
                if (isXStatic) pecaAnalizada = jogo[estatico][var];
                else pecaAnalizada = jogo[var][estatico];

                //encontrou uma peça no meio do caminho
                if (pecaAnalizada != -1) {
                    Log.d("cyrus", "Há uma peça no caminho: " + pecaAnalizada);
                    return false;
                }
            }

            if (var == destino)
                encontrouFinal = true;
        }
        //Se chegou até aqui, a análise não econtrou a posição esperada
        Log.d("cyrus", "Movimento inconsistente");
        return false;
    }


    private boolean taNaVistaDaTorre(int torreX, int torreY, int destinoX, int destinoY) {
        if (torreY == destinoY)
            Log.d("cyrus", "mesma coluna da torre");
        if (torreX == destinoX)
            Log.d("cyrus", "mesma linha da torre");
        return (torreY == destinoY || torreX == destinoX);
    }


    @Override
    public boolean isDeliveringCheck(int posX, int posY, int[][] jogo) {
        int x, y;
        int reiAdversario;
        if (isPecaBranca())
            reiAdversario = TabuleiroDeXadrez.REI_PRETO;
        else reiAdversario = TabuleiroDeXadrez.REI_BRANCO;

        //procura o rei
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (jogo[i][j] == reiAdversario) {
                    //encontrou a posição do rei
                    //Se a torre pode se mover até o rei, então está dando cheque
                    return taNaVistaDaTorre(posX, posY, i, j) && isMovePossible(posX, posY, i, j, jogo);
                }

        return false;
    }

    public String toString() {
        return "Torre";
    }
}
