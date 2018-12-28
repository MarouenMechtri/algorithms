package dynamic_prog_sfc_without_matrices;


/**
 * @authors: Marouen Mechtri, Chaima Ghribi
 * @contacts: {mechtri.marwen, ghribii.chaima}@gmail.com
 * Created on Sep 15, 2016
 */

import java.util.ArrayList;
import java.util.List;



public class Node {

	public int id;
	public double cost;
	public Node succ;

	public List<Node_src_dst> reservedNode = new ArrayList<Node_src_dst>();
	public List<Link_src_dst> reservedLink = new ArrayList<Link_src_dst>();
	
	
	
	
	
	public Node(){}
	
	public Node(int id, double cost, Node succ) {
		super();
		this.id = id;
		this.cost = cost;
		this.succ = succ;
		this.reservedNode = new ArrayList<Node_src_dst>();
		this.reservedLink = new ArrayList<Link_src_dst>();
	}

	
		
	public Node(int id, double cost, Node succ, List<Node_src_dst> reservedNode, List<Link_src_dst> reservedLink) {
		super();
		this.id = id;
		this.cost = cost;
		this.succ = succ;
		this.reservedNode = reservedNode;
		this.reservedLink = reservedLink;
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
		
		String reservednodes = "";
		for (int i = 0; i < reservedNode.size(); i++) {
			reservednodes += " " +reservedNode.get(i).value1 + " " +reservedNode.get(i).value2;
			
		}
		
		String reservedlinks = "";
		for (int i = 0; i < reservedLink.size(); i++) {
			reservedlinks += " (" + reservedLink.get(i).value1.value1 + "," + reservedLink.get(i).value1.value2 + ") ("
					+ reservedLink.get(i).value2.value1 + "," + reservedLink.get(i).value2.value2 + ")";

		}
		
		return "Node [id=" + id + ", cost=" + cost + ", succ=" + succ  + ", reservedNode="
		+ reservednodes + ", reservedLink=" + reservedlinks + "]";
	}
	
	
	
	
 	
	
}
