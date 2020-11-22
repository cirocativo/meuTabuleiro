package com.example.mysmartchess.EngineS22k.search;

import java.util.Arrays;

import com.example.mysmartchess.EngineS22k.Assert;
import com.example.mysmartchess.EngineS22k.ChessConstants;
import com.example.mysmartchess.EngineS22k.Statistics;
import com.example.mysmartchess.EngineS22k.Util;
import com.example.mysmartchess.EngineS22k.engine.EngineConstants;
import com.example.mysmartchess.EngineS22k.eval.EvalConstants;
import com.example.mysmartchess.EngineS22k.move.MoveUtil;
import com.example.mysmartchess.EngineS22k.move.MoveWrapper;

public class TTUtil {

	private static int keyShifts;
	public static int maxEntries;

	private static long[] keys;
	private static long[] values;

	private static long usageCounter;

	public static final int FLAG_EXACT = 0;
	public static final int FLAG_UPPER = 1;
	public static final int FLAG_LOWER = 2;

	public static long halfMoveCounter = 0;

	// ///////////////////// DEPTH //12 bits
	private static final int FLAG = 12; // 2
	private static final int MOVE = 14; // 22
	private static final int SCORE = 48; // 16

	public static boolean isInitialized = false;

	public static void init(final boolean force) {
		if (force || !isInitialized) {
			keyShifts = 64 - EngineConstants.POWER_2_TT_ENTRIES;
			maxEntries = (int) Util.POWER_LOOKUP[EngineConstants.POWER_2_TT_ENTRIES] + 3;

			keys = new long[maxEntries];
			values = new long[maxEntries];
			usageCounter = 0;

			isInitialized = true;
		}
	}

	public static void clearValues() {
		if (!isInitialized) {
			return;
		}
		Arrays.fill(keys, 0);
		Arrays.fill(values, 0);
		usageCounter = 0;
	}

	public static long getTTValue(final long key) {

		final int index = getIndex(key);

		for (int i = 0; i < 4; i++) {
			long value = values[index + i];
			if ((keys[index + i] ^ value) == key) {
				if (Statistics.ENABLED) {
					Statistics.ttHits++;
				}
				return value;
			}
		}

		if (Statistics.ENABLED) {
			Statistics.ttMisses++;
		}
		return 0;
	}

	private static int getIndex(final long key) {
		return (int) (key >>> keyShifts);
	}

	public static void addValue(final long key, int score, final int ply, final int depth, final int flag, final int move) {

		if (EngineConstants.ASSERT) {
			Assert.isTrue(depth >= 1);
			Assert.isTrue(move != 0);
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
			Assert.isTrue(MoveUtil.getSourcePieceIndex(move) != 0);
			Assert.isTrue(score != ChessConstants.SCORE_NOT_RUNNING);
		}

		final int index = getIndex(key);
		int replacedDepth = Integer.MAX_VALUE;
		int replacedIndex = index;
		for (int i = index; i < index + 4; i++) {

			if (keys[i] == 0) {
				if (Statistics.ENABLED) {
					usageCounter++;
				}
				replacedIndex = i;
				break;
			}

			long currentValue = values[i];
			int currentDepth = getDepth(currentValue);
			if ((keys[i] ^ currentValue) == key) {
				if (currentDepth > depth && flag != FLAG_EXACT) {
					return;
				}
				replacedIndex = i;
				break;
			}

			// replace the lowest depth
			if (currentDepth < replacedDepth) {
				replacedIndex = i;
				replacedDepth = currentDepth;
			}
		}

		// correct mate-score
		if (score > EvalConstants.SCORE_MATE_BOUND) {
			// Math.min because of qsearch
			score = Math.min(score + ply, Util.SHORT_MAX);
		} else if (score < -EvalConstants.SCORE_MATE_BOUND) {
			// Math.max because of qsearch
			score = Math.max(score - ply, Util.SHORT_MIN);
		}
		if (EngineConstants.ASSERT) {
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
		}

		final long value = createValue(score, move, flag, depth);
		keys[replacedIndex] = key ^ value;
		values[replacedIndex] = value;
	}

	public static int getScore(final long value, final int ply) {
		int score = (int) (value >> SCORE);

		// correct mate-score
		if (score > EvalConstants.SCORE_MATE_BOUND) {
			score -= ply;
		} else if (score < -EvalConstants.SCORE_MATE_BOUND) {
			score += ply;
		}

		if (EngineConstants.ASSERT) {
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
		}

		return score;
	}

	public static int getDepth(final long value) {
		return (int) ((value & 0xff) - halfMoveCounter);
	}

	public static int getFlag(final long value) {
		return (int) (value >>> FLAG & 3);
	}

	public static int getMove(final long value) {
		return (int) (value >>> MOVE & 0x3fffff);
	}

	// SCORE,HALF_MOVE_COUNTER,MOVE,FLAG,DEPTH
	public static long createValue(final long score, final long move, final long flag, final long depth) {
		if (EngineConstants.ASSERT) {
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
			Assert.isTrue(depth <= 255);
		}
		return score << SCORE | move << MOVE | flag << FLAG | (depth + halfMoveCounter);
	}

	public static String toString(long ttValue) {
		return "score=" + TTUtil.getScore(ttValue, 0) + " " + new MoveWrapper(getMove(ttValue)) + " depth=" + TTUtil.getDepth(ttValue) + " flag="
				+ TTUtil.getFlag(ttValue);
	}

	public static void setSizeMB(int value) {
		switch (value) {
		case 1:
		case 2:
		case 4:
		case 8:
		case 16:
		case 32:
		case 64:
		case 128:
		case 256:
		case 512:
		case 1024:
		case 2048:
		case 4096:
		case 8192:
		case 16384:
			int power2Entries = (int) (Math.log(value) / Math.log(2) + 16);
			if (EngineConstants.POWER_2_TT_ENTRIES != power2Entries) {
				EngineConstants.POWER_2_TT_ENTRIES = power2Entries;
				init(true);
			}
			break;
		default:
			throw new RuntimeException("Hash-size must be between 1-16384 mb and a multiple of 2");
		}
	}

	public static long getUsagePercentage() {
		return usageCounter * 1000 / TTUtil.maxEntries;
	}
}
