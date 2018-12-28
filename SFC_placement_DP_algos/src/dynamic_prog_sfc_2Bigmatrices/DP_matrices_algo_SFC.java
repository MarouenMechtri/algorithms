package dynamic_prog_sfc_2Bigmatrices;

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

import sfc_4_DP.Cost;
import sfc_4_DP.Server;
import sfc_4_DP.WeightedGraph;

public class DP_matrices_algo_SFC {

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

	public static Node[][] copymatrix(Node[][] G) {
		Node[][] clonedMatrix = new Node[G.length][G.length];
		for (int i = 0; i < G.length; i++) {
			for (int j = 0; j < G.length; j++) {
				clonedMatrix[i][j] = new Node(G[i][j].id, G[i][j].cost, G[i][j].succ);
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
			System.out.println("----------------------- " + i);
			printmatrix(D[i]);
			System.out.println("-----------------------");
		}
	}

	public static void printmatrix3D(double[][][] D) {
		for (int i = 0; i < D.length; i++) {
			for (int j = 0; j < D.length; j++) {
				for (int l = 0; l < D.length; l++) {
					System.out.print(D[i][j][l] + "\t");
				}
				System.out.println("||");
			}
			System.out.println("-----------------------");
		}
	}

	public static Node[][] convertMatrix(double[][] matrix) {
		Node[][] G = new Node[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				if (i == j)
					G[i][j] = new Node(i, matrix[i][j], null);
				else
					// there is no ID for links
					G[i][j] = new Node(-1, matrix[i][j], null);
			}
		}
		return G;
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

	public static double[][] read_RG_graph(String type, int size, int index) throws IOException {

		FileInputStream fstream = new FileInputStream("instance" + type + size + "-" + index);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String str;
		boolean N = true;
		boolean L = false;
		br.readLine();
		br.readLine();
		str = br.readLine();
		double[][] matrix = new double[size][size];
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
				matrix[Integer.parseInt(a[0])][Integer.parseInt(a[0])] = Float.parseFloat(a[2]);
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
				matrix[Integer.parseInt(a[0])][Integer.parseInt(a[1])] = Float.parseFloat(a[2]);
				matrix[Integer.parseInt(a[1])][Integer.parseInt(a[0])] = Float.parseFloat(a[2]);
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

	public static double[][] read_IG_graph(String type, int size, int index) throws IOException {

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
		double[][] matrix = new double[IG_size][IG_size];
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
				matrix[Integer.parseInt(a[0])][Integer.parseInt(a[0])] = Float.parseFloat(a[2]);

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
				matrix[Integer.parseInt(a[0])][Integer.parseInt(a[1])] = SFCbw;
				// matrix.put(Integer.parseInt(a[1]), Integer.parseInt(a[0]),
				// SFCbw);
			}

		}
		in.close();

		return matrix;

	}

	private static void copyMatrices(double[][] src, double[][] dst) {
		for (int i = 0; i < src.length; i++) {
			for (int j = 0; j < src.length; j++) {
				dst[i][j] = src[i][j];
			}
		}
	}

