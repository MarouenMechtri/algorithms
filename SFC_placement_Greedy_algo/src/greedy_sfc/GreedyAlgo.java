package greedy_sfc;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jblas.DoubleMatrix;

import BellmanFord.BellmanFordSP;
import BellmanFord.DirectedEdge;
import BellmanFord.EdgeWeightedDigraph;
import BellmanFord.StdOut;

import sfc.Cost;
import sfc.Server;
import sfc.WeightedGraph;

public class GreedyAlgo {

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

	public static void print_non_symetric_Matrix(DoubleMatrix matrix, int rwo_size, int column_size) {
		System.out.println("-----------------------------------------------------------------");
		for (int i = 0; i < rwo_size; i++) {
			for (int j = 0; j < column_size; j++) {
				System.out.print(String.format("%.2f \t", matrix.get(i, j)));
			}
			System.out.println();
		}
		System.out.println("-----------------------------------------------------------------");
	}

	public static void PrintSolution(int IG_size, int[][] Result) {

		System.out.println("result= **************************");

		// for (int row = 0; row < Result.length; row++) {
		// for (int column = 0; column < Result[row].length; column++)
		// System.out.print(Result[row][column] + " ");
		// System.out.println();
		// }
		// System.out.println("**************************");

		for (int i = 0; i < IG_size; i++) {
			if (Result[i][1] == -1)
				System.out.println("h" + (Result[i][0]) + " is not mapped");
			else
				System.out.println("h" + (Result[i][0]) + " is mapped to g" + (Result[i][1]));
		}
		/*********************************************************************************************/
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

	public static void print_table(int[][] matrixDistance, int L, int H) {
		for (int i = 0; i < L; i++) {
			for (int j = 0; j < H; j++) {
				System.out.print(matrixDistance[i][j] + "\t");
			}
			System.out.println();
		}
	}

	public static void print_tab(float[] tab) {
		for (int i = 0; i < tab.length; i++) {
			System.out.print(tab[i] + "\t");
		}
		System.out.println();
	}

	public static void save_Matrix(DoubleMatrix matrix, String type, int size, int index) throws IOException {

		FileWriter writer = new FileWriter("instance" + type + size + "-" + index);

		writer.write("Number of Nodes, Number of Servers\n");
		writer.write(size + " " + size + "\n");
		writer.write("Nodes\n");
		for (int i = 0; i < size; i++) {
			if (type.equals("RG"))
				writer.write(i + " S:" + i + " " + (int) matrix.get(i, i) + "\n");
			else
				writer.write(i + " " + i + " " + (int) matrix.get(i, i) + "\n");
		}
		writer.write("EDGES\n");
		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				if (matrix.get(i, j) != 0)
					writer.write(i + " " + j + " " + (int) matrix.get(i, j) + "\n");
			}
		}
		if (type.contains("IG")) {
			writer.write("VMs in same server \n");
			writer.write("VMs in different server \n");
			for (int i = 0; i < size; i++) {
				for (int j = i; j < size; j++) {
					if (i != j) {
						writer.write(i + " " + j + "\n");
					}
				}
			}
		}
		writer.close();
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



	public static DoubleMatrix reduceCapaNode(DoubleMatrix matrix, int row, int column, double value) {
		matrix.put(row, column, matrix.get(row, column) - value);
		return matrix;
	}

	public static boolean checkremainingCapaNode(DoubleMatrix matrix, int row, int column, double value) {
		if (matrix.get(row, column) - value >= threshold_consolidation) {
			return true;
		} else
			return false;
	}

	public static DoubleMatrix reduceCapaPath(DoubleMatrix matrix, int row, int column, double value) {

		int[] path = weightedgraphRG.getWeight(row, column).getPathtab();

		for (int i = 0; i < path.length - 1; i++) {
			matrix.put(path[i], path[i + 1], matrix.get(path[i], path[i + 1]) - value);
			matrix.put(path[i + 1], path[i], matrix.get(path[i + 1], path[i]) - value);
		}
		return matrix;
	}

	public static boolean checkremainingCapaPath(DoubleMatrix matrix, int row, int column, double value) {
		boolean check = true;

		int[] path = weightedgraphRG.getWeight(row, column).getPathtab();

		for (int i = 0; i < path.length - 1; i++) {
			if (matrix.get(path[i], path[i + 1]) - value < threshold_consolidation) {
				return false;
			}
		}
		return check;
	}


