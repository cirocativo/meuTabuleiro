package com.example.mysmartchess.EngineS22k;

import android.os.Build;

import java.util.Arrays;
import java.util.stream.IntStream;

import com.example.mysmartchess.EngineS22k.eval.EvalCache;
import com.example.mysmartchess.EngineS22k.eval.MaterialCache;
import com.example.mysmartchess.EngineS22k.eval.PawnEvalCache;
import com.example.mysmartchess.EngineS22k.eval.SEEUtil;
import com.example.mysmartchess.EngineS22k.move.MoveUtil;
import com.example.mysmartchess.EngineS22k.search.TTUtil;

public class Statistics {

	public static final boolean ENABLED = false;

	public static long startTime = System.nanoTime();
	public static long evalNodes, abNodes, seeNodes, pvNodes, cutNodes, allNodes, qNodes, evaluatedInCheck;
	public static long ttHits, ttMisses;
	public static int staleMateCount, mateCount;
	public static int depth, maxDepth;
	public static int epCount, castleCount, promotionCount;
	public static long pawnEvalCacheHits, pawnEvalCacheMisses;
	public static long materialCacheMisses, materialCacheHits;
	public static int bestMoveTT, bestMoveTTLower, bestMoveTTUpper, bestMoveCounter, bestMoveKiller1, bestMoveKiller2, bestMoveKillerEvasive1,
			bestMoveKillerEvasive2, bestMoveOther, bestMovePromotion, bestMoveWinningCapture, bestMoveLosingCapture;
	public static int repetitions, repetitionTests;
	public static int checkExtensions, endGameExtensions;
	public static int nullMoveHit, nullMoveMiss;
	public static long evalCacheHits, evalCacheMisses;
	public static int iidCount;
	public static final int[] razored = new int[10];
	public static final int[] futile = new int[10];
	public static final int[] staticNullMoved = new int[10];
	public static final int[] lmped = new int[10];
	public static final int[] failHigh = new int[64];
	public static int drawishByMaterialCount;

	public static long calculateNps() {
		return ChessBoard.getTotalMoveCount() * 1000 / Math.max(getPassedTimeMs(), 1);
	}

	public static void reset() {
		Arrays.fill(razored, 0);
		Arrays.fill(futile, 0);
		Arrays.fill(staticNullMoved, 0);
		Arrays.fill(lmped, 0);
		Arrays.fill(failHigh, 0);

		bestMoveCounter = 0;
		evaluatedInCheck = 0;
		qNodes = 0;
		pvNodes = 1; // so we never divide by zero
		cutNodes = 0;
		allNodes = 0;
		drawishByMaterialCount = 0;
		pawnEvalCacheMisses = 0;
		pawnEvalCacheHits = 0;
		startTime = System.nanoTime();
		castleCount = 0;
		epCount = 0;
		evalNodes = 0;
		ttHits = 0;
		ttMisses = 0;
		staleMateCount = 0;
		mateCount = 0;
		depth = 0;
		maxDepth = 0;
		abNodes = 0;
		promotionCount = 0;
		seeNodes = 0;
		repetitions = 0;
		nullMoveHit = 0;
		nullMoveMiss = 0;
		bestMoveTT = 0;
		bestMoveTTLower = 0;
		bestMoveTTUpper = 0;
		bestMoveKiller1 = 0;
		bestMoveKiller2 = 0;
		bestMoveKillerEvasive1 = 0;
		bestMoveKillerEvasive2 = 0;
		bestMoveOther = 0;
		bestMovePromotion = 0;
		bestMoveWinningCapture = 0;
		bestMoveLosingCapture = 0;
		checkExtensions = 0;
		endGameExtensions = 0;
		repetitionTests = 0;
		evalCacheHits = 0;
		evalCacheMisses = 0;
		iidCount = 0;
	}

