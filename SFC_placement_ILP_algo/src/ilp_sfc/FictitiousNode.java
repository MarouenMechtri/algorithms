package ilp_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

public class FictitiousNode 
{
//	private int name;//to be unique
	private int subNodeId;
	private int vnfType;
	private float remainResource;
	private int index;//used only in MCTS process with value higher than RG_size to say it is fictitious
	
	
	public FictitiousNode(int index, int subNodeId, int vnfType, float remainResource)
	{
		super();
//		this.name = name;
		this.subNodeId = subNodeId;
		this.vnfType = vnfType;
		this.remainResource = remainResource;
		this.index = index;
	}


//	public int getName() {
//		return name;
//	}
//
//
//	public void setName(int name) {
//		this.name = name;
//	}


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}



	public int getSubNodeId() {
		return subNodeId;
	}
	public void setSubNodeId(int subNodeId) {
		this.subNodeId = subNodeId;
	}
	public int getVnfType() {
		return vnfType;
	}
	public void setVnfType(int vnfType) {
		this.vnfType = vnfType;
	}
	public float getRemainResource() {
		return remainResource;
	}
	public void setRemainResource(float remainResource) {
		this.remainResource = remainResource;
	}
	
	
	
}
