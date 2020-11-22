package com.example.mysmartchess.EngineS22k;

import static com.example.mysmartchess.EngineS22k.ChessConstants.BISHOP;
import static com.example.mysmartchess.EngineS22k.ChessConstants.BLACK;
import static com.example.mysmartchess.EngineS22k.ChessConstants.EMPTY;
import static com.example.mysmartchess.EngineS22k.ChessConstants.KING;
import static com.example.mysmartchess.EngineS22k.ChessConstants.NIGHT;
import static com.example.mysmartchess.EngineS22k.ChessConstants.PAWN;
import static com.example.mysmartchess.EngineS22k.ChessConstants.QUEEN;
import static com.example.mysmartchess.EngineS22k.ChessConstants.ROOK;
import static com.example.mysmartchess.EngineS22k.ChessConstants.WHITE;

import com.example.mysmartchess.EngineS22k.engine.EngineConstants;
import com.example.mysmartchess.EngineS22k.engine.MainEngine;
import com.example.mysmartchess.EngineS22k.eval.EvalConstants;
import com.example.mysmartchess.EngineS22k.eval.EvalUtil;
import com.example.mysmartchess.EngineS22k.eval.MaterialUtil;
import com.example.mysmartchess.EngineS22k.move.MagicUtil;
import com.example.mysmartchess.EngineS22k.move.MoveUtil;
import com.example.mysmartchess.EngineS22k.move.StaticMoves;

public final class ChessBoard {

	private ChessBoard() {

	}

	private static ChessBoard[] instances;
	static {
		initInstances(MainEngine.nrOfThreads);
	}

	public static ChessBoard getInstance() {
		return instances[0];
	}

	public static ChessBoard getInstance(int instanceNumber) {
		return instances[instanceNumber];
	}

	public static ChessBoard getTestInstance() {
		return instances[1];
	}

	public static void initInstances(int numberOfInstances) {
		instances = new ChessBoard[numberOfInstances];
		for (int i = 0; i < numberOfInstances; i++) {
			instances[i] = new ChessBoard();
		}
	}

	public static long getTotalMoveCount() {
		long totalMoveCount = 0;
		for (int i = 0; i < MainEngine.nrOfThreads; i++) {
			totalMoveCount += getInstance(i).moveCount;
		}
		return totalMoveCount;
	}

	/** color, piece */
	public final long[][] pieces = new long[2][7];
	public final long[] friendlyPieces = new long[2];

	/** 4 bits: white-king,white-queen,black-king,black-queen */
	public int castlingRights;
	public int psqtScore;
	public int colorToMove, colorToMoveInverse;
	public int epIndex;
	public int materialKey;
	public int phase;

	public long allPieces, emptySpaces;
	public long zobristKey, pawnZobristKey;
	public long checkingPieces, pinnedPieces, discoveredPieces;

	public long moveCount;

	/** which piece is on which square */
	public final int[] pieceIndexes = new int[64];
	public final int[] kingIndex = new int[2];
	public final long[] kingArea = new long[2];

	public int moveCounter = 0;
	public final int[] castlingHistory = new int[EngineConstants.MAX_MOVES];
	public final int[] epIndexHistory = new int[EngineConstants.MAX_MOVES];
	public final long[] zobristKeyHistory = new long[EngineConstants.MAX_MOVES];
	public final long[] checkingPiecesHistory = new long[EngineConstants.MAX_MOVES];
	public final long[] pinnedPiecesHistory = new long[EngineConstants.MAX_MOVES];
	public final long[] discoveredPiecesHistory = new long[EngineConstants.MAX_MOVES];

	// attack boards
	public final long[][] attacks = new long[2][7];
	public final long[] attacksAll = new long[2];
	public final long[] doubleAttacks = new long[2];
	public final int[] kingAttackersFlag = new int[2];

	public long passedPawnsAndOutposts;

	@Override
	public String toString() {
		return ChessBoardUtil.toString(this);
	}

