package sfc_4_ilp;

/**
 * @authors: Oussama Soualah, Marouen Mechtri
 * @contacts: {oussama.soualah, mechtri.marwen}@gmail.com
 * Created on Sep 15, 2016
 */

import java.util.ArrayList;
import java.util.HashMap;

import org.jblas.DoubleMatrix;

public class WeightedGraph {

	private static final int INFINITE = Integer.MAX_VALUE;

	private Cost[][] edges; // adjacency matrix
	private Server[] nodes;

	public WeightedGraph(int n) {
		edges = new Cost[n][n];
		nodes = new Server[n];
	}

	public WeightedGraph(Cost[][] edges, Server[] nodes) {
		super();
		this.edges = new Cost[nodes.length][nodes.length];
		this.nodes = new Server[nodes.length];
		this.edges = edges;
		this.nodes = nodes;
		// print();
	}

	public int nbNodes() {
		return nodes.length;
	}

	public void setNodes(Server[] nodes) {
		this.nodes = nodes;
	}

	public void setNodes(int vertex, Server nodes) {
		// System.out.println(this.nodes[vertex]);
		this.nodes[vertex] = nodes;
	}

	public Server getNodes(int vertex) {
		return nodes[vertex];
	}

	public void setEdges(Cost[][] edges) {
		this.edges = edges;
	}

	public void addEdge(int source, int target, Cost w) {
		// System.out.println("source: "+source+"target: "+target+"cost:
		// "+w.getBandwidth());
		edges[source][target] = w;
	}

	// public boolean isEdge(int source, int target) {
	// return edges[source][target].getExist() > 0;
	// }
	//
	// public void removeEdge(int source, int target) {
	// edges[source][target].setExist(0);
	// }

	public Cost getWeight(int source, int target) {
		return edges[source][target];
	}
	
	public void setWeight(int source, int target, Cost e) {
		edges[source][target] = e;
	}

	public int[] neighbors(int vertex) {
		int count = 0;
		for (int i = 0; i < edges[vertex].length; i++) {
			if (edges[vertex][i] != null && edges[vertex][i].getBandwidth() != INFINITE)
				count++;
		}
		final int[] answer = new int[count];
		count = 0;
		for (int i = 0; i < edges[vertex].length; i++) {
			if (edges[vertex][i] != null && edges[vertex][i].getBandwidth() != INFINITE)
				answer[count++] = i;
		}
		return answer;
	}

	public void print() {
		for (int j = 0; j < edges.length; j++) {
			System.out.print(edges.length + "   " + nodes[j].getId() + ":  ");
			for (int i = 0; i < edges[j].length; i++) {
				if (edges[j][i] != null && edges[j][i].getBandwidth() != INFINITE)
					System.out.print(edges[j][i].getBandwidth() + "\t");
				else
					System.out.print("--\t");
			}
			System.out.println();
		}
	}

	// elle permet de construire le tableau qui contient la liste des noeuds
	// et la matrice d'adjacence avec seulement les liens direct
	// public void construireMatriceWithDirectLink(HashMap allNodes,
	// ArrayList<Link> listEdges) {
	// nodes = new Node[allNodes.size()];
	// edges = new Cost[allNodes.size()][allNodes.size()];
	// System.out.println(allNodes.size());
	// Iterator<Node> k = allNodes.values().iterator();
	// while (k.hasNext()) {
	// Node a = k.next();
	// //System.out.println(a.getIndex());
	// setNodes(a.getIndex(), a);
	// }
	//
	// for (int i = 0; i < listEdges.size(); i++) {
	// addEdge(listEdges.get(i).getSrc().getIndex(), listEdges.get(i)
	// .getDst().getIndex(), listEdges.get(i).getCost());
	// addEdge(listEdges.get(i).getDst().getIndex(), listEdges.get(i)
	// .getSrc().getIndex(), listEdges.get(i).getCost());
	// }
	//
	// }

	// elle permet de construire la matrice d'adjacence en ajoutant les liens
	// indirect avec des coûts
	public DoubleMatrix construireMatriceWithIndirectLink(WeightedGraph initG, DoubleMatrix matrix) {
		WeightedGraph G = initG.clone();
		Cost[][] adgencyConnex = new Cost[G.nbNodes()][G.nbNodes()];
		for (int j = 0; j < G.nbNodes(); j++) {
			for (int k = 0; k < G.nbNodes(); k++) {
				if(G.edges[j][k] != null && G.edges[j][k].getBandwidth() > 0)
				{
					adgencyConnex[j][k] = new Cost(1, 1, 1);//G.edges[j][k];
					G.setWeight(j, k, new Cost(1, 1, 1));
				}
				else
					adgencyConnex[j][k] = G.edges[j][k];
			}

		}
		// adgencyConnex = G.edges;
		for (int i = 0; i < G.nbNodes(); i++) {
			final int[] pred = Dijkstra.dijkstra(G, i);
			for (int n = 0; n < G.nbNodes(); n++) {
				// Dijkstra.printPath(t, pred, i, n);
				if (adgencyConnex[i][n] == null) {
					ArrayList<Server> path;
					path = Dijkstra.getPath(G, pred, i, n);
					if (adgencyConnex[n][i] != null)
						adgencyConnex[i][n] = new Cost(0, Dijkstra.dist[n], 0);
					else
						adgencyConnex[i][n] = new Cost(0, Dijkstra.dist[n], 0);
					if (i != n) {
						matrix.put(i, n, Dijkstra.dist[n]);
						// System.out.println("Path [" + i + " " + n + "] : " +
						// adgencyConnex[i][n].getBandwidth());
						// printPath(path);
					}

				}
			}
		}
		
		return matrix;
	}

