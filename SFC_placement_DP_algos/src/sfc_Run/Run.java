package sfc_Run;

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

import dynamic_prog_sfc_2Bigmatrices.DP_matrices_algo_SFC;
import dynamic_prog_sfc_without_matrices.DP_no_matrices_algo_SFC;

public class Run {

	

	
	public static void rundp_matricesalgo(int nbNodeRG, int nbServerIG, int indexRG, int indexIG) throws IOException {

		ArrayList<String> listSol = new ArrayList<String>();
		FileWriter writer = new FileWriter("DP_matricesSolution" + nbNodeRG + "-" + nbServerIG, true);
		FileWriter writer1 = new FileWriter("DP_matricesTime" + nbNodeRG + "-" + nbServerIG, true);
		FileWriter writer_reject = new FileWriter("DP_matricesTime_reject" + nbNodeRG + "-" + nbServerIG, true);
		FileWriter writereigenCost1 = new FileWriter("DP_matricesCost" + nbNodeRG + "-" + nbServerIG, true);
		//FileWriter consolidation = new FileWriter("DP_matricesConsolidation" + nbNodeRG + "-" + nbServerIG, true);
		long timeResolution;
		String[] argdpmatrix = { String.valueOf(nbNodeRG), String.valueOf(nbServerIG), String.valueOf(indexRG),
				String.valueOf(indexIG) };
		String[] a = DP_matrices_algo_SFC.main(argdpmatrix).split(" ");
		timeResolution = Long.parseLong(a[0]);
		if (timeResolution >= 0) {
			listSol.add("instanceRG" + nbNodeRG + "-" + indexRG + ",instanceIG" + nbServerIG + "-" + indexIG);
			writer.write("instanceRG" + nbNodeRG + "-" + indexRG + " instanceIG" + nbServerIG + "-" + indexIG + "\n");
			writer.flush();
			writer1.write(timeResolution + "\n");
			writer1.flush();
			writereigenCost1.write(a[1] + "\n");
			writereigenCost1.flush();
			//consolidation.write(a[2] + "\t" + a[3] + "\n");
			//consolidation.flush();
		} else {
			writer_reject.write(a[a.length - 1] + "\n");
			writer_reject.flush();
		}

		writer.close();
		writereigenCost1.close();
		writer1.close();
		writer_reject.close();
		//consolidation.close();

	}
		
	public static void rundp_no_matricesalgo(int nbNodeRG, int nbServerIG, int indexRG, int indexIG) throws IOException {

		ArrayList<String> listSol = new ArrayList<String>();
		FileWriter writer = new FileWriter("DP_no_matricesSolution" + nbNodeRG + "-" + nbServerIG, true);
		FileWriter writer1 = new FileWriter("DP_no_matricesTime" + nbNodeRG + "-" + nbServerIG, true);
		FileWriter writer_reject = new FileWriter("DP_no_matricesTime_reject" + nbNodeRG + "-" + nbServerIG, true);
		FileWriter writereigenCost1 = new FileWriter("DP_no_matricesCost" + nbNodeRG + "-" + nbServerIG, true);
		//FileWriter consolidation = new FileWriter("DP_no_matricesConsolidation" + nbNodeRG + "-" + nbServerIG, true);
		long timeResolution;
		String[] argdp = { String.valueOf(nbNodeRG), String.valueOf(nbServerIG), String.valueOf(indexRG),
				String.valueOf(indexIG) };
		String[] a = DP_no_matrices_algo_SFC.main(argdp).split(" ");
		timeResolution = Long.parseLong(a[0]);
		if (timeResolution >= 0) {
			listSol.add("instanceRG" + nbNodeRG + "-" + indexRG + ",instanceIG" + nbServerIG + "-" + indexIG);
			writer.write("instanceRG" + nbNodeRG + "-" + indexRG + " instanceIG" + nbServerIG + "-" + indexIG + "\n");
			writer.flush();
			writer1.write(timeResolution + "\n");
			writer1.flush();
			writereigenCost1.write(a[1] + "\n");
			writereigenCost1.flush();
			//consolidation.write(a[2] + "\t" + a[3] + "\n");
			//consolidation.flush();
		} else {
			writer_reject.write(a[a.length - 1] + "\n");
			writer_reject.flush();
		}

		writer.close();
		writereigenCost1.close();
		writer1.close();
		writer_reject.close();
		//consolidation.close();

	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		System.out.println("(1) If you are running the Run.java file with eclipse, please add the line bellow in the Program arguments:");
		System.out.println("reqsize=3 dp_matrices=true dp_no_matrices=false");
		System.out.println("(2) If you are running the sfc_dp_algo.jar file, please use the following command:");
		System.out.println("java -Xmx20g -jar sfc_dp_algo.jar reqsize=3 dp_matrices=true dp_no_matrices=false");
		int nbIGnodes = -1;
		boolean dp_matrices = false, dp_no_matrices = false;
		

		for (int i = 0; i < args.length; i++) {
			String[] params = args[i].split("=");
			switch (params[0]) {
			case "reqsize":
				nbIGnodes = Integer.parseInt(params[1]);
				System.out.println(params[0] + " " + params[1]);
				break;
			case "dp_matrices":
				System.out.println(params[0] + " " + params[1]);
				dp_matrices = Boolean.parseBoolean(params[1]);
				break;
			case "dp_no_matrices":
				System.out.println(params[0] + " " + params[1]);
				dp_no_matrices = Boolean.parseBoolean(params[1]);
				break;	
			default:
				break;
			}
		}

		/****************************************************
		 * Infra Reading
		 ****************************************************/
		// Read Infrastructure from infra file
		FileInputStream fstream;
		fstream = new FileInputStream("sub.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String str;
		str = br.readLine();
		String[] b = str.split(" ");
		int nbserverRG = Integer.parseInt(b[0]);
		in.close();

		/******************************************************************/
		
		if (dp_matrices) {
			rundp_matricesalgo(nbserverRG, nbIGnodes, 0, 0);
			
		}
		if (dp_no_matrices) {
			rundp_no_matricesalgo(nbserverRG, nbIGnodes, 0, 0);
		}
	}

}
