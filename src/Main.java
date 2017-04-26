import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class Main {
	public static double[][] P;
	public static double[][] stateVector;
	static Scanner sc;
	static int V; //Number of Nodes
	static int E; //Number of Edges
	static ArrayList<String> links = new ArrayList<String>(); //index of Link is index into array + 1
	static ArrayList<Pair> edges = new ArrayList<Pair>();
	
	public static void main(String[] args){
		try{
			 sc = new Scanner(new File("./res/hollins.dat"));
		}catch(Exception e){
			System.out.println("Failed to load hollins.dat!");
			return;
		}
		V = sc.nextInt();
		E = sc.nextInt();
		
		P = new double[V][V];
		
		stateVector = new double[V][1];
		double initialStateValue = 1/(double)V;
		System.out.println("Initializing stateVector to: " + initialStateValue);
		for(int i=0; i<V; i++)
			stateVector[i][0] = initialStateValue;
		System.out.println("Finished");
			
		for(int i=0; i<V; i++){
			sc.nextInt();//get rid of index
			links.add(i, sc.next()); //index of link is index into array +1
		}
		
		for(int i=0; i<E; i++)
			edges.add(new Pair(sc.nextInt()-1, sc.nextInt()-1));

		
		//initializeP();	//initialize P from hollins.dat
		//writePToFile();	//write P to P.dat
		
		//once we've initialized P.dat we can load data faster using the method below
		initPFromFile();	//initialize P from P.dat
		normalizeP();
		//dampingP(0.85d);	//Damping Factor
		//dampingP(0.95d);	//Damping Factor
		dampingP(0.50d);	//Damping Factor
		
		double[][] pingas = pn(P, stateVector); //due to floating point rounding errors perfect equilibrium won't be reached; therefore method continues until difference is very small
		//printMatrix(pingas);
		
		ArrayList<RankLinkPair> rankLinkList = new ArrayList<RankLinkPair>();
		for(int i=0; i<V; i++){
			rankLinkList.add(new RankLinkPair(pingas[i][0], links.get(i)));
		}
		
		//sort the results in decreasing order
		Collections.sort(rankLinkList, new Comparator<RankLinkPair>() {
            public int compare(RankLinkPair lhs, RankLinkPair rhs) {
                return lhs.getRank() > rhs.getRank() ? -1 : (lhs.getRank() < rhs.getRank()) ? 1 : 0;
            }
        });
		
		//output results in decreasing order
		int index = 1;
		for(RankLinkPair rlp : rankLinkList){
			System.out.println(index + " " + rlp.getLink());
			index++;
		}
		
		/*double pingCount = 0;
		for(int i=0; i<V; i++)
			pingCount+=pingas[i][0];
		
		System.out.println("PINGAS: " + pingCount);*/

		//Test code for multMatrix 
		//http://stackoverflow.com/questions/21547462/how-to-multiply-2-dimensional-arrays-matrix-multiplication
/*		double[][] A = {
				{0, 0, 1, 0.5d},
				{1/3d, 0, 0, 0},
				{1/3d, 0.5d, 0, 0.5d},
				{1/3d, 0.5d, 0, 0}
		};
		double[][] V = {
				{0.25d},
				{0.25d},
				{0.25d},
				{0.25d}
		};
		
		printMatrix(pn(A, V, 1000));*/
		
		
		
		/*float sumOfJthCol = 0;
		for(int j=0; j<V; j++){
			for(int i=0; i<V; i++){	
				sumOfJthCol+=P[i][j];
			}
			System.out.println("SumOfColJ_"+ (j+1) + ": " + sumOfJthCol);
			sumOfJthCol = 0;
		}*/
		
		/*System.out.println("IM HERE!");
		for(int i=0; i<V; i++){
			for(int j=0; j<V; j++){
				if(P[i][j] != 0.0)
					System.out.println("[" + i + "," + j + "]: " + P[i][j]);
			}
		}*/
	}
	
	public static void normalizeP(){
		System.out.println("Normalizing P");
		float sumOfJthCol = 0;
		for(int j=0; j<V; j++){
			for(int i=0; i<V; i++){	
				sumOfJthCol+=P[i][j];
			}
			if(sumOfJthCol == 0){//redistribute the worth of the page evenly
				for(int i=0; i<V; i++){	
					P[i][j] = 1/(double)V;
				}
			}	
			sumOfJthCol = 0;
		}
		System.out.println("Finished Normalizing P!");
	}
	
	public static double[][] pn(double[][] A, double[][] V){
		System.out.println("Starting pn calculations");
		double[][] answer = multMatrix(A, V);
		double[][] previousAnswer = V;
		int i = 0;
		System.out.println("Iter until difference is less than: " + ((1/(double)links.size())/1000000d)); //iterate until difference is very very small
		
		while(Math.abs(answer[0][0] - previousAnswer[0][0]) > (1/(double)links.size())/1000000d){
			i++;
			System.out.println("pn iter: " + i + " Difference: " + Math.abs(answer[0][0] - previousAnswer[0][0]));
			previousAnswer = answer.clone();
			answer = multMatrix(A, answer);
		}
		System.out.println("Finshed pn calculations!");
		return answer;
	} 	
	
	/*public static double[][] pn(double[][] A, double[][] V, double n){
		System.out.println("Starting pn calculations");
		double[][] answer = multMatrix(A, V);
		for(int i=1; i<n; i++){
			System.out.println("pn iter " + i);
			answer = multMatrix(A, answer);
		}
		System.out.println("Finshed pn calculations!");
		return answer;
	} */
	
	public static double[][] multMatrix(double a[][], double b[][]){//a[m][n], b[n][p]
		int n = a[0].length;
		int m = a.length;
		int p = b[0].length;
		double ans[][] = new double[m][p];
		if(a.length == 0) return new double[0][0];
		if(a[0].length != b.length) return null; //invalid dims
		
		for(int i = 0;i < m;i++){
			for(int j = 0;j < p;j++){
				for(int k = 0;k < n;k++){
					ans[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return ans;
	}

	public static void printMatrix(double[][] mat) {
		System.out.println("Matrix["+mat.length+"]["+mat[0].length+"]");
		int rows = mat.length;
		int columns = mat[0].length;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				System.out.print(mat[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	private static void dampingP(double dampingFactor){
		System.out.println("Applying Damping to P");
		for(int j=0; j<V; j++){
			for(int i=0; i<V; i++){
				if(i == j)
					P[i][j] = ((dampingFactor)*(P[i][j])+(1-dampingFactor)); //adds 0.15
				else
					P[i][j] = ((dampingFactor)*(P[i][j]));
			}
		}
		System.out.println("Finished!");
	}
	
	private static void initPFromFile(){
		try{
			 sc = new Scanner(new File("./res/P.dat"));
		}catch(Exception e){
			System.out.println("Failed to load P.dat!");
			return;
		}
		
		System.out.println("Initializing P from P.dat");
		while(sc.hasNext()){
			int i = sc.nextInt();
			int j = sc.nextInt();
			double v = sc.nextDouble();
			
			P[i][j] = v;
		}
		System.out.println("Finished!");
	}
	
	private static void writePToFile(){
		System.out.println("Writing P To File");
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter("./res/P.dat"));
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		for(int i=0; i<V; i++){
			for(int j=0; j<V; j++){
				try{
					bw.write(i + " " + j + " " + P[i][j] + "\n");
				}catch(IOException e){
					e.printStackTrace();
					return;
				}
			}
		}
		try{
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		System.out.println("Finished writing P To File");
	}
	
	private static void initializeP(){
		Populator p1 = new Populator(0, V/7);
		Populator p2 = new Populator(V/7, 2*(V/7));
		Populator p3 = new Populator(2*(V/7), 3*(V/7));
		Populator p4 = new Populator(3*(V/7), 4*(V/7));
		Populator p5 = new Populator(4*(V/7), 5*(V/7));
		Populator p6 = new Populator(5*(V/7), 6*(V/7));
		Populator p7 = new Populator(6*(V/7), V);
		
		p1.start();
		p2.start();
		p3.start();
		p4.start();
		p5.start();
		p6.start();
		p7.start();
		
		while(!(p1.isFinished() && 
				p2.isFinished() &&
				p3.isFinished() && 
				p4.isFinished() && 
				p5.isFinished() &&
				p6.isFinished() && 
				p7.isFinished())){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static boolean pageJLinksToPageI(int j, int i){
		for(Pair p : edges){
			if(p.getSource() == j && p.getDestination() == i) //if page j links to page i
				return true;
		}
		return false;
	}
	
	private static int numberOfOutgoingLinksFromNode(int j){
		int count = 0;
		for(Pair p : edges){
			if(p.getSource() == j)
				count++;
		}
		return count;
	}
	
	
	private static class Populator extends Thread{
		int start;
		int end;
		boolean finished = false;
		
		public Populator(int s, int e){
			start = s;
			end = e;
		}
		
		@Override
		public void run() {
			for(int j=0; j<V; j++){
				for(int i=start; i<end; i++){
					if(!pageJLinksToPageI(j, i)){
						P[i][j] = 0;
					}else{
						P[i][j] = 1/(double)numberOfOutgoingLinksFromNode(j);
					}
				}
			}
			finished = true;
			System.out.println("FINISHED = TRUE");
		}
		
		public boolean isFinished(){
			return finished;
		}
	
	}
	
	private static class RankLinkPair{
		double rank;
		String link;
		public RankLinkPair(double r, String string){
			rank = r;
			link = string;
		}
		public double getRank(){
			return rank;
		}
		public String getLink(){
			return link;
		}
	}
	
	private static class Pair{
		int sourceNode;
		int destinationNode;
		public Pair(int source, int destination){
			sourceNode = source;
			destinationNode = destination;
		}
		public int getSource(){
			return sourceNode;
		}
		public int getDestination(){
			return destinationNode;
		}
	}
}
