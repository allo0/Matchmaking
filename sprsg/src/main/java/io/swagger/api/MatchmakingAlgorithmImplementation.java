package io.swagger.api;

//import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;

import io.swagger.model.UserCollaborationIntentions;
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

	IntentionEnum intentionOfIToCollabWithJ = IntentionEnum.IDC;
	UserScore userJ_GlobalScore = new UserScore();
	List<UserPairwiseScore> ratingsForUserJ_byUserI = new ArrayList<UserPairwiseScore>();
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

		long startTime = System.nanoTime();

		UserScore userI = new UserScore();
		UserScore userJ = new UserScore();
		UserPairwiseScore score_by_userI_for_userJ = new UserPairwiseScore();
		UserCollaborationIntentions intentions_by_userI_for_userJ = new UserCollaborationIntentions();
		for (int i = 0; i < users_count; i++) {
			for (int j = 0; j < users_count; j++) {
				UtilityUser utility_user = new UtilityUser();
				ratingsForUserJ_byUserI = new ArrayList<>();

				userI = list.get(i);
				userJ = list.get(j);

				// if userI==userJ skip it
				if (userI.getUserId().equals(userJ.getUserId())) {
					continue;
				} else {
//					System.out.println("> " + userI.getUserId() + " " + userJ.getUserId());

					// Loop for the UserPairwiseScore list
					for (int k = 0; k < list2.size(); k++) {

						score_by_userI_for_userJ = list2.get(k);
						if ((score_by_userI_for_userJ.getGradingUser().equals(userI.getUserId())
								&& score_by_userI_for_userJ.getScoresGiven().get(0).getUserId()
										.equals(userJ.getUserId()))
								|| (score_by_userI_for_userJ.getGradingUser().equals(userJ.getUserId())
										&& score_by_userI_for_userJ.getScoresGiven().get(0).getUserId()
												.equals(userI.getUserId()))) {

							ratingsForUserJ_byUserI.add(score_by_userI_for_userJ);

						} else {
							/*
							 * If there is no entry in list2 for the userI having played with the userJ,then
							 * the ratingsForUserJ_byUserI stays with the default values and the algorithm
							 * continues with the next entry
							 */
							continue;
						}

					}

					intentionOfIToCollabWithJ = IntentionEnum.IDC;
					// Loop for the UserColaborationIntentions list
					for (int m = 0; m < list3.size(); m++) {
						intentions_by_userI_for_userJ = list3.get(m);
						if ((intentions_by_userI_for_userJ.getGradingUser().equals(userI.getUserId())
								&& intentions_by_userI_for_userJ.getIntentions().get(0).getUserId()
										.equals(userJ.getUserId()))
								|| (intentions_by_userI_for_userJ.getGradingUser().equals(userJ.getUserId())
										&& intentions_by_userI_for_userJ.getIntentions().get(0).getUserId()
												.equals(userJ.getUserId()))) {

							intentionOfIToCollabWithJ = intentions_by_userI_for_userJ.getIntentions().get(0)
									.getIntention();
						} else {
							/*
							 * If there is no entry in list2 for the userI having played with the userJ,then
							 * the intentionOfIToCollabWithJ stays with the default value (IDC) and the
							 * algorithm continues with the next entry
							 */
							continue;
						}

					}

				}
				userJ_GlobalScore = userJ;

//				System.out.println(">> " + userJ_GlobalScore);

				weight = weight(intentionOfIToCollabWithJ, ratingsForUserJ_byUserI, userJ_GlobalScore);

				utility_user.setUser_i(list.get(i).getUserId());
				utility_user.setUser_j(list.get(j).getUserId());
				utility_user.setWeight(weight);
				utility_per_user.add(utility_user);

				// System.out.println("---------------------------------");
			}

		}

		utility_per_user.sort(Comparator.comparing(UtilityUser::getUser_i).thenComparing(UtilityUser::getUser_j));
		
		long endTime = System.nanoTime();
		// get the difference between the two nano time values
		long timeElapsed = endTime - startTime;
		System.out
				.println("Time required for the creation of the pairs \nto be maximized and the sorting of the lists: "
						+ timeElapsed);
		System.out.println(
				"Time required for the creation of the pairs \nto be maximized and the sorting of the lists in milliseconds: "
						+ timeElapsed / 1000000);

		long timeElapsed2 = 0;
		try {
			startTime = System.nanoTime();

			// maximization problem
			final_pairs = maximize_lp(utility_per_user);

			endTime = System.nanoTime();
			// get the difference between the two nano time values
			timeElapsed2 = endTime - startTime;
			System.out.println("Time required for the maximization problem: " + timeElapsed2);
			System.out.println("Time required for the maximization problem in milliseconds: " + timeElapsed2 / 1000000);

		} catch (Exception e) {
			System.out.println("Something went wrong: " + e);
		}

		/////////// ~~~~~~~printing the dump text~~~~~~~///////////////
		/*
		 * String user_i, user_j; String weight; Scanner read = new Scanner(new
		 * File("temp_file.txt")); // use the "," and the "\n" as delimiters
		 * read.useDelimiter(",|\\n");
		 * 
		 * while (read.hasNext()) { user_i = read.next().trim(); user_j =
		 * read.next().trim(); weight = read.next().trim(); // x = read.next().trim();
		 * // dSystem.out.print(user_i + " " + user_j + " " + weight + "\n"); }
		 * read.close();
		 */
		/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
		System.out.println("Total time required: " + (timeElapsed2 + timeElapsed));
		System.out.println("Total time required in milliseconds: " + (timeElapsed2 + timeElapsed) / 1000000);
		return final_pairs;
	}

	/*
	 * Creates the maximization problem and returns the pairs.
	 */
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
		// StringBuffer s = uglobal.convertToCPLEX();
		// System.out.println(s);

		 LinearProgramSolver solver = SolverFactory.newDefault();
		

		double[] solution = solver.solve(uglobal);

