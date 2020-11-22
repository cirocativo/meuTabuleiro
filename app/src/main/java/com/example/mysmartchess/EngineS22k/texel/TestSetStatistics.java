package com.example.mysmartchess.EngineS22k.texel;

import java.util.Map;
import java.util.Map.Entry;

import com.example.mysmartchess.EngineS22k.ChessBoard;
import com.example.mysmartchess.EngineS22k.ChessBoardUtil;

public class TestSetStatistics {

	private static int[] pieceCounts = new int[33];

	public static void main(String[] args) {

		Map<String, Double> fens = Tuner.loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, false);
		System.out.println(fens.size() + " fens found");

		ChessBoard cb = ChessBoardUtil.getNewCB();
		for (Entry<String, Double> entry : fens.entrySet()) {
			ChessBoardUtil.setFenValues(entry.getKey(), cb);
			ChessBoardUtil.init(cb);

			pieceCounts[Long.bitCount(cb.allPieces)]++;

		}

		for (int i = 0; i < 33; i++) {
			System.out.println(i + " " + pieceCounts[i]);
		}

	}

}