	public boolean isDrawishByMaterial(final int color) {
		// no pawns or queens
		if (MaterialUtil.hasPawnsOrQueens(materialKey, color)) {
			return false;
		}

		// material difference bigger than bishop + 50
		// TODO do not include pawn score (why...?)
		return EvalUtil.getImbalances(this) * ChessConstants.COLOR_FACTOR[color] < EvalConstants.MATERIAL[BISHOP]
				+ EvalConstants.OTHER_SCORES[EvalConstants.IX_DRAWISH];
	}

	public void changeSideToMove() {
		colorToMove = colorToMoveInverse;
		colorToMoveInverse = 1 - colorToMove;
	}

	public boolean isDiscoveredMove(final int fromIndex) {
		return (discoveredPieces & (1L << fromIndex)) != 0;
	}

	private void pushHistoryValues() {
		castlingHistory[moveCounter] = castlingRights;
		epIndexHistory[moveCounter] = epIndex;
		zobristKeyHistory[moveCounter] = zobristKey;
		pinnedPiecesHistory[moveCounter] = pinnedPieces;
		discoveredPiecesHistory[moveCounter] = discoveredPieces;
		checkingPiecesHistory[moveCounter] = checkingPieces;
		moveCounter++;
	}

	private void popHistoryValues() {
		moveCounter--;
		epIndex = epIndexHistory[moveCounter];
		zobristKey = zobristKeyHistory[moveCounter];
		castlingRights = castlingHistory[moveCounter];
		pinnedPieces = pinnedPiecesHistory[moveCounter];
		discoveredPieces = discoveredPiecesHistory[moveCounter];
		checkingPieces = checkingPiecesHistory[moveCounter];
	}

