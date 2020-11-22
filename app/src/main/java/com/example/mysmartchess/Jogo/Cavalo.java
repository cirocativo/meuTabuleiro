package com.example.mysmartchess.Jogo;

import android.widget.ImageView;

import java.util.List;

public class Cavalo extends PecaXadrez {
    Cavalo(boolean pecaBranca) {
        super(pecaBranca);
    }

    Cavalo(boolean pecaBranca, ImageView imageView) {
        super(pecaBranca, imageView);
    }

    public boolean isMovePossible(int inicioX, int inicioY, int destinoX, int destinoY, int[][] jogo, List<PecaXadrez> pecas) {
        PecaXadrez pecaXadrez;
        if (isMovePossible(inicioX, inicioY, destinoX, destinoY, jogo)) {

            //resta analisar se há peça para ser tomada, e se é inimiga

            if (jogo[destinoX][destinoY] == -1)
                return true;
            pecaXadrez = pecas.get(jogo[destinoX][destinoY]);

            //posição final não deve estar ocupada por uma peça de mesma cor
            return (!pecaXadrez.isPecaBranca() || !isPecaBranca()) && (pecaXadrez.isPecaBranca() || isPecaBranca());
            //O android studio simplificou pra mim, testei e realmente funciona :o
        }

        return false;
    }

    private boolean analisa(int variaDe1Inicio, int variaDe2Inicio, int variaDe1Destino, int variaDe2Destino) {
        int var1, var2;

        for (var1 = variaDe1Inicio - 1; var1 <= variaDe1Inicio + 1; var1 += 2) {
            for (var2 = variaDe2Inicio - 2; var2 <= variaDe2Inicio + 2; var2 += 4) {

                //exclui casas além do 8x8
                if (var1 >= 0 && var1 < 8 && var2 < 8 && var2 >= 0) {

                    //encontrou o destino, movimento legal
                    if (variaDe1Destino == var1 && variaDe2Destino == var2) {
                        return true;
                    }
                }
            }
        }
        //analisou os movimentos possíveis e não encontrou o destino, é ilegal
        return false;
    }

    private boolean isMovePossible(int inicioX, int inicioY, int destinoX, int destinoY, int[][] jogo) {
        boolean horizontal = analisa(inicioX, inicioY, destinoX, destinoY);
        boolean vertical = analisa(inicioY, inicioX, destinoY, destinoX);
        return horizontal || vertical;
    }


    public boolean isDeliveringCheck(int posX, int posY, int[][] jogo) {
        //Primeiro encontra rei adversario, depois ve se ta no raio de ação do cavalo
        int x, y;
        int reiX = -1, reiY = -1;
        int reiInimigo;

        if (isPecaBranca())
            reiInimigo = TabuleiroDeXadrez.REI_PRETO;
        else reiInimigo = TabuleiroDeXadrez.REI_BRANCO;

        for (x = 0; x < 8; x++) {
            for (y = 0; y < 8; y++) {
                if (jogo[x][y] == reiInimigo) {
                    reiX = x;
                    reiY = y;
                    break;
                }
            }
        }
        //vê se está no raio de ação do cavalo
        if (reiX >= 0) {
            return isMovePossible(posX, posY, reiX, reiY, jogo);
        }

        return false;
    }

    public String toString() {
        return "Cavalo";
    }
}
