package eigen_sfc;

/**
 * @authors: Chaima Ghribi, Marouen Mechtri
 * @contacts: {ghribii.chaima, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.jblas.DoubleMatrix;
import org.jblas.Eigen;


public class SFCmappingAlgo {

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

	public static HashMap<Integer, Server> Vvms;
	public static HashMap<Integer, Server> Vswitches;
	public static HashMap<Integer, Server> Vvnfp;
	public static HashMap<Integer, Server> Vvnfc;


	public static void print_Matrix(DoubleMatrix matrix, int size) {
		System.out.println("-----------------------------------------------------------------");
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				System.out.print(String.format("%.2f \t",matrix.get(i, j)));
			}
			System.out.println();
		}
		System.out.println("-----------------------------------------------------------------");
	}
	
	public static void print_Matrix(DoubleMatrix matrix, int ligne, int column) {
		System.out.println("-----------------------------------------------------------------");
		for (int i = 0; i < ligne; i++) {
			for (int j = 0; j < column; j++) {
				System.out.print(String.format("%.2f \t",matrix.get(i, j)));
			}
			System.out.println();
		}
		System.out.println("-----------------------------------------------------------------");
	}

	public static void save_Matrix(DoubleMatrix matrix, int size) throws IOException {
		System.out.println("-----------------------------------------------------------------");
		FileWriter writer = new FileWriter("matrix");

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				writer.write((String.format("%.2f \t",matrix.get(i, j))));
			}
			writer.write("\n");
		}
		writer.close();
		System.out.println("-----------------------------------------------------------------");
	}
	
	public static DoubleMatrix reduceCapaNode(DoubleMatrix matrix, int row, int column, double value) {
			matrix.put(row, column,matrix.get(row, column)-value);
			return matrix;
	}

	public static boolean checkremainingCapaNode(DoubleMatrix matrix, int row, int column, double value) {
		if (matrix.get(row, column)-value>=threshold_consolidation){
			return true;
		}else
			return false;
	}
	
	public static DoubleMatrix reduceCapaPath(DoubleMatrix matrix, int row, int column, double value) {
		
		int[] path = weightedgraphRG.getWeight(row, column).getPathtab();
		
		for (int i=0; i<path.length-1; i++){
			matrix.put(path[i], path[i+1],matrix.get(path[i], path[i+1])-value);
			matrix.put(path[i+1], path[i],matrix.get(path[i+1], path[i])-value);
		}
		return matrix;
	}

	public static boolean checkremainingCapaPath(DoubleMatrix matrix, int row, int column, double value) {
		boolean check=true;

		int[] path = weightedgraphRG.getWeight(row, column).getPathtab();
		
		for (int i=0; i<path.length-1; i++){
			if (matrix.get(path[i], path[i+1])-value<threshold_consolidation){
				return false;
			}
		}
		return check;
	}


	public static void print_table(int[][] matrix, int L, int H) {
		for (int i = 0; i < L; i++) {
			for (int j = 0; j < H; j++) {
				System.out.print(matrix[i][j] + "\t");
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
					break;
				case pswitches:
					tempserver = new Server(nbservers, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
							Float.parseFloat(a[2]), Float.parseFloat(a[2]), pswitches, -1, -1, -1, -1);
					Pswitches.put(Integer.parseInt(a[0]), tempserver);
					break;
				case pvnfp:
					tempserver = new Server(nbservers, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
							Float.parseFloat(a[2]), Float.parseFloat(a[2]), pvnfp, Integer.parseInt(a[4]), -1, -1, -1);
					Pvnfp.put(Integer.parseInt(a[0]), tempserver);
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
		/********************* Mesh ***********************/
		weightedgraphRG = new WeightedGraph(adjaMatrixGraph, serverRG);
		//weightedgraphRG.construireMatriceWithIndirectLink(weightedgraphRG, matrix);
		weightedgraphRG.construireMatriceWithdirectLink(weightedgraphRG);
		/**************************************************/
		// print_Matrix(matrix, size);
		return matrix;

	}

	public static DoubleMatrix read_IG_graph(String type, int size, int index, int RG_size) throws IOException {

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
					break;
				case vswitches:
					tempserver = new Server(nbVirtualNode, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
							Float.parseFloat(a[2]), Float.parseFloat(a[2]), vswitches, -1, -1, Integer.parseInt(a[4]),
							tenantID);
					Vswitches.put(Integer.parseInt(a[0]), tempserver);
					break;
				case vvnfp:
					tempserver = new Server(nbVirtualNode, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
							Float.parseFloat(a[2]), Float.parseFloat(a[2]), vvnfp, Integer.parseInt(a[4]), -1, -1,
							tenantID);
					Vvnfp.put(Integer.parseInt(a[0]), tempserver);
					break;
				case vvnfc:
					tempserver = new Server(nbVirtualNode, (char) (index + 65) + a[0], a[1], Float.parseFloat(a[2]),
							Float.parseFloat(a[2]), Float.parseFloat(a[2]), vvnfc, Integer.parseInt(a[4]),
							Integer.parseInt(a[5]), -1, tenantID);
					Vvnfc.put(Integer.parseInt(a[0]), tempserver);
					break;
				default:
					System.out.println("there is no type assigned to virtual node: " + Integer.parseInt(a[0]));
				}
				nbVirtualNode++;
			}
			if (L) {
				String[] a = str.split(" ");
				float SFCbw=0;
				for(int i=0; i<nbSFC; i++)
					SFCbw+=Float.parseFloat(a[2+i]);
				matrix.put(Integer.parseInt(a[0]), Integer.parseInt(a[1]), SFCbw);
				matrix.put(Integer.parseInt(a[1]), Integer.parseInt(a[0]), SFCbw);
			}

		}
		in.close();
		// print_Matrix(matrix, IG_size);

		DoubleMatrix H = new DoubleMatrix(RG_size, RG_size);

		for (int i = 0; i < RG_size; i++)
			for (int j = 0; j < RG_size; j++)
				H.put(i, j, 0);

		for (int i = 0; i < IG_size; i++)
			for (int j = 0; j < IG_size; j++)
				H.put(i, j, Math.floor(matrix.get(i, j)));

		// System.out.println("************* Matrix H *************");
		// print_Matrix(H, RG_size);

		return H;

	}


	private static boolean is_reserved(int j, int size, int[][] R) {
		int k = 0;
		boolean reserved = false;
		// print_table(R, size, 2);
		while ((!reserved) && (k < size)) {
			if (R[k][1] == j) {
				reserved = true;
			}
			k++;
		}
		return reserved;
	}

	private static int getindexmax(float[] tab) {
		float largest = -1;
		int index = -1;
		for (int i = 0; i < tab.length; i++) {
			if (tab[i] > largest) {
				largest = tab[i];
				index = i;
			}
		}
		return index;
	}

	private static float[] getrow(DoubleMatrix permut, int i) {
		float[] tab = new float[permut.columns];
		for (int j = 0; j < permut.columns; j++) {
			tab[j] = (float) permut.get(i, j);
		}
		return tab;
	}
	
	private static DoubleMatrix copymatrix(DoubleMatrix RGmatrix, int size) {
		DoubleMatrix matrix = new DoubleMatrix(size,size);
		for (int i = 0; i < RGmatrix.rows; i++) {
			for (int j = 0; j < RGmatrix.columns; j++) {
				matrix.put(i, j, RGmatrix.get(i,j));
			}
		}
		return matrix;
	}
	

	private static boolean is_total_matching(int IG_size, int[][] Result) {

		boolean total = true;
		for (int i = 0; i < IG_size; i++)
			if (Result[i][1] == -1)
				total = false;

		return total;
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
				if (threshold_consolidation <= G.get(Result[i][1], Result[i][1]) - H.get(i, i)) {
					nb_mapped_node++;
				} else
					solutionmatching = false;
				for (int j = i + 1; j < IG_size; j++) {
					if (threshold_consolidation <= (G.get(Result[i][1], Result[j][1]) - H.get(i, j))
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

	private static long matching_persentige(int IG_size, int[][] Result, DoubleMatrix G, DoubleMatrix H) {
		int nb_mapped_node = 0;
		int nb_mapped_link = 0;
		int nb_total_link = 0;
		for (int i = 0; i < IG_size; i++) {
			if (Result[i][1] != -1) {
				if (threshold_consolidation <= G.get(Result[i][1], Result[i][1]) - H.get(i, i)) {
					nb_mapped_node++;
				}
			}
			for (int j = i + 1; j < IG_size; j++) {
				if (Result[i][1] != -1 && Result[j][1] != -1) {
					if (threshold_consolidation <= (G.get(Result[i][1], Result[j][1]) - H.get(i, j))
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
				if (threshold_consolidation <= G.get(Result[i][1], Result[i][1]) - H.get(i, i)) {
					node_diff_H_G += G.get(Result[i][1], Result[i][1]) - H.get(i, i);
				} else
					solutionmatching = false;
				for (int j = i + 1; j < IG_size; j++) {
					if (threshold_consolidation <= (G.get(Result[i][1], Result[j][1]) - H.get(i, j))
							&& H.get(i, j) != 0) {
						link_diff_H_G += G.get(Result[i][1], Result[j][1]) - H.get(i, j);
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

	public static String main(String[] args) throws IOException {

		int RG_size = Integer.parseInt(args[0]);
		int IG_size = Integer.parseInt(args[1]);

		// DoubleMatrix G = generate_RG_graph(RG_size, 50, 30, 0);
		// DoubleMatrix H = generate_IG_graph(IG_size, 100, 10, RG_size, 0);

		DoubleMatrix G = read_RG_graph("RG", RG_size, Integer.parseInt(args[2]));
		//PrintPhysicalNodes();
		
		System.out.println("**************************");
		DoubleMatrix H = read_IG_graph("IG", IG_size, Integer.parseInt(args[3]), RG_size);
		IG_size = real_size_of_IG;
		//PrintVirtualNodes();

		//print_Matrix(H, IG_size);
		long startTime = System.currentTimeMillis();

		// compute the spectral decomposition
		//System.out.println("**************************");
		//System.out.println("Decomposition of matrix G");

		DoubleMatrix[] Vg = Eigen.symmetricEigenvectors(G);

		// System.out.println("Time needed for decomposing G : " +
		// (System.currentTimeMillis() - startTime));

		//System.out.println("**************************");
		//System.out.println("Decomposition of matrix H");

		DoubleMatrix[] Vh = Eigen.symmetricEigenvectors(G);

		// System.out.println("Time needed for decomposing H : " +
		// (System.currentTimeMillis() - startTime));

		// les vecteurs propre de H
		for (int i = 0; i < RG_size; i++)
			for (int j = 0; j < RG_size; j++)
				Vh[0].put(i, j, Math.abs(Vh[0].get(i, j)));

		// les vecteurs propre de G
		for (int i = 0; i < RG_size; i++)
			for (int j = 0; j < RG_size; j++)
				Vg[0].put(i, j, Math.abs(Vg[0].get(i, j)));

		DoubleMatrix permut = new DoubleMatrix(RG_size, RG_size);

		permut = Vh[0].mul(Vg[0].transpose());

		//System.out.println("**************************");
		//System.out.println("Permutation Matrix:");
		//System.out.println("**************************\n");

		
		// Vms should be mapped only on physical servers
		for(Integer indexMap:Vvms.keySet()){
			for(int i=0; i<RG_size; i++){
				if (Pswitches.containsKey(i) || Pvnfp.containsKey(i)){
					permut.put(indexMap, i, -1);
				}
			}	
		}
		
		// switches should be mapped only on switches having the same id
		for(Integer indexMap:Vswitches.keySet()){
			for(int i=0; i<RG_size; i++){
				if (Pservers.containsKey(i) || Pvnfp.containsKey(i) || (Pswitches.containsKey(i) && Vswitches.get(indexMap).getSwitchid()!=i) ){
					permut.put(indexMap, i, -1);
				}
				
			}	
		}
		
		// Physical VNF should be mapped only on physical VNF having the same VNF type
		for(Integer indexMap:Vvnfp.keySet()){
			for(int i=0; i<RG_size; i++){
				if (Pservers.containsKey(i) || Pswitches.containsKey(i) || 
						(Pvnfp.containsKey(i) && Vvnfp.get(indexMap).getVnftype()!=Pvnfp.get(i).getVnftype()) ){
					permut.put(indexMap, i, -1);
				}
				
			}	
		}
		
		// Virtual VNF should be mapped only on servers.
		// In the case of reusing existing virtual VNF, vnfc should be mapped only on servers that hosts vnfc and having the same VNF type.
		for(Integer indexMap:Vvnfc.keySet()){
			for(int i=0; i<RG_size; i++){
				if (Pvnfp.containsKey(i) || Pswitches.containsKey(i)){
					permut.put(indexMap, i, -1);
				}else if(Vvnfc.get(indexMap).getReuse()==reuse){
					// choose servers hosting vnfc of tenant Pswitches.get(indexMap).getTenantid() and having the same VNF type. 
					System.out.println("VM " + Vvnfc.get(indexMap).getName() + " of tenant "+ Vvnfc.get(indexMap).getTenantid() + 
							" should be mapped on existing VNF-C");
				}
				
			}	
		}
		// print_Matrix(permut, RG_size);

		// System.out.println("Time needed for multiplying G and H: " +
		// (System.currentTimeMillis() - startTime));

		//return "null";
		// Matching algorithm (written by Chaima Ghribi)
		// Result of the matching
		int[][] Result = new int[IG_size][2];

		for (int i = 0; i < IG_size; i++) {
			Result[i][0] = i;
			Result[i][1] = -1;
		}

		boolean solution = false;
		boolean nodeTypeexist=true;
		int iter = 0;
		DoubleMatrix AdjRGmatrix = null ;
		
		while (!solution && iter < RG_size && nodeTypeexist) {

			////AdjRGmatrix = copymatrix(G, RG_size);
			//print_Matrix(AdjRGmatrix, RG_size);
			for (int i = 0; i < IG_size; i++) {

				float[] tab = getrow(permut, i);

				
				boolean found = false;
				int nb_iterations = 0;

				int j = -1;

				if (i == 0) {

					for (int s = 0; s < IG_size; s++) {
						Result[s][0] = s;
						Result[s][1] = -1;
					}

					while ((found == false) && (nb_iterations < tab.length)) {

						for (int t = nb_iterations; t < iter; t++) {
							tab[getindexmax(tab)] = -1;
							nb_iterations++;
							
						}
						if (getindexmax(tab)==-1){
							nodeTypeexist=false;
							break;
						}
						j = getindexmax(tab);
						

						nb_iterations++;

						if (G.get(j, j) - H.get(i, i) < threshold_consolidation) {
							tab[j] = -1;
							
						} else {
							////if(checkremainingCapaNode(AdjRGmatrix,j,j,H.get(i,i))){
								found = true;
							////	AdjRGmatrix=reduceCapaNode(AdjRGmatrix,j,j,H.get(i,i));								
							////}else{ 
							////	tab[j] = -1;								
							////}
						}
					}
					iter = nb_iterations;
					if (found == false) {
						System.out.println("No solution found for the first node");
						break;
					}
					Result[i][1] = j;
					
				} else {
					
		
					while ((found == false) && (nb_iterations < tab.length)) {

						
						if (getindexmax(tab)==-1)
							break;
						
							
						j = getindexmax(tab);
						nb_iterations++;

						boolean valid = true;

						int h = 0;

						
						////DoubleMatrix AdjRGmatrixchecklink = copymatrix(AdjRGmatrix, RG_size);
						while ((valid == true) && (h < IG_size) && (!found)) {

							
							// physical node is already reserved by another
							// virtual node
							if (is_reserved(j, IG_size, Result)) {
								valid = false;
								tab[j] = -1;
							} 
							else {
								// node capacity is not respected	
								
								if ((G.get(j, j) - H.get(i, i) < threshold_consolidation)) {
									valid = false;
									tab[j] = -1;		
								} 
								// node capacity is respected but remaining capacity is not enough to host multi nodes
								////else if (!checkremainingCapaNode(AdjRGmatrixchecklink,j,j,H.get(i,i))){
								////	valid = false;
								////	tab[j] = -1;
								////}
								else {
									if ((h != i) && (H.get(i, h) != 0) && Result[h][1] != -1) {
										if (threshold_consolidation > G.get(j, Result[h][1]) - H.get(i, h)) {
											valid = false;
											tab[j] = -1;
										}
										////else if (!checkremainingCapaPath(AdjRGmatrixchecklink, j, Result[h][1], H.get(i,h))){
										////	valid = false;
										////	tab[j] = -1;
										////}
										////else{
										////	reduceCapaPath(AdjRGmatrixchecklink, j, Result[h][1], H.get(i,h));											
										////}
									}
								}
							}
							h++;
						}
						if (valid == true) {
							Result[i][1] = j;
							found = true;
							////AdjRGmatrix=copymatrix(AdjRGmatrixchecklink,RG_size);
							////reduceCapaNode(AdjRGmatrix, j, j, H.get(i,i));
						}
					}
				}
				//System.out.println("***********************"+iter);
				

			}

			if (is_total_matching(IG_size, Result))
				solution = true;

			if (!solution) {
			} else {
				// check_matching(IG_size, Result, G, H);
			}

		}

		//print_Matrix(permut, IG_size, RG_size);
		
		//print_table(Result, IG_size, 2);
		//print_Matrix(AdjRGmatrix, RG_size);
		
		long stopTime = System.currentTimeMillis();

		long elapsedTime = stopTime - startTime;
		System.out.println("time :" + elapsedTime / 1000 + "s   " + elapsedTime + "ms");

		if (!solution) {
			System.out.println("Mapping Noooot found :( iter: " + iter);
			// PrintSolution(IG_size, Result);

			return -1 + " " + matching_persentige(IG_size, Result, G, H) + " " + get_diff_G_H(IG_size, Result, G, H) + " " +elapsedTime;
		} else {
			System.out.println("Mapping with sucess Yeeeeeeeeeeep :) iter= " + iter);
			//PrintSolution(IG_size, Result);
			//check_matching(IG_size, Result, G, H);
			FileWriter writer = new FileWriter("SolutionMappingEigen-instanceRG" + args[0] + "-" + args[2]
					+ "-instanceIG" + args[1] + "-" + args[3]);
			FileWriter wr_Nodes = new FileWriter("Eigen_nodes_mapping" + args[0] + "-" + args[1], true);

			FileWriter wr_Links = new FileWriter("Eigen_links_mapping" + args[0] + "-" + args[1], true);

			for (int i = 0; i < real_size_of_IG; i++) {
				writer.write(i + " " + Result[i][1] + "\n");
				wr_Nodes.write(Result[i][1] + "\t");
			}
			wr_Nodes.write("\n");
			for (int i = 0; i < real_size_of_IG; i++) {
				for (int j = i + 1; j < real_size_of_IG; j++) {
					if (H.get(i, j) != 0) {
						//if (Result[i][1] <= Result[j][1]) {
							
							writer.write(i + " " + j + "\n" + weightedgraphRG.getWeight(Result[i][1], Result[j][1]).getPathString() + "\n");
							wr_Links.write(weightedgraphRG.getWeight(Result[i][1], Result[j][1]).getPathString() + "\t");
						//} else {
						//	writer.write(i + " " + j + "\n" + weightedgraphRG.getWeight(Result[j][1], Result[i][1]).getPathString() + "\n");
						//	wr_Links.write(weightedgraphRG.getWeight(Result[j][1], Result[i][1]).getPathString() + "\t");
						//}
					}
				}
			}
			wr_Links.write("\n");
			writer.close();
			wr_Nodes.close();
			wr_Links.close();
			return elapsedTime + " " + matching_persentige(IG_size, Result, G, H) + " "
					+ get_diff_G_H(IG_size, Result, G, H);
		}

	}

}