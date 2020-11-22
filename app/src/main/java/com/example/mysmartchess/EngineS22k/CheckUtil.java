package com.example.mysmartchess.EngineS22k;

import static com.example.mysmartchess.EngineS22k.ChessConstants.BISHOP;
import static com.example.mysmartchess.EngineS22k.ChessConstants.KING;
import static com.example.mysmartchess.EngineS22k.ChessConstants.NIGHT;
import static com.example.mysmartchess.EngineS22k.ChessConstants.PAWN;
import static com.example.mysmartchess.EngineS22k.ChessConstants.QUEEN;
import static com.example.mysmartchess.EngineS22k.ChessConstants.ROOK;

import com.example.mysmartchess.EngineS22k.move.MagicUtil;
import com.example.mysmartchess.EngineS22k.move.StaticMoves;

public final class CheckUtil {

	//@formatter:off

	public static long getCheckingPieces(final ChessBoard cb) {
		final int kingIndex = cb.kingIndex[cb.colorToMove];

		// put 'super-piece' in kings position
		return (cb.pieces[cb.colorToMoveInverse][NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (cb.pieces[cb.colorToMoveInverse][ROOK]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getRookMoves(kingIndex, cb.allPieces)
				| (cb.pieces[cb.colorToMoveInverse][BISHOP]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getBishopMoves(kingIndex, cb.allPieces) 
				| cb.pieces[cb.colorToMoveInverse][PAWN] & StaticMoves.PAWN_ATTACKS[cb.colorToMove][kingIndex]
			);
	}
	
	public static long getCheckingPieces(final ChessBoard cb, final int sourcePieceIndex) {
		switch(sourcePieceIndex) {
			case PAWN:
				return cb.pieces[cb.colorToMoveInverse][PAWN] & StaticMoves.PAWN_ATTACKS[cb.colorToMove][cb.kingIndex[cb.colorToMove]];
			case NIGHT:
				return cb.pieces[cb.colorToMoveInverse][NIGHT] & StaticMoves.KNIGHT_MOVES[cb.kingIndex[cb.colorToMove]];
			case BISHOP:
				return cb.pieces[cb.colorToMoveInverse][BISHOP] & MagicUtil.getBishopMoves(cb.kingIndex[cb.colorToMove], cb.allPieces);
			case ROOK:
				return cb.pieces[cb.colorToMoveInverse][ROOK] & MagicUtil.getRookMoves(cb.kingIndex[cb.colorToMove], cb.allPieces);
			case QUEEN:
				return cb.pieces[cb.colorToMoveInverse][QUEEN] & MagicUtil.getQueenMoves(cb.kingIndex[cb.colorToMove], cb.allPieces);
			default:
				//king can never set the other king in check
				return 0;	
		}
	}

	public static boolean isInCheck(final int kingIndex, final int colorToMove, final long[] enemyPieces, final long allPieces) {
	
		// put 'super-piece' in kings position
		return (enemyPieces[NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (enemyPieces[ROOK] | enemyPieces[QUEEN]) & MagicUtil.getRookMoves(kingIndex, allPieces)
				| (enemyPieces[BISHOP] | enemyPieces[QUEEN]) & MagicUtil.getBishopMoves(kingIndex, allPieces) 
				| enemyPieces[PAWN] & StaticMoves.PAWN_ATTACKS[colorToMove][kingIndex]
			)!= 0;
	}

	public static boolean isInCheckIncludingKing(final int kingIndex, final int colorToMove, final long[] enemyPieces, final long allPieces, final int enemyMajorPieces) {

		//TODO
		if(enemyMajorPieces==0) {
			return (enemyPieces[PAWN] & StaticMoves.PAWN_ATTACKS[colorToMove][kingIndex]
					| enemyPieces[KING] & StaticMoves.KING_MOVES[kingIndex]
				)!= 0;
		}
		
		// put 'super-piece' in kings position
		return (enemyPieces[NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (enemyPieces[ROOK] | enemyPieces[QUEEN]) & MagicUtil.getRookMoves(kingIndex, allPieces)
				| (enemyPieces[BISHOP] | enemyPieces[QUEEN]) & MagicUtil.getBishopMoves(kingIndex, allPieces) 
				| enemyPieces[PAWN] & StaticMoves.PAWN_ATTACKS[colorToMove][kingIndex]
				| enemyPieces[KING] & StaticMoves.KING_MOVES[kingIndex]
			)!= 0;
	}
}
