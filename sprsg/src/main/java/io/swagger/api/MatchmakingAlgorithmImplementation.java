package io.swagger.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;

import io.swagger.model.UserCollaborationIntentions;
import io.swagger.model.UserCollaborationSpec;
import io.swagger.model.UserCollaborationSpec.IntentionEnum;
import io.swagger.model.UserPairAssignment;
import io.swagger.model.UserPairwiseScore;
import io.swagger.model.UserScore;
import io.swagger.model.UtilityUser;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

public class MatchmakingAlgorithmImplementation {

	String intentions;
	boolean played_again = false;
	float weight = 0;
	int users_count = 0;

	MultiKeyMap multiKeyMap = MultiKeyMap.multiKeyMap(new LinkedMap());

	public ArrayList<UserPairAssignment> final_pair(List<UserScore> list, List<UserPairwiseScore> list2,
			List<UserCollaborationIntentions> list3) throws IOException {

		ArrayList<UtilityUser> global_utility = new ArrayList<>();
		ArrayList<UtilityUser> utility_per_user = new ArrayList<>();
		ArrayList<UtilityUser> tettt = new ArrayList<>();
		ArrayList<UserPairAssignment> final_pairs = new ArrayList<>();
		UserScore us = new UserScore();
		UtilityUser utility_user;

		users_count = list.size();

		/*
		 * Create an dictionary with all the users' possible combinations with the 2
		 * users each time as Keys and an index as values e.g : [user_1,user_2],2
		 */
		int cnt_temp = 0;
		for (int i = 0; i < users_count; i++) {
			for (int j = 0; j < users_count; j++) {
				multiKeyMap.put(list.get(i).getUserId(), list.get(j).getUserId(), cnt_temp);
				cnt_temp++;
			}
		}

		System.out.println("------------------------------------");
		System.out.println("Main");
		System.out.println("------------------------------------");

		UserPairwiseScore ups = null;

		// Iterate through the UPS list
		for (UserPairwiseScore element : list2) {
			ups = element;
			UserPairwiseScore ups_2 = null;
			// Iterate through the UPS list again so we can get the nested users
			for (UserPairwiseScore element2 : list2) {
				utility_user = new UtilityUser();
				ups_2 = element2;
				us = ups_2.getScoresGiven().get(0);

				// Check if the Outter user is the same as the nested one
				if (ups.getGradingUser().equals(ups_2.getGradingUser())
						&& ups.getScoresGiven().get(0).getUserId().equals(ups_2.getScoresGiven().get(0).getUserId())) {

					// System.out.println("The users match:");
					// System.out.println(" ups.getGradingUser(): " + ups.getGradingUser());
					// System.out.println(" ups_2.getGradingUser(): " + ups_2.getGradingUser());

					// If the both scores are 0 the players haven't played again
					if (us.getScore().getColaboration() != 0 && us.getScore().getQuality() != 0) {

						played_again = true;
						/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
						intentions = get_intentions((ArrayList<UserCollaborationIntentions>) list3,
								ups.getGradingUser(), us.getUserId());
						/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
						// System.out.println("(alt)Intentions for the pair: " + intentions);

					} else {

						played_again = false;
						/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
						intentions = get_intentions((ArrayList<UserCollaborationIntentions>) list3,
								ups.getGradingUser(), us.getUserId());
						/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
						// System.out.println("Intentions for the pair: " + intentions);

					}

					/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
					// Call the Weight for the pair function
					weight = weight(ups, ups_2, played_again, intentions);
					// System.out.println("Weight for the pair: " + weight);
					/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

					// Get the 2 users (outter and nested) and add them to the Utility User object
					utility_user.setUser_i(us.getUserId());
					utility_user.setUser_j(ups.getGradingUser());
					utility_user.setWeight(weight);
					utility_per_user.add(utility_user);

					continue;

				} else
					continue;

			}

		}

		/*
		 * call utility per user, to get the utility of each player with all others he
		 * has played with
		 */
		global_utility = utility_per_user_calculator(utility_per_user);

		/*
		 * Sort the arraylist on 2 levels, first by the user_i and then by the user_j
		 */
		global_utility.sort(Comparator.comparing(UtilityUser::getUser_i).thenComparing(UtilityUser::getUser_j));

		/////////// ~~~~~global utility function~~~~~~///////////////

		tettt = global_utilityFunc2(global_utility);
		/*
		 * Sort the arraylist a second time. So it can be in the same order as the
		 * dictionary
		 */
		tettt.sort(Comparator.comparing(UtilityUser::getUser_i).thenComparing(UtilityUser::getUser_j));
		try {
			// maximization problem
			final_pairs = maximize_lp(tettt);
		} catch (Exception e) {
			System.out.println("Something went wrong: " + e);
		}

		/////////// ~~~~~~~printing the dump text~~~~~~~///////////////
		String user_i, user_j;
		String weight;
		Scanner read = new Scanner(new File("temp_file.txt"));
		// use the "," and the "\n" as delimiters
		read.useDelimiter(",|\\n");

		while (read.hasNext()) {
			user_i = read.next().trim();
			user_j = read.next().trim();
			weight = read.next().trim();
			// x = read.next().trim();
			//dSystem.out.print(user_i + " " + user_j + " " + weight + "\n");
		}
		read.close();
		/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

//		while (final_pairs.size() != users_count / 2)
//			final_pairs.remove(final_pairs.size() - 1);

		return final_pairs;
	}

