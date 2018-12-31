package ilp_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.jblas.DoubleMatrix;

import sfc_4_ilp.Cost;
import sfc_4_ilp.Server;
import sfc_4_ilp.WeightedGraph;

public class LinearProgram 
{
	//Green begin
	    private final static int maxCandidates = 5;
	    private static final int maxPathCandidates = 1;
	    private static final boolean forceDiffCandidates = true;
	    
		private static int RG_size = 0;
		private static Vector<Float> vnf_spec;
		private static int sharingPayoff = 0;
		private static int fictitiousId = 0; 
		
		private static double p_idle = 0;
		private static double p_max = 50;		
		
		static int real_size_of_IG;
		static int threshold_consolidation = 0;

		final static int pservers = 0;
		final static int pswitches = 1;
		final static int pvnfp = 2;

		final static int vvms = 0;
		final static int vswitches = 1;
		final static int vvnfp = 2;
		public final static int vvnfc = 3;

		final static int fW = 0;
		final static int proxy = 1;
		final static int nat = 2;
		final static int IDS = 3;

		final static int noreuse = 0;
		final static int reuse = 1;

		public static WeightedGraph weightedgraphRG;

		public static HashMap<Integer, Server> Pservers;
		public static HashMap<Integer, Server> Pswitches;
		public static HashMap<Integer, Server> Pvnfp;
		public static HashMap<Integer, Server> Pnodes;
		
		
	   public static long cplexTime = 0;
	   public static double nbOnServers = 0;
	   public static double consumedEnergy = 0;
		public static Vector<MiniServer> initialServers = null;
		public static Vector<FictitiousNode> fictNodes;

		public static HashMap<Integer, Server> Vvms;
		public static HashMap<Integer, Server> Vswitches;
		public static HashMap<Integer, Server> Vvnfp;
		public static HashMap<Integer, Server> Vvnfc;
		public static HashMap<Integer, Server> Vnodes;


		public static void PrintPhysicalNodes() {

			System.out.println("servers:");
			for (Server s : Pservers.values()) {
				s.printServer();
			}
			System.out.println("switches:");
			for (Server s : Pswitches.values()) {
				s.printServer();
			}
			System.out.println("vnfp:");
			for (Server s : Pvnfp.values()) {
				s.printServer();
			}
			System.out.println("Pnodes:");
			for (Server s : Pnodes.values()) {
				s.printServer();
			}
		}

		public static void PrintVirtualNodes() {

			System.out.println("VMs:");
			for (Server s : Vvms.values()) {
				s.printvirtualnode();
			}
			System.out.println("switches:");
			for (Server s : Vswitches.values()) {
				s.printvirtualnode();
			}
			System.out.println("vnfp:");
			for (Server s : Vvnfp.values()) {
				s.printvirtualnode();
			}
			System.out.println("vnfc:");
			for (Server s : Vvnfc.values()) {
				s.printvirtualnode();
			}
		}

		private static DoubleMatrix copymatrix(DoubleMatrix RGmatrix) {
			DoubleMatrix matrix = new DoubleMatrix(RGmatrix.rows, RGmatrix.columns);
			for (int i = 0; i < RGmatrix.rows; i++)
			{
				for (int j = 0; j < RGmatrix.columns; j++) {
					matrix.put(i, j, RGmatrix.get(i, j));
				}
			}
			return matrix;
		}
		
		private static void cloneMatrix(DoubleMatrix RGmatrix, DoubleMatrix matrix) {
//			matrix = new DoubleMatrix(RGmatrix.rows, RGmatrix.columns);
			for (int i = 0; i < RGmatrix.rows; i++) {
				for (int j = 0; j < RGmatrix.columns; j++) {
					matrix.put(i, j, RGmatrix.get(i, j));
				}
			}
//			return matrix;
		}

		public static DoubleMatrix read_RG_graph(String type, int size, int index) throws IOException {

			nbOnServers = 0;
			consumedEnergy = 0;
			
			FileInputStream fstream = new FileInputStream("instance" + type + size + "-" + index);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String str;
			boolean N = true;
			boolean L = false;
			br.readLine();
			br.readLine();
			str = br.readLine();
			DoubleMatrix matrix = new DoubleMatrix(size, size);
			Server[] serverRG = new Server[size];
			Cost[][] adjaMatrixGraph = new Cost[size][size];
			int nbservers = 0;
			Pservers = new HashMap<Integer, Server>();
			Pswitches = new HashMap<Integer, Server>();
			Pvnfp = new HashMap<Integer, Server>();
			Pnodes = new HashMap<Integer, Server>();
			
			nbOnServers = 0;
			consumedEnergy = 0;
			
			double usage;
			
			boolean firstTime = false;
			if (initialServers == null) 
			{
				firstTime = true;
				initialServers = new Vector<MiniServer>();
			}

			while ((str = br.readLine()) != null) {
				if (str.equals("EDGES")) {
					N = false;
					L = true;
					str = br.readLine();
				}

				if (N) {
					String[] a = str.split(" ");
					matrix.put(Integer.parseInt(a[0]), Integer.parseInt(a[0]), Float.parseFloat(a[2]));
					Server tempserver = new Server();
					switch (Integer.parseInt(a[3])) 
					{
					case pservers:
						tempserver = new Server(nbservers, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
								Float.parseFloat(a[2]), Float.parseFloat(a[2]), pservers, -1, -1, -1, -1);
						Pservers.put(Integer.parseInt(a[0]), tempserver);
						Pnodes.put(Integer.parseInt(a[0]), tempserver);
						
						if(firstTime)
						{
							//nbservers or Integer.parseInt(a[0]) ???
							MiniServer mini = new MiniServer(nbservers, Float.parseFloat(a[2]));
							initialServers.add(mini);
						}
						if(getMiniServer(nbservers).getCpu() != tempserver.getCpu())
							nbOnServers++;
						
						if(getMiniServer(nbservers).getCpu() != 0)
							usage = (getMiniServer(nbservers).getCpu() - tempserver.getCpu())/getMiniServer(nbservers).getCpu();
						else 
							usage = 0;
						consumedEnergy += p_idle + (p_max - p_idle) * usage;

						break;
					case pswitches:
						tempserver = new Server(nbservers, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
								Float.parseFloat(a[2]), Float.parseFloat(a[2]), pswitches, -1, -1, -1, -1);
						Pswitches.put(Integer.parseInt(a[0]), tempserver);
						Pnodes.put(Integer.parseInt(a[0]), tempserver);
						
						if(firstTime)
						{
							//nbservers or Integer.parseInt(a[0]) ???
							MiniServer mini = new MiniServer(nbservers, Float.parseFloat(a[2]));
							initialServers.add(mini);
						}
						
						if(getMiniServer(nbservers).getCpu() != tempserver.getCpu())
							nbOnServers++;
						
						if(getMiniServer(nbservers).getCpu() != 0)
							usage = (getMiniServer(nbservers).getCpu() - tempserver.getCpu())/getMiniServer(nbservers).getCpu();
						else 
							usage = 0;
						consumedEnergy += p_idle + (p_max - p_idle) * usage;

						break;
					case pvnfp:
						tempserver = new Server(nbservers, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
								Float.parseFloat(a[2]), Float.parseFloat(a[2]), pvnfp, Integer.parseInt(a[4]), -1, -1, -1);
						Pvnfp.put(Integer.parseInt(a[0]), tempserver);
						Pnodes.put(Integer.parseInt(a[0]), tempserver);
						if(firstTime)
						{
							//nbservers or Integer.parseInt(a[0]) ???
							MiniServer mini = new MiniServer(nbservers, Float.parseFloat(a[2]));
							initialServers.add(mini);
						}
						if(getMiniServer(nbservers).getCpu() != tempserver.getCpu())
							nbOnServers++;
						
						if(getMiniServer(nbservers).getCpu() != 0)
							usage = (getMiniServer(nbservers).getCpu() - tempserver.getCpu())/getMiniServer(nbservers).getCpu();
						else 
							usage = 0;
						consumedEnergy += p_idle + (p_max - p_idle) * usage;
						break;
					default:
						System.out.println("there is no type assigned to node: " + Integer.parseInt(a[0]));
					}
					nbservers++;
					serverRG[Integer.parseInt(a[0])] = tempserver;

				}
				if (L) {
					String[] a = str.split(" ");
					matrix.put(Integer.parseInt(a[0]), Integer.parseInt(a[1]), Float.parseFloat(a[2]));
					matrix.put(Integer.parseInt(a[1]), Integer.parseInt(a[0]), Float.parseFloat(a[2]));
					adjaMatrixGraph[Integer.parseInt(a[0])][Integer.parseInt(a[1])] = new Cost(Float.parseFloat(a[2]),
							Float.parseFloat(a[2]), Float.parseFloat(a[2]));
					adjaMatrixGraph[Integer.parseInt(a[1])][Integer.parseInt(a[0])] = new Cost(Float.parseFloat(a[2]),
							Float.parseFloat(a[2]), Float.parseFloat(a[2]));
				}

			}
			in.close();
			// print_Matrix(matrix, size);
			weightedgraphRG = new WeightedGraph(adjaMatrixGraph, serverRG);
			// weightedgraphRG.construireMatriceWithIndirectLink(weightedgraphRG,
			// matrix);
			weightedgraphRG.construireMatriceWithdirectLink(weightedgraphRG);
			// print_Matrix(matrix, size);
			return matrix;

		}

