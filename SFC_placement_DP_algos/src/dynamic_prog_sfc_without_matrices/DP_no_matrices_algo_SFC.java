package dynamic_prog_sfc_without_matrices;

/**
 * @authors: Marouen Mechtri, Chaima Ghribi
 * @contacts: {mechtri.marwen, ghribii.chaima}@gmail.com
 * Created on Sep 15, 2016
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.jblas.DoubleMatrix;

import sfc_4_DP.Cost;
import sfc_4_DP.Server;
import sfc_4_DP.WeightedGraph;

public class DP_no_matrices_algo_SFC {

	static int real_size_of_IG;

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
	public static double INFINI = Double.MAX_VALUE;

	public static Node[][] copymatrix(Node[][] G, boolean copyRemainMatrix) {
		Node[][] clonedMatrix = new Node[G.length][G.length];
		for (int i = 0; i < G.length; i++) {
			for (int j = 0; j < G.length; j++) {
//				if (copyRemainMatrix)
//					clonedMatrix[i][j] = new Node(G[i][j].id, G[i][j].cost, G[i][j].succ, G[i][j].matrix);
//				else
				clonedMatrix[i][j] = new Node(G[i][j].id, G[i][j].cost, G[i][j].succ, G[i][j].reservedNode,
						G[i][j].reservedLink);
			}
		}
		return clonedMatrix;
	}

	public static void printmatrix(Node[][] G) {
		for (int i = 0; i < G.length; i++) {
			for (int j = 0; j < G.length; j++) {
				if (G[i][j].cost == INFINI)
					System.out.print(-1 + "\t");
				else
					System.out.print(G[i][j].cost + "\t");
			}
			System.out.println("||");
		}
	}

	public static void printmatrix3D(Node[][][] D) {
		for (int i = 0; i < D.length; i++) {
			printmatrix(D[i]);
			System.out.println("-----------------------");
		}
	}

	// public static Node[][] convertMatrix(double[][] D) {
	// Node[][] G = new Node[D.length][D.length];
	// for (int i = 0; i < D.length; i++) {
	// for (int j = 0; j < D.length; j++) {
	// if (i == j)
	// G[i][j] = new Node(i, D[i][j], null);
	// else
	// // there is no ID for links
	// G[i][j] = new Node(-1, D[i][j], null);
	// }
	// }
	// return G;
	//
	// }

	public static Node[][] convertMatrix(DoubleMatrix matrix) {
		//DoubleMatrix RG_ramin_capa = new DoubleMatrix();
		Node[][] G = new Node[matrix.rows][matrix.columns];
		for (int i = 0; i < matrix.rows; i++) {
			for (int j = 0; j < matrix.columns; j++) {
				if (i == j)
					G[i][j] = new Node(i, matrix.get(i, j), null);
				else
					// there is no ID for links
					G[i][j] = new Node(-1, matrix.get(i, j), null, null, null);
			}
		}
		return G;

	}

	public static void print_Matrix(DoubleMatrix matrix, int size) {
		System.out.println("-----------------------------------------------------------------");
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				System.out.print(String.format("%.2f \t", matrix.get(i, j)));
			}
			System.out.println();
		}
		System.out.println("-----------------------------------------------------------------");
	}

	public static void print_Matrix(Node[][] matrixNode, int size) {
		System.out.println("-----------------------------------------------------------------");
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				System.out.print(String.format("%.2f \t", matrixNode[i][j].cost));
			}
			System.out.println();
		}
		System.out.println("-----------------------------------------------------------------");
	}

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
				// matrix.put(Integer.parseInt(a[1]), Integer.parseInt(a[0]),
				// SFCbw);
			}

		}
		in.close();

		return matrix;

	}

	
	private static void storeSolution(int vNodeToMap, int pNodeToHost, int succ_pNode, DoubleMatrix IG, DoubleMatrix RG,
			Node[][][] D) {

		// lorsqu'on choisi le minimum de l'avant derniere matrice, il faut
		// mettre à jour la capacité restante en prenant en compte le mapping du
		// engress point

		if (vNodeToMap == IG.columns - 2) {

			D[vNodeToMap][pNodeToHost][pNodeToHost].reservedNode.add(new Node_src_dst(vNodeToMap + 1, succ_pNode));

		}

		D[vNodeToMap][pNodeToHost][pNodeToHost].reservedNode.add(new Node_src_dst(vNodeToMap, pNodeToHost));
		D[vNodeToMap][pNodeToHost][pNodeToHost].reservedLink.add(new Link_src_dst(
				new Node_src_dst(vNodeToMap, vNodeToMap + 1), new Node_src_dst(pNodeToHost, succ_pNode)));

	}
	
//	private static void updateMatrix4Remaincapa(int vNodeToMap, int pNodeToHost, int succ_pNode,
//			DoubleMatrix IG, DoubleMatrix RG, Node[][][] D) {
//
//		DoubleMatrix clonedRemainMatrix = new DoubleMatrix();
//
//		// lorsqu'on choisi le minimum de l'avant derniere matrice, il faut
//		// mettre à jour la capacité restante en prenant en compte le mapping du
//		// engress point
//		clonedRemainMatrix.copy(RG);
//		if (vNodeToMap == IG.columns - 2) {
//			double remainCPUengressPoint = RG.get(succ_pNode, succ_pNode)
//					- IG.get(vNodeToMap + 1, vNodeToMap + 1);
//			if (remainCPUengressPoint >= 0) {
//				D[vNodeToMap][pNodeToHost][pNodeToHost].reservedNode
//						.add(new Node_src_dst(vNodeToMap + 1, succ_pNode));
//			}
//		} else{
//
//			D[vNodeToMap][pNodeToHost][pNodeToHost].reservedNode.add(new Node_src_dst(vNodeToMap, pNodeToHost));
//			D[vNodeToMap][pNodeToHost][pNodeToHost].reservedLink
//					.add(new Link_src_dst(new Node_src_dst(vNodeToMap, vNodeToMap + 1), new Node_src_dst(pNodeToHost, succ_pNode)));
//			
//			Node success = D[vNodeToMap + 1][succ_pNode][succ_pNode];
//			Node prev = null;
//			while (success.succ != null) {
//				if (success.reservedNode.size() != 0)
//					clonedRemainMatrix.put(success.reservedNode.get(0).value2, success.reservedNode.get(0).value2,
//							clonedRemainMatrix.get(success.reservedNode.get(0).value2, success.reservedNode.get(0).value2)
//									- IG.get(success.reservedNode.get(0).value1, success.reservedNode.get(0).value1));
//				if (success.reservedLink.size() != 0){
//					double remain_bandwidth = clonedRemainMatrix.get(success.reservedLink.get(0).value2.value1, success.reservedLink.get(0).value2.value2)
//							- IG.get(success.reservedLink.get(0).value1.value1, success.reservedLink.get(0).value1.value2);
//					clonedRemainMatrix.put(success.reservedLink.get(0).value2.value1, success.reservedLink.get(0).value2.value2, remain_bandwidth);
//					clonedRemainMatrix.put(success.reservedLink.get(0).value2.value2, success.reservedLink.get(0).value2.value1, remain_bandwidth);
//				}
//				
//				prev = success;
//				success = success.succ;
//			}
//			if (prev != null) {
//				clonedRemainMatrix.put(prev.reservedNode.get(1).value2, prev.reservedNode.get(1).value2,
//						clonedRemainMatrix.get(prev.reservedNode.get(1).value2, prev.reservedNode.get(1).value2)
//								- IG.get(prev.reservedNode.get(1).value1, prev.reservedNode.get(1).value1));
//			}
//			
//		}
//		double remainCPU = clonedRemainMatrix.get(pNodeToHost, pNodeToHost) - IG.get(vNodeToMap, vNodeToMap);
//		double remainBDW = clonedRemainMatrix.get(pNodeToHost, succ_pNode) - IG.get(vNodeToMap, vNodeToMap + 1);
//
//		if (remainCPU >= 0 && remainBDW >= 0) {
//			D[vNodeToMap][pNodeToHost][pNodeToHost].reservedNode.add(new Node_src_dst(vNodeToMap, pNodeToHost));
//			D[vNodeToMap][pNodeToHost][pNodeToHost].reservedLink
//					.add(new Link_src_dst(new Node_src_dst(vNodeToMap, vNodeToMap + 1), new Node_src_dst(pNodeToHost, succ_pNode)));
//			
//		}
//
//		clonedRemainMatrix = null;
//	}

//	private static boolean check_remaining_capa(int vNodeToMap, int pNodeToHost, int succ_pNode, Node[][][] D,
//			DoubleMatrix IG, DoubleMatrix RG) {
//		// lorsqu'on choisi le minimum de l'avant derniere matrice, il faut
//		// mettre à jour la capacité restante en prenant en compte le mapping du
//		// engress point
//		DoubleMatrix RemainMatrix = null;
//
//		if (vNodeToMap == IG.columns - 2) {
//			RemainMatrix = RG;
//			double remainCPUengressPoint = RemainMatrix.get(succ_pNode, succ_pNode)
//					- IG.get(vNodeToMap + 1, vNodeToMap + 1);
//			if (remainCPUengressPoint < 0)
//				return false;
//		} else
//			RemainMatrix = D[vNodeToMap + 1][succ_pNode][succ_pNode].matrix;
//
//		double remainCPU = RemainMatrix.get(pNodeToHost, pNodeToHost) - IG.get(vNodeToMap, vNodeToMap);
//
//		double remainBDW = RemainMatrix.get(pNodeToHost, succ_pNode) - IG.get(vNodeToMap, vNodeToMap + 1);
//		if (remainCPU >= 0 && remainBDW >= 0)
//			return true;
//		else
//			return false;
//
//	}
	
	
	private static boolean check_remaining_capa_without_matrix(int vNodeToMap, int pNodeToHost, int succ_pNode, Node[][][] D,
			DoubleMatrix IG, DoubleMatrix RG) {
		// lorsqu'on choisi le minimum de l'avant derniere matrice, il faut
		// mettre à jour la capacité restante en prenant en compte le mapping du
		// engress point
		DoubleMatrix copyRG = new DoubleMatrix();
		copyRG.copy(RG);
		Node success = D[vNodeToMap + 1][succ_pNode][succ_pNode];
		Node prev = null;
		while (success.succ != null) {
			
			if (success.reservedNode.size() != 0) {
				double remain_cpu = copyRG.get(success.reservedNode.get(0).value2, success.reservedNode.get(0).value2)
						- IG.get(success.reservedNode.get(0).value1, success.reservedNode.get(0).value1);
				copyRG.put(success.reservedNode.get(0).value2, success.reservedNode.get(0).value2, remain_cpu);
			}
			
			if (success.reservedLink.size() != 0){
				double remain_bandwidth = copyRG.get(success.reservedLink.get(0).value2.value1, success.reservedLink.get(0).value2.value2)
						- IG.get(success.reservedLink.get(0).value1.value1, success.reservedLink.get(0).value1.value2);
				copyRG.put(success.reservedLink.get(0).value2.value1, success.reservedLink.get(0).value2.value2, remain_bandwidth);
				copyRG.put(success.reservedLink.get(0).value2.value2, success.reservedLink.get(0).value2.value1, remain_bandwidth);
			}
			
			prev = success;
			success = success.succ;
		}
		if (prev != null) {
			double remain_cpu = copyRG.get(prev.reservedNode.get(1).value2, prev.reservedNode.get(1).value2)
					- IG.get(prev.reservedNode.get(1).value1, prev.reservedNode.get(1).value1);
			copyRG.put(prev.reservedNode.get(1).value2, prev.reservedNode.get(1).value2, remain_cpu);
		}
		
		double remainCPU = copyRG.get(pNodeToHost, pNodeToHost) - IG.get(vNodeToMap, vNodeToMap);
		double remainBDW = copyRG.get(pNodeToHost, succ_pNode) - IG.get(vNodeToMap, vNodeToMap + 1);

		if (vNodeToMap == IG.columns - 2) {
			double remainCPUengressPoint = RG.get(succ_pNode, succ_pNode) - IG.get(vNodeToMap + 1, vNodeToMap + 1);
			if (remainCPUengressPoint < 0){
				copyRG = null;
				return false;
			}
		}
		
		
		if (remainCPU >= 0 && remainBDW >= 0){
			copyRG.put(pNodeToHost, pNodeToHost, remainCPU);
			copyRG.put(pNodeToHost, succ_pNode, remainBDW);
			copyRG.put(succ_pNode, pNodeToHost, remainBDW);
			//System.out.println("        Copy RG         ");
			//print_Matrix(copyRG, RG.rows);
			//System.out.println("          RG         ");
			//print_Matrix(RG, RG.rows);
			copyRG = null;
			return true;
		}
		else{
			copyRG = null;
			return false;
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
	
	private static long matching_percentage(int IG_size, int[] Result, DoubleMatrix G, DoubleMatrix H) {
		int nb_mapped_node = 0;
		int nb_mapped_link = 0;
		int nb_total_link = 0;
		for (int i = 0; i < IG_size; i++) {
			if (Result[i] != -1) {
				if (0 <= G.get(Result[i], Result[i]) - H.get(i, i)
						& respect_nodeType(i, Result[i])) {
					nb_mapped_node++;
				}
			}
			for (int j = i + 1; j < IG_size; j++) {
				if (Result[i] != -1 && Result[j] != -1) {
					if ((0 <= (G.get(Result[i], Result[j]) - H.get(i, j))
							|| 0 <= (weightedgraphRG.getWeight(Result[i], Result[j])
									.getBandwidth_shortestPath() - H.get(i, j)))
							&& H.get(i, j) != 0) {
						nb_mapped_link++;
						nb_total_link++;
					} else if (0 > (G.get(Result[i], Result[j]) - H.get(i, j))
							&& H.get(i, j) != 0) {
						System.out.println("virtual Link [" + i + "," + j + "] = " + H.get(i, j) + " mapped on ["
								+ Result[i] + "," + Result[j] + "] = " + G.get(Result[i], Result[j]));
						nb_total_link++;
					}
				} else
					nb_total_link++;
			}
		}
		//System.out.println((nb_mapped_link + nb_mapped_node) / (nb_total_link + IG_size));
		return (nb_mapped_link + nb_mapped_node) / (nb_total_link + IG_size);
	}

	private static String get_diff_G_H(int IG_size, int[] Result, DoubleMatrix G, DoubleMatrix H) {

		int node_diff_H_G = 0;
		int link_diff_H_G = 0;
		boolean solutionmatching = true;
		for (int i = 0; i < IG_size; i++)
			if (Result[i] == -1)
				solutionmatching = false;

		if (solutionmatching) {
			for (int i = 0; i < IG_size; i++) {
				if (0 <= G.get(Result[i], Result[i]) - H.get(i, i)
						& respect_nodeType(i, Result[i])) {
					node_diff_H_G += G.get(Result[i], Result[i]) - H.get(i, i);
				} else
					solutionmatching = false;
				for (int j = i + 1; j < IG_size; j++) {
					if ((0 <= (G.get(Result[i], Result[j]) - H.get(i, j))
							|| 0 <= (weightedgraphRG.getWeight(Result[i], Result[j])
									.getBandwidth_shortestPath() - H.get(i, j)))
							&& H.get(i, j) != 0) {
						if (G.get(Result[i], Result[j]) != 0)
							link_diff_H_G += G.get(Result[i], Result[j]) - H.get(i, j);
						else if (weightedgraphRG.getWeight(Result[i], Result[j]).getBandwidth_shortestPath() != 0)
							link_diff_H_G += weightedgraphRG.getWeight(Result[i], Result[j])
									.getBandwidth_shortestPath() - H.get(i, j);

					} else if (0 > (G.get(Result[i], Result[j]) - H.get(i, j))
							&& H.get(i, j) != 0) {
						System.out.println("virtual Link [" + i + "," + j + "] = " + H.get(i, j) + " mapped on ["
								+ Result[i] + "," + Result[j] + "] = " + G.get(Result[i], Result[j]));
						solutionmatching = false;
					}
				}
			}

		}
		if (solutionmatching)
			return node_diff_H_G + " " + link_diff_H_G;
		else
			return -1 + "";
	}
	
	public static String main(String[] args) throws IOException {
//	public static void main(String[] args) throws IOException {

//		args = new String[4];
//		args[0] = "1000"; // size of substrate graph (RG)
//		args[1] = "20"; // size of request graph/SFC (IG)
//		args[2] = "0"; // index of RG
//		args[3] = "0"; // index of IG

		System.out.println("Usage: java -Xmx20g -jar dp_sfc_*.jar");
		int RG_size = Integer.parseInt(args[0]);
		int IG_size = Integer.parseInt(args[1]);

		DoubleMatrix RG = read_RG_graph("RG", RG_size, Integer.parseInt(args[2]));
		// PrintPhysicalNodes();
		// print_Matrix(RG, RG_size);

		DoubleMatrix IG = read_IG_graph("IG", IG_size, Integer.parseInt(args[3]));
		IG_size = real_size_of_IG;
		// PrintVirtualNodes();
		// print_Matrix(IG, IG_size);

		// Request: Matrix
		// double[][] R = { { 4, 1, 0 }, { 0, 4, 3 }, { 0, 0, 3 } };

		// Substrate graph: Matrix
		// double[][] doubleG = { { 4, 2, 0, 1 }, { 2, 5, 3, 8 }, { 0, 3, 3, 9
		// }, { 1, 8, 9, 7 } };

		// Convert Substrate graph to Matrix of Node
		// Node[][] G = convertMatrix(doubleG);
		Node[][] G = convertMatrix(RG);

		// list of matrices
		Node[][][] D = new Node[IG_size][][];

		long startTime = System.currentTimeMillis();
		int nb_infini = 0;

		// initialize & update D
		for (int i = IG_size - 1; i >= 0; i--) {

			// if (i == IG_size - 1)
			// D[i] = copymatrix(G, true);
			// else
			D[i] = copymatrix(G, false);
			// update Di update Nodes & links
			for (int j = 0; j < RG_size; j++) {

				// Nodes
				if (((D[i][j][j].cost - IG.get(i, i)) >= 0) && respect_nodeType(i, j)) {
					D[i][j][j].setCost(D[i][j][j].cost - IG.get(i, i));
				} else {
					D[i][j][j].setCost(INFINI);
				}

				// Links
				if (i > 0)
					for (int k = 0; k < RG_size; k++) {
						if (k != j) {
							if ((D[i][j][k].cost - IG.get(i - 1, i)) >= 0)
								D[i][j][k].setCost(D[i][j][k].cost - IG.get(i - 1, i));
							else
								D[i][j][k].setCost(INFINI);
						}
					}
			}

			// Compute distance
			// System.out.println(" i *********************: " + i);
			if (i != IG_size - 1) {
				nb_infini = 0;
				for (int j = 0; j < RG_size; j++) {
					// System.out.println(" j *************: " + j);
					double min = INFINI;
					Node Succ = null;
					int index_succ = -1;
					for (int l = 0; l < RG_size; l++) {
						// if ((D[i][j][j].cost != INFINI)) {
						if ((l != j) && (D[i][j][j].cost != INFINI)) {
							if (min > (D[i][j][j].cost + D[i + 1][l][l].cost + D[i + 1][l][j].cost)) {
								//if (check_remaining_capa(i, j, l, D, IG, RG)) {
								if (check_remaining_capa_without_matrix(i, j, l, D, IG, RG)) {
									//check_remaining_capa_without_matrix(i, j, l, D, IG, RG);
									min = D[i][j][j].cost + D[i + 1][l][l].cost + D[i + 1][l][j].cost;
									Succ = D[i + 1][l][l];
									index_succ = l;
								}
							}
						}
					}

					D[i][j][j].setCost(min);
					if (min != INFINI) {
						
						D[i][j][j].setSucc(Succ);
						//D[i][j][j].matrix = updateMatrix4Remaincapa(i, j, index_succ, IG,
						//		D[i + 1][index_succ][index_succ].matrix, RG, D);
						//updateMatrix4Remaincapa(i, j, index_succ, IG, RG, D);
						storeSolution(i, j, index_succ, IG, RG, D);
					} else
						nb_infini++;

				}

				if (nb_infini == RG_size) {
					System.out.println("Problem occured in matrix number: " + i);
					System.out.println("there is no solution since nb_infini is equal to number of SG nodes");
					break;
				}

			}

			// nettoyer le ramining matrix dans la (i+2) matrice de D
//			if (i < IG_size - 3) {
//				for (int j = 0; j < RG_size; j++) {
//					D[i + 2][j][j].matrix = null;
//				}
//			}

		}

		long stopTime = System.currentTimeMillis();
		int[] mappingResult = new int[IG_size];
		if (nb_infini != RG_size) {
			Double minMapping = INFINI;
			int indexMappingResult = -1;
			for (int j = 0; j < RG_size; j++) {
				if (D[0][j][j].cost < minMapping) {
					minMapping = D[0][j][j].cost;
					indexMappingResult = j;
				}

			}

			mappingResult[0] = indexMappingResult;
			int i = 1;
			Node succNode = D[0][indexMappingResult][indexMappingResult];
			//System.out.println("D[0][indexMappingResult][indexMappingResult]*****   "+D[0][indexMappingResult][indexMappingResult]);

			while (succNode.succ != null) {
				mappingResult[i] = succNode.succ.id;
				i++;
				succNode = succNode.succ;
			}

			// printmatrix3D(D);
			System.out.println("Successful Mapping.....!!!!");
			for (int j = 0; j < IG_size; j++) {
				System.out.println("V" + j + " =====> P" + mappingResult[j]);
			}
			
			FileWriter writer = new FileWriter("SolutionMappingDP_no_matrices-instanceRG" + args[0] + "-" + args[2]
					+ "-instanceIG" + args[1] + "-" + args[3]);
			FileWriter wr_Nodes = new FileWriter("DP_no_matrices_nodes_mapping" + args[0] + "-" + args[1], true);

			FileWriter wr_Links = new FileWriter("DP_no_matrices_links_mapping" + args[0] + "-" + args[1], true);

			for (int j = 0; j < IG_size; j++) {
				writer.write(j + " " + mappingResult[j] + "\n");
				wr_Nodes.write(mappingResult[j] + "\t");

			}
			wr_Nodes.write("\n");
			
			for (int j = 0; j < IG_size-1; j++) {
				writer.write(j + " " + (j + 1) + "\n"
						+ mappingResult[j] + " " + mappingResult[j+1] + "\n");
				wr_Links.write(mappingResult[j] + " " + mappingResult[j+1] + "\t");
			}

			wr_Links.write("\n");
			writer.close();
			wr_Nodes.close();
			wr_Links.close();
		}

		long elapsedTime = stopTime - startTime;
		System.out.println("time(s) :" + elapsedTime / 1000 + " s   " + "  time(ms) :" + elapsedTime + " ms");
		// System.out.println("time(micro-s) :" + elapsedTime / 1000 + "
		// micro-seconds " + elapsedTime + " nanos");
		return elapsedTime + " " + matching_percentage(IG_size, mappingResult, RG, IG) + " "
		+ get_diff_G_H(IG_size, mappingResult, RG, IG);
	}

}