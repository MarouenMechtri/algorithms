package ilp_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

//import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;

public class CplexVarX {
	
	private int virtNode;// i
	private int candidateId;// k

	// cplex variable
	private IloNumVar x;

	public int getvirtNode() {
		return virtNode;
	}

	public void setvirtNode(int virtNodeMigId) {
		this.virtNode = virtNodeMigId;
	}

	public int getcandidateId() {
		return candidateId;
	}

	public void setcandidateId(int candidateId) {
		this.candidateId = candidateId;
	}

	public IloNumVar getX() {
		return x;
	}

	public void setX(IloNumVar x) {
		this.x = x;
	}

}
