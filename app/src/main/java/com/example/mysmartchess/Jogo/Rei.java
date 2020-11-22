package com.example.mysmartchess.Jogo;

import android.util.Log;
import android.widget.ImageView;

import java.util.List;

public class Rei extends PecaXadrez {

    Rei(boolean pecaBranca) {
        super(pecaBranca);
    }

    Rei(boolean pecaBranca, ImageView imageView) {
        super(pecaBranca, imageView);
    }

    @Override
    public boolean isDeliveringCheck(int posX, int posY, int[][] jogo) {
        int x = posX, y = posY;
        int reiAdversario;
        if (isPecaBranca())
            reiAdversario = TabuleiroDeXadrez.REI_PRETO;
        else reiAdversario = TabuleiroDeXadrez.REI_BRANCO;
        //analisa se há um rei ao redor do rei
        // essa possibilidade existe quando o outro rei se move e se está verificando se há alguma peça o ameaçando
        for (x = posX - 1; x <= posX + 1; x++) {
            for (y = posY - 1; y <= posY + 1; y++) {
                //exclui casas além do 8x8
                if (x >= 0 && x < 8 && y < 8 && y >= 0) {
                    //Se o rei tiver por ali, tá dando "cheque"
                    if (jogo[x][y] == reiAdversario)
                        return true;
                }

            }
        }
        //Log.d("cyrus", "rei não está dando cheque não.");
        return false;
    }

    @Override
    public boolean isMovePossible(int inicioX, int inicioY, int destinoX, int destinoY, int[][] jogo, List<PecaXadrez> pecas) {

        PecaXadrez pecaXadrez;
        //
        //
        //Verificação do roque
        //
        //
        if (hasNotMoved()) {
            PecaXadrez pecaDoCanto = new Rei(false);

            if ((Math.abs(destinoY - inicioY) >= 2) && inicioX == destinoX) {
                int var = 0;
                if (destinoY == 2 || destinoY == 0) {
                    var = -1;
                    if (jogo[inicioX][0] == -1)//a torre saiu do canto, roque impossível
                        return false;
                    else
                        pecaDoCanto = pecas.get(jogo[inicioX][0]);
                }
                if (destinoY >= 6) {
                    var = 1;
                    if (jogo[inicioX][7] == -1)//a torre saiu do canto, roque impossível
                        return false;
                    else
                        pecaDoCanto = pecas.get(jogo[inicioX][7]);
                }
                //Não pode ter nenhuma peça pelo caminho
                for (int i = inicioY + var; i > 0 && i < 7; i += var) {
                    if (jogo[inicioX][i] != -1) {
                        //Log.d("cyrus", "há uma peça no caminho");
                        return false;
                    }
                }
                //chegou aqui, não há peças no caminho


                //Tem ter que ter uma peça, tem que ser uma torre, e não pode ter se movido

                    if (pecaDoCanto.toString().equals("Torre") && pecaDoCanto.hasNotMoved()) {
                        return true;

                    }
                    //Torre já se moveu, ou não é uma torre

                    else {
                        //Log.d("cyrus", "Torre se moveu, ou não é torre");
                        return false;
                    }
            }
        }

        //Verifica se andou uma casa apenas
        if (Math.abs(inicioX - destinoX) <= 1 && Math.abs(inicioY - destinoY) <= 1) {
            //posição final não deve estar ocupada por uma peça de mesma cor
            if (jogo[destinoX][destinoY] != -1) {
                pecaXadrez = pecas.get(jogo[destinoX][destinoY]);
                return (!pecaXadrez.isPecaBranca() || !isPecaBranca()) && (pecaXadrez.isPecaBranca() || isPecaBranca());
                //O android studio simplificou pra mim, testei e realmente funciona :o
            }
            //Destino vazio, movimento legal
            setHasNotMoved(false);
            return true;

        }
        //Andou mais que uma casa, movimento ilegal
        else return false;
    }

    public String toString() {
        return "Rei";
    }
}