		public static DoubleMatrix read_IG_graph(String type, int size, int index) throws IOException {

			FileInputStream fstream = new FileInputStream("instance" + type + size + "-" + index);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String str;
			boolean N = true;
			boolean L = false;
			br.readLine();
			String b[] = br.readLine().split(" ");
			int IG_size = Integer.parseInt(b[0]);
			int nbSFC = Integer.parseInt(b[1]);
			int tenantID = Integer.parseInt(b[2]);
			real_size_of_IG = IG_size;
			str = br.readLine();
			DoubleMatrix matrix = new DoubleMatrix(IG_size, IG_size);
			int nbVirtualNode = 0;

			Vvms = new HashMap<Integer, Server>();
			Vswitches = new HashMap<Integer, Server>();
			Vvnfp = new HashMap<Integer, Server>();
			Vvnfc = new HashMap<Integer, Server>();
			Vnodes = new HashMap<Integer, Server>();

			while ((str = br.readLine()) != null) {
				if (str.equals("EDGES")) {
					N = false;
					L = true;
					str = br.readLine();
				}

				if (str.contains("same")) {
					break;
				}
				if (N) {
					String[] a = str.split(" ");
					matrix.put(Integer.parseInt(a[0]), Integer.parseInt(a[0]), Float.parseFloat(a[2]));

					Server tempserver = new Server();
					switch (Integer.parseInt(a[3])) {
					case vvms:
						tempserver = new Server(nbVirtualNode, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
								Float.parseFloat(a[2]), Float.parseFloat(a[2]), vvms, -1, -1, -1, tenantID);
						Vvms.put(Integer.parseInt(a[0]), tempserver);
						Vnodes.put(Integer.parseInt(a[0]), tempserver);
						break;
					case vswitches:
						tempserver = new Server(nbVirtualNode, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
								Float.parseFloat(a[2]), Float.parseFloat(a[2]), vswitches, -1, -1, Integer.parseInt(a[4]),
								tenantID);
						Vswitches.put(Integer.parseInt(a[0]), tempserver);
						Vnodes.put(Integer.parseInt(a[0]), tempserver);
						break;
					case vvnfp:
						tempserver = new Server(nbVirtualNode, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
								Float.parseFloat(a[2]), Float.parseFloat(a[2]), vvnfp, Integer.parseInt(a[4]), -1, -1,
								tenantID);
						Vvnfp.put(Integer.parseInt(a[0]), tempserver);
						Vnodes.put(Integer.parseInt(a[0]), tempserver);
						break;
					case vvnfc:
						tempserver = new Server(nbVirtualNode, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
								Float.parseFloat(a[2]), Float.parseFloat(a[2]), vvnfc, Integer.parseInt(a[4]),
								Integer.parseInt(a[5]), -1, tenantID);
						Vvnfc.put(Integer.parseInt(a[0]), tempserver);
						Vnodes.put(Integer.parseInt(a[0]), tempserver);
						break;
					default:
						System.out.println("there is no type assigned to virtual node: " + Integer.parseInt(a[0]));
					}
					nbVirtualNode++;
				}
				if (L) {
					String[] a = str.split(" ");
					float SFCbw = 0;
					for (int i = 0; i < nbSFC; i++)
						SFCbw += Float.parseFloat(a[2 + i]);
					matrix.put(Integer.parseInt(a[0]), Integer.parseInt(a[1]), SFCbw);
					matrix.put(Integer.parseInt(a[1]), Integer.parseInt(a[0]), SFCbw);
				}

			}
			in.close();

			return matrix;

		}

		public static DoubleMatrix reduceCapa_calculated_Path(DoubleMatrix matrix, int row, int column, double value,
				ArrayList<Server> path) {

			for (int i = 0; i < path.size() - 1; i++) {
				matrix.put(path.get(i).getIndex(), path.get(i + 1).getIndex(),
						matrix.get(path.get(i).getIndex(), path.get(i + 1).getIndex()) - value);
				matrix.put(path.get(i + 1).getIndex(), path.get(i).getIndex(),
						matrix.get(path.get(i + 1).getIndex(), path.get(i).getIndex()) - value);
			}
			return matrix;
		}

		public static String getPathString(int[] path) {
			String stringpath = "";

			if(path.length == 0)
			{
				Cost p_null = null;
				p_null.getBandwidth();
			}
			for (int i = 0; i < path.length; i++) {
				stringpath += path[i] + " ";
			}
			return stringpath;
		}

		static boolean respect_nodeType(int indexIG, int indexRG) {

			boolean respect = true;

			// Vms should be mapped only on physical servers
			if (Vvms.containsKey(indexIG) && (Pswitches.containsKey(indexRG) || Pvnfp.containsKey(indexRG)))
				return false;
			// switches should be mapped only on switches having the same id
			if (Vswitches.containsKey(indexIG) && (Pservers.containsKey(indexRG) || Pvnfp.containsKey(indexRG)
					|| (Pswitches.containsKey(indexRG) && Vswitches.get(indexIG).getSwitchid() != indexRG)))
				return false;
			// Physical VNF should be mapped only on physical VNF having the same
			// VNF type
			if (Vvnfp.containsKey(indexIG) && (Pservers.containsKey(indexRG) || Pswitches.containsKey(indexRG)
					|| (Pvnfp.containsKey(indexRG) && Vvnfp.get(indexIG).getVnftype() != Pvnfp.get(indexRG).getVnftype())))
				return false;
			// Virtual VNF should be mapped only on servers.
			// In the case of reusing existing virtual VNF, vnfc should be mapped
			// only on
			// servers that hosts vnfc and having the same VNF type.
			if (Vvnfc.containsKey(indexIG) && (Pvnfp.containsKey(indexRG) || Pswitches.containsKey(indexRG)))
				return false;

			return respect;
		}
		
		private static double check_matching(int IG_size, Vector<NodeMCTS> bestSequence, DoubleMatrix G, DoubleMatrix H) {

//			int[][] Result = new int[IG_size][3];
			if(bestSequence == null)
				return 0;
			
			
			boolean solutionmatching = true;

			int nb_mapped_node = 0;
			int nb_mapped_link = 0;
			int nb_total_link = 0;

			for (int i = 0; i < bestSequence.size() ; i++)
			{
				if(bestSequence.elementAt(i).getMyIndex() == -1)//root node
					continue;
				
				if(bestSequence.elementAt(i).getSubNodeId() > RG_size)//root node
				{
					if(respect_nodeType(bestSequence.elementAt(i).getVirtNodeId(),getFictiousNodeViaId(fictNodes,bestSequence.elementAt(i).getSubNodeId()).getSubNodeId()))
						nb_mapped_node++;
				}
//				System.out.println("VirtNode:"+bestSequence.elementAt(i).getVirtNodeId()+" mapped in: "+bestSequence.elementAt(i).getSubNodeId());
				else if (threshold_consolidation <= G.get(bestSequence.elementAt(i).getSubNodeId(), bestSequence.elementAt(i).getSubNodeId()) - 
						H.get(bestSequence.elementAt(i).getVirtNodeId(), bestSequence.elementAt(i).getVirtNodeId()))
				{
					if(respect_nodeType(bestSequence.elementAt(i).getVirtNodeId(), bestSequence.elementAt(i).getSubNodeId()))
						nb_mapped_node++;
					else
					{
						solutionmatching = false;
						Cost p_null = null;
						p_null.getBandwidth();
					}
				}
				 else
				{
					solutionmatching = false;
					Cost p_null = null;
					p_null.getBandwidth();
				}
//				System.out.println("Substrate Path:");
				
				if(i >= 2)
				{
					double reqBand = H.get(bestSequence.elementAt(i-1).getVirtNodeId(), bestSequence.elementAt(i).getVirtNodeId());
					double remainBand = 0;
					for (int j = 0; j < bestSequence.elementAt(i).getPathHostingVirtLink().size()-1; j++) 
					{
						remainBand = G.get(bestSequence.elementAt(i).getPathHostingVirtLink().elementAt(j), 
								bestSequence.elementAt(i).getPathHostingVirtLink().elementAt(j+1));
						remainBand = remainBand - reqBand;
						
						G.put(bestSequence.elementAt(i).getPathHostingVirtLink().elementAt(j), 
								bestSequence.elementAt(i).getPathHostingVirtLink().elementAt(j+1), remainBand);
						
						G.put(bestSequence.elementAt(i).getPathHostingVirtLink().elementAt(j+1), 
						bestSequence.elementAt(i).getPathHostingVirtLink().elementAt(j), remainBand);
						
						if(remainBand < 0){
							solutionmatching = false;
							Cost p_null = null;
							p_null.getBandwidth();
							break;
						}
						
					}
					if (solutionmatching)
						nb_mapped_link++;
					else 
						break;
					
				}				
				
			}


			return (nb_mapped_link+nb_mapped_node)/(2*H.getLength()-1);// nb de ressource virtuelle mapped/ nb de ressources virtuelle requested
		}

