package io.swagger.api;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

	public ArrayList<UserPairAssignment> final_pair(List<UserScore> list, List<UserPairwiseScore> list2,
			List<UserCollaborationIntentions> list3) {

		ArrayList<UtilityUser> global_utility = new ArrayList<UtilityUser>();
		ArrayList<UtilityUser> utility_per_user = new ArrayList<UtilityUser>();
		ArrayList<UserPairAssignment> temp_res = new ArrayList<UserPairAssignment>();
		ArrayList<UtilityUser> tettt = new ArrayList<UtilityUser>();
		UserScore us = new UserScore();
		UtilityUser utility_user;

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

				// Check if the Outter user is the same as the nested one
				if (ups.getGradingUser().equals(ups_2.getGradingUser())) {
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
//		    progressPercentage(a, list2.size());
//		    try {
//		        Thread.sleep(500);
//		    } catch (Exception e) {
//		    }

		}

/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
		// call utility per user, to get the utility of each player
		// with all others he has played with
		global_utility = utility_per_user_calculator(utility_per_user);
/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////
		// global utility function
		System.out.println(global_utility.size());
		temp_res = global_utilityFunc(global_utility);
		// System.out.println(temp_res);

		// global utility function
		tettt = global_utilityFunc2(global_utility);
		try {
			maximize_lp(tettt);
		} catch (Exception e) {
			System.out.println("Something went wrong: " + e);
		}
/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

//		System.out.println("Final pairs(?): " + temp_res);
		// it will return the results from tettt, not temp_res
		return temp_res;
	}

	private void maximize_lp(ArrayList<UtilityUser> last_users) {

		/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

//		UtilityUser uu = new UtilityUser();
//		UtilityUser uu_2 = null;
//		int temp = 0;
//		LPWizard lp = new LPWizard();
//		for (int f = 0; f < last_users.size(); f++) {
//			uu = last_users.get(f);
//
//			for (int g = f; g < last_users.size(); g++) {
//				uu_2 = last_users.get(g);
//
//				if (uu.getUser_i().equals(uu_2.getUser_j())) {
//
//					// add as objects in the maximize problem, each user with his weight
//					lp.plus(uu.getUser_i(), uu.getWeight()).plus(uu_2.getUser_i(), uu_2.getWeight());
//					// lp.plus(uu.getUser_j(), uu_2.getWeight());
//
//					// add the constraints necessary
//					// for each pair:
//
//					lp.addConstraint("c" + temp, 1, "<=").plus(uu.getUser_i(), 1.0).plus(uu_2.getUser_i(), 1.0)
//							.setAllVariablesInteger();
//					lp.addConstraint("c" + (temp + 1), 1, ">=").plus(uu.getUser_i(), 1.0).setAllVariablesInteger();
//					lp.addConstraint("c" + (temp + 2), 1, ">=").plus(uu_2.getUser_i(), 1.0).setAllVariablesInteger();
//
//					temp++;
//
//					continue;
//				} else {
//					continue;
//				}
//
//			}
//
//		}
//		lp.setMinProblem(false);
//		System.out.println(lp.solve());
////		System.out.println(lp.getLP().getIndexmap());
//		System.out.println(lp.getLP().convertToCPLEX());

		/////////// ~~~~~~~~~~~~~~~~~~~~~~~///////////////

		glp_prob lp;
		glp_smcp parm;
		SWIGTYPE_p_int ind;
		SWIGTYPE_p_double val;
		int ret;
		try {
			// Create problem
			lp = GLPK.glp_create_prob();
			System.out.println("Problem created");
			GLPK.glp_set_prob_name(lp, "myProblem");

			// Define columns
			GLPK.glp_add_cols(lp, 3);
			GLPK.glp_set_col_name(lp, 1, "x1");
			GLPK.glp_set_col_kind(lp, 1, GLPKConstants.GLP_CV);
			GLPK.glp_set_col_bnds(lp, 1, GLPKConstants.GLP_DB, 0, .5);
			GLPK.glp_set_col_name(lp, 2, "x2");
			GLPK.glp_set_col_kind(lp, 2, GLPKConstants.GLP_CV);
			GLPK.glp_set_col_bnds(lp, 2, GLPKConstants.GLP_DB, 0, .5);
			GLPK.glp_set_col_name(lp, 3, "x3");
			GLPK.glp_set_col_kind(lp, 3, GLPKConstants.GLP_CV);
			GLPK.glp_set_col_bnds(lp, 3, GLPKConstants.GLP_DB, 0, .5);

			// Create constraints

			// Allocate memory
			ind = GLPK.new_intArray(3);
			val = GLPK.new_doubleArray(3);

			// Create rows
			GLPK.glp_add_rows(lp, 2);

			// Set row details
			GLPK.glp_set_row_name(lp, 1, "c1");
			GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_DB, 0, 0.2);
			GLPK.intArray_setitem(ind, 1, 1);
			GLPK.intArray_setitem(ind, 2, 2);
			GLPK.doubleArray_setitem(val, 1, 1.);
			GLPK.doubleArray_setitem(val, 2, -.5);
			GLPK.glp_set_mat_row(lp, 1, 2, ind, val);

			GLPK.glp_set_row_name(lp, 2, "c2");
			GLPK.glp_set_row_bnds(lp, 2, GLPKConstants.GLP_UP, 0, 0.4);
			GLPK.intArray_setitem(ind, 1, 2);
			GLPK.intArray_setitem(ind, 2, 3);
			GLPK.doubleArray_setitem(val, 1, -1.);
			GLPK.doubleArray_setitem(val, 2, 1.);
			GLPK.glp_set_mat_row(lp, 2, 2, ind, val);

			// Free memory
			GLPK.delete_intArray(ind);
			GLPK.delete_doubleArray(val);

			// Define objective
			GLPK.glp_set_obj_name(lp, "z");
			GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
			GLPK.glp_set_obj_coef(lp, 0, 1.);
			GLPK.glp_set_obj_coef(lp, 1, -.5);
			GLPK.glp_set_obj_coef(lp, 2, .5);
			GLPK.glp_set_obj_coef(lp, 3, -1);

			// Write model to file
			// GLPK.glp_write_lp(lp, null, "lp.lp");

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
		float utility_weight = 0;
		int x_ij = 0;
		UtilityUser uu = new UtilityUser();
		UtilityUser uu_j = new UtilityUser();

		// Iterate through the List with the other players, a player has played
		// and store the user in our utility object
		for (int c = 0; c < utility_per_user.size(); c++) {

			uu.setUser_i(utility_per_user.get(c).getUser_i());
			uu.setUser_j(utility_per_user.get(c).getUser_j());
			uu.setWeight(utility_per_user.get(c).getWeight());
//			System.out.println("Utility Per User Func#!!!");
//			System.out.println(" User i: " + uu.getUser_i());
//			System.out.println(" User j: " + uu.getUser_j());
//			System.out.println(" Weight: " + uu.getWeight());
			x_ij = rand.nextInt(2);

			UtilityUser tmp = new UtilityUser();

			// Iterate through the List with the other players, a player has played
			// to get the nested user
			for (int d = 0; d < utility_per_user.size(); d++) {
				uu_j = utility_per_user.get(d);

				// ∑_j,j≠i x_(i,j)=1
				if (x_ij == 0 && (uu.getUser_i() != uu_j.getUser_j())) {/////////////////////

					x_ij = 1; // αυτο να αλλαξει μολις δω πως παιρνει τιμές το x
				} else if (x_ij == 1 && (uu.getUser_i() != uu_j.getUser_j())) {
					// x_ij = 1; // αυτο να αλλαξει μολις δω πως παιρνει τιμές το x

				}

				// if the pair (nested outter ) matches
				// add the new pair ij to the arraylist
				if (uu.getUser_i() == uu_j.getUser_i()) {

					utility_weight = uu.getWeight();// * x_ij;
					tmp.setWeight(utility_weight);
					tmp.setUser_i(uu.getUser_i());
					tmp.setUser_j(uu.getUser_j());
					tmp.setX(x_ij);
//					System.out.println("1) Utility Per User Func: ");
//					System.out.println(" User i: " + tmp.getUser_i());
//					System.out.println(" User j: " + tmp.getUser_j());
//					System.out.println(" Weight: " + tmp.getWeight());
//					System.out.println(" x_ij: " + tmp.getX());
					utility_user.add(tmp);
					break;
				}

				// reset the counter
//				x_ij = 0;
			}

		}

		return utility_user;
	}

	private ArrayList<UserPairAssignment> global_utilityFunc(ArrayList<UtilityUser> global_utility) {
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

			for (int q = e; q < global_utility.size(); q++) {
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

//					 System.out.println("Sucess the xi=xj\nThe flag= " + flag );
//					 System.out.println("Global Utility Func");
//					 System.out.println(" User i: " + trial.getUser1());
//					 System.out.println(" User j: " + trial.getUser2());
//					 System.out.println("Global Utility Func#2");
//					 System.out.println(" User i#2: " + tmp.getUser_i());
//					 System.out.println(" User j#2: " + tmp.getUser_j());

					utility_pair.add(trial);

					flag++;
					break;
				} else
					continue;
			}
		}

		return utility_pair;
	}

	private ArrayList<UtilityUser> global_utilityFunc2(ArrayList<UtilityUser> global_utility) {

		ArrayList<UtilityUser> utility_user = new ArrayList<UtilityUser>();
		Random rand = new Random();
		int x_ij = 0;
		int x_ji = 0;
		UtilityUser uu = new UtilityUser();
		UtilityUser uu_j = new UtilityUser();

		for (int e = 0; e < global_utility.size(); e++) {
			uu = global_utility.get(e);
			// for (UtilityUser uu : global_utility) {
			UserPairAssignment trial = new UserPairAssignment();
			UtilityUser tmp = new UtilityUser();

			// check if uu.getWeight() !=0 x_ij=1 else 0
			x_ij = uu.getWeight() != 0 ? 1 : 0;

			for (int q = e; q < global_utility.size(); q++) {// for (int q = 0; q < global_utility.size(); q++) { //the
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

					tmp.setUser_i(uu.getUser_i());
					tmp.setUser_j(uu.getUser_j());
					tmp.setWeight(uu.getWeight());
					tmp.setX(uu.getX());

					utility_user.add(tmp);

					break;

				} else

					continue;

			}

		}

		return utility_user;
	}

}
