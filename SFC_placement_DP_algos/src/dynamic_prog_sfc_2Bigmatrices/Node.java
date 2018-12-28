package dynamic_prog_sfc_2Bigmatrices;

/**
 * @authors: Marouen Mechtri, Chaima Ghribi
 * @contacts: {mechtri.marwen, ghribii.chaima}@gmail.com
 * Created on Sep 15, 2016
 */

public class Node {

	public int id;
	public double cost;
	public Node succ;
	
	
	public Node(){}
	
	public Node(int id, double cost, Node succ) {
		super();
		this.id = id;
		this.cost = cost;
		this.succ = succ;
	}

		
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public Node getSucc() {
		return succ;
	}
	public void setSucc(Node succ) {
		this.succ = succ;
	}
	
	@Override
	public String toString() {
		return "Node [id=" + id + ", cost=" + cost + ", succ=" + succ  + "]";
	}
	
}