	private ArrayList<UserPairAssignment> maximize_lp(ArrayList<UtilityUser> last_users) {
		/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
		ArrayList<UserPairAssignment> final_pairs = new ArrayList<>();

		UtilityUser uu = new UtilityUser();

		// Our objective function simply sums up all the x_i,j.
		double[] objectiveFunction = new double[users_count * users_count];

		/*
		 * Iterate through the (sorted) Arraylist and the Dictionary. when a pair is
		 * found e.g user_1,user_3 then the weight of the pair is inserted in the
		 * objectiveFunction in the position specified by the Dictionary
		 */
		MapIterator it = multiKeyMap.mapIterator();
		for (UtilityUser last_user : last_users) {
			uu = last_user;
			while (it.hasNext()) {

				it.next();
				MultiKey mk = (MultiKey) it.getKey();

				if (uu.getUser_i().equals(mk.getKey(0)) && uu.getUser_j().equals(mk.getKey(1))) {
					objectiveFunction[(int) it.getValue()] = uu.getWeight();
					break;
				} else {
					objectiveFunction[(int) it.getValue()] = 0;
				}

			}
		}

		LinearProgram uglobal = new LinearProgram(objectiveFunction);
		uglobal.setMinProblem(false);

		/* All of the x_i,j variables are binary (0-1). */
		for (int i = 0; i < users_count * users_count; i++) {
			uglobal.setBinary(i);

		}

		rowConst(users_count, uglobal);
		System.out.println();

		System.out.println("\nStarting calculations . . .\n");
//		StringBuffer s = uglobal.convertToCPLEX();
//		System.out.println(s);

		LinearProgramSolver solver = SolverFactory.newDefault();
		double[] solution = solver.solve(uglobal);
		System.out.print(uglobal.convertToCPLEX());
		System.out.println("\nThe calculations ended . . .\n");

		// Print the solution for the pairing
		System.out.println();
		for (int i = 0; i < users_count; i++) {
			for (int j = 0; j < users_count; j++) {
				System.out.print("x" + (j + 1) + "," + (i + 1) + "=" + (int) solution[users_count * j + i] + " ");
			}
			System.out.println();
		}

		/*
		 * Get the results form the solution and with a comparison from the dictionary
		 * add them to the final arraylist
		 */
		it = multiKeyMap.mapIterator();
		int i = 0;

		while (it.hasNext() && i < users_count * users_count) {
			UserPairAssignment user_pair = new UserPairAssignment();
			UserPairAssignment user_pair_check = new UserPairAssignment();
			it.next();
			MultiKey mk = (MultiKey) it.getKey();

			if (solution[i] == 1) {

				user_pair.setUser1((String) mk.getKey(0));
				user_pair.setUser2((String) mk.getKey(1));

				// We use the check in order to avoid adding to the final
				// arraylist duplicate pairs AND to add the sigle player
				// if users_count is an odd number
				user_pair_check.setUser1((String) mk.getKey(1));
				user_pair_check.setUser2((String) mk.getKey(0));

				if (final_pairs.contains(user_pair_check)) {
					i++;
					continue;
				} else {
					final_pairs.add(user_pair);

				}

			} 

			i++;
		}
		return final_pairs;

	}