//		System.out.print(uglobal.convertToCPLEX());
//		System.out.println("\nThe calculations ended . . .\n");

		// Print the solution for the pairing
//		System.out.println();
//		for (int i = 0; i < users_count; i++) {
//			for (int j = 0; j < users_count; j++) {
//				System.out.print("x" + (j + 1) + "," + (i + 1) + "=" + (int) solution[users_count * j + i] + " ");
//			}
//			System.out.println();
//		}

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
				// arraylist duplicate pairs AND to add the single player
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

	/*
	 * Adds the constraints: one user per row, the constraints on the diagonal, and
	 * the condition that x_ij=x_ji
	 */
	public static void rowConst(int n, LinearProgram lp) {

		double[][] rowConstArr = new double[n][n * n];
		double[][] rowConstArr2;

		for (int row = 0; row < n; row++) {
			for (int column = n * row; column < n * row + n; column++) {
				// if (row != column)
				// rowConstArr[row][column] = 1;
				// else
				// rowConstArr[row][column] = 0;
				// System.out.print((int) rowConstArr[row][column] + " ");
				rowConstArr[row][column] = 1;
			}
			// System.out.println();
			lp.addConstraint(new LinearEqualsConstraint(rowConstArr[row], 1, "r" + row));
		}

		if (n % 2 == 0) {
			rowConstArr2 = new double[n * n][n * n];
			for (int row = 0; row < n; row++) {
				for (int column = 0; column < n; column++) {
					// build constraint for element x_row_column
					int vindex = row * n + column;
					// initialize all coefficients to zero
					for (int k = 0; k < n * n; k++)
						rowConstArr2[vindex][k] = 0;
					if (row == column) {
						// diagonal elements have a restriction element = 0
						rowConstArr2[vindex][vindex] = 1;
					} else {
						// non-diagonal elements implement the restriction x_ij = x_ji.
						// Variable x_ij = row*n + column;
						// variable x_ji = column * n + row
						rowConstArr2[vindex][vindex] = 1;
						rowConstArr2[vindex][column * n + row] = -1;
					}
					lp.addConstraint(new LinearEqualsConstraint(rowConstArr2[vindex], 0, "vIc" + vindex));

				}
			}
		} else {
			rowConstArr2 = new double[n * n - n + 1][n * n];

			int vindex = 0;

			for (int row = 0; row < n; row++) {
				for (int column = 0; column < n; column++) {
					if (vindex == n * n - n)
						break;
					// build constraint for element x_row_column
					// initialize all coefficients to zero
					for (int k = 0; k < n * n; k++)
						rowConstArr2[vindex][k] = 0;
					if (row != column) {
						// non-diagonal elements implement the restriction x_ij = x_ji.
						// Variable x_ij = row*n + column;
						// variable x_ji = column * n + row
						rowConstArr2[vindex][vindex] = 1;
						rowConstArr2[vindex][column * n + row] = -1;

					}
					lp.addConstraint(new LinearEqualsConstraint(rowConstArr2[vindex], 0, "vOc" + vindex));
					vindex++;
				}

			}

			// add the last constraint, that ensures that only one diagonial element is
			// equal to one
			for (int k = 0; k < n * n; k++)
				rowConstArr2[vindex][k] = 0;
			for (int i = 0; i < n; i++)
				rowConstArr2[vindex][i * n + i] = 1;
			lp.addConstraint(new LinearEqualsConstraint(rowConstArr2[vindex], 1, "vAc" + vindex));

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
		System.out.println();
	}

	private float weight(IntentionEnum intentionOfIToCollabWithJ, List<UserPairwiseScore> ratingsForUserJ_byUserI,
			UserScore userJ_GlobalScore) {
		float quality = 0;
		float colaboration = 0;

		if (!ratingsForUserJ_byUserI.isEmpty()) {// users have collaborated before

			for (int i = 0; i < ratingsForUserJ_byUserI.size(); i++) {
				quality += ratingsForUserJ_byUserI.get(i).getScoresGiven().get(0).getScore().getQuality();
				colaboration += ratingsForUserJ_byUserI.get(i).getScoresGiven().get(0).getScore().getColaboration();
			}
			quality = quality / ratingsForUserJ_byUserI.size();
			colaboration = colaboration / ratingsForUserJ_byUserI.size();
			if (intentionOfIToCollabWithJ == IntentionEnum.WANT) {
				return (1 + (quality + colaboration) / 10);
			} else if (intentionOfIToCollabWithJ == IntentionEnum.DWANT) {
				return (-2 + (quality + colaboration) / 10);
			} else {// intentionOfIToCollabWithJ == IntentionEnum.IDC)
				return (float) (-0.5 + (quality + colaboration) / 10);
			}
		} else { // users have not collaborated in the past

			if (intentionOfIToCollabWithJ == IntentionEnum.WANT) {
				return (1 + (userJ_GlobalScore.getScore().getQuality() + userJ_GlobalScore.getScore().getColaboration())
						/ 10);
			} else if (intentionOfIToCollabWithJ == IntentionEnum.DWANT) {
				return (-2
						+ (userJ_GlobalScore.getScore().getQuality() + userJ_GlobalScore.getScore().getColaboration())
								/ 10);
			} else { // intentionOfIToCollabWithJ == IntentionEnum.IDC)
				return (float) (-0.5
						+ (userJ_GlobalScore.getScore().getQuality() + userJ_GlobalScore.getScore().getColaboration())
								/ 10);
			}
		}

	}




}
