package edu.wit.cs.comp2350;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * 
 * @author kreimendahl
 */

// Provides a solution to the 0-1 knapsack problem
public class LAB7 {

	public static class Item {
		public int weight;
		public int value;
		public int index;

		public Item(int w, int v, int i) {
			weight = w;
			value = v;
			index = i;
		}

		public String toString() {
			return "(" + weight + "#, $" + value + ")"; 
		}
	}

	// set by calls to Find* methods
	private static int best_value = 0;

	// TODO: document this method
	public static Item[] FindDynamic(Item[] table, int weight) {
		
		// TODO: implement this method
		int[][] values = new int[table.length + 1][weight + 1];
		int[][] keeps = new int[table.length + 1][weight + 1];
		best_value = 0; //best value is initialized to zero every time this method is run in order to make sure the global best value starts from 0 every time this method is run.

		for (int i = 0; i < values[0].length; i ++) {
			values[0][i] = 0;
		}

		for (int i = 1; i <= table.length; i++) {
			for (int j = 1; j <= weight; j++ ) {
				if (table[i -1].weight <= j) {
					if (max(table[i-1].value + values[i-1][j-table[i-1].weight], values[i-1][j])) {
						values[i][j] = table[i - 1].value + values[i-1][j-table[i-1].weight];
						keeps[i-1][j] = 1;
					} else {
						values[i][j] = values[i-1][j];
					}
				} else {
					values[i][j] = values[i-1][j];
				}
			}
		}

		Item[] newkeep = new Item[table.length]; //adds all keep values with 1 to the newkeep knapsack of type item.
		int num = 0;
		for (int i = table.length-1; i>= 0; i--) {
			if (keeps[i][weight] == 1) {
				newkeep[num] = table[i];
				num++;
				best_value += table[i].value;
				weight -= table[i].weight;
			}
		}
		Item[] finalsack = new Item[num]; // adding values to this knapsack because the newkeep sack has extra empty spaces. also why num counter is needed.
		for (int i= 0; i < num; i ++) {
			finalsack[i] = newkeep[i];
		}
		/* for (int i = 0; i < values.length; i ++) {
			for (int j = 0; j < values[0].length; j++) {
				System.out.print(values[i][j] + " ");
			} System.out.println();
		} */

		return finalsack;
	}

	public static boolean max(int x, int y) {
		return ((x > y) ? true : false);
	}



	
	// enumerates all subsets of items to find maximum value that fits in knapsack
	public static Item[] FindEnumerate(Item[] table, int weight) {

		if (table.length > 63) {	// bitshift fails for larger sizes
			System.err.println("Problem size too large. Exiting");
			System.exit(0);
		}
		
		int nCr = 1 << table.length; // bitmask for included items
		int bestSum = -1;
		boolean[] bestUsed = {}; 
		boolean[] used = new boolean[table.length];
		
		for (int i = 0; i < nCr; i++) {	// test all combinations
			int temp = i;

			for (int j = 0; j < table.length; j++) {
				used[j] = (temp % 2 == 1);
				temp = temp >> 1;
			}

			if (TotalWeight(table, used) <= weight) {
				if (TotalValue(table, used) > bestSum) {
					bestUsed = Arrays.copyOf(used, used.length);
					bestSum = TotalValue(table, used);
				}
			}
		}

		int itemCount = 0;	// count number of items in best result
		for (int i = 0; i < bestUsed.length; i++)
			if (bestUsed[i])
				itemCount++;

		Item[] ret = new Item[itemCount];
		int retIndex = 0;

		for (int i = 0; i < bestUsed.length; i++) {	// construct item list
			if (bestUsed[i]) {
				ret[retIndex] = table[i];
				retIndex++;
			}
		}
		best_value = bestSum;
		return ret;

	}

	// returns total value of all items that are marked true in used array
	private static int TotalValue(Item[] table, boolean[] used) {
		int ret = 0;
		for (int i = 0; i < table.length; i++)
			if (used[i])
				ret += table[i].value;

		return ret;
	}

	// returns total weight of all items that are marked true in used array
	private static int TotalWeight(Item[] table, boolean[] used) {
		int ret = 0;
		for (int i = 0; i < table.length; i++) {
			if (used[i])
				ret += table[i].weight;
		}

		return ret;
	}

	// adds items to the knapsack by picking the next item with the highest
	// value:weight ratio. This could use a max-heap of ratios to run faster, but
	// it runs in n^2 time wrt items because it has to scan every item each time
	// an item is added
	public static Item[] FindGreedy(Item[] table, int weight) {
		boolean[] used = new boolean[table.length];
		int itemCount = 0;

		while (weight > 0) {	// while the knapsack has space
			int bestIndex = GetGreedyBest(table, used, weight);
			if (bestIndex < 0)
				break;
			weight -= table[bestIndex].weight;
			best_value += table[bestIndex].value;
			used[bestIndex] = true;
			itemCount++;
		}

		Item[] ret = new Item[itemCount];
		int retIndex = 0;

		for (int i = 0; i < used.length; i++) { // construct item list
			if (used[i]) {
				ret[retIndex] = table[i];
				retIndex++;
			}
		}

		return ret;
	}
	
	// finds the available item with the best value:weight ratio that fits in
	// the knapsack
	private static int GetGreedyBest(Item[] table, boolean[] used, int weight) {

		double bestVal = -1;
		int bestIndex = -1;
		for (int i = 0; i < table.length; i++) {
			double ratio = (table[i].value*1.0)/table[i].weight;
			if (!used[i] && (ratio > bestVal) && (weight >= table[i].weight)) {
				bestVal = ratio;
				bestIndex = i;
			}
		}

		return bestIndex;
	}

	public static int getBest() {
		return best_value;
	}
	
	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		String file1;
		int weight = 0;
		System.out.printf("Enter <objects file> <knapsack weight> <algorithm>, ([d]ynamic programming, [e]numerate, [g]reedy).\n");
		System.out.printf("(e.g: objects/small 10 g)\n");
		file1 = s.next();
		weight = s.nextInt();

		ArrayList<Item> tableList = new ArrayList<Item>();

		try (Scanner f = new Scanner(new File(file1))) {
			int i = 0;
			while(f.hasNextInt())
				tableList.add(new Item(f.nextInt(), f.nextInt(), i++));
		} catch (IOException e) {
			System.err.println("Cannot open file " + file1 + ". Exiting.");
			System.exit(0);
		}

		Item[] table = new Item[tableList.size()];
		for (int i = 0; i < tableList.size(); i++)
			table[i] = tableList.get(i);

		String algo = s.next();
		Item[] result = {};

		switch (algo.charAt(0)) {
		case 'd':
			result = FindDynamic(table, weight);
			break;
		case 'e':
			result = FindEnumerate(table, weight);
			break;
		case 'g':
			result = FindGreedy(table, weight);
			break;
		default:
			System.out.println("Invalid algorithm");
			System.exit(0);
			break;
		}

		s.close();

		System.out.printf("Index of included items: ");
		for (int i = 0; i < result.length; i++)
			System.out.printf("%d ", result[i].index);
		System.out.printf("\nBest value: %d\n", best_value);	
	}

}


		/*

		//for (int i = 0; i < table.length; i++) {
		//	for (int j = 0; j < weight; j++) {
		//		if (table[i -1].weight < weight && max(table[i].value, table[i-1].value) > ) ) {
//
//				}
//			}
//		}
		int i = 1;
		if (weight == 0) {
			return t;
		} else {
			if (table[i - 1].weight < weight) {

			}
		}

		*/