	public ArrayList<Server> shortestPath(WeightedGraph G, int src, int dst) {
		if (G.edges[src][dst].getBandwidth() == 0) {

			final int[] pred = Dijkstra.dijkstra(G, src);
			ArrayList<Server> path;
			path = Dijkstra.getPath(G, pred, src, dst);
			return path;

		} else {
			return G.edges[src][dst].getPath();
		}

	}
	
	public HashMap<Integer, ArrayList<Server>>shortestPath_and_cost(WeightedGraph G, int src, int dst, double reqBandwidth) {
		
		HashMap<Integer, ArrayList<Server>> result = new HashMap<Integer, ArrayList<Server>>();

		if (false)
//		if(G.edges[src][dst].getBandwidth() > reqBandwidth)//oussama 
		{
			result.put((int) G.edges[src][dst].getBandwidth(),G.edges[src][dst].getPath());
			return result;
		} 
		else 
		{
			final int[] pred = Dijkstra.dijkstra(G, src);
			if(pred == null) return null;//oussama
			ArrayList<Server> path;
			path = Dijkstra.getPath(G, pred, src, dst);
			result.put(Dijkstra.dist[dst], path);
			return result;
		}

	}
	
//	public HashMap<Integer, ArrayList<Server>>shortestPath_and_cost(WeightedGraph G, int src, int dst, double reqBandwidth) {
//		HashMap<Integer, ArrayList<Server>> result = new HashMap<Integer, ArrayList<Server>>();
//		if (true)
////		if(G.edges[src][dst].getBandwidth() == 0)//< reqBandwidth)//oussama 
//		{
//
//			final int[] pred = Dijkstra.dijkstra(G, src);
//			ArrayList<Server> path;
//			path = Dijkstra.getPath(G, pred, src, dst);
//			result.put(Dijkstra.dist[dst], path);
//			return result;
//
//		} 
//		else {
//			result.put((int) G.edges[src][dst].getBandwidth(),G.edges[src][dst].getPath());
//			return result;
//		}
//
//	}
	

	public float costshortestPath(WeightedGraph G, int src, int dst) {
		if (G.edges[src][dst].getBandwidth() == 0) {
			Dijkstra.dijkstra(G, src);
			return Dijkstra.dist[dst];
		} else
			return G.edges[src][dst].getBandwidth();

	}


	public WeightedGraph clone()
	{
		WeightedGraph clone = new WeightedGraph(nodes.length);
		Server[] clonedNodes = new Server[nodes.length];
		
		for (int i = 0; i < nodes.length; i++) {
			clonedNodes[i] = nodes[i].clone();
		}
		clone.setNodes(clonedNodes);
		
		Cost[][] clonedEdges = new Cost[edges.length][edges.length];
		
		for (int i = 0; i < edges.length; i++) 
		{
			for (int j = 0; j < edges.length; j++) 
			{
				clonedEdges[i][j] = edges[i][j].clone();
			}
			
		}
		
		clone.setEdges(clonedEdges);
		
		return clone;
	}
	
	
	public void copy(WeightedGraph clone)
	{
		Server[] clonedNodes = new Server[nodes.length];
		
		for (int i = 0; i < nodes.length; i++) {
			clonedNodes[i] = nodes[i].clone();
		}
		clone.setNodes(clonedNodes);
		
		Cost[][] clonedEdges = new Cost[edges.length][edges.length];
		
		for (int i = 0; i < edges.length; i++) 
		{
			for (int j = 0; j < edges.length; j++) 
			{
				clonedEdges[i][j] = edges[i][j].clone();;
			}
			
		}
		
		clone.setEdges(clonedEdges);
		
	}
	
	
	public void construireMatriceWithIndirectLink(WeightedGraph G) {
		Cost[][] adgencyConnex = new Cost[G.nbNodes()][G.nbNodes()];
		for (int j = 0; j < G.nbNodes(); j++) {
			for (int k = 0; k < G.nbNodes(); k++) {
				adgencyConnex[j][k] = G.edges[j][k];
			}

		}
		// adgencyConnex = G.edges;
		for (int i = 0; i < G.nbNodes(); i++) {
			final int[] pred = Dijkstra.dijkstra(G, i);
			for (int n = 0; n < G.nbNodes(); n++) {
				// Dijkstra.printPath(t, pred, i, n);
				if (adgencyConnex[i][n] == null) {
					ArrayList<Server> path;
					path = Dijkstra.getPath(G, pred, i, n);
					adgencyConnex[i][n] = new Cost(0, Dijkstra.dist[n], 0, path);
					if (i != n) {
						System.out.println("Path [" + i + " " + n + "] : " + adgencyConnex[i][n].getBandwidth());
						printPath(path);
					}

				} else {
					ArrayList<Server> directLink = new ArrayList<Server>();
					directLink.add(G.getNodes(i));
					directLink.add(G.getNodes(n));
					adgencyConnex[i][n].setPath(directLink);
					System.out.println("Direct link [" + i + " " + n + "] : " + adgencyConnex[i][n].getBandwidth());
					printPath(directLink);
				}
			}
		}
		G.edges = adgencyConnex;
	}

