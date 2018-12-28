package sfc;

/**
 * @authors: Marouen Mechtri, Chaima Ghribi
 * @contacts: {mechtri.marwen, ghribii.chaima}@gmail.com
 * Created on Sep 15, 2016
 */


import java.util.ArrayList;

public class Dijkstra {

	public static int[] dist;

	// Dijkstra's algorithm to find best path (that maximizes the minimum
	// bandwidth along the path) from s to all other nodes
	public static int[] dijkstra(WeightedGraph G, int s) {
		dist = new int[G.nbNodes()]; // shortest known distance from "s"
		final int[] pred = new int[G.nbNodes()]; // preceeding node in path
		final boolean[] visited = new boolean[G.nbNodes()]; // all false
															// initially

		for (int i = 0; i < dist.length; i++) {
			//// dist[i] = Integer.MAX_VALUE;
			dist[i] = 0;
		}
		//// dist[s] = 0;
		dist[s] = Integer.MAX_VALUE;

		for (int i = 0; i < dist.length; i++) {
			//// final int next = minVertex(dist, visited);
			final int next = maxVertex(dist, visited);
			visited[next] = true;

			// The best path to next is dist[next] and via pred[next].

			final int[] n = G.neighbors(next);
			for (int j = 0; j < n.length; j++) {
				final int v = n[j];
				//// final int d = dist[next] + G.getWeight(next,
				//// v).getLatency();
				final int d = (int)Math.min(dist[next], G.getWeight(next, v).getBandwidth());
				//// if (dist[v] > d) {
				if (dist[v] < d) {
					dist[v] = d;
					pred[v] = next;
				}
			}
		}
		return pred; // (ignore pred[s]==0!)
	}

	//// private static int minVertex(int[] dist, boolean[] v) {
	private static int maxVertex(int[] dist, boolean[] v) {
		//// int x = Integer.MAX_VALUE;
		int x = 0;
		int y = -1; // graph not connected, or no unvisited vertices
		for (int i = 0; i < dist.length; i++) {
			//// if (!v[i] && dist[i] < x) {
			if (!v[i] && dist[i] > x) {
				y = i;
				x = dist[i];
			}
		}
		return y;
	}

	/*public static void printPath(WeightedGraph G, int[] pred, int s, int e) {
		final ArrayList path = new ArrayList();
		int x = e;
		while (x != s) {
			path.add(0, G.getNodes(x).getId());
			x = pred[x];
		}
		path.add(0, G.getNodes(s).getId());
		System.out.println(path);
		System.out.println(dist[e]);

	}*/

	public static ArrayList<Server> getPath(WeightedGraph G, int[] pred, int s, int e) {
		final ArrayList<Server> path = new ArrayList<Server>();
		int x = e;
		while (x != s) {
			path.add(0, G.getNodes(x));// (0, G.getNodes(x).getId());
			x = pred[x];
		}
		path.add(0, G.getNodes(s));
		// System.out.println(path);
		// System.out.println(dist[e]);
		return path;
	}

}