	public void doNullMove() {
		pushHistoryValues();

		zobristKey ^= Zobrist.sideToMove;
		if (epIndex != 0) {
			zobristKey ^= Zobrist.epIndex[epIndex];
			epIndex = 0;
		}
		changeSideToMove();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}
	}

	public void undoNullMove() {
		popHistoryValues();
		changeSideToMove();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}
	}

	public void doMove(int move) {

		moveCount++;

		final int fromIndex = MoveUtil.getFromIndex(move);
		int toIndex = MoveUtil.getToIndex(move);
		long toMask = 1L << toIndex;
		final long fromToMask = (1L << fromIndex) ^ toMask;
		final int sourcePieceIndex = MoveUtil.getSourcePieceIndex(move);
		final int attackedPieceIndex = MoveUtil.getAttackedPieceIndex(move);

		if (EngineConstants.ASSERT) {
			Assert.isTrue(attackedPieceIndex != KING);
			Assert.isTrue(attackedPieceIndex == 0 || (Util.POWER_LOOKUP[toIndex] & friendlyPieces[colorToMove]) == 0);
		}

		pushHistoryValues();

		zobristKey ^= Zobrist.piece[fromIndex][colorToMove][sourcePieceIndex] ^ Zobrist.piece[toIndex][colorToMove][sourcePieceIndex] ^ Zobrist.sideToMove;
		if (epIndex != 0) {
			zobristKey ^= Zobrist.epIndex[epIndex];
			epIndex = 0;
		}

		friendlyPieces[colorToMove] ^= fromToMask;
		pieceIndexes[fromIndex] = EMPTY;
		pieceIndexes[toIndex] = sourcePieceIndex;
		pieces[colorToMove][sourcePieceIndex] ^= fromToMask;
		psqtScore += EvalConstants.PSQT[sourcePieceIndex][colorToMove][toIndex] - EvalConstants.PSQT[sourcePieceIndex][colorToMove][fromIndex];

		switch (sourcePieceIndex) {
		case PAWN:
			pawnZobristKey ^= Zobrist.piece[fromIndex][colorToMove][PAWN];
			if (MoveUtil.isPromotion(move)) {
				phase -= EvalConstants.PHASE[MoveUtil.getMoveType(move)];
				materialKey += MaterialUtil.VALUES[colorToMove][MoveUtil.getMoveType(move)] - MaterialUtil.VALUES[colorToMove][PAWN];
				pieces[colorToMove][PAWN] ^= toMask;
				pieces[colorToMove][MoveUtil.getMoveType(move)] |= toMask;
				pieceIndexes[toIndex] = MoveUtil.getMoveType(move);
				zobristKey ^= Zobrist.piece[toIndex][colorToMove][PAWN] ^ Zobrist.piece[toIndex][colorToMove][MoveUtil.getMoveType(move)];
				psqtScore += EvalConstants.PSQT[MoveUtil.getMoveType(move)][colorToMove][toIndex] - EvalConstants.PSQT[PAWN][colorToMove][toIndex];
			} else {
				pawnZobristKey ^= Zobrist.piece[toIndex][colorToMove][PAWN];
				// 2-move
				if (ChessConstants.IN_BETWEEN[fromIndex][toIndex] != 0) {
					if ((StaticMoves.PAWN_ATTACKS[colorToMove][Long.numberOfTrailingZeros(ChessConstants.IN_BETWEEN[fromIndex][toIndex])]
							& pieces[colorToMoveInverse][PAWN]) != 0) {
						epIndex = Long.numberOfTrailingZeros(ChessConstants.IN_BETWEEN[fromIndex][toIndex]);
						zobristKey ^= Zobrist.epIndex[epIndex];
					}
				}
			}
			break;

		case ROOK:
			if (castlingRights != 0) {
				zobristKey ^= Zobrist.castling[castlingRights];
				castlingRights = CastlingUtil.getRookMovedOrAttackedCastlingRights(castlingRights, fromIndex);
				zobristKey ^= Zobrist.castling[castlingRights];
			}
			break;

		case KING:
			updateKingValues(colorToMove, toIndex);
			if (castlingRights != 0) {
				if (MoveUtil.isCastlingMove(move)) {
					CastlingUtil.castleRookUpdateKeyAndPsqt(this, toIndex);
				}
				zobristKey ^= Zobrist.castling[castlingRights];
				castlingRights = CastlingUtil.getKingMovedCastlingRights(castlingRights, fromIndex);
				zobristKey ^= Zobrist.castling[castlingRights];
			}
		}

		// piece hit?
		switch (attackedPieceIndex) {
		case EMPTY:
			break;
		case PAWN:
			if (MoveUtil.isEPMove(move)) {
				toIndex += ChessConstants.COLOR_FACTOR_8[colorToMoveInverse];
				toMask = Util.POWER_LOOKUP[toIndex];
				pieceIndexes[toIndex] = EMPTY;
			}
			pawnZobristKey ^= Zobrist.piece[toIndex][colorToMoveInverse][PAWN];
			psqtScore -= EvalConstants.PSQT[PAWN][colorToMoveInverse][toIndex];
			friendlyPieces[colorToMoveInverse] ^= toMask;
			pieces[colorToMoveInverse][PAWN] ^= toMask;
			zobristKey ^= Zobrist.piece[toIndex][colorToMoveInverse][PAWN];
			materialKey -= MaterialUtil.VALUES[colorToMoveInverse][PAWN];
			break;
		case ROOK:
			if (castlingRights != 0) {
				zobristKey ^= Zobrist.castling[castlingRights];
				castlingRights = CastlingUtil.getRookMovedOrAttackedCastlingRights(castlingRights, toIndex);
				zobristKey ^= Zobrist.castling[castlingRights];
			}
			// fall-through
		default:
			phase += EvalConstants.PHASE[attackedPieceIndex];
			psqtScore -= EvalConstants.PSQT[attackedPieceIndex][colorToMoveInverse][toIndex];
			friendlyPieces[colorToMoveInverse] ^= toMask;
			pieces[colorToMoveInverse][attackedPieceIndex] ^= toMask;
			zobristKey ^= Zobrist.piece[toIndex][colorToMoveInverse][attackedPieceIndex];
			materialKey -= MaterialUtil.VALUES[colorToMoveInverse][attackedPieceIndex];
		}

		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];
		emptySpaces = ~allPieces;
		changeSideToMove();

		// update checking pieces
		if (isDiscoveredMove(fromIndex)) {
			checkingPieces = CheckUtil.getCheckingPieces(this);
		} else {
			if (MoveUtil.isNormalMove(move)) {
				checkingPieces = CheckUtil.getCheckingPieces(this, sourcePieceIndex);
			} else {
				checkingPieces = CheckUtil.getCheckingPieces(this);
			}
		}

		// TODO can this be done incrementally?
		setPinnedAndDiscoPieces();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}

	}

	public void setPinnedAndDiscoPieces() {

		pinnedPieces = 0;
		discoveredPieces = 0;

		for (int kingColor = ChessConstants.WHITE; kingColor <= ChessConstants.BLACK; kingColor++) {

			int enemyColor = 1 - kingColor;

			if (!MaterialUtil.hasSlidingPieces(materialKey, enemyColor)) {
				continue;
			}

			long enemyPiece = (pieces[enemyColor][BISHOP] | pieces[enemyColor][QUEEN]) & MagicUtil.getBishopMovesEmptyBoard(kingIndex[kingColor])
					| (pieces[enemyColor][ROOK] | pieces[enemyColor][QUEEN]) & MagicUtil.getRookMovesEmptyBoard(kingIndex[kingColor]);
			while (enemyPiece != 0) {
				final long checkedPiece = ChessConstants.IN_BETWEEN[kingIndex[kingColor]][Long.numberOfTrailingZeros(enemyPiece)] & allPieces;
				if (Long.bitCount(checkedPiece) == 1) {
					pinnedPieces |= checkedPiece & friendlyPieces[kingColor];
					discoveredPieces |= checkedPiece & friendlyPieces[enemyColor];
				}
				enemyPiece &= enemyPiece - 1;
			}

		}
	}

	public void undoMove(int move) {

		final int fromIndex = MoveUtil.getFromIndex(move);
		int toIndex = MoveUtil.getToIndex(move);
		long toMask = 1L << toIndex;
		final long fromToMask = (1L << fromIndex) ^ toMask;
		final int sourcePieceIndex = MoveUtil.getSourcePieceIndex(move);
		final int attackedPieceIndex = MoveUtil.getAttackedPieceIndex(move);

		popHistoryValues();

		// undo move
		friendlyPieces[colorToMoveInverse] ^= fromToMask;
		pieceIndexes[fromIndex] = sourcePieceIndex;
		pieces[colorToMoveInverse][sourcePieceIndex] ^= fromToMask;
		psqtScore += EvalConstants.PSQT[sourcePieceIndex][colorToMoveInverse][fromIndex] - EvalConstants.PSQT[sourcePieceIndex][colorToMoveInverse][toIndex];

		switch (sourcePieceIndex) {
		case EMPTY:
			// not necessary but provides a table-index
			break;
		case PAWN:
			pawnZobristKey ^= Zobrist.piece[fromIndex][colorToMoveInverse][PAWN];
			if (MoveUtil.isPromotion(move)) {
				phase += EvalConstants.PHASE[MoveUtil.getMoveType(move)];
				materialKey -= MaterialUtil.VALUES[colorToMoveInverse][MoveUtil.getMoveType(move)] - MaterialUtil.VALUES[colorToMoveInverse][PAWN];
				pieces[colorToMoveInverse][PAWN] ^= toMask;
				pieces[colorToMoveInverse][MoveUtil.getMoveType(move)] ^= toMask;
				psqtScore += EvalConstants.PSQT[PAWN][colorToMoveInverse][toIndex]
						- EvalConstants.PSQT[MoveUtil.getMoveType(move)][colorToMoveInverse][toIndex];
			} else {
				pawnZobristKey ^= Zobrist.piece[toIndex][colorToMoveInverse][PAWN];
			}
			break;
		case KING:
			if (MoveUtil.isCastlingMove(move)) {
				CastlingUtil.uncastleRookUpdatePsqt(this, toIndex);
			}
			updateKingValues(colorToMoveInverse, fromIndex);
		}

		// undo hit
		switch (attackedPieceIndex) {
		case EMPTY:
			break;
		case PAWN:
			if (MoveUtil.isEPMove(move)) {
				pieceIndexes[toIndex] = EMPTY;
				toIndex += ChessConstants.COLOR_FACTOR_8[colorToMove];
				toMask = Util.POWER_LOOKUP[toIndex];
			}
			psqtScore += EvalConstants.PSQT[PAWN][colorToMove][toIndex];
			pawnZobristKey ^= Zobrist.piece[toIndex][colorToMove][PAWN];
			pieces[colorToMove][attackedPieceIndex] |= toMask;
			friendlyPieces[colorToMove] |= toMask;
			materialKey += MaterialUtil.VALUES[colorToMove][PAWN];
			break;
		default:
			psqtScore += EvalConstants.PSQT[attackedPieceIndex][colorToMove][toIndex];
			phase -= EvalConstants.PHASE[attackedPieceIndex];
			materialKey += MaterialUtil.VALUES[colorToMove][attackedPieceIndex];
			pieces[colorToMove][attackedPieceIndex] |= toMask;
			friendlyPieces[colorToMove] |= toMask;
		}

		pieceIndexes[toIndex] = attackedPieceIndex;
		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];
		emptySpaces = ~allPieces;
		changeSideToMove();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}
	}

	public void updateKingValues(final int kingColor, final int index) {
		kingIndex[kingColor] = index;
		kingArea[kingColor] = ChessConstants.KING_AREA[kingColor][index];
	}

	public boolean isLegal(final int move) {
		if (MoveUtil.getSourcePieceIndex(move) == KING) {
			return !CheckUtil.isInCheckIncludingKing(MoveUtil.getToIndex(move), colorToMove, pieces[colorToMoveInverse],
					allPieces ^ Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)], MaterialUtil.getMajorPieces(materialKey, colorToMoveInverse));
		}

		if (MoveUtil.getAttackedPieceIndex(move) != 0) {
			if (MoveUtil.isEPMove(move)) {
				return isLegalEPMove(MoveUtil.getFromIndex(move));
			}
			return true;
		}

		if (checkingPieces != 0) {
			return !CheckUtil.isInCheck(kingIndex[colorToMove], colorToMove, pieces[colorToMoveInverse],
					allPieces ^ Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)] ^ Util.POWER_LOOKUP[MoveUtil.getToIndex(move)]);
		}
		return true;
	}

	private boolean isLegalEPMove(final int fromIndex) {

		// do move, check if in check, undo move. slow but also not called very often

		final long fromToMask = Util.POWER_LOOKUP[fromIndex] ^ Util.POWER_LOOKUP[epIndex];

		// do-move and hit
		friendlyPieces[colorToMove] ^= fromToMask;
		pieces[colorToMoveInverse][PAWN] ^= Util.POWER_LOOKUP[epIndex + ChessConstants.COLOR_FACTOR_8[colorToMoveInverse]];
		allPieces = friendlyPieces[colorToMove]
				| friendlyPieces[colorToMoveInverse] ^ Util.POWER_LOOKUP[epIndex + ChessConstants.COLOR_FACTOR_8[colorToMoveInverse]];

		/* Check if is in check */
		final boolean isInCheck = CheckUtil.getCheckingPieces(this) != 0;

		// undo-move and hit
		friendlyPieces[colorToMove] ^= fromToMask;
		pieces[colorToMoveInverse][PAWN] |= Util.POWER_LOOKUP[epIndex + ChessConstants.COLOR_FACTOR_8[colorToMoveInverse]];
		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];

		return !isInCheck;

	}

	public boolean isValidQuietMove(final int move) {

		// check piece at from square
		final int fromIndex = MoveUtil.getFromIndex(move);
		if ((pieces[colorToMove][MoveUtil.getSourcePieceIndex(move)] & Util.POWER_LOOKUP[fromIndex]) == 0) {
			return false;
		}

		// no piece should be at to square
		final int toIndex = MoveUtil.getToIndex(move);
		if (pieceIndexes[toIndex] != EMPTY) {
			return false;
		}

		// check if move is possible
		switch (MoveUtil.getSourcePieceIndex(move)) {
		case PAWN:
			// 2-move
			if (Math.abs(fromIndex - toIndex) > 8 && (allPieces & Util.POWER_LOOKUP[fromIndex + ChessConstants.COLOR_FACTOR_8[colorToMove]]) != 0) {
				return false;
			}
			break;
		case NIGHT:
			break;
		case BISHOP:
			if ((MagicUtil.getBishopMoves(fromIndex, allPieces) & Util.POWER_LOOKUP[toIndex]) == 0) {
				return false;
			}
			break;
		case ROOK:
			if ((MagicUtil.getRookMoves(fromIndex, allPieces) & Util.POWER_LOOKUP[toIndex]) == 0) {
				return false;
			}
			break;
		case QUEEN:
			if ((MagicUtil.getQueenMoves(fromIndex, allPieces) & Util.POWER_LOOKUP[toIndex]) == 0) {
				return false;
			}
			break;
		case KING:
			if (MoveUtil.isCastlingMove(move)) {
				long castlingIndexes = CastlingUtil.getCastlingIndexes(this);
				while (castlingIndexes != 0) {
					if (toIndex == Long.numberOfTrailingZeros(castlingIndexes)) {
						return CastlingUtil.isValidCastlingMove(this, fromIndex, toIndex);
					}
					castlingIndexes &= castlingIndexes - 1;
				}
				return false;
			}
			return true;

		}

		if ((Util.POWER_LOOKUP[fromIndex] & pinnedPieces) == 0) {
			return true;
		}
		return (ChessConstants.PINNED_MOVEMENT[fromIndex][kingIndex[colorToMove]] & Util.POWER_LOOKUP[toIndex]) != 0;
	}

	/**
	 * Method for testing the validity of tt-moves. This test is most of the time disabled.
	 */
	public boolean isValidMove(int move) {
		if (MoveUtil.getAttackedPieceIndex(move) == 0 && !MoveUtil.isPromotion(move)) {
			return isValidQuietMove(move);
		}

		if (MoveUtil.isPromotion(move) || MoveUtil.isCastlingMove(move) || MoveUtil.isEPMove(move)) {
			// TODO
			return true;
		}

		// check piece at from-position
		final int fromIndex = MoveUtil.getFromIndex(move);
		if ((pieces[colorToMove][MoveUtil.getSourcePieceIndex(move)] & Util.POWER_LOOKUP[fromIndex]) == 0) {
			return false;
		}

		// same piece should be at to-position
		final int toIndex = MoveUtil.getToIndex(move);
		if ((pieces[colorToMoveInverse][MoveUtil.getAttackedPieceIndex(move)] & Util.POWER_LOOKUP[toIndex]) == 0) {
			return false;
		}

		// TODO
		return true;
	}

	public boolean isRepetition(final int move) {

		if (!EngineConstants.ENABLE_REPETITION_TABLE) {
			return false;
		}

		// if move was an attacking-move or pawn move, no repetition
		if (!MoveUtil.isQuiet(move) || MoveUtil.getSourcePieceIndex(move) == PAWN) {
			return false;
		}

		final int moveCounterMin = Math.max(0, moveCounter - 50);
		for (int i = moveCounter - 2; i >= moveCounterMin; i -= 2) {
			if (zobristKey == zobristKeyHistory[i]) {
				if (Statistics.ENABLED) {
					Statistics.repetitions++;
				}
				return true;
			}
		}
		return false;
	}

	public void clearEvalAttacks() {
		kingAttackersFlag[WHITE] = 0;
		kingAttackersFlag[BLACK] = 0;
		attacks[WHITE][NIGHT] = 0;
		attacks[BLACK][NIGHT] = 0;
		attacks[WHITE][BISHOP] = 0;
		attacks[BLACK][BISHOP] = 0;
		attacks[WHITE][ROOK] = 0;
		attacks[BLACK][ROOK] = 0;
		attacks[WHITE][QUEEN] = 0;
		attacks[BLACK][QUEEN] = 0;
		doubleAttacks[WHITE] = 0;
		doubleAttacks[BLACK] = 0;
	}

}