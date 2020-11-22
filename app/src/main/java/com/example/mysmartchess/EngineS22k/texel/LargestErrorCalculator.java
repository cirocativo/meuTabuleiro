package com.example.mysmartchess.EngineS22k.texel;

import java.util.Map;
import java.util.Map.Entry;

import com.example.mysmartchess.EngineS22k.ChessBoard;
import com.example.mysmartchess.EngineS22k.ChessBoardUtil;
import com.example.mysmartchess.EngineS22k.eval.EvalUtil;

public class LargestErrorCalculator {

	private static double[] largestError = new double[100];
	private static String[] largestErrorFen = new String[100];

	public static void main(String[] args) {

		Map<String, Double> fens = Tuner.loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, false);
		System.out.println(fens.size() + " fens found");

		ChessBoard cb = ChessBoardUtil.getNewCB();
		for (Entry<String, Double> entry : fens.entrySet()) {
			ChessBoardUtil.setFenValues(entry.getKey(), cb);
			ChessBoardUtil.init(cb);
			double error = Math.pow(entry.getValue() - ErrorCalculator.calculateSigmoid(EvalUtil.calculateScore(cb)), 2);

			for (int i = 0; i < largestError.length; i++) {
				if (error > largestError[i]) {
					largestError[i] = error;
					largestErrorFen[i] = entry.getKey();
					break;
				}
			}

		}

		for (int i = 0; i < largestError.length; i++) {
			System.out.println(String.format("%60s -> %s", largestErrorFen[i], largestError[i]));
		}

	}

}