	public void printPath(ArrayList<Server> path) {
		System.out.print("[");
		for (Server s : path)
			System.out.print(s.getIndex() + ",");
		System.out.println("]");
	}

	public void construireMatriceWithdirectLink(WeightedGraph G) {
		Cost[][] adgencyConnex = new Cost[G.nbNodes()][G.nbNodes()];
		for (int j = 0; j < G.nbNodes(); j++) {
			for (int k = 0; k < G.nbNodes(); k++) {
				adgencyConnex[j][k] = G.edges[j][k];
			}

		}
		// adgencyConnex = G.edges;
		for (int i = 0; i < G.nbNodes(); i++) {
			for (int n = 0; n < G.nbNodes(); n++) {
				// Dijkstra.printPath(t, pred, i, n);
				if (adgencyConnex[i][n] == null) {
					adgencyConnex[i][n] = new Cost();
				} else {
					ArrayList<Server> directLink = new ArrayList<Server>();
					directLink.add(G.getNodes(i));
					directLink.add(G.getNodes(n));
					adgencyConnex[i][n].setPath(directLink);
					// System.out.println("******************PATH**************"+Dijkstra.dist[n]);
					// for (Server s: directLink){
					// s.printServer();
					// }
				}
			}
		}
		G.edges = adgencyConnex;
	}

	public static void main(String args[]) {
		final WeightedGraph t = new WeightedGraph(6);
		t.setNodes(0, new Server(0, "v0", "ID0"));
		t.setNodes(1, new Server(1, "v1", "ID1"));
		t.setNodes(2, new Server(2, "v2", "ID2"));
		t.setNodes(3, new Server(3, "v3", "ID3"));
		t.setNodes(4, new Server(4, "v4", "ID4"));
		t.setNodes(5, new Server(5, "v5", "ID5"));

		t.addEdge(0, 1, new Cost(2, 1, 3));
		t.addEdge(0, 5, new Cost(2, 9, 3));
		t.addEdge(1, 2, new Cost(2, 8, 3));
		t.addEdge(1, 3, new Cost(2, 15, 3));
		t.addEdge(1, 5, new Cost(2, 6, 3));
		t.addEdge(2, 3, new Cost(2, 1, 3));
		t.addEdge(4, 3, new Cost(2, 3, 3));
		t.addEdge(4, 2, new Cost(2, 7, 3));
		t.addEdge(5, 4, new Cost(2, 3, 3));

		t.addEdge(1, 0, new Cost(2, 1, 3));
		t.addEdge(5, 0, new Cost(2, 9, 3));
		t.addEdge(2, 1, new Cost(2, 8, 3));
		t.addEdge(3, 1, new Cost(2, 15, 3));
		t.addEdge(5, 1, new Cost(2, 6, 3));
		t.addEdge(3, 2, new Cost(2, 1, 3));
		t.addEdge(3, 4, new Cost(2, 3, 3));
		t.addEdge(2, 4, new Cost(2, 7, 3));
		t.addEdge(4, 5, new Cost(2, 3, 3));
		t.print();

		// Cost[][] adgencyConnex = new Cost[t.nbNodes()][t.nbNodes()];
		// adgencyConnex = t.edges;
		// for (int i = 0; i < 6; i++) {
		// final int[] pred = Dijkstra.dijkstra(t, i);
		// for (int n = 0; n < 6; n++) {
		// //Dijkstra.printPath(t, pred, i, n);
		// if (adgencyConnex[i][n] == null) {
		// adgencyConnex[i][n] = new Cost(Dijkstra.dist[n], 0, 0);
		// }
		// }
		// }
		// t.edges = adgencyConnex;
		// System.out.print("\n");
		// t.print();

		t.construireMatriceWithIndirectLink(t);
		t.print();
	}

	public void removePath(Integer[] path) 
	{
		for (int i = 0; i < path.length-1; i++) {
			edges[path[i]][path[i+1]].setBandwidth(0);
		}
		
	}

}