	/**
	 * Adds the "one user per row" constraints to the linear program, ie. the sum
	 * from j=1 to n of x_i,j = 1 for all i=1,...,n.
	 */
	public static void rowConst(int n, LinearProgram lp) {

		double[][] rowConstArr = new double[n][n * n];

		for (int row = 0; row < n; row++) {
			for (int column = n * row; column < n * row + n; column++) {
				if (row != column)
					rowConstArr[row][column] = 1;
				else
					rowConstArr[row][column] = 0;
				// System.out.print((int) rowConstArr[row][column] + " ");
			}
			// System.out.println();
			lp.addConstraint(new LinearEqualsConstraint(rowConstArr[row], 1, "r" + row));
		}

		double[][] rowConstArr2 = new double[n * n][n * n];
		for (int row = 0; row < n; row++) {
			for (int column = 0; column < n; column++) {
				// build constraint for element x_row_column
				int vindex = row * n + column;
				// initialize all coefficients to zero
				for (int k = 0; k < n * n; k++)
					rowConstArr2[vindex][k] = 0;
				if (row == column) {
					// diagonal elements have a restriction element = 1 if n= even or element=0 if
					// n= odd
					if (n % 2 == 0)
						rowConstArr2[vindex][vindex] = 1;
					else
						rowConstArr2[vindex][vindex] = 0;
				} else {
					// non-diagonal elements implement the restriction x_ij = x_ji.
					// Variable x_ij = row*n + column;
					// variable x_ji = column * n + row
					rowConstArr2[vindex][vindex] = 1;
					rowConstArr2[vindex][column * n + row] = -1;
				}

				lp.addConstraint(new LinearEqualsConstraint(rowConstArr2[vindex], 0, "vc" + vindex));

			}
		}

//		System.out.println("Row constraints");
//		printConstraints(rowConstArr);
//		System.out.println("Other constraints");
//		printConstraints(rowConstArr2);

	}

	public static void printConstraints(double[][] constr_arr) {
		for (double[] element : constr_arr) {
			for (int j = 0; j < element.length; j++) {
				System.out.print((int) element[j] + " ");
			}
			System.out.println();
		}
	}

	private float weight(UserPairwiseScore ups_i, UserPairwiseScore ups_j, boolean played_again, String intentions) {

		float weight = 0;
		UserScore us = new UserScore();
		UserScore us_2 = new UserScore();
		us = ups_i.getScoresGiven().get(0);
		us_2 = ups_j.getScoresGiven().get(0);

		/*
		 * if (played_again == true && intentions == IntentionEnum.WANT.toString()) {
		 * weight = 1 + ((us.getScore().getColaboration() +
		 * us_2.getScore().getColaboration()) / 2 + (us.getScore().getQuality() +
		 * us_2.getScore().getQuality()) / 2) / 10; } else if (played_again == true &&
		 * intentions == IntentionEnum.DWANT.toString()) { weight = -2 +
		 * ((us.getScore().getColaboration() + us_2.getScore().getColaboration()) / 2 +
		 * (us.getScore().getQuality() + us_2.getScore().getQuality()) / 2) / 10; } else
		 * if (played_again == false && intentions == IntentionEnum.WANT.toString()) {
		 * weight = 1; } else if (played_again == false && intentions ==
		 * IntentionEnum.IDC.toString()) { weight = 0; }
		 */
		if (played_again && intentions == IntentionEnum.WANT.toString()) {
			// System.out.println("~1~");
			weight = 1 + (((us.getScore().getColaboration() + us_2.getScore().getColaboration()) / 2
					+ (us.getScore().getQuality() + us_2.getScore().getQuality()) / 2) / 10);
		} else if (played_again && intentions == IntentionEnum.DWANT.toString()) {
			// System.out.println("~2~");
			weight = -2 + (((us.getScore().getColaboration() + us_2.getScore().getColaboration()) / 2
					+ (us.getScore().getQuality() + us_2.getScore().getQuality()) / 2) / 10);
		} else if (played_again && intentions == IntentionEnum.IDC.toString()) {
			// System.out.println("~3~");
			weight = (float) (-0.5 + ((us.getScore().getColaboration() + us_2.getScore().getColaboration()) / 2
					+ (us.getScore().getQuality() + us_2.getScore().getQuality()) / 2) / 10);
		} else if (!played_again && intentions == IntentionEnum.WANT.toString()) {
			// System.out.println("~4~");
			weight = 1 + (us_2.getScore().getColaboration() + us_2.getScore().getQuality() / 10);
		} else if (!played_again && intentions == IntentionEnum.DWANT.toString()) {
			// System.out.println("~5~");
			weight = -2 + (us_2.getScore().getColaboration() + us_2.getScore().getQuality() / 10);
		} else if (!played_again && intentions == IntentionEnum.IDC.toString()) {
			// System.out.println("~6~");
			weight = (float) (-0.5 + (us_2.getScore().getColaboration() + us_2.getScore().getQuality()) / 10);
		}

		return weight;
	}

