package com.example.mysmartchess;

import android.os.Handler;

import com.example.mysmartchess.EngineS22k.ChessBoard;
import com.example.mysmartchess.EngineS22k.ChessBoardUtil;


public class MyEngine {
    private Handler handler;
    private String comando = "";
    private ChessBoard cb = ChessBoardUtil.getNewCB();

    public MyEngine(Handler handler) {
        this.handler = handler;
    }

    public void setComando(String comando) {
        this.comando = comando;
    }

    public Handler getHandler() {
        return handler;
    }

    public String getComando() {
        return comando;
    }

    public ChessBoard getCb() {
        return cb;
    }

}
