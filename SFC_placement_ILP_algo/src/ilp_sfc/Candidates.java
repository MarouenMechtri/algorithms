package ilp_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

import java.util.Vector;

public class Candidates 
{
	private Vector<Integer> cands;//substrate candidates nodes id
	private int virtualNodeId;

	public Candidates() {
		super();
		cands = new Vector<Integer>();
	}
	
	public Candidates(int virtualNodeId) {
		super();
		cands = new Vector<Integer>();
		this.virtualNodeId = virtualNodeId;
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

	public int getVirtualNodeId() {
		return virtualNodeId;
	}

	public void setVirtualNodeId(int virtualNodeId) {
		this.virtualNodeId = virtualNodeId;
	}
	
	
}
