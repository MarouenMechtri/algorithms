package ilp_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

import java.util.Vector;

public class NodeMCTS {

	private double nb_visit;//should be double (not int) to avoid calculating problem
	private double payoff_sum;
	private double payoff_square_sum;
	private boolean leaf;
	private boolean feasible = true;///to be checked only for leaf nodes
	private double current_payoff = 0;//used only when generating new branch: it describes the current embedding quality
								  //it will be used
	private int myIndex;//index in the MCTS tree
	private Vector<Integer> childrenIndex;//index in the MCTS tree
	private int fatherIndex;//index in the MCTS tree

	private Vector<Integer> pathHostingVirtLink;//List of nodes in the path. The path is FROM the Father TO the Current
	private int virtNodeId;
	private int subNodeId;
	private final double ct_D = 0;
	private final double ct_C = 1;
	
	
	
	public NodeMCTS(int fatherIndex) {
		super();
		this.fatherIndex = fatherIndex;
		
		myIndex = 0;
		leaf = false;
		nb_visit = 0;
		payoff_sum = 0;
		payoff_square_sum = 0;
		childrenIndex = new Vector<Integer>();
		
		this.virtNodeId = -1;
		this.subNodeId = -1;
	}
	
//	public NodeMCTS(int fatherIndex, int virtNodeId, int subNodeId) {
//		super();
//		this.fatherIndex = fatherIndex;
//		this.virtNodeId = virtNodeId;
//		this.subNodeId = subNodeId;
//		
//		leaf = false;
//		nb_visit = 0;
//		payoff_sum = 0;
//		payoff_square_sum = 0;
//		childrenIndex = null;
//	}
//	
	
	
	public NodeMCTS(int myIndex, int fatherIndex, int virtNodeId, int subNodeId) {
		super();
		this.myIndex = myIndex;
		this.fatherIndex = fatherIndex;
		this.virtNodeId = virtNodeId;
		this.subNodeId = subNodeId;
		
		leaf = false;
		nb_visit = 0;
		payoff_sum = 0;
		payoff_square_sum = 0;
		childrenIndex = new Vector<Integer>();
		pathHostingVirtLink = new Vector<Integer>();
	}

	public double getCurrent_payoff() {
		return current_payoff;
	}

	public void setCurrent_payoff(double current_payoff) {
		this.current_payoff = current_payoff;
	}

	public boolean isFeasible() {
		return feasible;
	}

	public void setFeasible(boolean feasible) {
		this.feasible = feasible;
	}

	public int getMyIndex() {
		return myIndex;
	}

	public void setMyIndex(int myIndex) {
		this.myIndex = myIndex;
	}

	public int getVirtNodeId() {
		return virtNodeId;
	}



	public void setVirtNodeId(int virtNodeId) {
		this.virtNodeId = virtNodeId;
	}



	public int getSubNodeId() {
		return subNodeId;
	}



	public void setSubNodeId(int subNodeId) {
		this.subNodeId = subNodeId;
	}



	public void addChildIndex(int childIndex)
	{
		if (childrenIndex == null) {
			childrenIndex = new Vector<Integer>();
		}
		
		childrenIndex.add(childIndex);
	}
	
	public double getPayoffAverage()
	{
		return payoff_sum/nb_visit;
	}
	
	public double getNb_visit() {
		return nb_visit;
	}
	public void setNb_visit(double nb_visit) {
		this.nb_visit = nb_visit;
	}
	public double getPayoff_sum() {
		return payoff_sum;
	}
	public void setPayoff_sum(double payoff_sum) {
		this.payoff_sum = payoff_sum;
	}
	public double getPayoff_square_sum() {
		return payoff_square_sum;
	}
	public void setPayoff_square_sum(double payoff_square_sum) {
		this.payoff_square_sum = payoff_square_sum;
	}
	public boolean isLeaf() {
		return leaf;
	}
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	public Vector<Integer> getChildrenIndex() {
		return childrenIndex;
	}
	public void setChildrenIndex(Vector<Integer> childrenIndex) {
		this.childrenIndex = childrenIndex;
	}
	public int getFatherIndex() {
		return fatherIndex;
	}
	public void setFatherIndex(int fatherIndex) {
		this.fatherIndex = fatherIndex;
	}
	public Vector<Integer> getPathHostingVirtLink() {
		return pathHostingVirtLink;
	}
	public void setPathHostingVirtLink(Vector<Integer> pathHostingVirtLink) {
		this.pathHostingVirtLink = pathHostingVirtLink;
	}
	
	//Only when it is leaf node
	public double getPayoff() {
		return payoff_sum;//when it is leaf node
	}

	//Gérer le cas ou on ne peut pas choisir un candidat car il a été utilisé dans le mapping de l'un des parents
	//C fait grace subNodesAlreadyUsedForThisBranch
	public boolean isAllChildrenGenerated(int candidateSize) {
		if(childrenIndex.size() > candidateSize)
		{
			Vector<Integer> p_null = null;
			System.out.println("There is problem in children size!!!!!");
			p_null.elementAt(5);
		}
		if(childrenIndex.size() == candidateSize)
			return true;
		return false;
	}

	public double computeSelectionValue(double fatherNbVisit) {
		double average = payoff_sum / (double)nb_visit;
		
		double lastPart = (payoff_square_sum - nb_visit*Math.pow(average, 2) + ct_D)/nb_visit;
		lastPart = Math.sqrt(lastPart);
		
		double secondPart = ct_D * Math.sqrt(Math.log(fatherNbVisit)/nb_visit);
		
		return average + secondPart + lastPart;
	}

	public void updateNb_visit() {
		nb_visit++;
		
	}

	public void updateWithPayoff(double payoff) {
		payoff_sum += payoff;
		payoff_square_sum += payoff * payoff;
		
	}
	
	
}