	private static void updateMatrix4Remaincapa(int vNodeToMap, int pNodeToHost, int succ_pNode, double[][] IG,
			double[][] RG, double[][][] remain_Matrices_0, double[][][] remain_Matrices_1, int indexMatrix_toUpdate) {

		// lorsqu'on choisi le minimum de l'avant derniere matrice, il faut
		// mettre à jour la capacité restante en prenant en compte le mapping du
		// engress point
		if (vNodeToMap == IG.length - 2) {
			if (indexMatrix_toUpdate == 0) {
				copyMatrices(remain_Matrices_1[succ_pNode], remain_Matrices_0[pNodeToHost]);
				remain_Matrices_0[succ_pNode][succ_pNode][succ_pNode] = remain_Matrices_0[succ_pNode][succ_pNode][succ_pNode]
						- IG[vNodeToMap + 1][vNodeToMap + 1];
			} else {
				copyMatrices(remain_Matrices_0[succ_pNode], remain_Matrices_1[pNodeToHost]);
				remain_Matrices_1[succ_pNode][succ_pNode][succ_pNode] = remain_Matrices_1[succ_pNode][succ_pNode][succ_pNode]
						- IG[vNodeToMap + 1][vNodeToMap + 1];
			}

		} else {
			if (indexMatrix_toUpdate == 0) {
				copyMatrices(remain_Matrices_1[succ_pNode], remain_Matrices_0[pNodeToHost]);
			} else {
				copyMatrices(remain_Matrices_0[succ_pNode], remain_Matrices_1[pNodeToHost]);
			}
		}

		if (indexMatrix_toUpdate == 0) {
			double remainCPU = remain_Matrices_0[pNodeToHost][pNodeToHost][pNodeToHost] - IG[vNodeToMap][vNodeToMap];
			double remainBDW = remain_Matrices_0[pNodeToHost][pNodeToHost][succ_pNode] - IG[vNodeToMap][vNodeToMap + 1];

			if (remainCPU >= 0 && remainBDW >= 0) {
				remain_Matrices_0[pNodeToHost][pNodeToHost][pNodeToHost] = remainCPU;
				remain_Matrices_0[pNodeToHost][pNodeToHost][succ_pNode] = remainBDW;
				remain_Matrices_0[pNodeToHost][succ_pNode][pNodeToHost] = remainBDW;
			}

		} else {
			double remainCPU = remain_Matrices_1[pNodeToHost][pNodeToHost][pNodeToHost] - IG[vNodeToMap][vNodeToMap];
			double remainBDW = remain_Matrices_1[pNodeToHost][pNodeToHost][succ_pNode] - IG[vNodeToMap][vNodeToMap + 1];

			if (remainCPU >= 0 && remainBDW >= 0) {
				remain_Matrices_1[pNodeToHost][pNodeToHost][pNodeToHost] = remainCPU;
				remain_Matrices_1[pNodeToHost][pNodeToHost][succ_pNode] = remainBDW;
				remain_Matrices_1[pNodeToHost][succ_pNode][pNodeToHost] = remainBDW;
			}
		}
	}