	private static boolean check_matching(int IG_size, int[][] Result, DoubleMatrix G, DoubleMatrix H) {

		boolean solutionmatching = true;
		for (int i = 0; i < IG_size; i++)
			if (Result[i][1] == -1)
				solutionmatching = false;

		if (solutionmatching) {
			int nb_mapped_node = 0;
			int nb_mapped_link = 0;
			int nb_total_link = 0;
			for (int i = 0; i < IG_size; i++) {
				if (threshold_consolidation <= G.get(Result[i][1], Result[i][1]) - H.get(i, i)
						& respect_nodeType(i, Result[i][1], G.getColumns())) {
					nb_mapped_node++;
				} else
					solutionmatching = false;
				for (int j = i + 1; j < IG_size; j++) {
					if ((threshold_consolidation <= (G.get(Result[i][1], Result[j][1]) - H.get(i, j))
							|| threshold_consolidation <= (weightedgraphRG.getWeight(Result[i][1], Result[j][1])
									.getBandwidth_shortestPath() - H.get(i, j)))
							&& H.get(i, j) != 0) {
						System.out.print("Virtual Link [" + i + "," + j + "] is mapped on ");
						weightedgraphRG.printPath(weightedgraphRG.getWeight(Result[i][1], Result[j][1]).getPath());
						nb_mapped_link++;
						nb_total_link++;
					} else if (threshold_consolidation > (G.get(Result[i][1], Result[j][1]) - H.get(i, j))
							&& H.get(i, j) != 0) {
						System.out.println("virtual Link [" + i + "," + j + "] = " + H.get(i, j) + " mapped on ["
								+ Result[i][1] + "," + Result[j][1] + "] = " + G.get(Result[i][1], Result[j][1]));
						solutionmatching = false;
						nb_total_link++;
					}
				}
			}

			System.out.println("Number of mapped request nodes: " + nb_mapped_node);
			System.out.println("Number of mapped request links: " + nb_mapped_link);
		}
		if (solutionmatching)
			System.out.println("***********check***************: \nMapping with sucess Yeeeeeeeeeeep :)");
		else
			System.out.println("************check**************: Noooo Mapping found :(");

		return solutionmatching;
	}

	private static long matching_percentage(int IG_size, int[][] Result, DoubleMatrix G, DoubleMatrix H) {
		int nb_mapped_node = 0;
		int nb_mapped_link = 0;
		int nb_total_link = 0;
		for (int i = 0; i < IG_size; i++) {
			if (Result[i][1] != -1) {
				if (threshold_consolidation <= G.get(Result[i][1], Result[i][1]) - H.get(i, i)
						& respect_nodeType(i, Result[i][1], G.getColumns())) {
					nb_mapped_node++;
				}
			}
			for (int j = i + 1; j < IG_size; j++) {
				if (Result[i][1] != -1 && Result[j][1] != -1) {
					if ((threshold_consolidation <= (G.get(Result[i][1], Result[j][1]) - H.get(i, j))
							|| threshold_consolidation <= (weightedgraphRG.getWeight(Result[i][1], Result[j][1])
									.getBandwidth_shortestPath() - H.get(i, j)))
							&& H.get(i, j) != 0) {
						nb_mapped_link++;
						nb_total_link++;
					} else if (threshold_consolidation > (G.get(Result[i][1], Result[j][1]) - H.get(i, j))
							&& H.get(i, j) != 0) {
						System.out.println("virtual Link [" + i + "," + j + "] = " + H.get(i, j) + " mapped on ["
								+ Result[i][1] + "," + Result[j][1] + "] = " + G.get(Result[i][1], Result[j][1]));
						nb_total_link++;
					}
				} else
					nb_total_link++;
			}
		}
		//System.out.println((nb_mapped_link + nb_mapped_node) / (nb_total_link + IG_size));
		return (nb_mapped_link + nb_mapped_node) / (nb_total_link + IG_size);
	}

