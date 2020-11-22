package com.example.mysmartchess.Jogo;

import android.widget.ImageView;

import java.util.List;

public class Bispo extends PecaXadrez {

    Bispo(boolean pecaBranca) {
        super(pecaBranca);
    }

    Bispo(boolean pecaBranca, ImageView imageView) {
        super(pecaBranca, imageView);
    }

    @Override
    public boolean isMovePossible(int inicioX, int inicioY, int destinoX, int destinoY, int[][] jogo, List<PecaXadrez> pecas) {

        boolean direita, cima;
        int i, j;

        //Se não ta na vista do bispo, nem perde tempo
        if(!taNaVistaDoBispo(inicioX, inicioY, destinoX, destinoY))
            return false;

        //descobre a direção do movimento
        if (inicioX - destinoX < 0)
            cima = true;
        else cima = false;
        if (inicioY - destinoY < 0)
            direita = true;
        else direita = false;

        //diagonal pra direita e pra cima
        if (direita && cima)
            for (i = inicioX + 1, j = inicioY + 1; i < destinoX && j < destinoY; i++, j++) {
                //Se encontrou uma peça qualquer, não pode mover
                if (jogo[i][j] != -1) {
                    return false;
                }
            }
        //diagonal pra direita e pra baixo
        if (direita && !cima)
            for (i = inicioX - 1, j = inicioY + 1; i > destinoX && j < destinoY; i--, j++) {
                //Se encontrou uma peça qualquer, não pode mover
                if (jogo[i][j] != -1) {
                    return false;
                }
            }
        //diagonal pra esquerda e pra baixo
        if (!direita && !cima)
            for (i = inicioX - 1, j = inicioY - 1; i > destinoX && j > destinoY; i--, j--) {
                //Se encontrou uma peça qualquer, não pode mover
                if (jogo[i][j] != -1) {
                    return false;
                }
            }
        //diagonal pra esquerda e pra cima
        if (!direita && cima)
            for (i = inicioX + 1, j = inicioY - 1; i < destinoX && j > destinoY; i++, j--) {
                //Se encontrou uma peça qualquer, não pode mover
                if (jogo[i][j] != -1) {
                    return false;
                }
            }
        //posição final não deve estar ocupada por uma peça de mesma cor
        PecaXadrez pecaAdversaria;
        if (jogo[destinoX][destinoY] != -1) {
            pecaAdversaria = pecas.get(jogo[destinoX][destinoY]);
            return (!pecaAdversaria.isPecaBranca() || !isPecaBranca()) && (pecaAdversaria.isPecaBranca() || isPecaBranca());
            //O android studio simplificou pra mim, testei e realmente funciona :o
        }
        //Destino vazio, movimento legal
        else return true;
    }

    private boolean taNaVistaDoBispo(int bispoX, int bispoY, int destinoX, int destinoY){
        //basta que a quantidade de x andado seja igual à de y
        return (Math.abs(bispoX-destinoX) == Math.abs(bispoY-destinoY));
    }


    @Override
    public boolean isDeliveringCheck(int posX, int posY, int[][] jogo) {
        int i, j, reiInimigo;
        if (isPecaBranca())
            reiInimigo = TabuleiroDeXadrez.REI_PRETO;
        else reiInimigo = TabuleiroDeXadrez.REI_BRANCO;
        //
        //Análise para brancas e pretas
        //
        //diagonal pra direita e pra cima
        for (i = posX + 1, j = posY + 1; i < 8 && j < 8; i++, j++) {
            if (jogo[i][j] == reiInimigo) {
                return true;
            }
            //Se encontrou uma peça qualquer, não tá dando cheque
            else if (jogo[i][j] != -1) {
                break;
            }
        }
        //diagonal pra esquerda e pra cima
        for (i = posX + 1, j = posY - 1; i < 8 && j >= 0; i++, j--) {
            if (jogo[i][j] == reiInimigo) {
                return true;
            }
            //Se encontrou uma peça qualquer, não tá dando cheque
            else if (jogo[i][j] != -1) {
                break;
            }
        }
        //diagonal pra esquerda e pra baixo
        for (i = posX - 1, j = posY - 1; i >= 0 && j >= 0; i--, j--) {
            if (jogo[i][j] == reiInimigo) {
                return true;
            }
            //Se encontrou uma peça qualquer, não tá dando cheque
            else if (jogo[i][j] != -1) {
                break;
            }
        }
        //diagonal pra direita e pra baixo
        for (i = posX - 1, j = posY + 1; i >= 0 && j < 8; i--, j++) {
            if (jogo[i][j] == reiInimigo) {
                return true;
            }
            //Se encontrou uma peça qualquer, não tá dando cheque
            else if (jogo[i][j] != -1) {
                break;
            }
        }
        return false;
    }

    public String toString() {
        return "Bispo";
    }
}
