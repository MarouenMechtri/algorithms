package greedy_sfc;

/**
 * @authors: Marouen Mechtri, Chaima Ghribi
 * @contacts: {mechtri.marwen, ghribii.chaima}@gmail.com
 * Created on Sep 15, 2016
 */

import java.util.ArrayList;


public class ListMappingNode {

	private ArrayList<MappingNode> listMapping;
	private int costMatching;

	public ArrayList<MappingNode> getListMapping() {
		return listMapping;
	}

	public void setListMapping(ArrayList<MappingNode> listMapping) {
		this.listMapping = listMapping;
	}

	public void addListMapping(MappingNode mappingNode) {
		this.listMapping.add(mappingNode);
	}

	public ListMappingNode(ArrayList<MappingNode> listMapping) {
		super();
		this.listMapping = listMapping;
	}

	public ListMappingNode() {
		super();
		this.listMapping = new ArrayList<MappingNode>();
	}

	public int getCostMatching() {
		return costMatching;
	}

	public void setCostMatching(int costMatching) {
		this.costMatching = costMatching;
	}

}