	private static boolean check_remaining_capa(int vNodeToMap, int pNodeToHost, int succ_pNode, double[][] IG,
			double[][] RG, double[][][] remain_Matrices_0, double[][][] remain_Matrices_1, int indexMatrix_toUpdate) {
		// lorsqu'on choisi le minimum de l'avant derniere matrice, il faut
		// mettre à jour la capacité restante en prenant en compte le mapping du
		// engress point
		double[][] RemainMatrix = null;

		if (vNodeToMap == IG.length - 2) {

			if (indexMatrix_toUpdate == 0)
				RemainMatrix = remain_Matrices_1[succ_pNode];
			else
				RemainMatrix = remain_Matrices_0[succ_pNode];

			double remainCPUengressPoint = RemainMatrix[succ_pNode][succ_pNode] - IG[vNodeToMap + 1][vNodeToMap + 1];
			if (remainCPUengressPoint < 0)
				return false;
		} else {
			if (indexMatrix_toUpdate == 0)
				RemainMatrix = remain_Matrices_1[succ_pNode];
			else
				RemainMatrix = remain_Matrices_0[succ_pNode];
		}
		double remainCPU = RemainMatrix[pNodeToHost][pNodeToHost] - IG[vNodeToMap][vNodeToMap];

		double remainBDW = RemainMatrix[pNodeToHost][succ_pNode] - IG[vNodeToMap][vNodeToMap + 1];
		if (remainCPU >= 0 && remainBDW >= 0)
			return true;
		else
			return false;

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
	
	private static long matching_percentage(int IG_size, int[] Result, double[][] G, double[][] H) {
		int nb_mapped_node = 0;
		int nb_mapped_link = 0;
		int nb_total_link = 0;
		for (int i = 0; i < IG_size; i++) {
			if (Result[i] != -1) {
				if (0 <= G[Result[i]][Result[i]] - H[i][i]
						& respect_nodeType(i, Result[i])) {
					nb_mapped_node++;
				}
			}
			for (int j = i + 1; j < IG_size; j++) {
				if (Result[i] != -1 && Result[j] != -1) {
					if ((0 <= (G[Result[i]][Result[j]] - H[i][j])
							|| 0 <= (weightedgraphRG.getWeight(Result[i], Result[j])
									.getBandwidth_shortestPath() - H[i][j]))
							&& H[i][j] != 0) {
						nb_mapped_link++;
						nb_total_link++;
					} else if (0 > (G[Result[i]][Result[j]] - H[i][j])
							&& H[i][j] != 0) {
						System.out.println("virtual Link [" + i + "," + j + "] = " + H[i][j] + " mapped on ["
								+ Result[i] + "," + Result[j] + "] = " + G[Result[i]][Result[j]]);
						nb_total_link++;
					}
				} else
					nb_total_link++;
			}
		}
		//System.out.println((nb_mapped_link + nb_mapped_node) / (nb_total_link + IG_size));
		return (nb_mapped_link + nb_mapped_node) / (nb_total_link + IG_size);
	}

	private static String get_diff_G_H(int IG_size, int[] Result, double[][] G, double[][] H) {

		int node_diff_H_G = 0;
		int link_diff_H_G = 0;
		boolean solutionmatching = true;
		for (int i = 0; i < IG_size; i++)
			if (Result[i] == -1)
				solutionmatching = false;

		if (solutionmatching) {
			for (int i = 0; i < IG_size; i++) {
				if (0 <= G[Result[i]][Result[i]] - H[i][i]
						& respect_nodeType(i, Result[i])) {
					node_diff_H_G += G[Result[i]][Result[i]] - H[i][i];
				} else
					solutionmatching = false;
				for (int j = i + 1; j < IG_size; j++) {
					if ((0 <= (G[Result[i]][Result[j]] - H[i][j])
							|| 0 <= (weightedgraphRG.getWeight(Result[i], Result[j])
									.getBandwidth_shortestPath() - H[i][j]))
							&& H[i][j] != 0) {
						if (G[Result[i]][Result[j]] != 0)
							link_diff_H_G += G[Result[i]][Result[j]] - H[i][j];
						else if (weightedgraphRG.getWeight(Result[i], Result[j]).getBandwidth_shortestPath() != 0)
							link_diff_H_G += weightedgraphRG.getWeight(Result[i], Result[j])
									.getBandwidth_shortestPath() - H[i][j];

					} else if (0 > (G[Result[i]][Result[j]] - H[i][j])
							&& H[i][j] != 0) {
						System.out.println("virtual Link [" + i + "," + j + "] = " + H[i][j] + " mapped on ["
								+ Result[i] + "," + Result[j] + "] = " + G[Result[i]][Result[j]]);
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
//
//		args = new String[4];
//		args[0] = "1000"; // size of substrate graph (RG)
//		args[1] = "20"; // size of request graph/SFC (IG)
//		args[2] = "0"; // index of RG
//		args[3] = "0"; // index of IG	
//		System.out.println("Usage: java -Xmx20g -jar sfc_dp_algo.jar");
		
		int RG_size = Integer.parseInt(args[0]);
		int IG_size = Integer.parseInt(args[1]);

		// read substrate graph
		double[][] RG = read_RG_graph("RG", RG_size, Integer.parseInt(args[2]));
		// PrintPhysicalNodes();
		// print_Matrix(RG, RG_size);

		// read request graph
		double[][] IG = read_IG_graph("IG", IG_size, Integer.parseInt(args[3]));
		IG_size = real_size_of_IG;
		// PrintVirtualNodes();
		// print_Matrix(IG, IG_size);

		// convert substrate graph matrix to a matrix of node (we need to keep
		// the best success node)
		Node[][] G = convertMatrix(RG);

		// list of matrices
		Node[][][] D = new Node[IG_size][][];

		// these matrices store all remaining capacities when mapping a virtual
		// node and link
		double[][][] remain_Matrices_0 = new double[RG_size][RG_size][RG_size];
		double[][][] remain_Matrices_1 = new double[RG_size][RG_size][RG_size];

		long startTime = System.currentTimeMillis();
		int nb_infini = 0;

		// initialize & update D
		for (int i = IG_size - 1; i >= 0; i--) {

			// store the elements of RG matrix in the i'th matrix in D
			D[i] = copymatrix(G);

			// update nodes and links of D[i] with the remaining capacities when
			// mapping i'th node of IG and the associated link
			for (int j = 0; j < RG_size; j++) {

				// Nodes
				//if ((D[i][j][j].cost - IG[i][i]) >= 0) {
				if (((D[i][j][j].cost - IG[i][i]) >= 0) && respect_nodeType(i, j)) {
					D[i][j][j].setCost(D[i][j][j].cost - IG[i][i]);
				} else {
					D[i][j][j].setCost(INFINI);
				}

				// Links
				if (i > 0)
					for (int k = 0; k < RG_size; k++) {
						if (k != j) {
							if ((D[i][j][k].cost - IG[i - 1][i]) >= 0)
								D[i][j][k].setCost(D[i][j][k].cost - IG[i - 1][i]);
							else
								D[i][j][k].setCost(INFINI);
						}
					}
			}

			// Compute distance
			// System.out.println(" i *********************: " + i);

			if (i == IG_size - 1) {
				if (i % 2 == 0) {
					for (int j = 0; j < remain_Matrices_0.length; j++) {
						copyMatrices(RG, remain_Matrices_0[j]);
					}
				} else
					for (int j = 0; j < remain_Matrices_1.length; j++) {
						copyMatrices(RG, remain_Matrices_1[j]);
					}

			} else {
				nb_infini = 0;
				for (int j = 0; j < RG_size; j++) {
					// System.out.println(" j *************: " + j);
					double min = INFINI;
					Node Succ = null;
					int index_succ = -1;
					for (int l = 0; l < RG_size; l++) {
						if ((l != j) && (D[i][j][j].cost != INFINI)) {
							if (min > (D[i][j][j].cost + D[i + 1][l][l].cost + D[i + 1][l][j].cost)) {
								if (check_remaining_capa(i, j, l, IG, RG, remain_Matrices_0, remain_Matrices_1,
										i % 2)) {
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
						updateMatrix4Remaincapa(i, j, index_succ, IG, RG, remain_Matrices_0, remain_Matrices_1, i % 2);

					} else
						nb_infini++;

				}

				if (nb_infini == RG_size) {
					System.out.println("Problem occured in matrix number: " + i);
					System.out.println("there is no solution since nb_infini is equal to number of SG nodes");
					break;
				}

			}

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
			//System.out.println("minMapping: " + minMapping);
			mappingResult[0] = indexMappingResult;
			int i = 1;
			Node succNode = D[0][indexMappingResult][indexMappingResult];
			//System.out.println("D[0][indexMappingResult][indexMappingResult]*****"+D[0][indexMappingResult][indexMappingResult]);

			while (succNode.succ != null) {
				mappingResult[i] = succNode.succ.id;
				i++;
				succNode = succNode.succ;
			}

			printmatrix3D(D);
			System.out.println("Successful Mapping.....!!!!");
			for (int j = 0; j < IG_size; j++) {
				System.out.println("V" + j + " =====> P" + mappingResult[j]);
			}

			FileWriter writer = new FileWriter("SolutionMappingDP_matrices-instanceRG" + args[0] + "-" + args[2]
					+ "-instanceIG" + args[1] + "-" + args[3]);
			FileWriter wr_Nodes = new FileWriter("DP_matrices_nodes_mapping" + args[0] + "-" + args[1], true);

			FileWriter wr_Links = new FileWriter("DP_matrices_links_mapping" + args[0] + "-" + args[1], true);

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