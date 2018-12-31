package ilp_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

public class End_to_end_V {
	
	int src;
	int dst;
	double requiredBandwidth;

	public End_to_end_V(int src, int dst, double requiredBandwidth) {
		super();
		this.src = src;
		this.dst = dst;
		this.requiredBandwidth = requiredBandwidth;
	}

	public int getSrc() {
		return src;
	}

	public void setSrc(int src) {
		this.src = src;
	}

	public int getDst() {
		return dst;
	}

	public void setDst(int dst) {
		this.dst = dst;
	}

	public double getRequiredBandwidth() {
		return requiredBandwidth;
	}

	public void setRequiredBandwidth(double requiredBandwidth) {
		this.requiredBandwidth = requiredBandwidth;
	}

	public void printEnd_to_end() {
		System.out.println("src: " + getSrc() + " dst: " + getDst() + " requiredBandwidth: " + getRequiredBandwidth());
	}

}
