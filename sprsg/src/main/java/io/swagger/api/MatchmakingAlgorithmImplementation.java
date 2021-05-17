package io.swagger.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import io.swagger.model.UserCollaborationIntentions;
import io.swagger.model.UserCollaborationSpec;
import io.swagger.model.UserCollaborationSpec.IntentionEnum;
import io.swagger.model.UserPairAssignment;
import io.swagger.model.UserPairwiseScore;
import io.swagger.model.UserScore;
import io.swagger.model.UtilityUser;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import me.tongfei.progressbar.ProgressBar;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LinearProgram;
import scpsolver.problems.MathematicalProgram;
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;


public class MatchmakingAlgorithmImplementation {

	boolean played_again = false;
	String intentions;
	float weight = 0;
	int users_count = 0;

	Integer[][] x_for_players;
	Double[][] weight_for_players;

	public ArrayList<UserPairAssignment> final_pair(List<UserScore> list, List<UserPairwiseScore> list2,
			List<UserCollaborationIntentions> list3) throws IOException {

		ArrayList<UtilityUser> global_utility = new ArrayList<UtilityUser>();
		ArrayList<UtilityUser> utility_per_user = new ArrayList<UtilityUser>();
		ArrayList<UserPairAssignment> temp_res = new ArrayList<UserPairAssignment>();
		ArrayList<UtilityUser> tettt = new ArrayList<UtilityUser>();
		UserScore us = new UserScore();
		UtilityUser utility_user;

		users_count = list.size();
		x_for_players = new Integer[users_count][users_count];
		weight_for_players = new Double[users_count][users_count];
		// Initialize both arrays as null
		for (int i = 0; i < users_count; i++) {
			for (int j = 0; j < users_count; j++) {
//				if(i==j && (i!=0 && j!=0))
//					continue;
				x_for_players[i][j] = null;
				weight_for_players[i][j] = null;
			}
		}

		System.out.println("------------------------------------");
		System.out.println("Main");
		System.out.println("------------------------------------");

		UserPairwiseScore ups = null;
	
		// Iterate through the UPS list

		if (list2.size() < users_count * (users_count - 1)) {
			System.out.println("HEY");
			 
		}
		
		for (int a = 0; a < list2.size(); a++) {
			ups = list2.get(a);
			UserPairwiseScore ups_2 = null;
			// Iterate through the UPS list again so we can get the nested users
			for (int b = 0; b < list2.size(); b++) {
				utility_user = new UtilityUser();
				ups_2 = list2.get(b);
				us = ups_2.getScoresGiven().get(0);

				// Check if the Outter user is the same as the nested one
				if (ups.getGradingUser().equals(ups_2.getGradingUser())
						&& ups.getScoresGiven().get(0).getUserId().equals(ups_2.getScoresGiven().get(0).getUserId())) {
//					 System.out.println("The users match:");
//					 System.out.println(" ups.getGradingUser(): " + ups.getGradingUser());
//					 System.out.println(" ups_2.getGradingUser(): " + ups_2.getGradingUser());

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
//		Collections.sort(utility_per_user, (UtilityUser s1, UtilityUser s2) -> {
//			return s1.getUser_i().compareToIgnoreCase(s2.getUser_i());
//		});
/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
		
		
/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
		// global utility function
//		System.out.println(global_utility.size());
		temp_res = global_utilityFunc(global_utility);
		// System.out.println(temp_res);

		tettt = global_utilityFunc2(global_utility);
		try {
			maximize_lp(tettt);
		} catch (Exception e) {
			System.out.println("Something went wrong: " + e);
		}

/////////// ~~~~~~~printing the dump text~~~~~~~///////////////
		String user_i, user_j;
		String weight;
		String x;
		Scanner read = new Scanner(new File("../temp_file.txt"));
		read.useDelimiter(",|\\n");

		while (read.hasNext()) {
			user_i = read.next().trim();
			user_j = read.next().trim();
			weight = read.next().trim();
			x = read.next().trim();
			System.out.print(user_i + " " + user_j + " " + weight + " " + x + "\n");
		}
		read.close();
/////////// ~~~~~printing the testing array~~~~~~///////////////
		for (int i = 0; i < users_count; i++) {
			for (int j = 0; j < users_count; j++) {
				System.out.print(weight_for_players[i][j] + " ");
			}
			System.out.println();
		}
		
		System.out.println("~~~~~~");
		
		for (int i = 0; i < users_count; i++) {
			for (int j = 0; j < users_count; j++) {
				System.out.print(x_for_players[i][j] + " ");
			}
			System.out.println();
		}
/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

//		System.out.println("Final pairs(?): " + temp_res);
		// it will return the results from tettt, not temp_res
		return temp_res;
	}

	private void maximize_lp(ArrayList<UtilityUser> last_users) {

		glp_prob lp;
		glp_smcp parm;
		SWIGTYPE_p_int ind;
		SWIGTYPE_p_double val;
		int ret;
		try {
			// Create problem
			lp = GLPK.glp_create_prob();
			System.out.println("Problem created");
			GLPK.glp_set_prob_name(lp, "Testing Problem");

			UtilityUser uu = new UtilityUser();
			UtilityUser uu_2 = null;

			// Define columns
			GLPK.glp_add_cols(lp, last_users.size());
			// Create rows
			GLPK.glp_add_rows(lp, last_users.size());

			// Define objective
			GLPK.glp_set_obj_name(lp, "Uglob");
			GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);

			for (int i = 0; i < last_users.size(); i++) {

				// Define columns
				GLPK.glp_set_col_name(lp, i + 1, "x" + (i + 1));
				GLPK.glp_set_col_kind(lp, i + 1, GLPKConstants.GLP_IV);
				GLPK.glp_set_col_bnds(lp, i + 1, GLPKConstants.GLP_DB, 0, 1);

			}

			for (int i = 0; i < last_users.size(); i++) {

				uu = last_users.get(i);

				// Allocate memory
				ind = GLPK.new_intArray(last_users.size());
				val = GLPK.new_doubleArray(last_users.size());

				for (int j = 0; j < last_users.size(); j++) {
					uu_2 = last_users.get(j);

					// Set row details
					GLPK.glp_set_row_name(lp, j + 1, "c" + (j + 1));
					GLPK.glp_set_row_bnds(lp, j + 1, GLPKConstants.GLP_FX, 1.0, 1.0);
					GLPK.intArray_setitem(ind, j + 1, j + 1);
					GLPK.intArray_setitem(ind, i + 1, i + 1);
					GLPK.doubleArray_setitem(val, i + 1, (double) uu.getX());
					GLPK.doubleArray_setitem(val, i + 1, (double) uu.getX());

				}

				GLPK.glp_set_mat_row(lp, i + 1, last_users.size(), ind, val);

				// Free memory
				GLPK.delete_intArray(ind);
				GLPK.delete_doubleArray(val);

			}

			for (int i = 0; i < last_users.size(); i++) {
				uu = last_users.get(i);

				GLPK.glp_set_obj_coef(lp, i + 1, uu.getWeight());
			}

//			// Define columns
//			GLPK.glp_add_cols(lp, 2);
//			GLPK.glp_set_col_name(lp, 1, "x12");
//			GLPK.glp_set_col_kind(lp, 1, GLPKConstants.GLP_IV);
//			GLPK.glp_set_col_bnds(lp, 1, GLPKConstants.GLP_DB, 0, 1);
//			GLPK.glp_set_col_name(lp, 2, "x21");
//			GLPK.glp_set_col_kind(lp, 2, GLPKConstants.GLP_IV);
//			GLPK.glp_set_col_bnds(lp, 2, GLPKConstants.GLP_DB, 0, 1);
//
//			// Create constraints
//
//			// Allocate memory
//			ind = GLPK.new_intArray(2);
//			val = GLPK.new_doubleArray(2);
//
//			// Create rows
//			GLPK.glp_add_rows(lp, 2);
//
//			// Set row details
//			GLPK.glp_set_row_name(lp, 1, "c1");
//			GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_FX, 1.0, 1.0);
//			GLPK.intArray_setitem(ind, 1, 1);
//			GLPK.intArray_setitem(ind, 2, 2);
//			GLPK.doubleArray_setitem(val, 1, 1.);
//			GLPK.doubleArray_setitem(val, 2, 1);
//			GLPK.glp_set_mat_row(lp, 1, 2, ind, val);
//
//
//			// Free memory
//			GLPK.delete_intArray(ind);
//			GLPK.delete_doubleArray(val);
//
//			// Define objective
//			GLPK.glp_set_obj_name(lp, "fucking");
//			GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
//			GLPK.glp_set_obj_coef(lp, 1, 0.28);
//			GLPK.glp_set_obj_coef(lp, 2, 1.69);

			// Write model to file
			GLPK.glp_write_lp(lp, null, "lp2.lp");

			// Solve model
			parm = new glp_smcp();
			GLPK.glp_init_smcp(parm);
			ret = GLPK.glp_simplex(lp, parm);

			// Retrieve solution
			if (ret == 0) {
				write_lp_solution(lp);
			} else {
				System.out.println("The problem could not be solved");
			}

			// Free memory
			GLPK.glp_delete_prob(lp);
		} catch (GlpkException ex) {
			ex.printStackTrace();
			ret = 1;
		}

		System.out.println("The calculations ended . . .\n");

	}

	/**
	 * write simplex solution
	 * 
	 * @param lp problem
	 */
	static void write_lp_solution(glp_prob lp) {
		int i;
		int n;
		String name;
		double val;

		name = GLPK.glp_get_obj_name(lp);
		val = GLPK.glp_get_obj_val(lp);
		System.out.print(name);
		System.out.print(" = ");
		System.out.println(val);
		n = GLPK.glp_get_num_cols(lp);
		for (i = 1; i <= n; i++) {
			name = GLPK.glp_get_col_name(lp, i);
			val = GLPK.glp_get_col_prim(lp, i);
			System.out.print(name);
			System.out.print(" = ");
			System.out.println(val);
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

		// Iterate through the List with the Intentions for the players,
		// in order to get the outter player
		for (int i = 0; i < collaboration_intentions.size(); i++) {
			uci = collaboration_intentions.get(i);
			List<UserCollaborationSpec> uci_2;

			// Iterate again through the List with the Intentions for the players,
			// in order to get the nested player
			for (int j = 0; j < collaboration_intentions.size(); j++) {
				uci_2 = collaboration_intentions.get(j).getIntentions();

				// Check if the Outter user is the one that is getting graded
				if (uci.getGradingUser().equals(gradee)) {
					ucs = uci.getIntentions().get(0);
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
		Random rand = new Random();

		int x_ij = 0;
		UtilityUser uu = new UtilityUser();
		UtilityUser uu_j = new UtilityUser();

		// Iterate through the List with the other players, a player has played
		// and store the user in our utility object
		for (int c = 0; c < utility_per_user.size(); c++) {

			uu.setUser_i(utility_per_user.get(c).getUser_i());
			uu.setUser_j(utility_per_user.get(c).getUser_j());
			uu.setWeight(utility_per_user.get(c).getWeight());

			x_ij = rand.nextInt(2);

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
					tmp.setX(x_ij);

					System.out.printf("%d %d\n", c, d);
					System.out.printf("%d) Utility Per User Func: \n", c);
					System.out.println(" User i: " + tmp.getUser_i());
					System.out.println(" User j: " + tmp.getUser_j());
					System.out.println(" Weight: " + tmp.getWeight());
					System.out.println(" x_ij: " + tmp.getX());

					utility_user.add(tmp);

//					array_creator(c,d,tmp.getX(),tmp.getWeight());

					break;
				}
				// reset the counter
//				x_ij = 0;
			}

		}

		return utility_user;
	}

	// TODO this function is to be removed, using global_utilityFunc2
	private ArrayList<UserPairAssignment> global_utilityFunc(ArrayList<UtilityUser> global_utility) throws IOException {
		ArrayList<UserPairAssignment> utility_pair = new ArrayList<UserPairAssignment>();
		Random rand = new Random();
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
		FileWriter writer = new FileWriter("../temp_file.txt", false);

		for (int e = 0; e < global_utility.size(); e++) {
			uu = global_utility.get(e);

			UtilityUser tmp = new UtilityUser();

			// check if uu.getWeight() !=0 x_ij=1 else 0
			x_ij = uu.getWeight() != 0 ? 1 : 0;

			for (int q = 0; q < global_utility.size(); q++) {// for (int q = 0; q < global_utility.size(); q++) { //the
																// original is with q=0, got wrong iterrations
				uu_j = global_utility.get(q);

				// check if uu.getWeight() !=0 x_ji=1 else 0
				x_ji = uu_j.getWeight() != 0 ? 1 : 0;

//				 System.out.println("~~~~~~~~~~~~~~~~~~~");
//				 System.out.println("x_ij: " + x_ij + "\n" + "x_ji: " + x_ji);
//				 System.out
//					 .println("uu.getWeight(): " + uu.getWeight() + "\n" + "uu_j.getWeight(): " +
//					 uu_j.getWeight());

				if (x_ij == x_ji) {

//					if ((uu.getUser_i().equals(uu_j.getUser_j()) && uu.getUser_j().equals(uu_j.getUser_i()))
//							&& uu.getX() == uu_j.getX()) {

					// The output form of the users with comma "," as a delimiter
					// test1,test2,1.690000057220459,1
					// test2,test1,0.2800000309944153,1
					writer.append(uu.getUser_i());
					writer.append(",");
					writer.append(uu.getUser_j());
					writer.append(",");
					writer.append(Double.toString(uu.getWeight()));
					writer.append(",");
					writer.append(Integer.toString(uu.getX()));
					writer.append("\n");

//					if (uu.getUser_i().equals(uu_j.getUser_i()) && uu.getX() == 1) {
//
//						writer.append("1");
//						writer.append("\n");
//					} else {
//						writer.append("0");
//						writer.append("\n");
//					}
					tmp.setUser_i(uu.getUser_i());
					tmp.setUser_j(uu.getUser_j());
					tmp.setWeight(uu.getWeight());
					tmp.setX(uu.getX());
					// tmp.setX(uu.getX());

					utility_user.add(tmp);

					break;
//					} else
//						continue;

				} else

					continue;

			}

		}

		writer.flush();
		writer.close();
		return utility_user;
	}

	public void array_creator(int point_x, int point_y, int x, double weight) {

		if (point_x == point_y) {
			x_for_players[point_x][point_y] = -1;
			weight_for_players[point_x][point_y] = -100.;

		} else {
			x_for_players[point_x][point_y] = x;
			weight_for_players[point_x][point_y] = weight;
		}



	}

}