		private static Vector<java.lang.Integer> getServerIds(
				ArrayList<Server> path) {
			Vector<Integer> serverIds = new Vector<Integer>(); 
			for (int i = 0; i < path.size(); i++) 
			{
				serverIds.add(path.get(i).getIndex());
			}
			return serverIds;
		}

		private static void updateWeightedGraph(WeightedGraph cloneRGraph,
				ArrayList<sfc_4_ilp.Server> path, double bandwidth) 
		{
			float tmp;
			Cost costTmp;
			for (int i = 0; i < path.size()-1; i++) 
			{
				costTmp = cloneRGraph.getWeight(path.get(i).getIndex(), path.get(i+1).getIndex());
				tmp = (float) (costTmp.getBandwidth() - bandwidth);
				costTmp.setBandwidth(tmp);
				
				
				costTmp = cloneRGraph.getWeight(path.get(i+1).getIndex(), path.get(i).getIndex());
				tmp = (float) (costTmp.getBandwidth() - bandwidth);
				costTmp.setBandwidth(tmp);

			}
			
		}

		private static boolean checkBandwidth(double bandwidth, int cost) 
		{

			if(bandwidth > cost)
				return false;
			
			return true;
		}

		
		private static MiniServer getMiniServer(Integer id) 
		{
			int size = initialServers.size();
			for (int i = 0; i < size; i++) 
			{
				if(initialServers.elementAt(i).getIndex() == id)
					return initialServers.elementAt(i);
			}
			return null;
		}

		private static FictitiousNode getFictiousNodeViaId(
				Vector<FictitiousNode> fNodes, int id) 
		{

			for (int i = 0; i < fNodes.size(); i++) 
			{
				if (fNodes.elementAt(i).getIndex() == id) {
					return fNodes.elementAt(i);
				}
			}
			return null;
		}

		
		private static Vector<Candidates> computeCandidates(int maxCandNb) 
		{
			// TODO Auto-generated method stub
			int sizeVirt = Vnodes.size();
			Vector<Candidates> candidates = new Vector<Candidates>(sizeVirt);// new Vector<Candidates>();
//			int sizeSub = 0;
			Candidates virtNodeCandidates = null;
			
			for (int i = 0; i < sizeVirt; i++) {
				virtNodeCandidates = new Candidates();
				if(Vnodes.get(i).getNodetype() == vvms || Vnodes.get(i).getNodetype() == vvnfc)
				{		
					if(Vnodes.get(i).getNodetype() == vvnfc)
					{
//						System.out.println("reqCpu="+Vnodes.get(i).getCpu()+" fictNodes.size()="+fictNodes.size());
						
						for (int j = 0; j < fictNodes.size(); j++) 
						{
							if(virtNodeCandidates.getCands().size() >= maxCandNb)
								break;
							
							if(fictNodes.elementAt(j).getVnfType() == Vnodes.get(i).getVnftype()
									&& fictNodes.elementAt(j).getRemainResource() >= 
									/*Vnodes.get(i).getCpu()*/ vnf_spec.elementAt(Vnodes.get(i).getVnftype())//resource demand
									&& checkLink(fictNodes.elementAt(j).getSubNodeId(), Vnodes.get(i).getIndex()) == true)
							{
								virtNodeCandidates.addNewCandidate(fictNodes.elementAt(j).getIndex());//index>RG_size since they are fictitious node
							}
						}
						
						for (Server s : Pservers.values()) 
						{
							if(virtNodeCandidates.getCands().size() >= maxCandNb)
								break;
							
							if(s.getCpu() >= 
									Vnodes.get(i).getCpu()
									//vnf_spec.elementAt(Vnodes.get(i).getVnftype())//resource demand of the VNF
									&& checkLink(s.getIndex(), Vnodes.get(i).getIndex()) == true)
										virtNodeCandidates.addNewCandidate(s.getIndex());
							
							if (respect_nodeType(Vnodes.get(i).getIndex(), s.getIndex()) == false) 
							{
								Cost p_null = null;
								p_null.getBandwidth();
							}
						}
						
//						printCandidates(virtNodeCandidates);
						
					}
					else //VM
					{
						for (Server s : Pservers.values()) 
						{
							if(virtNodeCandidates.getCands().size() >= maxCandNb)
								break;
							
							if(s.getCpu() >= 
									Vnodes.get(i).getCpu() 
									&& checkLink(s.getIndex(), Vnodes.get(i).getIndex()) == true)
								virtNodeCandidates.addNewCandidate(s.getIndex());
							
							if (respect_nodeType(Vnodes.get(i).getIndex(), s.getIndex()) == false) 
							{
		//						System.out.println("Vnodes.get(i).getIndex()="+Vnodes.get(i).getIndex()
		//								+" s.getIndex()="+s.getIndex());
		//						
		//						System.out.println("Vnodes.get(i).getNodetype()="+Vnodes.get(i).getNodetype()
		//								+" s.getNodetype()="+s.getNodetype());
								Cost p_null = null;
								p_null.getBandwidth();
							}
						}
					}
//					candidates.add(i, virtNodeCandidates);
					candidates.add(virtNodeCandidates);
					
				}
				else if(Vnodes.get(i).getNodetype() == vswitches)
				{
					
					for (Server s : Pswitches.values()) {
						if(virtNodeCandidates.getCands().size() >= maxCandNb)
							break;
						
						if(Vnodes.get(i).getSwitchid() == s.getIndex()
							&&	s.getCpu() >= 
								Vnodes.get(i).getCpu() 
								&& checkLink(s.getIndex(), Vnodes.get(i).getIndex()) == true)
						{
							virtNodeCandidates.addNewCandidate(s.getIndex());
						
							if (respect_nodeType(Vnodes.get(i).getIndex(), s.getIndex()) == false) 
							{
//								System.out.println("Vnodes.get(i).getIndex()="+Vnodes.get(i).getIndex()
//										+" s.getIndex()="+s.getIndex());
//								
//								System.out.println("Vnodes.get(i).getNodetype()="+Vnodes.get(i).getNodetype()
//										+" s.getNodetype()="+s.getNodetype());
		
								Cost p_null = null;
								p_null.getBandwidth();
							}
						}
					}
					
					candidates.add(virtNodeCandidates);
					
				
				}
				else if(Vnodes.get(i).getNodetype() == vvnfp)
				{
					for (Server s : Pvnfp.values()) {
						if(virtNodeCandidates.getCands().size() >= maxCandNb)
							break;
						
						if(Vnodes.get(i).getVnftype() == s.getVnftype()
							&&	s.getCpu() >= 
								Vnodes.get(i).getCpu() 
								&& checkLink(s.getIndex(), Vnodes.get(i).getIndex()) == true)
						{
							virtNodeCandidates.addNewCandidate(s.getIndex());
						
							if (respect_nodeType(Vnodes.get(i).getIndex(), s.getIndex()) == false) 
							{
//								System.out.println("Vnodes.get(i).getIndex()="+Vnodes.get(i).getIndex()
//										+" s.getIndex()="+s.getIndex());
//								
//								System.out.println("Vnodes.get(i).getNodetype()="+Vnodes.get(i).getNodetype()
//										+" s.getNodetype()="+s.getNodetype());
		
								Cost p_null = null;
								p_null.getBandwidth();
							}
						}
					}
					
					candidates.add(virtNodeCandidates);
				}
				
			}

			return candidates;
		}

		private static void printCandidates(Candidates virtNodeCandidates) 
		{
			System.out.println("*********************");
			for (int i = 0; i < virtNodeCandidates.getCands().size(); i++) 
			{
				System.out.println(i+" = "+virtNodeCandidates.getCands().elementAt(i));
			}
			System.out.println("#####################");
			
		}

