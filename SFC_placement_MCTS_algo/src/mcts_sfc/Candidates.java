package mcts_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

import java.util.Vector;

public class Candidates {
	private Vector<Integer> cands;//substrate candidates nodes id

	public Candidates() {
		super();
		cands = new Vector<Integer>();
	}

	public Vector<Integer> getCands() {
		return cands;
	}

	public void setCands(Vector<Integer> cands) {
		this.cands = cands;
	}
	
	public void addNewCandidate(int candidateId){
		cands.add(candidateId);
	}
	
}
