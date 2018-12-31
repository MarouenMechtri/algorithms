package sfc_4_ilp;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

public class MiniServer 
{
	private int index;
	private float cpu;
	public MiniServer(int index, float cpu) {
		super();
		this.index = index;
		this.cpu = cpu;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public float getCpu() {
		return cpu;
	}
	public void setCpu(float cpu) {
		this.cpu = cpu;
	}
	
	

}