		private static boolean checkLink(int indexSub, int indexVirt) {
			// TODO Oussama : should be updated
			
			
			return true;
		}
		
		
		 public static String main(String[] args) throws IOException, UnknownObjectException, IloException 
		 {

//				public static void main(String[] args) throws IOException {
//					args = new String[4];
//					args[0] = "6"; // size of substrate graph (RG)
//					args[1] = "4";  // size of request graph/SFC (IG)
//					args[2] = "0";  // index of RG
//					args[3] = "0";  // index of IG

			 		cplexTime = 0;
			 		
					RG_size = Integer.parseInt(args[0]);
					 //Green begin
					if(fictitiousId == 0)
						fictitiousId = RG_size + 5;
					
					int IG_size = Integer.parseInt(args[1]);

					DoubleMatrix G = read_RG_graph("RG", RG_size, Integer.parseInt(args[2]));
					// PrintPhysicalNodes();

					DoubleMatrix H = read_IG_graph("IG", IG_size, Integer.parseInt(args[3]));
					IG_size = real_size_of_IG;
					// PrintVirtualNodes();

					 //Green begin
					FileInputStream fstream;
					fstream = new FileInputStream("VNFSpec.txt");
					DataInputStream in = new DataInputStream(fstream);
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String str;
					vnf_spec = new Vector<Float>();
					
					while ((str = br.readLine()) != null) 
					{
						String[] b = str.split(" ");
						vnf_spec.add(Float.parseFloat(b[1]));
					}
					
					in.close();
					br.close();
					
					fstream = new FileInputStream("ficTopo.txt");
					in = new DataInputStream(fstream);
					br = new BufferedReader(new InputStreamReader(in));
					
					fictNodes = new Vector<FictitiousNode>();
//					int j = RG_size + 5;
					
					while ((str = br.readLine()) != null) 
					{
//						ajouter un index quand on cree le vecteur pour identifier 
//						le noeud fictif comme candidat pour MCTS
						String[] b = str.split(" ");
						FictitiousNode fict = new FictitiousNode(Integer.parseInt(b[0]), 
								Integer.parseInt(b[1]), Integer.parseInt(b[2]),Float.parseFloat(b[3]));
						fictNodes.add(fict);
						
					}
					
					in.close();
					br.close();
					
					
					
					// create new model 'cplex'
					IloCplex cplex = new IloCplex();
						
					// create variables x and y			
					ArrayList<CplexVarX> listCplexVarX = new ArrayList<CplexVarX>();
					ArrayList<CplexVarY> listCplexVarY = new ArrayList<CplexVarY>();			
					
					
					
					Vector<Candidates> candidates = computeCandidates(maxCandidates);//the index in this vector are following the virtual nodes id
					
					long startTime = System.currentTimeMillis();
					long stopTime, elapsedTime;

					for (int i = 0; i < candidates.size(); i++) {
						Vector<Integer> VNF_cand = candidates.elementAt(i).getCands();
						
						if(VNF_cand.size() == 0)//No available candidates
						{
							System.out.println("1-ILP failed :'( i="+i);
//							System.out.println("i="+i);
							stopTime = System.currentTimeMillis();
							elapsedTime = stopTime - startTime;
							return -1 +" "+ "0" +" -1 " + elapsedTime;

						}
						
						for (int j = 0; j < VNF_cand.size(); j++) {
							CplexVarX x = new CplexVarX();			
							
							x.setcandidateId(VNF_cand.elementAt(j));  
							x.setvirtNode(i); 	 //i
							x.setX(cplex.boolVar());
							listCplexVarX.add(x);
//							System.out.println("*-*-*-*-*-*-*-*-*-*-* ik = "+node_ID+listCandidate[i].getIndex());
						
						}
					}
					int nbVirtLinks = 0;
					//Y variables begin
					for (int i = 0; i < H.rows; i++) //0 can be ignored and we can start from 1
					{
						for (int j = 0; j < i && j < H.columns; j++) 
						{
//							System.out.println("H.rows="+H.rows+" H.columns="+H.columns);
//							System.out.println("H.length="+H.length+" i="+i+" j="+j);
							if(H.get(i,j) <= 0)
								continue;
							
							nbVirtLinks++;
							
							ArrayList<CplexVarX> extremities1 = getX_variablesBasedOnVirtId(i,listCplexVarX);
							ArrayList<CplexVarX> extremities2 = getX_variablesBasedOnVirtId(j,listCplexVarX);
							
							End_to_end_V virtLink = new End_to_end_V(i, j, H.get(i,j));
							
							if(extremities1.size() == 0 || extremities2.size() == 0)
							{
								CplexVarY y = new CplexVarY();
								
//								IloIntVar y_var = cplex.boolVar();
//								y_var.setMax(0);//set value to 0
//								y_var.setMin(0);//set value to 0
//								y.setY(y_var);
								
//								y.setPhysicalNewPath(null);
//								y.setVirtualLink(virtLink);
								End_to_end_V p_null = null;
								p_null.getDst();//this condition should not be satisfied (no candidates)
								
//								listCplexVarY.add(y);keep it commented
							}
							else
							{
								for (int k = 0; k < extremities1.size(); k++) 
								{
									for (int k2 = 0; k2 < extremities2.size(); k2++) 
									{
										CplexVarY y = new CplexVarY();
										
//										IloIntVar y_var = cplex.boolVar();
										
										
										y.setVirtualLink(virtLink);
										
										int extrem1 = extremities1.get(k).getcandidateId();
										int extrem2 = extremities2.get(k2).getcandidateId();
										
										y.setExtremity1(extrem1);
										y.setExtremity2(extrem2);
										
										if(extrem1 > RG_size)
											extrem1 = getFictiousNodeViaId(fictNodes, extrem1).getSubNodeId();
										
										if(extrem2 > RG_size)
											extrem2 = getFictiousNodeViaId(fictNodes, extrem2).getSubNodeId();
										
										if(extrem1 == extrem2)
										{
//											if(extrem1 != y.getExtremity1() || extrem2 != y.getExtremity2())
//											{
//												String p_null = null;
//												p_null.length();
//											}
//											if(y.getExtremity1() != y.getExtremity2())
//											{
//												String p_null = null;
//												p_null.length();
//											}
											y.setSameNode(true);
											y.setPhysicalNewPath(null);
											y.setY(cplex.boolVar());
											listCplexVarY.add(y);
										}
										else
										{
											HashMap<Integer, ArrayList<Server>> resultPath = weightedgraphRG.shortestPath_and_cost(
													weightedgraphRG, extrem1, extrem2, virtLink.getRequiredBandwidth());
											
											if(resultPath == null)
											{
//												y_var.setMax(0);//set value to 0
//												y_var.setMin(0);//set value to 0
//												y.setY(y_var);
																								
												y.setPhysicalNewPath(null);
												y.setVirtualLink(virtLink);
//												listCplexVarY.add(y);keep it commented
											}
											else
											{
												ArrayList<Server> path = (ArrayList<Server>) resultPath.values().toArray()[0];
												if(path.size() == 0)
												{
//													y_var.setMax(0);//set value to 0
//													y_var.setMin(0);//set value to 0
//													y.setY(y_var);
													
													y.setPhysicalNewPath(null);
													y.setVirtualLink(virtLink);
//													listCplexVarY.add(y);keep it commented
												}
												else
												{
													Vector<Integer> vectorPath = getServerIds(path);							
//													Integer[] newPathInteger = new Integer[vectorPath.size()];
													Integer[] newPathInteger = convertToIntArray(vectorPath); 																		
													End_to_end_P physicalNewPath = new End_to_end_P(extrem1, extrem2, IntegertoInt(newPathInteger));
//													check if extrem1 and path[0] and path[length-1]
//													and extrem2
													if(!((extrem1 == newPathInteger[0] && extrem2 == newPathInteger[path.size()-1])
															|| extrem2 == newPathInteger[0] && extrem1 == newPathInteger[path.size()-1]))
														System.out.println("Aloooooooooooo");
													
													y.setY(cplex.boolVar());
													y.setPhysicalNewPath(physicalNewPath);
													listCplexVarY.add(y);
													
													WeightedGraph cloneRGraph = weightedgraphRG.clone();
													cloneRGraph.removePath(newPathInteger);
													
													//Add disjoint paths
													for (int l = 0; l < maxPathCandidates; l++) {
														
														HashMap<Integer, ArrayList<Server>> resPath = cloneRGraph.shortestPath_and_cost(
																cloneRGraph, extrem1, extrem2, virtLink.getRequiredBandwidth());
														if(resPath == null)//No more path candidates
														{
															System.out.println("1-break****");
															break;
														}
															
														path = (ArrayList<Server>) resPath.values().toArray()[0];
														
														if(path.size() == 0)//No more path candidates
														{
															System.out.println("2-break****");
															break;
														}
														
														vectorPath = getServerIds(path);							
														newPathInteger = convertToIntArray(vectorPath); 																		
														physicalNewPath = new End_to_end_P(extrem1, extrem2, IntegertoInt(newPathInteger));
														
														CplexVarY yy = new CplexVarY();
														
														yy.setVirtualLink(virtLink);
														
														yy.setExtremity1(extremities1.get(k).getcandidateId());
														yy.setExtremity2(extremities2.get(k2).getcandidateId());
														
														yy.setY(cplex.boolVar());
														yy.setPhysicalNewPath(physicalNewPath);
														listCplexVarY.add(yy);
														
														cloneRGraph.removePath(newPathInteger);
													}
													
													
												}
											}
										}
										
//										listCplexVarY.add(y);I put it in the if/else  blocs

									}
										
								}
							}
						}
					}
					//Y variables end
					
					/****************Constraints***********************/
					
					//Begin constraint Sum(x_vw.C(v)) <= C(w), for all physical node w
					for (int i = 0; i < G.rows; i++)
					{
						ArrayList<CplexVarX> x_vars = getX_variablesBasedOnPhysicalId(i,listCplexVarX);
						if(x_vars.size() <= 0)//it is not a candidate
							continue;
						
						IloLinearNumExpr exprX = cplex.linearNumExpr();
						for (int j = 0; j < x_vars.size(); j++) 
						{
							double requiredCpu = 0;
							if(Vnodes.get(x_vars.get(j).getvirtNode()).getNodetype() == vvnfc)
							{
								requiredCpu = H.get(x_vars.get(j).getvirtNode(), x_vars.get(j).getvirtNode());// vnf_spec.elementAt(Vnodes.get(x_vars.get(j).getvirtNode()).getVnftype());
							}
							else
							{
								requiredCpu = H.get(x_vars.get(j).getvirtNode(), x_vars.get(j).getvirtNode());
							}
							exprX.addTerm(requiredCpu , x_vars.get(j).getX());
						}
						
						cplex.addLe(exprX, G.get(i, i));
						
					}
					//End constraint Sum(x_vw.C(v)) <= C(w), for all physical node w
					
//					System.out.println("fictNodes.size()="+fictNodes.size());
					//Begin constraint Sum(x_vw.C(v)) <= C(w), for all fictitious node w
					for (int i = 0; i < fictNodes.size(); i++)
					{
						ArrayList<CplexVarX> x_vars = getX_variablesBasedOnPhysicalId(fictNodes.elementAt(i).getIndex(),listCplexVarX);
						if(x_vars.size() == 0)//it is not a candidate
							continue;
						
						IloLinearNumExpr exprX = cplex.linearNumExpr();
						for (int j = 0; j < x_vars.size(); j++) 
						{
							double requiredCpu = vnf_spec.elementAt(Vnodes.get(x_vars.get(j).getvirtNode()).getVnftype());// H.get(x_vars.get(j).getvirtNode(), x_vars.get(j).getvirtNode());
							exprX.addTerm(requiredCpu , x_vars.get(j).getX());
						}
						
						cplex.addLe(exprX, fictNodes.elementAt(i).getRemainResource());
						
					}
					//End constraint Sum(x_vw.C(v)) <= C(w), for all fictitious node w
					
					if(forceDiffCandidates)
					{
						//Begin constraint Sum(x_vw) <= 1, for all physical node w
						for (int i = 0; i < G.rows; i++)
						{
							ArrayList<CplexVarX> x_vars = getX_variablesBasedOnPhysicalId(i,listCplexVarX);
							if(x_vars.size() <= 0)//it is not a candidate
								continue;
							
							IloLinearNumExpr exprX = cplex.linearNumExpr();
							for (int j = 0; j < x_vars.size(); j++) 
							{
								
								exprX.addTerm(1 , x_vars.get(j).getX());
							}
							
							cplex.addLe(exprX, 1);
							
						}
						//End constraint Sum(x_vw) <= 1, for all physical node w
	
						//Begin constraint Sum(x_vw) <= 1, for all fictitious node w
						for (int i = 0; i < fictNodes.size(); i++)
						{
							ArrayList<CplexVarX> x_vars = getX_variablesBasedOnPhysicalId(fictNodes.elementAt(i).getIndex(),listCplexVarX);
							if(x_vars.size() == 0)//it is not a candidate
								continue;
							
							IloLinearNumExpr exprX = cplex.linearNumExpr();
							for (int j = 0; j < x_vars.size(); j++) 
							{
								double requiredCpu = H.get(x_vars.get(j).getvirtNode(), x_vars.get(j).getvirtNode());
								exprX.addTerm(1 , x_vars.get(j).getX());
							}
							
							cplex.addLe(exprX, 1);
							
						}
						//End constraint Sum(x_vw.C(v)) <= C(w), for all fictitious node w
						}
					
					
					//Begin constraint Sum(x_vw) = 1, for all virtual node v
					for (int i = 0; i < H.rows; i++)//H.length ==> H.rows ou H.columns
					{
						ArrayList<CplexVarX> x_vars = getX_variablesBasedOnVirtId(i, listCplexVarX);
						if(x_vars.size() == 0)//it has not some candidate
						{
							System.out.println("2-ILP failed :'(");
							stopTime = System.currentTimeMillis();
							elapsedTime = stopTime - startTime;

							End_to_end_V p_null = null;
							p_null.getDst();//this condition should not be satisfied (no candidates)

							return -1 +" "+ "0" +" -1 " + elapsedTime;

						}
						if(x_vars.size() > 0)
						{
							IloLinearNumExpr exprX = cplex.linearNumExpr();
							for (int j = 0; j < x_vars.size(); j++) 
							{
	//							double requiredCpu = H.get(x_vars.get(j).getvirtNode(), x_vars.get(j).getvirtNode());
								exprX.addTerm(1 , x_vars.get(j).getX());
							}
							
							cplex.addEq(exprX, 1);
						}
					}
					//End constraint Sum(x_vw) = 1, for all virtual node v
					
					
					//Begin Sum(u_dp)=1
					for (int i = 0; i < H.rows; i++) //0 can be ignored and we can start from 1
					{
						for (int j = 0; j < i; j++) 
						{
							if(H.get(i,j) <= 0)
								continue;
							
							ArrayList<CplexVarY> y_vars = get_y_variablesBasedOnVirtualLink(i,j,listCplexVarY);
							if(y_vars.size() == 0)
							{
								System.out.println("3-ILP failed :'(");
								stopTime = System.currentTimeMillis();
								elapsedTime = stopTime - startTime;
								
								End_to_end_V p_null = null;
								p_null.getDst();//this condition should not be satisfied (no candidates)

								return -1 +" "+ "0" +" -1 " + elapsedTime;

							}
							else
							{
								if(y_vars.size() > 0)
								{
									IloLinearNumExpr exprY = cplex.linearNumExpr();
									for (int k = 0; k < y_vars.size(); k++) 
									{
										exprY.addTerm(1, y_vars.get(k).getY());
									}
									cplex.addEq(exprY, 1);
								}
							}
							
						}
						
					}
					//End Sum(u_dp)=1
					
					
					//Begin Sum(C(d).u_dp) < C(e)
					for (int i = 0; i < G.rows; i++) //0 can be ignored and we can start from 1
					{
						for (int j = 0; j < i; j++) 
						{
							if(G.get(i,j) <= 0)
								continue;
							
							ArrayList<CplexVarY> y_vars = get_y_variablesBasedOnPhysicalLink
																				(i,j,listCplexVarY);
							if(y_vars.size() == 0)
								continue;
							
							IloLinearNumExpr exprY = cplex.linearNumExpr();
							for (int k = 0; k < y_vars.size(); k++) 
							{
								exprY.addTerm(y_vars.get(k).getVirtualLink().getRequiredBandwidth(),
										y_vars.get(k).getY());
							}
							
							cplex.addLe(exprY, G.get(i, j));
						}
					}
					//End Sum(C(d).u_dp) < C(e)

					
					

//					System.out.println("RG_size="+RG_size);
					//u_dp <= sum(x...) begin
					for (int i = 0; i < listCplexVarY.size(); i++) 
					{
						CplexVarY y_var = listCplexVarY.get(i);
						
//						System.out.println("y_var.getExtremity1="+y_var.getExtremity1()+" y_var.getExtremity2="+y_var.getExtremity2());
//						System.out.println("virt_ext1="+y_var.getVirtualLink().getSrc()+" virt_ext2="+y_var.getVirtualLink().getDst());
						
						//common point is that path == null
						if(y_var.isSameNode() == true || 
							    (y_var.getExtremity1() == y_var.getExtremity2())
							    /*|| 
								y_var.getPhysicalNewPath() == null*/)
						{
							
							int virt_ext1 = y_var.getVirtualLink().getSrc();
							int virt_ext2 = y_var.getVirtualLink().getDst();
							
							
							
							int sub_ext1 = y_var.getExtremity1();//may be fictitious node
							int sub_ext2 = y_var.getExtremity2();//may be fictitious node

//							ArrayList<CplexVarX> x_vars1;
							ArrayList<CplexVarX> x_vars2;
							
//							ArrayList<CplexVarX> x_vars3;
							ArrayList<CplexVarX> x_vars4;
							
							//First inequality begin
							x_vars2 = getX_variablesBasedOnVirtId_CandId(virt_ext1, sub_ext1, listCplexVarX);//physical candidates
							
							x_vars4 = getX_variablesBasedOnVirtId_CandId(virt_ext1, sub_ext2, listCplexVarX);//physical candidates
							
							IloLinearNumExpr exprY = cplex.linearNumExpr();
							exprY.addTerm(1, y_var.getY());//u_dp
							
//							System.out.println("y_var.isSameNode() == true ...");
//							System.out.println("1-x_vars2.size="+x_vars2.size());
//							System.out.println("1-x_vars4.size="+x_vars4.size());
							
							for (int k = 0; k < x_vars2.size(); k++) 
							{
								exprY.addTerm(-1, x_vars2.get(k).getX());
							}
							for (int k = 0; k < x_vars4.size(); k++) 
							{
								exprY.addTerm(-1, x_vars4.get(k).getX());
							}
							cplex.addLe(exprY, 0);
							//First inequality end
							
//							x_vars1 = null;
							x_vars2 = null;
//							x_vars3 = null;
							x_vars4 = null;
							
							
							//Second inequality begin
							x_vars2 = getX_variablesBasedOnVirtId_CandId(virt_ext2, sub_ext1, listCplexVarX);//physical candidates
							
							x_vars4 = getX_variablesBasedOnVirtId_CandId(virt_ext2, sub_ext2, listCplexVarX);//physical candidates
							
//							System.out.println("2-x_vars2.size="+x_vars2.size());
//							System.out.println("2-x_vars4.size="+x_vars4.size());
							
							IloLinearNumExpr exprY2 = cplex.linearNumExpr();
							exprY2.addTerm(1, y_var.getY());//u_dp
							for (int k = 0; k < x_vars2.size(); k++) 
							{
								exprY2.addTerm(-1, x_vars2.get(k).getX());
							}
							for (int k = 0; k < x_vars4.size(); k++) 
							{
								exprY2.addTerm(-1, x_vars4.get(k).getX());
							}
							cplex.addLe(exprY2, 0);
							//Second inequality end
							
//							continue;
						}
						else//Not same node
						{
							
							int virt_ext1 = y_var.getVirtualLink().getSrc();
							int virt_ext2 = y_var.getVirtualLink().getDst();
							
							int sub_ext1 = y_var.getExtremity1();//may be fictitious node
							int sub_ext2 = y_var.getExtremity2();//may be fictitious node
							
							int path_ext1 = y_var.getPhysicalNewPath().getSrc();
							int path_ext2 = y_var.getPhysicalNewPath().getDst();
							
							ArrayList<CplexVarX> x_vars1;
							ArrayList<CplexVarX> x_vars2;
							
							ArrayList<CplexVarX> x_vars3;
							ArrayList<CplexVarX> x_vars4;
							
							//First inequality begin
							if(sub_ext1 != path_ext1)
							{
								x_vars1 = getX_variablesBasedOnVirtId_CandId(virt_ext1, sub_ext1, listCplexVarX);//fictitious candidates
								x_vars2 = getX_variablesBasedOnVirtId_CandId(virt_ext1, path_ext1, listCplexVarX);//physical candidates
								for (int j = 0; j < x_vars1.size(); j++) 
								{
									x_vars2.add(x_vars1.get(j));
								}
								
							}
							else//it is not fictitious candidate
							{
								x_vars2 = getX_variablesBasedOnVirtId_CandId(virt_ext1, path_ext1, listCplexVarX);//physical candidates
							}
							
							if(sub_ext2 != path_ext2)
							{
								x_vars3 = getX_variablesBasedOnVirtId_CandId(virt_ext1, sub_ext2, listCplexVarX);//fictitious candidates
								x_vars4 = getX_variablesBasedOnVirtId_CandId(virt_ext1, path_ext2, listCplexVarX);//physical candidates
								for (int j = 0; j < x_vars3.size(); j++) 
								{
									x_vars4.add(x_vars3.get(j));
								}
							}
							else//it is not fictitious candidate
							{
								x_vars4 = getX_variablesBasedOnVirtId_CandId(virt_ext1, path_ext2, listCplexVarX);//physical candidates
							}
							IloLinearNumExpr exprY = cplex.linearNumExpr();
							exprY.addTerm(2, y_var.getY());//u_dp
							for (int k = 0; k < x_vars2.size(); k++) 
							{
								exprY.addTerm(-1, x_vars2.get(k).getX());
							}
							for (int k = 0; k < x_vars4.size(); k++) 
							{
								exprY.addTerm(-1, x_vars4.get(k).getX());
							}
//							cplex.addLe(exprY, 0);
							//First inequality end
							
//							x_vars1 = null;
//							x_vars2 = null;
//							x_vars3 = null;
//							x_vars4 = null;
							
							ArrayList<CplexVarX> x_vars10 = null;
							ArrayList<CplexVarX> x_vars20 = null;
							ArrayList<CplexVarX> x_vars30 = null;
							ArrayList<CplexVarX> x_vars40 = null;
							
							
							//Second inequality begin
							if(sub_ext1 != path_ext1)
							{
								x_vars10 = getX_variablesBasedOnVirtId_CandId(virt_ext2, sub_ext1, listCplexVarX);//fictitious candidates
								x_vars20 = getX_variablesBasedOnVirtId_CandId(virt_ext2, path_ext1, listCplexVarX);//physical candidates
								for (int j = 0; j < x_vars10.size(); j++) 
								{
									x_vars20.add(x_vars10.get(j));
								}
							}
							else//it is not fictitious candidate
							{
								x_vars20 = getX_variablesBasedOnVirtId_CandId(virt_ext2, path_ext1, listCplexVarX);//physical candidates
							}
							
							if(sub_ext2 != path_ext2)
							{
								x_vars30 = getX_variablesBasedOnVirtId_CandId(virt_ext2, sub_ext2, listCplexVarX);//fictitious candidates
								x_vars40 = getX_variablesBasedOnVirtId_CandId(virt_ext2, path_ext2, listCplexVarX);//physical candidates
								for (int j = 0; j < x_vars30.size(); j++) 
								{
									x_vars40.add(x_vars30.get(j));
								}
							}
							else//it is not fictitious candidate
							{
								x_vars40 = getX_variablesBasedOnVirtId_CandId(virt_ext2, path_ext2, listCplexVarX);//physical candidates
							}
							
//							IloLinearNumExpr exprY2 = cplex.linearNumExpr();
//							exprY2.addTerm(1, y_var.getY());//u_dp
							for (int k = 0; k < x_vars20.size(); k++) 
							{
								exprY.addTerm(-1, x_vars20.get(k).getX());
							}
							for (int k = 0; k < x_vars40.size(); k++) 
							{
								exprY.addTerm(-1, x_vars40.get(k).getX());
							}
							cplex.addLe(exprY, 0);
							//Second inequality end
							
							if(!forceDiffCandidates)
							{
	//							COMPARE X20 AND X2
								for (int j = 0; j < x_vars2.size(); j++) 
								{
									ArrayList<CplexVarX> x_vars = getX_variablesBasedOnPhysicalId(x_vars2.get(j).getcandidateId(), x_vars20);
									if(x_vars.size() != 0)
									{
										
										IloLinearNumExpr exprSanity = cplex.linearNumExpr();
										
										exprSanity.addTerm(1, y_var.getY());
										exprSanity.addTerm(1, x_vars2.get(j).getX());
										for (int k = 0; k < x_vars.size(); k++) 
										{
											exprSanity.addTerm(1, x_vars.get(k).getX());
										}
																				
										cplex.addLe(exprSanity, 2);
									}
								}
	
	//							COMPARE X40 AND X4
								for (int j = 0; j < x_vars4.size(); j++) 
								{
									ArrayList<CplexVarX> x_vars = getX_variablesBasedOnPhysicalId(x_vars4.get(j).getcandidateId(), x_vars40);
									if(x_vars.size() != 0)
									{
										
										IloLinearNumExpr exprSanity2 = cplex.linearNumExpr();
										
										exprSanity2.addTerm(1, y_var.getY());
										
										exprSanity2.addTerm(1, x_vars4.get(j).getX());
										
										for (int k = 0; k < x_vars.size(); k++) 
										{
											exprSanity2.addTerm(1, x_vars.get(k).getX());
										}
										
										cplex.addLe(exprSanity2, 2);
									
									}
								}

							}
							//case of same node ?
							//not same node but path null ?
						}
					}
					//u_dp <= sum(x...) end
					
					
					
					//objective function begin
					IloLinearNumExpr exprObj = cplex.linearNumExpr();

					
//					for (int i = 0; i < listCplexVarY.size(); i++) 
//					{
//						int pathSize = 99999999;
//						if(listCplexVarY.get(i).getPhysicalNewPath() != null)
//							pathSize = listCplexVarY.get(i).getPhysicalNewPath().getPath().length;
//						exprObj.addTerm(pathSize, listCplexVarY.get(i).getY());
//					}
					
					
					for (int i = 0; i < listCplexVarX.size(); i++) 
					{
						if(listCplexVarX.get(i).getcandidateId() > RG_size)
//							continue;
							exprObj.addTerm(0.000000, listCplexVarX.get(i).getX());
						else
						{
							int subId = listCplexVarX.get(i).getcandidateId();
							double requiredCpu = 0;
							//Il s'agit d une allocation d une nouvelle VNF donc utiliser la matrice H
							if(false)//(Vnodes.get(listCplexVarX.get(i).getvirtNode()).getNodetype() == vvnfc)
							{
								requiredCpu = vnf_spec.elementAt(Vnodes.get(listCplexVarX.get(i).getvirtNode()).getVnftype());
							}
							else
							{
								requiredCpu = H.get(listCplexVarX.get(i).getvirtNode(), listCplexVarX.get(i).getvirtNode());
							}
//							double reqCPU = H.get(listCplexVarX.get(i).getvirtNode(), listCplexVarX.get(i).getvirtNode());
							
							double usage;
							if(getMiniServer(subId).getCpu() != 0)
								usage = (getMiniServer(subId).getCpu() - G.get(subId, subId) + requiredCpu)/getMiniServer(subId).getCpu();
							//usage = (getMiniServer(i).getCpu() - G.get(i, i))/getMiniServer(i).getCpu();
							else 
								usage = 0;
							
							
							if(usage < 0)
							{
								System.out.println("usage < 0!!!!!!!!!!!!!! usage="+usage);
								Cost p_null = null;
								p_null.getBandwidth();
							}
							
							double power = p_idle + (p_max - p_idle) * usage;
//							double savedPower = p_max - power;
							exprObj.addTerm(power, listCplexVarX.get(i).getX());
						}
						
					}
					
					
					
					cplex.addMinimize(exprObj);
//					cplex.addMaximize(exprObj);
					//objective function end
					
//					cplex.exportModel("model-"+Integer.parseInt(args[3])+".lp");

//					long cplexStartTime = System.currentTimeMillis();
					startTime = System.currentTimeMillis();
					
					ArrayList<CplexVarX> solutionVarX = new ArrayList<CplexVarX>();
					ArrayList<CplexVarY> solutionVarY = new ArrayList<CplexVarY>();
					if (cplex.solve()) 
					{
						stopTime = System.currentTimeMillis();
						elapsedTime = stopTime - startTime;
						
//						cplexTime = stopTime - cplexStartTime;
						
						System.out.println("____ RESULT ____");
						System.out.println("obj = " + cplex.getObjValue());
						
						FileWriter wrPower = new FileWriter("Objective_Function_ILP"+ RG_size + "-" + IG_size, true);
						wrPower.append(args[3]+"\t"+ cplex.getObjValue()+"\n");//args[3] id of request
						wrPower.flush();
						wrPower.close();
						
						
						for (int i = 0; i < listCplexVarX.size(); i++) 
						{
							if(Math.abs(cplex.getValue(listCplexVarX.get(i).getX()) - 1) <= 0.001)
							{
								solutionVarX.add(listCplexVarX.get(i));
//								System.out.println(listCplexVarX.get(i).getvirtNode()+" in "+listCplexVarX.get(i).getcandidateId());
							}
							else if(cplex.getValue(listCplexVarX.get(i).getX()) != 0)
							{
								System.out.println("******cplex.getValue(listCplexVarX.get(i).getX())="+cplex.getValue(listCplexVarX.get(i).getX()));
//								System.out.println(listCplexVarX.get(i).getvirtNode()+" in "+listCplexVarX.get(i).getcandidateId());
							}
						}
						
						for (int i = 0; i < listCplexVarY.size(); i++) 
						{
							if(Math.abs(cplex.getValue(listCplexVarY.get(i).getY()) - 1) <= 0.001)
							{
								solutionVarY.add(listCplexVarY.get(i));
								
//								System.out.println("VLink extrems: "+listCplexVarY.get(i).getVirtualLink().getSrc()+" "+listCplexVarY.get(i).getVirtualLink().getDst());
//								System.out.println("nodes mapping: "+listCplexVarY.get(i).getExtremity1()+" "+listCplexVarY.get(i).getExtremity2());
//								System.out.println("listCplexVarY.get(i).getY().getName()="+listCplexVarY.get(i).getY());
//								System.out.println("Physical Path extrems: "+listCplexVarY.get(i).getPhysicalNewPath().getSrc()+" "+listCplexVarY.get(i).getPhysicalNewPath().getDst());
							}
							else if(cplex.getValue(listCplexVarY.get(i).getY()) != 0)
							{
								System.out.println("******listCplexVarY.get(i).getY())="+listCplexVarY.get(i).getY());
//								System.out.println(listCplexVarX.get(i).getvirtNode()+" in "+listCplexVarX.get(i).getcandidateId());
							}
						}
					}
					else
					{
						System.out.println("cplex not solved !!!");
						
						stopTime = System.currentTimeMillis();
						elapsedTime = stopTime - startTime;
						
						System.out.println("4-ILP failed :'(");
						System.out.println("solutionVarX.size()="+solutionVarX.size()+" solutionVarY.size()="+solutionVarY.size());
						cplex.end();
						return -1 +" "+ "0"/*check_matching(IG_size,bestSequence, G, H)*/+" -1 " + elapsedTime;

					}
					
					if(solutionVarX.size() != H.rows)//sanity check
					{
						System.out.println("solutionVarX.size()="+solutionVarX.size());
						System.out.println("H.rows="+H.rows);
						Cost p_null = null;
						p_null.getBandwidth();
					}
					//sanity check
					if(solutionVarY.size() != nbVirtLinks)//valid only for chains
					{
						System.out.println("solutionVarY.size()="+solutionVarY.size());
						System.out.println("H.rows="+H.rows);
						Cost p_null = null;
						p_null.getBandwidth();
					}
					
					//move to up to be precise
//					stopTime = System.currentTimeMillis();
//					elapsedTime = stopTime - startTime;
					
					if(solutionVarX.size() == 0 || solutionVarY.size() == 0)//No mapping
					{
						System.out.println("5-ILP failed :'(");
						System.out.println("solutionVarX.size()="+solutionVarX.size()+" solutionVarY.size()="+solutionVarY.size());
						cplex.end();
						return -1 +" "+ "0"/*check_matching(IG_size,bestSequence, G, H)*/+" -1 " + elapsedTime;
					}
					
					double totPower = 0;
					for (int i = 0; i < G.rows; i++) {
					
						int subId = i;
					
						double usage;
						if(getMiniServer(subId).getCpu() != 0)
							usage = (getMiniServer(subId).getCpu() - G.get(subId, subId))/getMiniServer(subId).getCpu();
						//usage = (getMiniServer(i).getCpu() - G.get(i, i))/getMiniServer(i).getCpu();
						else 
							usage = 0;
						
						double power = p_idle + (p_max - p_idle) * usage;
						totPower += power;
					}
					
										
//					if(success)
//					{
					System.out.println("ILP success :)");
					
					FileWriter writer = new FileWriter("SolutionMappingILP-instanceRG" + args[0] + "-" + args[2]
							+ "-instanceIG" + args[1] + "-" + args[3]);
					FileWriter wr_Nodes = new FileWriter("ILP_nodes_mapping" + args[0] + "-" + args[1], true);

					FileWriter wr_Links = new FileWriter("ILP_links_mapping" + args[0] + "-" + args[1], true);

					for (int i = 0; i < solutionVarX.size(); i++) 
					{
						if(solutionVarX.get(i).getcandidateId() < RG_size)
						{

							int subId = solutionVarX.get(i).getcandidateId();
							MiniServer miniS = getMiniServer(subId);
							
							double used = miniS.getCpu() - G.get(subId, subId);//before mapping
							
							if(G.get(subId, subId) != 0)
							{
								int virtNodeId = solutionVarX.get(i).getvirtNode();
								if(Vnodes.get(virtNodeId).getNodetype() == vvnfc)
								{
									used = used + H.get(virtNodeId,virtNodeId);//vnf_spec.elementAt(Vnodes.get(virtNodeId).getVnftype());
								}
								else//VM
								{
									used = used + H.get(virtNodeId,virtNodeId);
								}
								 
								double usageRate = used / miniS.getCpu();
								writer.write(i + " " + subId + " " + usageRate +"\n");
							}
							else
							{
								writer.write(i + " " + subId + " 0" +"\n");
							}
							
							wr_Nodes.write(subId + "\t");//+1 to skip the root
				    		
											    	    
				    	    Server vnf = Vnodes.get(solutionVarX.get(i).getvirtNode());
				    	    if(vnf.getNodetype() != vvnfc)
				    	    	continue;

//				    	    System.out.println("add in fictTopo");
				    	  //true = append file
				    	    FileWriter fileWritter = new FileWriter("ficTopo.txt",true);
				    	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

//				    	    float remainCpu = vnf_spec.elementAt(vnf.getVnftype()) - vnf.getCpu();
				    	    float remainCpu = vnf.getCpu() - vnf_spec.elementAt(vnf.getVnftype());
				    	    fileWritter.write(""+fictitiousId+" "
				    	    		+ subId + " "
				    	    		+ vnf.getVnftype() + " "+ remainCpu+" "+ vnf.getCpu() +"\n");
				    	    
				    	    
				    	    fileWritter.flush();
				    	    fileWritter.close();
				    	    
				    	    fictitiousId++;
						
						}
						else//fictitious node
						{
//							update the residual
							int fictId = solutionVarX.get(i).getcandidateId();
							//avant dernier terme a supprimer, juste pour debug
							writer.write(i + " " + fictId + " "+ getFictiousNodeViaId(fictNodes, fictId).getSubNodeId()+ "\n");//+1 to skip the root
							wr_Nodes.write(fictId + "\t");//+1 to skip the root
							FileReader in1 = new FileReader("ficTopo.txt");
							
							BufferedReader file = new BufferedReader(in1);
					        String line;String input = "";

				    	    Server vnf = Vnodes.get(solutionVarX.get(i).getvirtNode());
				    	    
				    	    if(vnf.getNodetype() != vvnfc)//test d integrite
				    	    {
				    	    	NodeMCTS p_null = null;
				    	    	p_null.getFatherIndex();
				    	    }
				    	    float askedCpu = vnf_spec.elementAt(vnf.getVnftype());//vnf.getCpu();
				    	    
					        while((line = file.readLine()) != null)
					        {
					        	String s = line; 
								String[] b = s.split(" ");
					        	if(Integer.parseInt(b[0]) == fictId)
					        	{
					        		float remainCPU = Float.parseFloat(b[3]) - askedCpu;
					        		if(remainCPU < 0)//test d integrite
						    	    {
						    	    	NodeMCTS p_null = null;
						    	    	System.out.println("remainCPU = "+remainCPU+
						    	    			" Float.parseFloat(b[3])="+Float.parseFloat(b[3])+
						    	    			" askedCpu="+askedCpu);
						    	    	p_null.getFatherIndex();
						    	    }
					        		input += "" + b[0] + " " + b[1] +" " + b[2] + " " + remainCPU + " " + b[4] + "\n";
					        	}
					        	else
					        		input += line + "\n";
					        }

//					        file.close();
					        in1.close();
							
//					    ecrire input dans le fichier
					    FileWriter fileWritter = new FileWriter("ficTopo.txt");
				    	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				    	    fileWritter.write(input);
				    	    fileWritter.flush();
				    	    fileWritter.close();
						
						}
					}
					
					wr_Nodes.write("\n");
//					int next, iTrace;
					for (int i = 0; i < solutionVarY.size(); i++)//1 because we print paths between i and i+1 and this information is saved in (i+1) MCTS node 
					{
						if(solutionVarY.get(i).getPhysicalNewPath() == null
								|| solutionVarY.get(i).getPhysicalNewPath().getPath() == null
								|| solutionVarY.get(i).getPhysicalNewPath().getPath().length == 0)
						{
							writer.write(solutionVarY.get(i).getVirtualLink().getSrc() + " "+ solutionVarY.get(i).getVirtualLink().getDst() + "\n"
									+ " "  + "\n");
							wr_Links.write(" " + "\t");
						}
						else
						{
							writer.write(solutionVarY.get(i).getVirtualLink().getSrc() + " "+ solutionVarY.get(i).getVirtualLink().getDst() + "\n"
									+ getPathString(solutionVarY.get(i).getPhysicalNewPath().getPath())  + "\n");//+1 because first element is the root
							wr_Links.write(getPathString(solutionVarY.get(i).getPhysicalNewPath().getPath()) + "\t");
						}
					}
					
					wr_Links.write("\n");
					writer.close();
					wr_Nodes.close();
					wr_Links.close();

					
					cplex.end();
//					return elapsedTime + " "+ check_matching(IG_size,bestSequence, G, H)+" -1 -1";
					return elapsedTime + " "+ "1"+" -1 -1";

//					}
//					else
//					{
//						System.out.println("MCTS failed :'(");
//						return -1 +" "+ check_matching(IG_size,bestSequence, G, H)+" -1 " + elapsedTime;
//
//					
//					}
		 }

		 
		private static Integer[] convertToIntArray(Vector<Integer> vectorPath) {
			Integer[] newPathInteger = new Integer[vectorPath.size()];
			for (int i = 0; i < vectorPath.size(); i++) 
			{
				newPathInteger[i] = vectorPath.elementAt(i);
			}
			return newPathInteger;
		}

