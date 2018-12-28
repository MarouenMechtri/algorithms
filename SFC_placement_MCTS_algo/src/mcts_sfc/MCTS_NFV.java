package mcts_sfc;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import org.jblas.DoubleMatrix;

import sfc_4_mcts.Cost;
import sfc_4_mcts.Server;
import sfc_4_mcts.WeightedGraph;

public class MCTS_NFV {

	private static int maxIter = 50;//for MCTS
	private static boolean stopIfsuccess = true;//for MCTS
	private static boolean consolidationMCTS = true;//for MCTS
	private static boolean loadBalanceMCTS = true;//for MCTS
	private static boolean randomMCTS = true;//for MCTS
	
	static int real_size_of_IG;
	static int threshold_consolidation = 0;

	final static int pservers = 0;
	final static int pswitches = 1;
	final static int pvnfp = 2;

	final static int vvms = 0;
	final static int vswitches = 1;
	final static int vvnfp = 2;
	final static int vvnfc = 3;

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
		for (int i = 0; i < RGmatrix.rows; i++) {
			for (int j = 0; j < RGmatrix.columns; j++) {
				matrix.put(i, j, RGmatrix.get(i, j));
			}
		}
		return matrix;
	}
	
	private static void cloneMatrix(DoubleMatrix RGmatrix, DoubleMatrix matrix) {
//		matrix = new DoubleMatrix(RGmatrix.rows, RGmatrix.columns);
		for (int i = 0; i < RGmatrix.rows; i++) {
			for (int j = 0; j < RGmatrix.columns; j++) {
				matrix.put(i, j, RGmatrix.get(i, j));
			}
		}
//		return matrix;
	}

	public static DoubleMatrix read_RG_graph(String type, int size, int index) throws IOException {

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
				switch (Integer.parseInt(a[3])) {
				case pservers:
					tempserver = new Server(nbservers, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
							Float.parseFloat(a[2]), Float.parseFloat(a[2]), pservers, -1, -1, -1, -1);
					Pservers.put(Integer.parseInt(a[0]), tempserver);
					Pnodes.put(Integer.parseInt(a[0]), tempserver);
					break;
				case pswitches:
					tempserver = new Server(nbservers, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
							Float.parseFloat(a[2]), Float.parseFloat(a[2]), pswitches, -1, -1, -1, -1);
					Pswitches.put(Integer.parseInt(a[0]), tempserver);
					Pnodes.put(Integer.parseInt(a[0]), tempserver);
					break;
				case pvnfp:
					tempserver = new Server(nbservers, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
							Float.parseFloat(a[2]), Float.parseFloat(a[2]), pvnfp, Integer.parseInt(a[4]), -1, -1, -1);
					Pvnfp.put(Integer.parseInt(a[0]), tempserver);
					Pnodes.put(Integer.parseInt(a[0]), tempserver);
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

	public static String getPathString(Vector<Integer> path) {
		String stringpath = "";

		if(path.size() == 0)
		{
			Cost p_null = null;
			p_null.getBandwidth();
		}
		for (int i = 0; i < path.size(); i++) {
			stringpath += path.elementAt(i) + " ";
		}
		return stringpath;
	}
	
	private static void printSolutionMCTS(Vector<NodeMCTS> bestSequence) 
	{
		if(bestSequence == null)
		{
			System.out.println("*****MCTS result*****bestSequence == null");
			return;
		}
		
		
		System.out.println("*****MCTS result*****bestSequence.size()="+bestSequence.size());
//		for (int i = bestSequence.size()-1; i >=0 ; i--)
		for (int i = 0; i < bestSequence.size() ; i++)
		{
			if(bestSequence.elementAt(i).getMyIndex() == -1)//root node
				continue;
			System.out.println("VirtNode:"+bestSequence.elementAt(i).getVirtNodeId()+" mapped in: "+bestSequence.elementAt(i).getSubNodeId());
			System.out.println("Substrate Path:");
			for (int j = 0; j < bestSequence.elementAt(i).getPathHostingVirtLink().size(); j++) 
			{
				System.out.print(bestSequence.elementAt(i).getPathHostingVirtLink().elementAt(j)+" ");
			}
			System.out.println("");
			
			if(bestSequence.elementAt(i).isLeaf())//root node
				System.out.println (" isLeaf() and feasible="+bestSequence.elementAt(i).isFeasible());
		}
		
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

//		int[][] Result = new int[IG_size][3];
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
//			System.out.println("VirtNode:"+bestSequence.elementAt(i).getVirtNodeId()+" mapped in: "+bestSequence.elementAt(i).getSubNodeId());
			if (threshold_consolidation <= G.get(bestSequence.elementAt(i).getSubNodeId(), bestSequence.elementAt(i).getSubNodeId()) - 
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
//			System.out.println("Substrate Path:");
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

	private static Vector<NodeMCTS> getBestSequence(Vector<NodeMCTS> tree,
			Vector<NodeMCTS> leafNodes) 
	{
		int size = leafNodes.size();
//		double bestPayoff = Double.POSITIVE_INFINITY;
		double bestPayoff = 0;// = Double.NEGATIVE_INFINITY;
		int indexBest = -1;
		
		Vector<NodeMCTS> bestSquence = new Vector<NodeMCTS>();
		
		//Get Best leaf Node
		if (consolidationMCTS) {
			bestPayoff = Double.POSITIVE_INFINITY;
			for (int i = 0; i < size; i++) 
			{
				if(leafNodes.elementAt(i).isFeasible() 
						&& leafNodes.elementAt(i).getPayoff() < bestPayoff)//if consolidation: "<" else ">"
//						&& leafNodes.elementAt(i).getPayoff() < bestPayoff /*&& leafNodes.elementAt(i).getPayoff() > 0*/)
				{
					bestPayoff = leafNodes.elementAt(i).getPayoff();
					indexBest = leafNodes.elementAt(i).getMyIndex();
				}
			}
		}
		else if(loadBalanceMCTS)
		{
			bestPayoff = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < size; i++) 
			{
				if(leafNodes.elementAt(i).isFeasible() 
						&& leafNodes.elementAt(i).getPayoff() > bestPayoff)//if consolidation: "<" else ">"
//						&& leafNodes.elementAt(i).getPayoff() < bestPayoff /*&& leafNodes.elementAt(i).getPayoff() > 0*/)
				{
					bestPayoff = leafNodes.elementAt(i).getPayoff();
					indexBest = leafNodes.elementAt(i).getMyIndex();
				}
			}
		}
		else //Random result: Promote load balancing solution
		{
			bestPayoff = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < size; i++) 
			{
				if(leafNodes.elementAt(i).isFeasible() 
						&& leafNodes.elementAt(i).getPayoff() > bestPayoff)//if consolidation: "<" else ">"
//						&& leafNodes.elementAt(i).getPayoff() < bestPayoff /*&& leafNodes.elementAt(i).getPayoff() > 0*/)
				{
					bestPayoff = leafNodes.elementAt(i).getPayoff();
					indexBest = leafNodes.elementAt(i).getMyIndex();
				}
			}
		}
		
//		for (int i = 0; i < size; i++) 
//		{
//			if(leafNodes.elementAt(i).isFeasible() 
//					&& leafNodes.elementAt(i).getPayoff() > bestPayoff)//if consolidation: "<" else ">"
////					&& leafNodes.elementAt(i).getPayoff() < bestPayoff /*&& leafNodes.elementAt(i).getPayoff() > 0*/)
//			{
//				bestPayoff = leafNodes.elementAt(i).getPayoff();
//				indexBest = leafNodes.elementAt(i).getMyIndex();
//			}
//		}
		
		if(indexBest == -1)
			return null;
		
		System.out.println("###indexBest="+indexBest+" bestPayoff="+bestPayoff);
		NodeMCTS tmpNodeMCTS = tree.elementAt(indexBest);
		
		if(tmpNodeMCTS.isLeaf() == false)
		{
			NodeMCTS p_null = null;
			p_null.getNb_visit();
		}
	
		while (true) {
			bestSquence.add(tmpNodeMCTS);
			if(tmpNodeMCTS.getFatherIndex() == -1)
				break;
			tmpNodeMCTS = tree.elementAt(tmpNodeMCTS.getFatherIndex());
		}
		
		return bestSquence;
	}

	private static NodeMCTS simulation(Vector<NodeMCTS> tree,
			NodeMCTS current_node, Vector<Candidates> candidates,
			DoubleMatrix subMatrix, DoubleMatrix virtMatrix, 
			Vector<java.lang.Integer> subNodesAlreadyUsedForThisBranch, Vector<NodeMCTS> leafNodes, WeightedGraph cloneRGraph) 
	{
		NodeMCTS tmpNode = current_node, childNode = null;
		int neighbourIndex, subNodeCandidateId;
		double embeddingQuality = 0, remainCPU;
		HashMap<Integer, ArrayList<Server>> resultPath;
		boolean feasible = true;
		Vector<Integer> pathHostingVirtLink;
		int remainBandwidth;
		
		if(current_node.getFatherIndex() == -1)//root node
		{
//			System.out.println("in if(current_node.getFatherIndex() == -1)");
			subNodeCandidateId = getCandidateMCTS(candidates.firstElement(),//le premier noeud
					subNodesAlreadyUsedForThisBranch, 0);
			
			//int myIndex, int fatherIndex == rootIndex, int virtNodeId, int subNodeId
			tmpNode = new NodeMCTS(tree.size(), 0, 0, subNodeCandidateId);
			if(subNodeCandidateId == -1)
			{
				feasible = false;
				tmpNode.setFeasible(feasible);
				childNode = tmpNode;
				tree.add(tmpNode);
				childNode.setCurrent_payoff(-1);
//				break;
			}
			else
			{
				subNodesAlreadyUsedForThisBranch.add(subNodeCandidateId);
				
				tmpNode.setCurrent_payoff(subMatrix.get(subNodeCandidateId, subNodeCandidateId) /*Pnodes.get(subNodeCandidateId).getCpu()*/);
				tree.add(tmpNode);
				
				remainCPU = subMatrix.get(subNodeCandidateId, subNodeCandidateId) /*Pnodes.get(subNodeCandidateId).getCpu()*/ - virtMatrix.get(0, 0);
				subMatrix.put(subNodeCandidateId, subNodeCandidateId, remainCPU);

			}
			
		}
		if(tmpNode.isFeasible() == true)
		{
			for (int i = tmpNode.getVirtNodeId(); i < virtMatrix.getColumns()-1; i++)/*the last line in the matrix should match with leaf node*/ 
			{
				neighbourIndex = i+1;
	//			requiredBand = virtMatrix.get(i, neighbourIndex);
				
				subNodeCandidateId = getCandidateMCTS(candidates.elementAt(neighbourIndex), 
						subNodesAlreadyUsedForThisBranch, neighbourIndex);
				
				
				childNode = new NodeMCTS(tree.size(), tmpNode.getMyIndex(), neighbourIndex, subNodeCandidateId);
				if(subNodeCandidateId == -1)
				{
					feasible = false;
					childNode.setFeasible(feasible);
					break;
				}
				
				subNodesAlreadyUsedForThisBranch.add(subNodeCandidateId);
				
				remainCPU = subMatrix.get(subNodeCandidateId, subNodeCandidateId) /*Pnodes.get(subNodeCandidateId).getCpu()*/ - virtMatrix.get(neighbourIndex, neighbourIndex);
				subMatrix.put(subNodeCandidateId, subNodeCandidateId, remainCPU);
				
//				System.out.println("subMatrix.get(subNodeCandidateId, subNodeCandidateId)="+subMatrix.get(subNodeCandidateId, subNodeCandidateId));
//				System.out.println("Pnodes.get(subNodeCandidateId).getCpu()="+Pnodes.get(subNodeCandidateId).getCpu());
//				System.out.println("virtMatrix.get(neighbourIndex, neighbourIndex)="+virtMatrix.get(neighbourIndex, neighbourIndex));
				
				
				resultPath = cloneRGraph.shortestPath_and_cost(cloneRGraph, 
						tmpNode.getSubNodeId(), subNodeCandidateId, virtMatrix.get(i, i+1));
				
				remainBandwidth = (int) resultPath.keySet().toArray()[0];
				
//				System.out.println("remainBandwidth="+remainBandwidth);
//				System.out.println("virtMatrix.get(i, i+1)="+virtMatrix.get(i, i+1));
	//			System.out.println("in if(current_node.getFatherIndex() == -1)");
				tree.add(childNode);
				//check if the computed path has enough bandwidth resource
				if(checkBandwidth(virtMatrix.get(i, i+1) ,remainBandwidth) == false)
				{
					feasible = false;
					childNode.setFeasible(feasible);
					break;
				}
				
				embeddingQuality = remainBandwidth + 
						subMatrix.get(subNodeCandidateId, subNodeCandidateId) /*Pnodes.get(subNodeCandidateId).getCpu()*/+
						tmpNode.getCurrent_payoff();
				childNode.setCurrent_payoff(embeddingQuality);
				
				ArrayList<Server> path = (ArrayList<sfc_4_mcts.Server>) resultPath.values().toArray()[0];
				
				pathHostingVirtLink = getServerIds(path);
				childNode.setPathHostingVirtLink(pathHostingVirtLink);
				subMatrix = reduceCapa_calculated_Path(subMatrix,
						tmpNode.getSubNodeId(),subNodeCandidateId, virtMatrix.get(i, i+1),	path);
				
				updateWeightedGraph(cloneRGraph, path, virtMatrix.get(i, i+1));
				tmpNode = childNode;
			}
		}
		childNode.setPayoff_sum(childNode.getCurrent_payoff());
		childNode.setLeaf(true);
		leafNodes.add(childNode);

//		childNode.setFeasible(feasible);
		
		return childNode;
	}

	private static Vector<java.lang.Integer> getServerIds(
			ArrayList<sfc_4_mcts.Server> path) {
		Vector<Integer> serverIds = new Vector<Integer>(); 
		for (int i = 0; i < path.size(); i++) 
		{
			serverIds.add(path.get(i).getIndex());
		}
		return serverIds;
	}

	private static void updateWeightedGraph(WeightedGraph cloneRGraph,
			ArrayList<sfc_4_mcts.Server> path, double bandwidth) 
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

	private static int getCandidateMCTS(Candidates candidateList,
			Vector<java.lang.Integer> subNodesAlreadyUsedForThisBranch, int virtNodeId) {
		boolean found = false;
		int resultIndex = -1, subNodeId = -1;
		Random rand = new Random();
//		int iter = 0;
		
		Vector<Integer> validCandidates = new Vector<Integer>();//candidateList.getCands()
		
		for (int i = 0; i < candidateList.getCands().size(); i++) {
			if(exist(candidateList.getCands().elementAt(i).intValue(), subNodesAlreadyUsedForThisBranch) == false)
			{
				validCandidates.add(candidateList.getCands().elementAt(i).intValue());
			}
		}
		
		if(validCandidates.size() == 0)
			return -1;
				
		subNodeId = validCandidates.elementAt(0);
		
		if (consolidationMCTS) {
			for (int i = 1; i < validCandidates.size(); i++) {
				if(Pnodes.get(subNodeId).getCpu() > Pnodes.get(validCandidates.elementAt(i)).getCpu())//if consolidation: ">" else "<"
					subNodeId = validCandidates.elementAt(i);
			}
		}
		else if(loadBalanceMCTS) {
			for (int i = 1; i < validCandidates.size(); i++) {
				if(Pnodes.get(subNodeId).getCpu() < Pnodes.get(validCandidates.elementAt(i)).getCpu())//if consolidation: ">" else "<"
					subNodeId = validCandidates.elementAt(i);
			}
		}
		else
		{
			int iter = 0;
			while (found == false && iter++ < 100) 
			{
				resultIndex = rand.nextInt(validCandidates.size());
				subNodeId = validCandidates.elementAt(resultIndex);
				if(/*respect_nodeType(virtNodeId, subNodeId, Pnodes.size()) == true
					&& */exist(subNodeId, subNodesAlreadyUsedForThisBranch) == false)
					found = true;
				
//				System.out.println("Random");
			}
			if(found)
				return subNodeId;
		}
		
//		for (int i = 1; i < validCandidates.size(); i++) {
////			System.out.println("validCandidates.elementAt("+i+")="+validCandidates.elementAt(i));
//			if(Pnodes.get(subNodeId).getCpu() < Pnodes.get(validCandidates.elementAt(i)).getCpu())//if consolidation: ">" else "<"
//				subNodeId = validCandidates.elementAt(i);
//		}
//		System.out.println("selected subNodeId="+subNodeId);
		
		return subNodeId;
		
		
//		int iter = 0;
//		
//		while (found == false && iter++ < 100) 
//		{
//			resultIndex = rand.nextInt(validCandidates.size());
//			subNodeId = validCandidates.elementAt(resultIndex);
//			if(/*respect_nodeType(virtNodeId, subNodeId, Pnodes.size()) == true
//				&& */exist(subNodeId, subNodesAlreadyUsedForThisBranch) == false)
//				found = true;
//			
////			System.out.println("Random");
//		}
//		if(found)
//			return subNodeId;
//		
//		return -1;
	}

	private static boolean exist(
			int subNodeId,
			Vector<java.lang.Integer> subNodesAlreadyUsedForThisBranch) 
	{
		for (int i = 0; i < subNodesAlreadyUsedForThisBranch.size(); i++) 
		{
			if (subNodesAlreadyUsedForThisBranch.elementAt(i).intValue() == subNodeId) 
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static int computeNonValidCandidateLength(
			Vector<java.lang.Integer> candidates,
			Vector<java.lang.Integer> subNodesAlreadyUsedForThisBranch) {
		int nonValidNumber = 0;
		for (int i = 0; i < subNodesAlreadyUsedForThisBranch.size(); i++) {
			for (int j = 0; j < candidates.size(); j++) {
				if (subNodesAlreadyUsedForThisBranch.elementAt(i) == candidates.elementAt(j)) 
				{
					nonValidNumber++;
				}
			}
		}
		return nonValidNumber;
	}

	private static void backpropagation(Vector<NodeMCTS> tree, NodeMCTS current_node, double payoff) 
	{
		NodeMCTS tmpNodeMCTS = current_node;
//		System.out.println("tree.size="+tree.size());
		
		while (tmpNodeMCTS.getFatherIndex() != -1) {
			tmpNodeMCTS.updateNb_visit();
			tmpNodeMCTS.updateWithPayoff(payoff);
			
//			System.out.println("tmpNodeMCTS.getFatherIndex()="+tmpNodeMCTS.getFatherIndex()
//					+" tmpNodeMCTS.getMyIndex()="+tmpNodeMCTS.getMyIndex());
			tmpNodeMCTS = tree.elementAt(tmpNodeMCTS.getFatherIndex());
		}
		
	}

	private static NodeMCTS selection(Vector<NodeMCTS> tree, NodeMCTS current_node) {
		int maxIndex = -1;
		double maxSelectionValue = -1, curSelectionValue  = -1;
		NodeMCTS tmpNode = null;
		Vector<Integer> childrenIndex = current_node.getChildrenIndex();
		double fatherNbVisit;
		
		if(current_node.getFatherIndex() == -1)//root
			fatherNbVisit = 1;
		else
			fatherNbVisit = tree.elementAt(current_node.getFatherIndex()).getNb_visit();
		
		for (int i = 0; i < childrenIndex.size(); i++) 
		{
			tmpNode = tree.elementAt(childrenIndex.elementAt(i).intValue());
			curSelectionValue  = tmpNode.computeSelectionValue(fatherNbVisit);
			if(maxSelectionValue  < curSelectionValue)
			{
				maxSelectionValue  = curSelectionValue;
				maxIndex = childrenIndex.elementAt(i);
			}
		}
		
		if(maxIndex == -1)
			return null;
		
		return tree.elementAt(maxIndex);
	}

	private static Vector<Candidates> computeCandidates(int maxCandidates) {
		// TODO Auto-generated method stub
		int sizeVirt = Vnodes.size();
		Vector<Candidates> candidates = new Vector<>(sizeVirt);// new Vector<Candidates>();
//		int sizeSub = 0;
		Candidates virtNodeCandidates = null;
		
		for (int i = 0; i < sizeVirt; i++) {
			virtNodeCandidates = new Candidates();
			if(Vnodes.get(i).getNodetype() == vvms || Vnodes.get(i).getNodetype() == vvnfc)
			{				
				for (Server s : Pservers.values()) {
					if(virtNodeCandidates.getCands().size() >= maxCandidates)
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
				
//				candidates.add(i, virtNodeCandidates);
				candidates.add(virtNodeCandidates);
				
			}
			else if(Vnodes.get(i).getNodetype() == vswitches)
			{
				
				for (Server s : Pswitches.values()) {
					if(virtNodeCandidates.getCands().size() >= maxCandidates)
						break;
					
					if(Vnodes.get(i).getSwitchid() == s.getIndex()
						&&	s.getCpu() >= 
							Vnodes.get(i).getCpu() 
							&& checkLink(s.getIndex(), Vnodes.get(i).getIndex()) == true)
					{
						virtNodeCandidates.addNewCandidate(s.getIndex());
					
						if (respect_nodeType(Vnodes.get(i).getIndex(), s.getIndex()) == false) 
						{
//							System.out.println("Vnodes.get(i).getIndex()="+Vnodes.get(i).getIndex()
//									+" s.getIndex()="+s.getIndex());
//							
//							System.out.println("Vnodes.get(i).getNodetype()="+Vnodes.get(i).getNodetype()
//									+" s.getNodetype()="+s.getNodetype());
	
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
					if(virtNodeCandidates.getCands().size() >= maxCandidates)
						break;
					
					if(Vnodes.get(i).getVnftype() == s.getVnftype()
						&&	s.getCpu() >= 
							Vnodes.get(i).getCpu() 
							&& checkLink(s.getIndex(), Vnodes.get(i).getIndex()) == true)
					{
						virtNodeCandidates.addNewCandidate(s.getIndex());
					
						if (respect_nodeType(Vnodes.get(i).getIndex(), s.getIndex()) == false) 
						{
//							System.out.println("Vnodes.get(i).getIndex()="+Vnodes.get(i).getIndex()
//									+" s.getIndex()="+s.getIndex());
//							
//							System.out.println("Vnodes.get(i).getNodetype()="+Vnodes.get(i).getNodetype()
//									+" s.getNodetype()="+s.getNodetype());
	
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

	private static boolean checkLink(int indexSub, int indexVirt) {
		// TODO Oussama : should be updated
		
		
		return true;
	}
	
	
	 public static String main(String[] args) throws IOException {

//			public static void main(String[] args) throws IOException {
//				args = new String[4];
//				args[0] = "6"; // size of substrate graph (RG)
//				args[1] = "4";  // size of request graph/SFC (IG)
//				args[2] = "0";  // index of RG
//				args[3] = "0";  // index of IG
 
				int RG_size = Integer.parseInt(args[0]);
				int IG_size = Integer.parseInt(args[1]);

				DoubleMatrix G = read_RG_graph("RG", RG_size, Integer.parseInt(args[2]));
				// PrintPhysicalNodes();

				DoubleMatrix H = read_IG_graph("IG", IG_size, Integer.parseInt(args[3]));
				IG_size = real_size_of_IG;
				// PrintVirtualNodes();

				long startTime = System.currentTimeMillis();

				
//				////MCTS Begin
				int iter_nb = 0;

				maxIter = Integer.parseInt(args[4]);
				stopIfsuccess = Boolean.parseBoolean(args[5]);
				consolidationMCTS = Boolean.parseBoolean(args[6]);
				loadBalanceMCTS = Boolean.parseBoolean(args[7]);
				
//				System.out.println("***loadBalanceMCTS="+loadBalanceMCTS+" consolidationMCTS="+consolidationMCTS);
//				if(true)
//					return "-1";
				
				Vector<NodeMCTS> tree = new Vector<NodeMCTS>();
				NodeMCTS root = new NodeMCTS(-1);//index is -1 to identify the root node
				root.setMyIndex(-1);
				tree.add(root);
				
				NodeMCTS current_node = root;
				int maxCandidates = G.getColumns();//H.getColumns()*2;
				DoubleMatrix matrixClone = copymatrix(G);
				Vector<Candidates> candidates = computeCandidates(maxCandidates);//the index in this vector are following the virtual nodes id
				Vector<Integer> subNodesAlreadyUsedForThisBranch = new Vector<Integer>();
				int nonValidCandidateSize = 0;
				Vector<NodeMCTS> leafNodes = new Vector<NodeMCTS>();
				WeightedGraph cloneRGraph = weightedgraphRG.clone();
				
				while (iter_nb++ < maxIter) 
				{
					subNodesAlreadyUsedForThisBranch.removeAllElements();//At the first level the is no constraint
					nonValidCandidateSize = 0;
					///current_node.getVirtNodeId()+1 because children match with candidates to host the next virtual node 
					while(current_node.isAllChildrenGenerated(
							candidates.elementAt(current_node.getVirtNodeId()+1).getCands().size() - 
							nonValidCandidateSize) == true)
					{
						current_node = selection(tree, current_node);
						if(current_node == null)
							break;
						subNodesAlreadyUsedForThisBranch.add(current_node.getSubNodeId());
						
						nonValidCandidateSize = computeNonValidCandidateLength(
								candidates.elementAt(current_node.getVirtNodeId()).getCands(), 
								subNodesAlreadyUsedForThisBranch);
					}
					if (current_node == null) {
						current_node = root;
						cloneMatrix(G, matrixClone);
						weightedgraphRG.copy(cloneRGraph);
						
						continue;
					}
					
					if (current_node.isLeaf() == false) 
					{
						current_node = simulation(tree, current_node, candidates, matrixClone, H, 
								subNodesAlreadyUsedForThisBranch, leafNodes, cloneRGraph);//should return leaf node
//						compute payoff and set it in the leaf node
						if(current_node.isFeasible() && current_node.getPayoff() <= 0)
						{
							System.out.println("current_node.getPayoff()="+current_node.getPayoff());
							NodeMCTS p_null = null;
							p_null.getCurrent_payoff();
						}
						backpropagation(tree, current_node, current_node.getPayoff());
					}
					else
					{//Leaf node
						backpropagation(tree, current_node,0);
					}			
					
					current_node = root;
					cloneMatrix(G, matrixClone);
					weightedgraphRG.copy(cloneRGraph);
//					iter_nb++;
					
					if(stopIfsuccess && current_node.isLeaf() && current_node.isFeasible())
						break;
				}
				
				Vector<NodeMCTS> bestSequence = getBestSequence(tree, leafNodes);

				long stopTime = System.currentTimeMillis();
//				int[][] Result = new int[IG_size][2];
				long elapsedTime = stopTime - startTime;
//				System.out.println("time :" + elapsedTime / 1000 + "s   " + elapsedTime + "ms");

				

				
				if (bestSequence != null) 
				{
					Collections.reverse(bestSequence);
					//printSolutionMCTS(bestSequence);
					
					FileWriter writer = new FileWriter("SolutionMappingMCTS-instanceRG" + args[0] + "-" + args[2]
							+ "-instanceIG" + args[1] + "-" + args[3]);
					FileWriter wr_Nodes = new FileWriter("MCTS_nodes_mapping" + args[0] + "-" + args[1], true);

					FileWriter wr_Links = new FileWriter("MCTS_links_mapping" + args[0] + "-" + args[1], true);
					
					
					for (int i = 0; i < real_size_of_IG; i++) 
					{
						writer.write(i + " " + bestSequence.elementAt(i+1).getSubNodeId() + "\n");//+1 to skip the root
						wr_Nodes.write(bestSequence.elementAt(i+1).getSubNodeId() + "\t");//+1 to skip the root
					}
					
					wr_Nodes.write("\n");
					int next, iTrace;
					for (int i = 1/*0*/; i < real_size_of_IG; i++)//1 because we print paths between i and i+1 and this information is saved in (i+1) MCTS node 
					{
						next = i+1;
						iTrace = i-1;
						writer.write(iTrace + " "	+ i + "\n"
								+ getPathString(bestSequence.elementAt(next).getPathHostingVirtLink())  + "\n");//+1 because first element is the root
						wr_Links.write(getPathString(bestSequence.elementAt(next).getPathHostingVirtLink()) + "\t");
					}
					
					wr_Links.write("\n");
					writer.close();
					wr_Nodes.close();
					wr_Links.close();

					
					// check mapping solution
//					check_matching(IG_size,bestSequence, G, H);
					
					System.out.println("MCTS Yeeeeeep !!!!!!!!!!!!!! :)");
					
					return elapsedTime + " "+ check_matching(IG_size,bestSequence, G, H)+" -1 -1";

				}
				else
				{
					System.out.println("MCTS failed :'(");
					return -1 +" "+ check_matching(IG_size,bestSequence, G, H)+" -1 " + elapsedTime;

				}
				////MCTS End

			}
}