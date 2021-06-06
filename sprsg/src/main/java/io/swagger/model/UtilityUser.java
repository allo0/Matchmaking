package io.swagger.model;

import java.util.ArrayList;
import java.util.Collections;

public class UtilityUser {
	private String user_i = null;
	private String user_j = null;
	private float weight = 0;
	private int x = 0;

	public UtilityUser user(String user_i, String user_j, float weight) {
		this.setUser_i(user_i);
		this.setUser_j(user_j);
		this.setWeight(weight);
		
		return this;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public String getUser_j() {
		return user_j;
	}

	public void setUser_j(String user_j) {
		this.user_j = user_j;
	}

	public String getUser_i() {
		return user_i;
	}

	public void setUser_i(String user_i) {
		this.user_i = user_i;
	}

	

	
}
