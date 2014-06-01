package com.example.sup;

public class SupItem {
	
	// Unique ID, based on the creation date of the item
	private String id;
	
	//Define if the user can only increment the value by one
	private boolean onlyPlusOne;
	
	//Value of the items
	private double value;
	
	// The label of the item
	private String label;
	
	public SupItem () {
		this.value = 0;
		this.onlyPlusOne = false;
	}
	
	public SupItem (String id, String s, double d) {
		this.id = id;
		this.label = s;
		this.value = d;
		this.onlyPlusOne = false;
	}
	
	public SupItem (String id, String s, double d, boolean b) {
		this.id = id;
		this.label = s;
		this.value = d;
		this.onlyPlusOne = b;
	}
	
	public boolean isOnlyPlusOne () { return this.onlyPlusOne; }
	
	public void add (double d) {
		this.value += d;
	}
	
	public void remove (double d) {
		this.value -= d;
	}
	
	public double getValue() { return this.value; }
	public String getLabel() { return this.label; }
	public String getID() { return this.id; }

}
