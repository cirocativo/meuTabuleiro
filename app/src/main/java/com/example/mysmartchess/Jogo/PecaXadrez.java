package com.example.mysmartchess.Jogo;

import android.widget.ImageView;

import java.util.List;

public class PecaXadrez {
    private ImageView imageView;
    private boolean pecaBranca;
    private boolean hasNotMoved = true;

    boolean hasNotMoved() {
        return hasNotMoved;
    }

    public void setHasNotMoved(boolean hasNotMoved) {
        this.hasNotMoved = hasNotMoved;
    }

    PecaXadrez(boolean pecaBranca) {
        this.pecaBranca = pecaBranca;
    }

    PecaXadrez(boolean pecaBranca, ImageView imageView) {
        this.pecaBranca = pecaBranca;
        this.imageView = imageView;
    }

    ImageView getImageView() {
        return imageView;
    }

    public boolean isMovePossible(int inicioX, int inicioY, int destinoX, int destinoY, int[][] jogo, List<PecaXadrez> pecas) {
        return true;
    }

    public boolean isDeliveringCheck(int posX, int posY, int[][] jogo) {
        return false;
    }

    public boolean isPecaBranca() {
        return pecaBranca;
    }
}
