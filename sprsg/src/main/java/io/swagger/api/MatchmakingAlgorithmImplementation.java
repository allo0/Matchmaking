package io.swagger.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.util.Scanner;

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

	boolean played_again = false;
	String intentions;
	float weight = 0;
	String[][] user2 = null;
	String[][] user1 = null;
	int users_count = 0;
	int users_count2 = 0;

	public ArrayList<UserPairAssignment> final_pair(List<UserScore> list, List<UserPairwiseScore> list2,
			List<UserCollaborationIntentions> list3) throws IOException {

		ArrayList<UtilityUser> global_utility = new ArrayList<UtilityUser>();
		ArrayList<UtilityUser> utility_per_user = new ArrayList<UtilityUser>();
		ArrayList<UserPairAssignment> temp_res = new ArrayList<UserPairAssignment>();
		ArrayList<UtilityUser> tettt = new ArrayList<UtilityUser>();
		UserScore us = new UserScore();
		UtilityUser utility_user;

		// Testing
		// Create the arrays for the weight and the x_ij of the players
		// initialized with null so as if the size of the UserPairwiseScore is <
		// than the UserScore we can somehow use it in the maximization problem
		users_count = list.size();
//		users_count2 = list2.size();
//		UserScore us1 = new UserScore();
//		UserScore us2 = new UserScore();
//		user2 = new String[users_count][users_count];
//		user1 = new String[users_count][users_count];
//		for (int i = 0; i < users_count; i++) {
//			us2 = list.get(i);
//			for (int j = 0; j < users_count; j++) {
//				us1 = list.get(j);
//
//				if (i == j) {
//					user1[i][j] = "0";
//					user2[i][j] = "0";
//				} else {
//					user2[i][j] = us2.getUserId();
//					user1[i][j] = us1.getUserId();
//				}
//
//			}
//
//		}
//		for (int i = 0; i < users_count; i++) {
//			for (int j = 0; j < users_count; j++) {
//				System.out.print(user2[i][j] + " ");
//			}
//			System.out.println();
//		}
//		for (int i = 0; i < users_count; i++) {
//			for (int j = 0; j < users_count; j++) {
//				System.out.print(user1[i][j] + " ");
//			}
//			System.out.println();
//		}

		System.out.println("------------------------------------");
		System.out.println("Main");
		System.out.println("------------------------------------");

		UserPairwiseScore ups = null;

		// Iterate through the UPS list
		for (int a = 0; a < list2.size(); a++) {
			ups = list2.get(a);
			UserPairwiseScore ups_2 = null;
			// Iterate through the UPS list again so we can get the nested users
			for (int b = 0; b < list2.size(); b++) {
				utility_user = new UtilityUser();
				ups_2 = list2.get(b);
				us = ups_2.getScoresGiven().get(0);

//				// Check if the Outter user is the same as the nested one
				if (ups.getGradingUser().equals(ups_2.getGradingUser())
						&& ups.getScoresGiven().get(0).getUserId().equals(ups_2.getScoresGiven().get(0).getUserId())) {
					// Check if the Outter user is the same as the nested one
//				if (ups.getGradingUser().equals(ups_2.getScoresGiven().get(0).getUserId())
//						&& ups.getScoresGiven().get(0).getUserId().equals(ups_2.getGradingUser())) {
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

/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
		// call utility per user, to get the utility of each player
		// with all others he has played with
		global_utility = utility_per_user_calculator(utility_per_user);
/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

/////////// ~Sorting the arraylist~///////////////
//		Collections.sort(global_utility, (UtilityUser s1, UtilityUser s2) -> {
//			return s1.getUser_i().compareToIgnoreCase(s2.getUser_i());
//		});
		global_utility.sort(Comparator.comparing(UtilityUser::getUser_i).thenComparing(UtilityUser::getUser_j));
/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

/////////// ~~~~~global utility function~~~~~~///////////////

		temp_res = global_utilityFunc(global_utility);

		tettt = global_utilityFunc2(global_utility);
		try {
			// maximization problem
			maximize_lp(tettt);
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
			System.out.print(user_i + " " + user_j + " " + weight + "\n");
		}
		read.close();

/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

		// it will return the results from global_utilityFunc2, not global_utilityFunc
		// (which will be removed)
		return temp_res;
	}

	private void maximize_lp(ArrayList<UtilityUser> last_users) {
		/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

		UtilityUser uu = new UtilityUser();

		 //Our objective function simply sums up all the x_i,j.
		double[] objectiveFunction = new double[users_count * users_count];
		int ofs = 0;
		for (int i = 0; i < users_count * users_count; i++) {
			if (i == (users_count * ofs + ofs)) { // ignore the elements that are on the "diagonal"

				ofs++;
//				objectiveFunction[i] =0;
				continue;
			} else {
				
				uu = last_users.get(i / users_count);
				objectiveFunction[i] = uu.getWeight();
//				System.out.println(i / users_count);
//				continue;
			}

		}

//		double[] objectiveFunction = { 0.0, 2.0, -1.88, 2.11, -1.88, 0.0, 1.51, 5.19, 1.79, 1.67, 0.0, 0, 1.21, 4.67, 0,
//				0.0 };
		for (double i : objectiveFunction) {
			System.out.println(i + " ");
		}

		LinearProgram uglobal = new LinearProgram(objectiveFunction);
		uglobal.setMinProblem(false);

		/* All of the x_i,j variables are binary (0-1). */
		for (int i = 0; i < users_count * users_count; i++) {
			uglobal.setBinary(i);

		}

		rowConst(users_count, uglobal);
		System.out.println();

//		System.out.println("\nStarting calculations . . .\n");
//		StringBuffer s = uglobal.convertToCPLEX();
//		System.out.println(s);

		LinearProgramSolver solver = SolverFactory.newDefault();
		double[] solution = solver.solve(uglobal);
//		System.out.print(uglobal.convertToCPLEX());
//		System.out.println("\nThe calculations ended . . .\n");
		
		// Print the solution for the pairing
		System.out.println();
		for (int i = 0; i < users_count; i++) {
			for (int j = 0; j < users_count; j++) {
				System.out.print("x" + (j + 1) + "," + (i + 1) + "=" + (int) solution[users_count * j + i] + "  ");
			}
			System.out.println();
		}

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
					// diagonal elements have a restriction element = 0
					rowConstArr2[vindex][vindex] = 1;
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
		for (int i = 0; i < constr_arr.length; i++) {
			for (int j = 0; j < constr_arr[i].length; j++) {
				System.out.print((int) constr_arr[i][j] + " ");
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
		if (played_again == true && intentions == IntentionEnum.WANT.toString()) {
			// System.out.println("~1~");
			weight = 1 + (((us.getScore().getColaboration() + us_2.getScore().getColaboration()) / 2
					+ (us.getScore().getQuality() + us_2.getScore().getQuality()) / 2) / 10);
		} else if (played_again == true && intentions == IntentionEnum.DWANT.toString()) {
			// System.out.println("~2~");
			weight = -2 + (((us.getScore().getColaboration() + us_2.getScore().getColaboration()) / 2
					+ (us.getScore().getQuality() + us_2.getScore().getQuality()) / 2) / 10);
		} else if (played_again == true && intentions == IntentionEnum.IDC.toString()) {
			// System.out.println("~3~");
			weight = (float) (-0.5 + ((us.getScore().getColaboration() + us_2.getScore().getColaboration()) / 2
					+ (us.getScore().getQuality() + us_2.getScore().getQuality()) / 2) / 10);
		} else if (played_again == false && intentions == IntentionEnum.WANT.toString()) {
			// System.out.println("~4~");
			weight = 1 + (us_2.getScore().getColaboration() + us_2.getScore().getQuality() / 10);
		} else if (played_again == false && intentions == IntentionEnum.DWANT.toString()) {
			// System.out.println("~5~");
			weight = -2 + (us_2.getScore().getColaboration() + us_2.getScore().getQuality() / 10);
		} else if (played_again == false && intentions == IntentionEnum.IDC.toString()) {
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
		for (int i = 0; i < collaboration_intentions.size(); i++) {
			uci = collaboration_intentions.get(i);
			List<UserCollaborationSpec> uci_2;

			// Iterate again through the List with the Intentions for the players,
			// in order to get the nested player
			for (int j = 0; j < collaboration_intentions.size(); j++) {
				uci_tmp = collaboration_intentions.get(j);
				uci_2 = collaboration_intentions.get(j).getIntentions();

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

		ArrayList<UtilityUser> utility_user = new ArrayList<UtilityUser>();

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

	// TODO this function is to be removed, using global_utilityFunc2
	private ArrayList<UserPairAssignment> global_utilityFunc(ArrayList<UtilityUser> global_utility) throws IOException {
		ArrayList<UserPairAssignment> utility_pair = new ArrayList<UserPairAssignment>();

		int x_ij = 0;
		int x_ji = 0;
		UtilityUser uu = new UtilityUser();
		UtilityUser uu_j = new UtilityUser();
		int flag = 0;

		for (int e = 0; e < global_utility.size(); e++) {

			uu = global_utility.get(e);
			UserPairAssignment trial = new UserPairAssignment();
			UtilityUser tmp = new UtilityUser();

			// check if uu.getWeight() !=0 x_ij=1 else 0
			x_ij = uu.getWeight() != 0 ? 1 : 0;

			for (int q = 0; q < global_utility.size(); q++) {
				uu_j = global_utility.get(q);

				// check if uu.getWeight() !=0 x_ji=1 else 0
				x_ji = uu_j.getWeight() != 0 ? 1 : 0;

//				 System.out.println("x_ij: " + x_ij + "\n" + "x_ji: " + x_ji);
//				 System.out.println("uu.getWeight(): " + uu.getWeight() + "\n" +
//				 "uu_j.getWeight(): " + uu_j.getWeight());

				// if the flag!=0 it means that we added a pair
				if (flag != 0) {
					flag = 0;
					break;

					// xi,j=xj,i, for each i, j
				} else if (x_ij == x_ji) {
					tmp.setUser_i(uu.getUser_i());
					tmp.setUser_j(uu.getUser_j());
					tmp.setWeight(uu.getWeight());
					trial.setUser1(tmp.getUser_i());
					trial.setUser2(tmp.getUser_j());

//					System.out.printf("%d %d\n", e, q);
//					System.out.println("Sucess the xi=xj\nThe flag= " + flag);
//					System.out.println("Global Utility Func");
//					System.out.println(" User i: " + trial.getUser1());
//					System.out.println(" User j: " + trial.getUser2());
//					System.out.println("Global Utility Func#2");
//					System.out.println(" User i#2: " + tmp.getUser_i());
//					System.out.println(" User j#2: " + tmp.getUser_j());

					utility_pair.add(trial);

					flag++;
					break;
				} else
					continue;
			}
		}

		return utility_pair;
	}

	private ArrayList<UtilityUser> global_utilityFunc2(ArrayList<UtilityUser> global_utility) throws IOException {

		ArrayList<UtilityUser> utility_user = new ArrayList<UtilityUser>();

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
