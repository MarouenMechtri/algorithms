package ilp_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

public class RequestRevenue implements Comparable<RequestRevenue>{
	private int reqId;
	private double revenue;
	
	
	public int getReqId() {
		return reqId;
	}
	public void setReqId(int reqId) {
		this.reqId = reqId;
	}
	public double getRevenue() {
		return revenue;
	}
	
	
	public void setRevenue(double revenue) {
		this.revenue = revenue;
	}
	
	//positive result ==> arg0 is greater
//	public int compare(RequestRevenue arg0, RequestRevenue arg1) {
//		// TODO Auto-generated method stub
//		return (int) (arg0.getRevenue() - arg1.getRevenue());
//	}
	
	public int compareTo(RequestRevenue arg1) 
	{
		// TODO Auto-generated method stub
		return (int) (revenue - arg1.getRevenue());
	}
	

}
