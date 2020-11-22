package com.example.mysmartchess.Jogo;

import android.util.Log;
import android.widget.ImageView;

import java.util.List;

public class Peao extends PecaXadrez {
    Peao(boolean pecaBranca) {
        super(pecaBranca);
    }

    private boolean enPassantActive = false;

    public boolean isEnPassantActive() {
        return enPassantActive;
    }

    public void setEnPassantActive(boolean enPassantActive) {
        this.enPassantActive = enPassantActive;
    }

    Peao(boolean pecaBranca, ImageView imageView) {
        super(pecaBranca, imageView);
    }

    @Override
    public boolean isDeliveringCheck(int posX, int posY, int[][] jogo) {
        int varX;
        int rei;
        if (isPecaBranca()) {
            rei = TabuleiroDeXadrez.REI_PRETO;
            varX = 1;
            //Log.d("cyrus", "rei inimigo é preto. posicao da peça analisada: " + posX + ", " + posY);
        } else {
            rei = TabuleiroDeXadrez.REI_BRANCO;
            varX = -1;
        }
        Log.d("cyrus", "rei: " + rei);

        varX = posX + varX;
        for (int j = posY - 1; j <= posY + 1; j += 2) {

            //verifica se ta dento do 8x8
            if (j >= 0 && j < 8) {
                Log.d("cyrus", "verificando se está dando cheque na posicao: " + varX + ", " + j);
                //Aqui é a área de ataque do peão
                //Se o rei estiver aqui, peão está dando cheque
                if (jogo[varX][j] == rei)
                    return true;
            }
        }
        //Terminou o laço e não encontrou nada, peão não está dando cheque
        return false;
    }


    @Override
    public boolean isMovePossible(int inicioX, int inicioY, int destinoX, int destinoY, int[][] jogo, List<PecaXadrez> pecas) {
        //Log.d("cyrus", "peça do destino:  " + jogo[destinoX][destinoY]);
        //Log.d("cyrus", "(" + destinoX + ", " + destinoY);
        //
        // Análise para movimento das brancas e das pretas
        //

        //analisa se andou pra frente
        if (inicioY == destinoY) {
            if((isPecaBranca() && (destinoX - inicioX) == 1) || (!isPecaBranca() && (destinoX - inicioX) == -1))
            //Verifica se tem alguem no destino
            if (jogo[destinoX][destinoY] == -1)
                return true;
            else {

                return false;
            }
        }
        //analisa se andou duas casas e se estava na casa inicial
        if ((inicioY == destinoY) && ((inicioX == 1 && destinoX == 3 && isPecaBranca()) || (inicioX == 6 && destinoX == 4 && !isPecaBranca()))) {
            //Verifica se tem peça pelo caminho
            if ((jogo[destinoX][destinoY] == -1) && ((jogo[destinoX - 1][destinoY] == -1 && isPecaBranca()) || (jogo[destinoX + 1][destinoY] == -1 && !isPecaBranca()))) {
                setEnPassantActive(true);
                return true;
            } else {
                //Log.d("cyrus", "tem uma peça pelo meio do caminho");
                return false;
            }
        }
        //analisa se comeu alguma peça
        PecaXadrez pecaCapturada;
        if (jogo[destinoX][destinoY] != -1)
            pecaCapturada = pecas.get(jogo[destinoX][destinoY]);
        else {
            if (jogo[inicioX][destinoY] == -1)
                return false;
            //Verifica se é en Passant
            //Log.d("ciro","tá vendo se é en passant");
            PecaXadrez peca = pecas.get(jogo[inicioX][destinoY]);
            if (peca.toString().equals("Peao")) {
                Peao peao = (Peao) peca;
                //Se a peça do lado for peao, e tiver com en passant ativado, então tá valendo
                if (peao.isEnPassantActive()) {
                    pecaCapturada = peao;
                    //Log.d("ciro","en passant ativo");
                }else {
                    //En passant não está ativo
                    return false;
                }
            }else{
                //Não é peão
                return false;
            }
        }
        //tá grande, mas cobre todos os casos. se capturou pra frente, tem que ser branco e a peça capturada deve ser preta.
        //se capturou pra trás, o contrário.
        if (((destinoX - inicioX == 1) && (!pecaCapturada.isPecaBranca() && isPecaBranca())) || ((destinoX - inicioX == -1) && (pecaCapturada.isPecaBranca() && !isPecaBranca()))) {
            if (Math.abs(destinoY - inicioY) == 1)
                return true;
            else return false;
        } else return false;
    }

    public String toString() {
        return "Peao";
    }

}