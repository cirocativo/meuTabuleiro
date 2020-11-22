package com.example.mysmartchess.EngineS22k.search;

import com.example.mysmartchess.EngineS22k.Assert;
import com.example.mysmartchess.EngineS22k.CheckUtil;
import com.example.mysmartchess.EngineS22k.ChessBoard;
import com.example.mysmartchess.EngineS22k.ChessConstants;
import com.example.mysmartchess.EngineS22k.Statistics;
import com.example.mysmartchess.EngineS22k.Util;
import com.example.mysmartchess.EngineS22k.engine.EngineConstants;
import com.example.mysmartchess.EngineS22k.eval.EvalConstants;
import com.example.mysmartchess.EngineS22k.eval.EvalUtil;
import com.example.mysmartchess.EngineS22k.eval.SEEUtil;
import com.example.mysmartchess.EngineS22k.move.MoveGenerator;
import com.example.mysmartchess.EngineS22k.move.MoveUtil;

public class QuiescenceUtil {

	private static final int FUTILITY_MARGIN = 200;

	public static int calculateBestMove(final ChessBoard cb, final MoveGenerator moveGen, int alpha, final int beta) {

		if (Statistics.ENABLED) {
			Statistics.qNodes++;
		}

		if (!NegamaxUtil.isRunning) {
			return ChessConstants.SCORE_NOT_RUNNING;
		}

		/* stand-pat check */
		int eval = Util.SHORT_MIN;
		if (cb.checkingPieces == 0) {
			eval = EvalUtil.getScore(cb);
			if (eval >= beta) {
				return eval;
			}
			alpha = Math.max(alpha, eval);
		}

		moveGen.startPly();
		moveGen.generateAttacks(cb);
		moveGen.setMVVLVAScores();
		moveGen.sort();

		while (moveGen.hasNext()) {
			final int move = moveGen.next();

			// skip under promotions
			if (MoveUtil.isPromotion(move)) {
				if (MoveUtil.getMoveType(move) != MoveUtil.TYPE_PROMOTION_Q) {
					continue;
				}
			} else if (EngineConstants.ENABLE_Q_FUTILITY_PRUNING
					&& eval + FUTILITY_MARGIN + EvalConstants.MATERIAL[MoveUtil.getAttackedPieceIndex(move)] < alpha) {
				// futility pruning
				continue;
			}

			if (!cb.isLegal(move)) {
				continue;
			}

			// skip bad-captures
			if (EngineConstants.ENABLE_Q_PRUNE_BAD_CAPTURES && !cb.isDiscoveredMove(MoveUtil.getFromIndex(move)) && SEEUtil.getSeeCaptureScore(cb, move) <= 0) {
				continue;
			}

			cb.doMove(move);

			if (EngineConstants.ASSERT) {
				cb.changeSideToMove();
				Assert.isTrue(0 == CheckUtil.getCheckingPieces(cb));
				cb.changeSideToMove();
			}

			final int score = -calculateBestMove(cb, moveGen, -beta, -alpha);

			cb.undoMove(move);

			if (score >= beta) {
				moveGen.endPly();
				return score;
			}
			alpha = Math.max(alpha, score);
		}

		moveGen.endPly();
		return alpha;
	}
}