		private static ArrayList<CplexVarX> getX_variablesBasedOnVirtId_CandId(
				int virt_ext1, int sub_ext1, ArrayList<CplexVarX> listCplexVarX) {
			ArrayList<CplexVarX> result = new ArrayList<CplexVarX>();
			
			if(sub_ext1 == -1)//no valid candidate
				return result;
			
			for (int j = 0; j < listCplexVarX.size(); j++) 
			{
				if(listCplexVarX.get(j).getvirtNode() == virt_ext1 
						&& listCplexVarX.get(j).getcandidateId() == sub_ext1)
					result.add(listCplexVarX.get(j));
			}
//			if(result.size() > 1)
//			{
//				Cost p_null = null;
//				p_null.getBandwidth();
//			}
			return result;
		}

		private static ArrayList<CplexVarY> get_y_variablesBasedOnPhysicalLink(
				int extrem1, int extrem2, ArrayList<CplexVarY> listCplexVarY) 
		{
				ArrayList<CplexVarY> result = new ArrayList<CplexVarY>();
				
				for (int i = 0; i < listCplexVarY.size(); i++) 
				{
					if(listCplexVarY.get(i).getPhysicalNewPath() == null)
						continue;
					
					if(listCplexVarY.get(i).getPhysicalNewPath().checkExistenceOflink(extrem1, extrem2))
						result.add(listCplexVarY.get(i));
				}	
					
				return result;
		}

