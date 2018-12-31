package sfc_Run;

/**
 * @authors: Marouen Mechtri, Chaima Ghribi, Oussama Soualah
 * @contacts: {mechtri.marwen, ghribii.chaima, oussama.soualah}@gmail.com
 * Created on Sep 15, 2016
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ilog.concert.IloException;
import ilog.cplex.IloCplex.UnknownObjectException;
import ilp_sfc.LinearProgram;


public class Run {

	
	
	private static void runILPalgo(int nbNodeRG, int nbServerIG, int indexRG, int indexIG) throws IOException, UnknownObjectException, IloException {

		ArrayList<String> listSol = new ArrayList<String>();
		FileWriter writer = new FileWriter("ILPSolution" + nbNodeRG + "-" + nbServerIG, true);
		FileWriter writer1 = new FileWriter("ILPTime" + nbNodeRG + "-" + nbServerIG, true);
//		FileWriter writerCplex = new FileWriter("ILPTimeCplex" + nbNodeRG + "-" + nbServerIG, true);
		FileWriter writer_reject = new FileWriter("ILPTime_reject" + nbNodeRG + "-" + nbServerIG, true);
		FileWriter writereigenCost1 = new FileWriter("ILPCost" + nbNodeRG + "-" + nbServerIG, true);
		FileWriter consolidation = new FileWriter("ILPConsolidation" + nbNodeRG + "-" + nbServerIG, true);
		long timeResolution;
		String[] argilp = { String.valueOf(nbNodeRG), String.valueOf(nbServerIG), String.valueOf(indexRG), String.valueOf(indexIG)};
		String[] a = LinearProgram.main(argilp).split(" ");
		timeResolution = Long.parseLong(a[0]);
		if (timeResolution >= 0)
		{
			listSol.add("instanceRG" + nbNodeRG + "-" + indexRG + ",instanceIG" + nbServerIG + "-" + indexIG);
			writer.write("instanceRG" + nbNodeRG + "-" + indexRG + " instanceIG" + nbServerIG + "-" + indexIG + "\n");
			writer.flush();
			writer1.write(timeResolution + "\n");
			writer1.flush();
			
//			writerCplex.write(LinearProgram.cplexTime + "\n");
//			writerCplex.flush();
			
			writereigenCost1.write(a[1] + "\n");
			writereigenCost1.flush();
			consolidation.write(a[2] + "\t" + a[3] + "\n");
			consolidation.flush();
		}else{
			writer_reject.write(a[a.length-1] + "\n");
			writer_reject.flush();
		}

		writer.close();
		writereigenCost1.close();
		writer1.close();
//		writerCplex.close();
		writer_reject.close();
		consolidation.close();

	}

	
	public static void main(String[] args) throws IOException, UnknownObjectException, IloException {
		// TODO Auto-generated method stub

		System.out.println("(1) If you are running the Run.java file with eclipse, please add the line bellow in the Program arguments:");
		System.out.println("reqsize=3");
		System.out.println("Add also in Java Build Path --> source tab --> Native library location the line bellow: ");
		System.out.println("/opt/ibm/ILOG/CPLEX_Studio128/cplex/bin/x86-64_linux");
		System.out.println("(2) If you are running the sfc_ilp_algo.jar file, please use the following command:");
		System.out.println("java -Xmx20g -Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio128/cplex/bin/x86-64_linux -jar sfc_ilp_algo.jar reqsize=3");
		int nbIGnodes = -1;
		
		
		for (int i = 0; i < args.length; i++) {
			String[] params = args[i].split("=");
			switch (params[0]) {
			case "reqsize":
				nbIGnodes = Integer.parseInt(params[1]);
				System.out.println(params[0] + " " + params[1]);
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
		runILPalgo(nbserverRG, nbIGnodes, 0, 0);
	}

}