	private static String get_diff_G_H(int IG_size, int[][] Result, DoubleMatrix G, DoubleMatrix H) {

		int node_diff_H_G = 0;
		int link_diff_H_G = 0;
		boolean solutionmatching = true;
		for (int i = 0; i < IG_size; i++)
			if (Result[i][1] == -1)
				solutionmatching = false;

		if (solutionmatching) {
			for (int i = 0; i < IG_size; i++) {
				if (threshold_consolidation <= G.get(Result[i][1], Result[i][1]) - H.get(i, i)
						& respect_nodeType(i, Result[i][1], G.getColumns())) {
					node_diff_H_G += G.get(Result[i][1], Result[i][1]) - H.get(i, i);
				} else
					solutionmatching = false;
				for (int j = i + 1; j < IG_size; j++) {
					if ((threshold_consolidation <= (G.get(Result[i][1], Result[j][1]) - H.get(i, j))
							|| threshold_consolidation <= (weightedgraphRG.getWeight(Result[i][1], Result[j][1])
									.getBandwidth_shortestPath() - H.get(i, j)))
							&& H.get(i, j) != 0) {
						if (G.get(Result[i][1], Result[j][1]) != 0)
							link_diff_H_G += G.get(Result[i][1], Result[j][1]) - H.get(i, j);
						else if (weightedgraphRG.getWeight(Result[i][1], Result[j][1]).getBandwidth_shortestPath() != 0)
							link_diff_H_G += weightedgraphRG.getWeight(Result[i][1], Result[j][1])
									.getBandwidth_shortestPath() - H.get(i, j);

					} else if (threshold_consolidation > (G.get(Result[i][1], Result[j][1]) - H.get(i, j))
							&& H.get(i, j) != 0) {
						System.out.println("virtual Link [" + i + "," + j + "] = " + H.get(i, j) + " mapped on ["
								+ Result[i][1] + "," + Result[j][1] + "] = " + G.get(Result[i][1], Result[j][1]));
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

	static boolean respect_nodeType(int indexIG, int indexRG, int RG_size) {

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

	public static <Integer, Server> Integer getKeyByValue(HashMap<Integer, Server> u, Server server) {

		for (Entry<Integer, Server> entry : u.entrySet()) {
			if (server.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
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

	/****************************
	 * Couplage maximal à distance minimal
	 **********************************************/
	// cette fonction permet de calculer le matching maximale de distance
	public static ListMappingNode maxMatchingMinDistance(int[][] matrixDistance, int indexIG, int indexRG,
			ArrayList<Server> IGnode, ArrayList<Server> RGnode) {

		ArrayList<MappingNode> Match1 = new ArrayList<MappingNode>();
		ArrayList<MappingNode> Match2 = new ArrayList<MappingNode>();
		ArrayList<MappingNode> path = new ArrayList<MappingNode>();
		HashMap<Integer, Server> U = new HashMap<Integer, Server>();
		HashMap<Integer, Server> W = new HashMap<Integer, Server>();
		HashMap<Integer, Server> Um = new HashMap<Integer, Server>();
		HashMap<Integer, Server> Wm = new HashMap<Integer, Server>();
		HashMap<Integer, Server> Umbarre = new HashMap<Integer, Server>();
		HashMap<Integer, Server> Wmbarre = new HashMap<Integer, Server>();

		// initialiser Match1 par l'arc qui contient le cout minimale
		Match1.add(new MappingNode(IGnode.get(indexIG), RGnode.get(indexRG)));

		// initilaliser Um par la liste des noeuds de IG qui n'appartiennent pas
		// à
		// Match1
		for (int i = 0; i < IGnode.size(); i++) {
			if (Match1.get(0).getIG() != IGnode.get(i)) {
				Um.put(i, IGnode.get(i));
			} else {
				Umbarre.put(i, IGnode.get(i));
			}
			U.put(i, IGnode.get(i));
		}

		// initilaliser Wm par la liste des noeuds de RG qui n'appartiennent pas
		// à
		// Match1
		for (int i = 0; i < RGnode.size(); i++) {
			if (Match1.get(0).getRG() != RGnode.get(i)) {
				Wm.put(i, RGnode.get(i));
			} else {
				Wmbarre.put(i, RGnode.get(i));
			}
			W.put(i, RGnode.get(i));
		}

		// Répeter tant qu'il y a des noeuds de U qui ne sont pas encore coupler
		while (Um.size() > 0) {
			// changer les signes des arcs Match1 en négative
			for (MappingNode p : Match1) {
				if (matrixDistance[getKeyByValue(U, (Server) p.getIG())][getKeyByValue(W, (Server) p.getRG())] > 0) {
					matrixDistance[getKeyByValue(U, (Server) p.getIG())][getKeyByValue(W,
							(Server) p.getRG())] = -matrixDistance[getKeyByValue(U, (Server) p.getIG())][getKeyByValue(
									W, (Server) p.getRG())];
				}
			}

			path = bellman_Distance(matrixDistance, Um, Wm, U, W, Match1);
			Match2 = path;

			// calculer delta=Match1 union path - Match1 inter path
			for (MappingNode p : Match1) {
				boolean find = false;
				for (int k = 0; k < Match2.size(); k++) {
					if (Match2.get(k).getIG() == p.getIG() && Match2.get(k).getRG() == p.getRG()) {
						Match2.remove(Match2.get(k));
						find = true;
					}
				}
				if (!find) {
					Match2.add(p);
				}

			}

			// remplir Umbarre et Wmbarre par les noeuds qui sont dans les arcs
			// des ARGs du delta
			Umbarre.clear();
			Wmbarre.clear();
			for (MappingNode p : Match2) {
				Umbarre.put(getKeyByValue(U, (Server) p.getIG()), (Server) p.getIG());
				Wmbarre.put(getKeyByValue(W, (Server) p.getRG()), (Server) p.getRG());
			}

			// remplir Um par les noeuds qui ne sont pas dans les arcs des
			// noeuds de IG
			// du delta
			Um.clear();
			Wm.clear();
			for (Integer p : U.keySet()) {
				if (!Umbarre.containsKey(p)) {
					Um.put(p, U.get(p));
				}
			}
			// remplir Wm par les noeuds qui ne sont pas dans les arcs des
			// noeuds de RG
			// du delta
			for (Integer p : W.keySet()) {
				if (!Wmbarre.containsKey(p)) {
					Wm.put(p, W.get(p));
				}
			}
			// changer les signes des arcs qui sont dans Match1 et ne sont pas
			// dans Match2
			for (MappingNode p : Match1) {
				if (!Match2.contains(p)) {
					matrixDistance[getKeyByValue(U, (Server) p.getIG())][getKeyByValue(W,
							(Server) p.getRG())] = -matrixDistance[getKeyByValue(U, (Server) p.getIG())][getKeyByValue(
									W, (Server) p.getRG())];
				}
			}

			Match1 = Match2;
		}
		ListMappingNode mapp = new ListMappingNode();
		int distanceMapping = 0;
		for (MappingNode p : Match1) {
			mapp.addListMapping(new MappingNode(p.getIG(), p.getRG()));
			if (Math.abs(matrixDistance[getKeyByValue(U, (Server) p.getIG())][getKeyByValue(W, (Server) p.getRG())])==Integer.MAX_VALUE){
				distanceMapping=-1;
				break;
			}
						
			distanceMapping = distanceMapping + Math
					.abs(matrixDistance[getKeyByValue(U, (Server) p.getIG())][getKeyByValue(W, (Server) p.getRG())]);
		}

		mapp.setCostMatching(distanceMapping);
		return mapp;
	}

	/***********************************************************************************************************/
	/******************************
	 * Bellman Ford pour le matching des feuilles
	 *********************************/
	public static ArrayList<MappingNode> bellman_Distance(int[][] matrixDistance, HashMap<Integer, Server> Um,
			HashMap<Integer, Server> Wm, HashMap<Integer, Server> U, HashMap<Integer, Server> W,
			ArrayList<MappingNode> Match1) {

		int sizefeuille_IG = U.size();
		int sizefeuille_RG = W.size();
		EdgeWeightedDigraph G = new EdgeWeightedDigraph(sizefeuille_IG + sizefeuille_RG);

		// ajouter les liens entre les ARGs Inputs et les ARGs de références
		for (int i = 0; i < sizefeuille_IG; i++) {
			for (int j = 0; j < sizefeuille_RG; j++) {
				boolean affected = false;
				if (matrixDistance[i][j] == Integer.MAX_VALUE)
					G.addEdge(new DirectedEdge(i, j + sizefeuille_IG, Double.POSITIVE_INFINITY));
				else

					affected = false;
				for (MappingNode p : Match1) {
					if (getKeyByValue(U, (Server) p.getIG()) == i && getKeyByValue(W, (Server) p.getRG()) == j) {
						G.addEdge(new DirectedEdge(j + sizefeuille_IG, i, matrixDistance[i][j]));
						affected = true;
					}
				}
				if (!affected) {
					G.addEdge(new DirectedEdge(i, j + sizefeuille_IG, matrixDistance[i][j]));
				}
			}
		}
		// sauvgarder le plus cours chemin
		BellmanFordSP minsp = null;
		double minDistSP = Double.POSITIVE_INFINITY;
		int destination = -1;

		for (int p : Um.keySet()) {
			int source = p;
			BellmanFordSP sp = new BellmanFordSP(G, source);

			// print negative cycle
			if (sp.hasNegativeCycle()) {
				for (DirectedEdge e : sp.negativeCycle())
					StdOut.println(e);
			}
			// print shortest paths
			else {
				for (int v = sizefeuille_IG; v < G.V(); v++) {
					if (sp.hasPathTo(v) && sp.distTo(v) < minDistSP && Wm.containsKey(v - sizefeuille_IG)) {
						minDistSP = sp.distTo(v);
						minsp = sp;
						destination = v;

					}
				}
			}
		}

		ArrayList<MappingNode> path = new ArrayList<MappingNode>();
		// Il existe un chemin
		if (minsp != null && destination != -1) {
			for (DirectedEdge e : minsp.pathTo(destination)) {

				// l'arc est dans la bonne orientation c-à-d de U vers W
				if (U.containsKey(e.from())) {
					path.add(new MappingNode(U.get(e.from()), W.get(e.to() - sizefeuille_IG)));
				}
				// l'arc est orienté de W vers U
				else {
					path.add(new MappingNode(U.get(e.to()), W.get(e.from() - sizefeuille_IG)));
				}
				// StdOut.println("*******************\n" + e.from() + " "
				// + (e.to() - sizeARGs_IG) + " " + e.weight());
			}
		}
		return path;
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

	// Comparator
	public static class CompServerIndex implements Comparator<MappingNode> {
		@Override
		public int compare(MappingNode arg0, MappingNode arg1) {
			return arg0.getIG().getIndex() - arg1.getIG().getIndex();
		}
	}

	public static String main(String[] args) throws IOException {

//	public static void main(String[] args) throws IOException {
//		args = new String[4];
//		args[0] = "10"; // size of substrate graph (RG)
//		args[1] = "4";  // size of request graph/SFC (IG)
//		args[2] = "0";  // index of RG
//		args[3] = "0";  // index of IG

		int RG_size = Integer.parseInt(args[0]);
		int IG_size = Integer.parseInt(args[1]);

		DoubleMatrix G = read_RG_graph("RG", RG_size, Integer.parseInt(args[2]));
		// PrintPhysicalNodes();

		DoubleMatrix H = read_IG_graph("IG", IG_size, Integer.parseInt(args[3]));
		IG_size = real_size_of_IG;
		// PrintVirtualNodes();

		long startTime = System.currentTimeMillis();

		ArrayList<Server> IGnode = new ArrayList<Server>();
		ArrayList<Server> RGnode = new ArrayList<Server>();

		int[][] bipartite_graph = new int[IG_size][RG_size];

		for (int j = 0; j < RG_size; j++) {
			RGnode.add(Pnodes.get(j));
		}

		for (int i = 0; i < IG_size; i++) {
			IGnode.add(Vnodes.get(i));
			for (int j = 0; j < RG_size; j++) {
				if (checkremainingCapaNode(G, j, j, H.get(i, i))) {
					bipartite_graph[i][j] = (int) (G.get(j, j) - H.get(i, i));
				} else{
					bipartite_graph[i][j] = Integer.MAX_VALUE;
				}
			}
		}

		// print_table(bipartite_graph, IG_size, RG_size);

		boolean Node_Mapping = false;
		boolean Request_Mapping = false;
		int mindistance, minindexIG, minindexRG;
		ListMappingNode bipartite_mapping = new ListMappingNode();

		while (!Request_Mapping) {
			
			// First stage: node mapping
			// - find index of the minimum value in the matrixDistance
			// - compute the maximum matching with the minimum distance
			// - check if the node mapping of IG satisfy the remaining capacity
			// and the node type
			// * if OK map link
			// * if not OK compute new mapping of nodes
			while (!Node_Mapping) {

				
				minindexIG = 0;
				minindexRG = 0;
				mindistance = bipartite_graph[minindexIG][minindexRG];
				for (int i = 0; i < IG_size; i++) {
					for (int j = 0; j < RG_size; j++) {
						if (bipartite_graph[i][j] < mindistance) {
							minindexIG = i;
							minindexRG = j;
							mindistance = bipartite_graph[i][j];
						}
					}
				}

				bipartite_mapping = maxMatchingMinDistance(bipartite_graph, minindexIG, minindexRG, IGnode, RGnode);
				Node_Mapping = true;
				for (MappingNode m : bipartite_mapping.getListMapping()) {
					if (!respect_nodeType(m.getIG().getIndex(), m.getRG().getIndex(), RG_size)
							|| !checkremainingCapaNode(G, m.getRG().getIndex(), m.getRG().getIndex(),
									m.getIG().getCpu())) {
						bipartite_graph[m.getIG().getIndex()][m.getRG().getIndex()] = Integer.MAX_VALUE;
						Node_Mapping = false;
					} else
						bipartite_graph[m.getIG().getIndex()][m.getRG().getIndex()] = Math
								.abs(bipartite_graph[m.getIG().getIndex()][m.getRG().getIndex()]);
				}

				
				if (bipartite_mapping.getCostMatching() < 0)
					break;
			}

			if (bipartite_mapping.getCostMatching() < 0)
				break;

			if (Node_Mapping) {
				Collections.sort(bipartite_mapping.getListMapping(), new CompServerIndex());

				
				Request_Mapping = true;
				DoubleMatrix AdjIGmatrixchecklink = copymatrix(H);
				DoubleMatrix AdjRGmatrixchecklink = copymatrix(G);
				// print_Matrix(AdjRGmatrixchecklink, RG_size);

				for (int i = 0; i < IG_size; i++) {
					for (int j = i + 1; j < IG_size; j++) {
						if (AdjIGmatrixchecklink.get(i, j) != 0) {
							if (weightedgraphRG.costshortestPath(weightedgraphRG,
									bipartite_mapping.getListMapping().get(i).getRG().getIndex(),
									bipartite_mapping.getListMapping().get(j).getRG().getIndex())
									- H.get(i, j) >= threshold_consolidation) {
								// weightedgraphRG.printPath(weightedgraphRG.shortestPath(weightedgraphRG,
								// bipartite_mapping.getListMapping().get(i).getRG().getIndex(),
								// bipartite_mapping.getListMapping().get(j).getRG().getIndex()));
								AdjIGmatrixchecklink.put(i, j, 0);
								ArrayList<Server> Path = new ArrayList<>();
								HashMap<Integer, ArrayList<Server>> result = weightedgraphRG.shortestPath_and_cost(
										weightedgraphRG, bipartite_mapping.getListMapping().get(i).getRG().getIndex(),
										bipartite_mapping.getListMapping().get(j).getRG().getIndex());
								// Path =
								// weightedgraphRG.shortestPath(weightedgraphRG,
								// bipartite_mapping.getListMapping().get(i).getRG().getIndex(),
								// bipartite_mapping.getListMapping().get(j).getRG().getIndex());
								Path = (ArrayList<sfc.Server>) result.values().toArray()[0];
								weightedgraphRG
										.getWeight(bipartite_mapping.getListMapping().get(i).getRG().getIndex(),
												bipartite_mapping.getListMapping().get(j).getRG().getIndex())
										.setPath(Path);
								weightedgraphRG
										.getWeight(bipartite_mapping.getListMapping().get(i).getRG().getIndex(),
												bipartite_mapping.getListMapping().get(j).getRG().getIndex())
										.setBandwidth_shortestPath((int) result.keySet().toArray()[0]);
								AdjRGmatrixchecklink = reduceCapa_calculated_Path(AdjRGmatrixchecklink,
										bipartite_mapping.getListMapping().get(i).getRG().getIndex(),
										bipartite_mapping.getListMapping().get(j).getRG().getIndex(), H.get(i, j),
										Path);
							}
						}
					}
				}

				// mettre à jour le graphe bipartie. si il y a un lien qui
				// connecte
				// un noeud de IG à un autre noeud de IG ne respecte pas la
				// capacité,
				// on met la correpondance entre du noeud IG sur RG à l'infini.
				// dans cette tous les liens sont bien mappés.
				// 15,00 0,00 0,00 0,00 0,00
				// 9,00 16,00 0,00 0,00 0,00
				// 4,00 0,00 17,00 0,00 0,00
				// 10,00 6,00 0,00 2,00 0,00
				// 0,00 11,00 0,00 0,00 9,00
				for (int i = 0; i < IG_size; i++) {
					boolean IG_node_OK = true;
					for (int j = i + 1; j < IG_size; j++) {
						//System.out.print(AdjIGmatrixchecklink.get(i, j) + "\t");
						if (AdjIGmatrixchecklink.get(i, j) != 0) {
							IG_node_OK = false;
							Request_Mapping = false;
							Node_Mapping = false;
							break;
						}

					}
					//System.out.println();
					if (!IG_node_OK) {
						bipartite_graph[i][bipartite_mapping.getListMapping().get(i).getRG()
								.getIndex()] = Integer.MAX_VALUE;
					} else {
						bipartite_graph[i][bipartite_mapping.getListMapping().get(i).getRG().getIndex()] = 0;
					}
				}

				// print_Matrix(AdjIGmatrixchecklink, IG_size);
				// print_Matrix(AdjRGmatrixchecklink, RG_size);
				// print_table(bipartite_graph, IG_size, RG_size);
			}

		}
		long stopTime = System.currentTimeMillis();

		int[][] Result = new int[IG_size][2];

		long elapsedTime = stopTime - startTime;
		System.out.println("time :" + elapsedTime / 1000 + "s   " + elapsedTime + "ms");

		if (!Request_Mapping) {
			System.out.println("No solution for the greedy algo :(");
			return -1 + " " + matching_percentage(IG_size, Result, G, H) + " " + 
					get_diff_G_H(IG_size, Result, G, H) + " " + elapsedTime;
		} else {
			System.out.println("Mapping with sucess Yeeeeeeeeeeep for the greedy algo :)");

			for (MappingNode p : bipartite_mapping.getListMapping()) {
				p.printLink();
				Result[p.getIG().getIndex()][0] = p.getIG().getIndex();
				Result[p.getIG().getIndex()][1] = p.getRG().getIndex();
			}

			check_matching(IG_size, Result, G, H);
			FileWriter writer = new FileWriter("SolutionMappingGreedy-instanceRG" + args[0] + "-" + args[2]
					+ "-instanceIG" + args[1] + "-" + args[3]);
			FileWriter wr_Nodes = new FileWriter("Greedy_nodes_mapping" + args[0] + "-" + args[1], true);

			FileWriter wr_Links = new FileWriter("Greedy_links_mapping" + args[0] + "-" + args[1], true);

			for (int i = 0; i < real_size_of_IG; i++) {
				writer.write(i + " " + Result[i][1] + "\n");
				wr_Nodes.write(Result[i][1] + "\t");
			}
			wr_Nodes.write("\n");
			for (int i = 0; i < real_size_of_IG; i++) {
				for (int j = i + 1; j < real_size_of_IG; j++) {
					if (H.get(i, j) != 0) {
						// if (Result[i][1] <= Result[j][1]) {

						writer.write(i + " " + j + "\n"
								+ weightedgraphRG.getWeight(Result[i][1], Result[j][1]).getPathString() + "\n");
						wr_Links.write(weightedgraphRG.getWeight(Result[i][1], Result[j][1]).getPathString() + "\t");
						// } else {
						// writer.write(i + " " + j + "\n" +
						// weightedgraphRG.getWeight(Result[j][1],
						// Result[i][1]).getPathString() + "\n");
						// wr_Links.write(weightedgraphRG.getWeight(Result[j][1],
						// Result[i][1]).getPathString() + "\t");
						// }
					}
				}
			}
			wr_Links.write("\n");
			writer.close();
			wr_Nodes.close();
			wr_Links.close();
			return elapsedTime + " " + matching_percentage(IG_size, Result, G, H) + " "
					+ get_diff_G_H(IG_size, Result, G, H);

		}

	}
}