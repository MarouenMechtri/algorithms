package ilp_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

//import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;

public class CplexVarY {
	
	private End_to_end_V virtualLink;		// ij
	private End_to_end_P physicalNewPath;		// P(k,n(j))
	private int extremity1 = -1;//keep it because it can be a fictitious node
	private int extremity2 = -1;//keep it because it can be a fictitious node

	private boolean sameNode = false;
	
	// cplex variable
	private IloNumVar y;

	public End_to_end_V getVirtualLink() {
		return virtualLink;
	}

	public void setVirtualLink(End_to_end_V virtualLinkMig) {
		this.virtualLink = virtualLinkMig;
	}

	public End_to_end_P getPhysicalNewPath() {
		return physicalNewPath;
	}

	
	public void setPhysicalNewPath(End_to_end_P physicalNewPath) {
		this.physicalNewPath = physicalNewPath;
	}

	public boolean isSameNode() {
		return sameNode;
	}

	public void setSameNode(boolean sameNode) {
		this.sameNode = sameNode;
	}

	
	public int getExtremity1() {
		return extremity1;
	}

	public void setExtremity1(int extremity1) {
		this.extremity1 = extremity1;
	}

	public int getExtremity2() {
		return extremity2;
	}

	public void setExtremity2(int extremity2) {
		this.extremity2 = extremity2;
	}

	public IloNumVar getY() {
		return y;
	}

	public void setY(IloNumVar y) {
		this.y = y;
	}

}
