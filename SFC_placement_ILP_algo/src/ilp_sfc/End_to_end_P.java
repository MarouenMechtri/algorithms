package ilp_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

public class End_to_end_P {
	int src = -1;
	int dst = -1;
	int path[];

	public End_to_end_P(int src, int dst, int path[]) {
		super();
		this.src = src;
		this.dst = dst;
		this.path = path;
	}
	
	public End_to_end_P(int src, int dst) {	
		this.src = src;
		this.dst = dst;	
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

	public void printEnd_to_end() {
		for (int i = 0; i < path.length; i++) {
			System.out.print(path[i] + " ");
		}
		System.out.print("\n");
	}

	public int[] getPath() {
		return path;
	}

	public void setPath(int[] path) {
		this.path = path;
	}

	public String printPath() {
		String s = "";
		for (int i = 0; i < path.length; i++) {
			s = s + path[i] + " ";
		}
		return s;
	}

	public boolean checkExistenceOflink(int extrem1, int extrem2) 
	{
		if(path == null)
			return false;
		
		for (int i = 0; i < path.length-1; i++)//path.length-1 because we are checking i+1
		{
			if((path[i] == extrem1 && path[i+1] == extrem2) || 
					(path[i] == extrem2 && path[i+1] == extrem1))
				return true;
		}

		return false;
	}

}
