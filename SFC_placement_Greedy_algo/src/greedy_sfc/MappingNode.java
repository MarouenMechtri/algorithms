package greedy_sfc;

/**
 * @authors: Marouen Mechtri, Chaima Ghribi
 * @contacts: {mechtri.marwen, ghribii.chaima}@gmail.com
 * Created on Sep 15, 2016
 */

import sfc.Server;

public class MappingNode {

	private Server IG;
	private Server RG;
	
	
	public Server getIG() {
		return IG;
	}

	public void setIG(Server IG) {
		this.IG = IG;
	}

	public Server getRG() {
		return RG;
	}

	public void setRG(Server RG) {
		this.RG = RG;
	}

	public MappingNode(Server IG, Server RG) {
		super();
		this.IG = IG;
		this.RG = RG;
	}

	public MappingNode() {
		super();
	}

	public void printLink() {
		System.out.println("IG: " + IG.getName() + " RG: "
				+ RG.getName());
	}
}
