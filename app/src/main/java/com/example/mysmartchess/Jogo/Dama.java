package com.example.mysmartchess.Jogo;

import android.util.Log;
import android.widget.ImageView;

import java.util.List;

public class Dama extends PecaXadrez {
    //A classe mais facil, pois é a fusão entre torre e bispo

    private Torre torre = new Torre(isPecaBranca());
    private Bispo bispo = new Bispo(isPecaBranca());

    Dama(boolean pecaBranca) {
        super(pecaBranca);
    }

    Dama(boolean pecaBranca, ImageView imageView) {
        super(pecaBranca, imageView);
    }

    @Override
    public boolean isMovePossible(int inicioX, int inicioY, int destinoX, int destinoY, int[][] jogo, List<PecaXadrez> pecas) {
        //if (torre.isMovePossible(inicioX, inicioY, destinoX, destinoY, jogo, pecas))
        //Log.d("cyrus", "pela torre, pode");
        //if (bispo.isMovePossible(inicioX, inicioY, destinoX, destinoY, jogo, pecas))
        //Log.d("cyrus", "pelo bispo, pode");
        return torre.isMovePossible(inicioX, inicioY, destinoX, destinoY, jogo, pecas) ||
                bispo.isMovePossible(inicioX, inicioY, destinoX, destinoY, jogo, pecas);
    }

    @Override
    public boolean isDeliveringCheck(int posX, int posY, int[][] jogo) {
        return torre.isDeliveringCheck(posX, posY, jogo) || bispo.isDeliveringCheck(posX, posY, jogo);
    }

    public String toString() {
        return "Dama";
    }
}
