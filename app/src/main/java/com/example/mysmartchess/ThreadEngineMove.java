package com.example.mysmartchess;

import android.os.Message;

import android.os.Handler;

import com.example.mysmartchess.EngineS22k.ChessBoard;
import com.example.mysmartchess.EngineS22k.ChessBoardUtil;
import com.example.mysmartchess.EngineS22k.move.MoveWrapper;
import com.example.mysmartchess.EngineS22k.move.PV;
import com.example.mysmartchess.EngineS22k.search.NegamaxUtil;
import com.example.mysmartchess.EngineS22k.search.TimeUtil;

public class ThreadEngineMove extends Thread {
    private Handler handler;
    private String comando;
    private ChessBoard cb;

    public ThreadEngineMove(MyEngine myEngine){
        this.handler = myEngine.getHandler();
        this.cb = myEngine.getCb();
        this.comando = myEngine.getComando();
    }

    @Override
    public void run() {
        Message message = new Message();

        MoveWrapper moveUser = new MoveWrapper(comando, cb);
        cb.doMove(moveUser.move);
        TimeUtil.setSimpleTimeWindow(10000);
        NegamaxUtil.start(cb);
        MoveWrapper bestMove = new MoveWrapper(PV.getBestMove());
        cb.doMove(bestMove.move);
        //captura movimento da engine
        message.obj = bestMove.toString();

        //Envia mensagem para a classe principal
        handler.sendMessage(message);
    }
}