	public static void print() {
		if (!Statistics.ENABLED) {
			return;
		}
		System.out.println("Time          " + getPassedTimeMs() + "ms");
		System.out.println("NPS           " + calculateNps() / 1000 + "k");
		System.out.println("Depth         " + depth + "/" + maxDepth);
		System.out.println("AB-nodes      " + abNodes);
		System.out.println("PV-nodes      " + pvNodes + " = 1/" + (pvNodes + cutNodes + allNodes) / pvNodes);
		System.out.println("Cut-nodes     " + cutNodes);
		printPercentage("Cut 1         ", failHigh[0], cutNodes - failHigh[0]);
		printPercentage("Cut 2         ", failHigh[1], cutNodes - failHigh[1]);
		printPercentage("Cut 3         ", failHigh[2], cutNodes - failHigh[2]);
		System.out.println("All-nodes     " + allNodes);
		System.out.println("Q-nodes       " + qNodes);
		System.out.println("See-nodes     " + seeNodes);
		System.out.println("Evaluated     " + evalNodes);
		System.out.println("Eval in check " + evaluatedInCheck);
		System.out.println("Moves         " + ChessBoard.getTotalMoveCount());
		System.out.println("IID           " + iidCount);

		System.out.println("### Caches #######");
		printPercentage("TT            ", ttHits, ttMisses);
		if (TTUtil.maxEntries != 0) {
			System.out.println("usage         " + TTUtil.getUsagePercentage() + "%");
		}
		printPercentage("Eval          ", evalCacheHits, evalCacheMisses);
		System.out.println("usage         " + EvalCache.usageCounter * 100 / EvalCache.MAX_TABLE_ENTRIES + "%");
		printPercentage("Pawn eval     ", pawnEvalCacheHits, pawnEvalCacheMisses);
		System.out.println("usage         " + PawnEvalCache.usageCounter * 100 / PawnEvalCache.MAX_TABLE_ENTRIES + "%");
		printPercentage("Material      ", materialCacheHits, materialCacheMisses);
		System.out.println("usage         " + PawnEvalCache.usageCounter * 100 / MaterialCache.MAX_TABLE_ENTRIES + "%");

		System.out.println("## Best moves #####");
		System.out.println("TT            " + bestMoveTT);
		System.out.println("TT-upper      " + bestMoveTTUpper);
		System.out.println("TT-lower      " + bestMoveTTLower);
		System.out.println("Win-cap       " + bestMoveWinningCapture);
		System.out.println("Los-cap       " + bestMoveLosingCapture);
		System.out.println("Promo         " + bestMovePromotion);
		System.out.println("Killer1       " + bestMoveKiller1);
		System.out.println("Killer2       " + bestMoveKiller2);
		System.out.println("Killer1 evasi " + bestMoveKillerEvasive1);
		System.out.println("Killer2 evasi " + bestMoveKillerEvasive2);
		System.out.println("Counter       " + bestMoveCounter);
		System.out.println("Other         " + bestMoveOther);

		System.out.println("### Outcome #####");
		System.out.println("Checkmate     " + mateCount);
		System.out.println("Stalemate     " + staleMateCount);
		System.out.println("Repetitions   " + repetitions + "(" + repetitionTests + ")");
		System.out.println("Drawish-mtrl  " + drawishByMaterialCount);

		System.out.println("### Extensions #####");
		System.out.println("Check         " + checkExtensions);
		System.out.println("Endgame       " + endGameExtensions);

		System.out.println("### Pruning #####");
		printPercentage("Null-move     ", nullMoveHit, nullMoveMiss);
		printDepthTotals("Static nmp    ", staticNullMoved, false);
		printDepthTotals("Razored       ", razored, false);
		printDepthTotals("Futile        ", futile, false);
		printDepthTotals("LMP           ", lmped, false);
	}

	private static void printDepthTotals(String message, int[] values, boolean printDetails) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			System.out.println(message + IntStream.of(values).sum());
		}
		if (printDetails) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] != 0) {
					System.out.println(i + " " + values[i]);
				}
			}
		}
	}

	private static void printPercentage(String message, long hitCount, long failCount) {
		if (hitCount != 0 && failCount != 0) {
			System.out.println(message + hitCount + "/" + (failCount + hitCount) + " (" + hitCount * 100 / (hitCount + failCount) + "%)");
		}
	}

	public static long getPassedTimeMs() {
		return (System.nanoTime() - startTime) / 1000000;
	}

	public static void setBestMove(ChessBoard cb, int bestMove, int ttMove, long ttValue, int flag, int counterMove, int killer1Move, int killer2Move) {
		if (flag == TTUtil.FLAG_LOWER) {
			Statistics.cutNodes++;
		} else if (flag == TTUtil.FLAG_UPPER) {
			Statistics.allNodes++;
		} else {
			Statistics.pvNodes++;
		}
		if (bestMove == ttMove) {
			if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_LOWER) {
				Statistics.bestMoveTTLower++;
			} else if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_UPPER) {
				Statistics.bestMoveTTUpper++;
			} else {
				Statistics.bestMoveTT++;
			}
		} else if (MoveUtil.isPromotion(bestMove)) {
			Statistics.bestMovePromotion++;
		} else if (MoveUtil.getAttackedPieceIndex(bestMove) != 0) {
			// slow but disabled when statistics are disabled
			if (SEEUtil.getSeeCaptureScore(cb, bestMove) < 0) {
				Statistics.bestMoveLosingCapture++;
			} else {
				Statistics.bestMoveWinningCapture++;
			}
		} else if (bestMove == counterMove) {
			Statistics.bestMoveCounter++;
		} else if (bestMove == killer1Move && cb.checkingPieces == 0) {
			Statistics.bestMoveKiller1++;
		} else if (bestMove == killer2Move && cb.checkingPieces == 0) {
			Statistics.bestMoveKiller2++;
		} else if (bestMove == killer1Move && cb.checkingPieces != 0) {
			Statistics.bestMoveKillerEvasive1++;
		} else if (bestMove == killer2Move && cb.checkingPieces != 0) {
			Statistics.bestMoveKillerEvasive2++;
		} else {
			Statistics.bestMoveOther++;
		}

	}

}
