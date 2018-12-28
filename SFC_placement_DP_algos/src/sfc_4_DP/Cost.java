package sfc_4_DP;

/**
 * @authors: Marouen Mechtri, Chaima Ghribi
 * @contacts: {mechtri.marwen, ghribii.chaima}@gmail.com
 * Created on Sep 15, 2016
 */

import java.util.ArrayList;

public class Cost {

	private float latency;
	private float bandwidth;
	private float lost;
	private float bandwidth_shortestPath;
	private ArrayList<Server> path;

	public float getLatency() {
		return latency;
	}

	public void setLatency(float latency) {
		this.latency = latency;
	}

	public float getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(float bandwidth) {
		this.bandwidth = bandwidth;
	}

	public float getLost() {
		return lost;
	}

	public void setLost(float lost) {
		this.lost = lost;
	}

	public ArrayList<Server> getPath() {
		return path;
	}

	public void setPath(ArrayList<Server> path) {
		this.path = path;
	}

	public float getBandwidth_shortestPath() {
		return bandwidth_shortestPath;
	}

	public void setBandwidth_shortestPath(float bandwidth_shortestPath) {
		this.bandwidth_shortestPath = bandwidth_shortestPath;
	}

	public Cost(float latency, float bandwidth, float lost) {
		super();
		this.latency = latency;
		this.bandwidth = bandwidth;
		this.lost = lost;
		this.bandwidth_shortestPath = 0;
	}

	public Cost(float latency, float bandwidth, float lost, ArrayList<Server> path) {
		// TODO Auto-generated constructor stub
		super();
		this.latency = latency;
		this.bandwidth = bandwidth;
		this.lost = lost;
		this.bandwidth_shortestPath=0;
		this.setPath(path);
	}

	public Cost() {
		super();
	}

	public void printCost() {
		System.out.println("latency: " + latency + " bandwidth: " + bandwidth
				+ " lost: " + lost);
	}

	public String getPathString() {
		String stringpath = "";

		for (int i = 0; i < path.size(); i++) {
			stringpath += path.get(i).getIndex() + " ";
		}
		return stringpath;
	}

	public int[] getPathtab() {
		int[] tabpath = new int[path.size()];

		for (int i = 0; i < path.size(); i++) {
			tabpath[i] = path.get(i).getIndex();
		}
		return tabpath;
	}
	
	public Cost clone()
	{
		Cost cln = new Cost(latency, bandwidth, lost);
		return cln;
	}

}
