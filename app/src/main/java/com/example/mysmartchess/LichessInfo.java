package com.example.mysmartchess;

public class LichessInfo {
    private String tokenLichess = "A2d9ZnMNkBuACKO5";
    private String gameIdLichess = "";
    private boolean lichessAtivado = false; //ativado se o jogador estiver jogando online
    private volatile String username = "";// pra salver o nome do jogador

    public LichessInfo(){

    }

    public String getGameIdLichess() {
        return gameIdLichess;
    }

    public boolean isLichessAtivado() {
        return lichessAtivado;
    }

    public String getTokenLichess() {
        return tokenLichess;
    }

    public String getUsername() {
        return username;
    }
}