	private String get_intentions(ArrayList<UserCollaborationIntentions> collaboration_intentions, String gradee,
			String graded) {
		UserCollaborationSpec ucs = new UserCollaborationSpec();
		UserCollaborationIntentions uci;
		UserCollaborationIntentions uci_tmp;

		// Iterate through the List with the Intentions for the players,
		// in order to get the outter player
		for (UserCollaborationIntentions collaboration_intention : collaboration_intentions) {
			uci = collaboration_intention;
			List<UserCollaborationSpec> uci_2;

			// Iterate again through the List with the Intentions for the players,
			// in order to get the nested player
			for (UserCollaborationIntentions collaboration_intention2 : collaboration_intentions) {
				uci_tmp = collaboration_intention2;
				uci_2 = collaboration_intention2.getIntentions();

				// Check if the Outter user is the one that is getting graded
				if (uci.getGradingUser().equals(gradee) && gradee.equals(uci_tmp.getGradingUser())) {

					ucs = uci_tmp.getIntentions().get(0);
					// Check if the inner user is the one that grades
					if (uci_2.get(0).getUserId().equals(graded)) {

						intentions = ucs.getIntention().toString();
						break;
					}

					continue;
				}
			}
		}
		return intentions;
	}

	private ArrayList<UtilityUser> utility_per_user_calculator(ArrayList<UtilityUser> utility_per_user) {

		ArrayList<UtilityUser> utility_user = new ArrayList<>();

		UtilityUser uu = new UtilityUser();
		UtilityUser uu_j = new UtilityUser();

		// Iterate through the List with the other players, a player has played
		// and store the user in our utility object
		for (int c = 0; c < utility_per_user.size(); c++) {

			uu.setUser_i(utility_per_user.get(c).getUser_i());
			uu.setUser_j(utility_per_user.get(c).getUser_j());
			uu.setWeight(utility_per_user.get(c).getWeight());

			// Iterate through the List with the other players, a player has played
			// to get the nested user
			for (int d = 0; d < utility_per_user.size(); d++) {

				UtilityUser tmp = new UtilityUser();
				uu_j = utility_per_user.get(d);

				// if the pair (nested outter ) matches
				// add the new pair ij to the arraylist
				if (uu.getUser_i().equals(uu_j.getUser_j()) && uu.getUser_j().equals(uu_j.getUser_i())) {

					tmp.setWeight(uu.getWeight());
					tmp.setUser_i(uu.getUser_i());
					tmp.setUser_j(uu_j.getUser_i());

//					System.out.printf("%d %d\n", c, d);
//					System.out.printf("%d) Utility Per User Func: \n", c);
//					System.out.println(" User i: " + tmp.getUser_i());
//					System.out.println(" User j: " + tmp.getUser_j());
//					System.out.println(" Weight: " + tmp.getWeight());

					utility_user.add(tmp);

//					array_creator(c,d,tmp.getX(),tmp.getWeight());

					break;
				}

			}

		}

		return utility_user;
	}

	private ArrayList<UtilityUser> global_utilityFunc2(ArrayList<UtilityUser> global_utility) throws IOException {

		ArrayList<UtilityUser> utility_user = new ArrayList<>();

		int x_ij = 0;
		int x_ji = 0;
		UtilityUser uu = new UtilityUser();
		UtilityUser uu_j = new UtilityUser();

		// Create the writer for the user dump
		FileWriter writer = new FileWriter("temp_file.txt", false);

		for (int e = 0; e < global_utility.size(); e++) {
			uu = global_utility.get(e);

			UtilityUser tmp = new UtilityUser();

			// check if uu.getWeight() !=0 x_ij=1 else 0
			x_ij = uu.getWeight() != 0 ? 1 : 0;

			for (int q = 0; q < global_utility.size(); q++) {

				uu_j = global_utility.get(q);

				// check if uu.getWeight() !=0 x_ji=1 else 0
				x_ji = uu_j.getWeight() != 0 ? 1 : 0;

//				 System.out.println("~~~~~~~~~~~~~~~~~~~");
//				 System.out.println("x_ij: " + x_ij + "\n" + "x_ji: " + x_ji);
//				 System.out
//					 .println("uu.getWeight(): " + uu.getWeight() + "\n" + "uu_j.getWeight(): " +
//					 uu_j.getWeight());

				if (x_ij == x_ji) {

					// The output form of the users with comma "," as a delimiter
					// test1,test2,1.690000057220459
					// test2,test1,0.2800000309944153
					writer.append(uu.getUser_i());
					writer.append(",");
					writer.append(uu.getUser_j());
					writer.append(",");
					writer.append(Double.toString(uu.getWeight()));
					writer.append(",");

					tmp.setUser_i(uu.getUser_i());
					tmp.setUser_j(uu.getUser_j());
					tmp.setWeight(uu.getWeight());

					utility_user.add(tmp);

					break;

				} else

					continue;

			}

		}

		writer.flush();
		writer.close();
		return utility_user;
	}

}
