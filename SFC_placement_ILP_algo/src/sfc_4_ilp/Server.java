package sfc_4_ilp;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

public class Server {

	private int index;
	private String id;
	private String name;
	private float cpu;
	private float mem;
	private float storage;
	private int nodetype;
	private int vnftype;
	private int reuse;
	private int switchid;
	private int tenantid;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getCpu() {
		return cpu;
	}

	public void setCpu(float cpu) {
		this.cpu = cpu;
	}

	public float getMem() {
		return mem;
	}

	public void setMem(float mem) {
		this.mem = mem;
	}

	public float getStorage() {
		return storage;
	}

	public void setStorage(float storage) {
		this.storage = storage;
	}

	public int getNodetype() {
		return nodetype;
	}

	public void setNodetype(int nodetype) {
		this.nodetype = nodetype;
	}

	public int getVnftype() {
		return vnftype;
	}

	public void setVnftype(int vnftype) {
		this.vnftype = vnftype;
	}

	public int getReuse() {
		return reuse;
	}

	public void setReuse(int reuse) {
		this.reuse = reuse;
	}

	public int getSwitchid() {
		return switchid;
	}

	public void setSwitchid(int switchid) {
		this.switchid = switchid;
	}

	public int getTenantid() {
		return tenantid;
	}

	public void setTenantid(int tenantid) {
		this.tenantid = tenantid;
	}

	public Server(int index, String id, String name, float cpu, float mem, float storage, int nodetype, int vnftype,
			int reuse, int switchid, int tenantid) {
		this.setIndex(index);
		this.setId(id);
		this.setName(name);
		this.cpu = cpu;
		this.mem = mem;
		this.storage = storage;
		this.nodetype = nodetype;
		this.vnftype = vnftype;
		this.reuse = reuse;
		this.switchid = switchid;
		this.setTenantid(tenantid);
	}

	public Server() {
		// TODO Auto-generated constructor stub
	}

	public Server(int index, String id, String name) {
		this.index = index;
		this.id = id;
		this.name = name;
	}
	public void printServer() {
		System.out
				.println("Index: " + getIndex() + " CPU: " + cpu + " Node type: " + nodetype + " VNF type: " + vnftype);
	}

	public void printvirtualnode() {
		System.out.println("Index: " + getIndex() + " CPU: " + cpu + " Node type: " + nodetype + " VNF type: " + vnftype
				+ " tenant ID: " + tenantid + " reuse: " + reuse + " switchid: " + switchid);
	}
	
	
	public Server clone()
	{
		Server srv = new Server(index, id, name, cpu, mem, storage, nodetype, vnftype, reuse, switchid, tenantid);
		return srv;
	}
}