		private static ArrayList<CplexVarY> get_y_variablesBasedOnVirtualLink(
				int extrem1, int extrem2, ArrayList<CplexVarY> listCplexVarY) 
		{
			ArrayList<CplexVarY> result = new ArrayList<CplexVarY>();
			for (int i = 0; i < listCplexVarY.size(); i++) 
			{
				int src = listCplexVarY.get(i).getVirtualLink().getSrc();
				int dst = listCplexVarY.get(i).getVirtualLink().getDst();
				if((src == extrem1 && dst == extrem2) || 
						(src == extrem2 && dst == extrem1))
					result.add(listCplexVarY.get(i));
			}	
				
			return result;
		}

			private static ArrayList<CplexVarX> getX_variablesBasedOnPhysicalId(
				int subId, ArrayList<CplexVarX> listCplexVarX) 
			{
				ArrayList<CplexVarX> result = new ArrayList<CplexVarX>();
				for (int j = 0; j < listCplexVarX.size(); j++) 
				{
					if(listCplexVarX.get(j).getcandidateId() == subId)
						result.add(listCplexVarX.get(j));
				}
				return result;
			}

			public static int[] IntegertoInt(Integer[] newPathInteger) {
				  int[] newPathInt = new int[newPathInteger.length];
				  int e = 0;
				  for (Integer val : newPathInteger) newPathInt[e++] = val;
				  return newPathInt;
				}

			private static ArrayList<CplexVarX> getX_variablesBasedOnVirtId(int virtId,
				ArrayList<CplexVarX> listCplexVarX) 
		{
			ArrayList<CplexVarX> result = new ArrayList<CplexVarX>();
			for (int j = 0; j < listCplexVarX.size(); j++) 
			{
				if(listCplexVarX.get(j).getvirtNode() == virtId)
					result.add(listCplexVarX.get(j));
			}
			return result;
		}
		
}
