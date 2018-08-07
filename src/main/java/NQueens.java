import java.util.*;

class Point { // utility class storing x, y values (need to be double for future calculations)
	public double x, y;
	
	public Point(int _x, int _y) {
		x = _x;
		y = _y;
	}
}

class PointPair { // utility class for storing Points pair
	public Point a, b;
	
	public PointPair(Point _a, Point _b) {
		a = _a;
		b = _b;
	}
}

enum AddOrSubtract { // there are the same operations for adding / subtracting forbidden cell information, we use this enum to set the mode
	Add,
	Subtract
}

public class NQueens { // main class
	int N;
	
	int[][] board;
	// coding for board:
	// 0  - empty
	// -1 - queen
	// > 0 - number of forbidden signs
	
	int[] line;
	// 0 - no queen in that column
	// > 0 - y + 1 of queen in x column (so y of queen in x = line[x] - 1)
	// < 0 - abs(line[x]) - 1 stores row of not valid queen position. used when doing "stepBack"
	
	int currentColumn; // used when stepping back
	
	static Boolean doOutput = false; // as for now we do not want debug info
	
	public NQueens(int _N){
		N=_N;
		board = new int[N][N];
		
		line = new int[N]; 
	}
	
	// add one forbidden in x,y
	private void addOneForbidden(int x, int y) { 
		if(board[x][y] == -1) {
			output("queen allready exists in " + x + "," + y + " where we are about to add forbidden. exiting");
			errorExit();
		}
		
		board[x][y]++;
	}
	
	// subtract one forbidden in x,y
	private void subtractOneForbidden(int x, int y) {
		if(board[x][y] == -1) {
			output("queen allready exists in " + x + "," + y + " where we are about to substract forbidden. exiting");
			errorExit();
		}
		
		if(board[x][y] == 0) {
			output("nothing to substract at " + x + "," + y + " where we are about to substract forbidden. exiting");
			errorExit();
		}
		
		board[x][y]--;
	}
	
	// modify one forbidden in x,y with given mode
	private void modifyOneForbidden(int x, int y, AddOrSubtract mode) { 
		if(mode == AddOrSubtract.Add)
			addOneForbidden(x, y);
		else
			subtractOneForbidden(x, y);
	}
	
	// modify forbidden cells by Queen's chess definition
	private void modifyChessForbiddens(int x, int y, AddOrSubtract mode) {
		for(int xc=0 ; xc < N ; xc++) // horizontal forbidden
			if(xc != x)
				modifyOneForbidden(xc, y, mode);
		
		for(int yc=0 ; yc < N ; yc++) // vertical forbidden
			if(yc != y)
				modifyOneForbidden(x, yc, mode);
		
		for(int xc=0 ; xc < N ; xc++) { 
			int ly = xc - x + y;
			if(xc != x && ly >= 0 && ly < N) // \ forbidden
				modifyOneForbidden(xc, ly, mode);
			
			ly = (y - ly) + y;
			
			if(xc != x && ly >= 0 && ly < N) // / forbidden
				modifyOneForbidden(xc, ly, mode);
		}
	}
	
	// try to modify additional forbiddens for one queens pair
	//
	// if there is "forbidden hit on existing queen" false is returned. 
	// true if success
	private Boolean modifyAdditionalForbiddensForOnePair(PointPair pair, AddOrSubtract mode) {
		double determinant = (pair.a.y - pair.b.y) / (pair.a.x - pair.b.x);
		
		double a = determinant;
		
		double b = pair.a.y - determinant * pair.a.x;
		
		for(double xc=0; xc < N ; xc++)
			if(xc != pair.a.x && xc != pair.b.x){
				double y = a * xc + b;
			
				int xInBoard = (int)Math.floor(xc);
				int yInBoard = (int)Math.floor(y);
				
				if(Math.abs((double)xInBoard - xc) < 0.01 && Math.abs((double)yInBoard - y) < 0.01 // checking if there is "angle hit". need to have some comparsion margin, becouse of finite point floating point
				//if(Math.abs((double)xInBoard - xc) == 0 && Math.abs((double)yInBoard - y) == 0 // it works like this also, but should do more tests to be sure we can leave it this way
						&& yInBoard >= 0 && yInBoard < N) {
				
					if(board[xInBoard][yInBoard] == -1) // 3rd Queen in line
						return false;
				
					modifyOneForbidden(xInBoard, yInBoard, mode);
				}
			}
		
		return true;
	}
	
	// try to modify additional forbiddens for all the new pairs for the new quuen
	//
	// if there was "forbidden on existing queen" hit we need to do rollback, and return false
	// true otherwise
	private Boolean modifyAdditionalForbiddens(int x, int y, AddOrSubtract mode) {
		if(x > 0) {
			List<PointPair> pairs = new ArrayList<PointPair>();
			
			for(int xc = 0 ; xc < x ; xc++) 
				pairs.add(new PointPair(new Point(xc,line[xc] - 1), new Point(x,y)));
			
			for(PointPair pair : pairs) {				
				Boolean modifyOK = modifyAdditionalForbiddensForOnePair(pair, mode);
				
				if(!modifyOK && mode == AddOrSubtract.Add) {
					modifyAdditionalForbiddens(x, y, AddOrSubtract.Subtract); // rollback
					return false;
				}
				
				if(!modifyOK)
					return false;
			}
		}
		
		return true;
	}
	
	// put new queen in x,y
	public Boolean putQueen(int x, int y) {
		if(line[x] > 0 )  {
			output("queen exists in " + x + " column. exiting");
			errorExit();
		}
		
		Boolean succesInPuttingAdditionalForbiddens = modifyAdditionalForbiddens(x, y, AddOrSubtract.Add);
		
		if(succesInPuttingAdditionalForbiddens) {
			line[x] = y + 1;		
			
			if(board[x][y] > 0)  {
				output("forbidden sign allready exists in " + x + "," + y + " where we are about to put queen. exiting");
				errorExit();
			}
			
			board[x][y] = -1;
			
			modifyChessForbiddens(x, y, AddOrSubtract.Add);
			
			return true;
		}else
			return false;	
	}
	
	// remove queen from x, y
	private void removeQueen(int x, int y) {		
		line[x] = 0;
		
		if(board[x][y] != -1)  {
			output("expecting quuen at " + x + "," + y + ", but not found. exiting");
			errorExit();
		}
		
		board[x][y] = 0;
	
		modifyChessForbiddens(x, y, AddOrSubtract.Subtract);
		
		modifyAdditionalForbiddens(x, y, AddOrSubtract.Subtract);
	}
	
	// remove last added queen
	private void stepBack() {
		currentColumn--;
		
		if(currentColumn == -1) {
			doOutput = true;
			output("all the search space has been searched. solution not found. exiting");
			
			print();
			errorExit();
		}
		
		int row = line[currentColumn] - 1;
		
		removeQueen(currentColumn, row);
		
		line[currentColumn] = - row - 1; // -1 as row might be 0, and 0 is value is allready used
		
		if(currentColumn + 1 < N)
			line[currentColumn + 1] = 0; // need to clear memory for that one
	}
	
	// main logic
	public void solve() {
		currentColumn = 0;
		
		while(currentColumn < N) {
			
			output("currentColumn: " + currentColumn);
			
			int tryRow = 0;
			
			if(line[currentColumn] < 0) 
				tryRow = Math.abs(line[currentColumn]); // start from next row, so we are not subtracting 1 :)
					
			while(tryRow < N) {
				if(board[currentColumn][tryRow]==0) {
					Boolean succesInPuttingQueen = putQueen(currentColumn, tryRow);
					
					if(succesInPuttingQueen) {				
						output("queen put at: " + currentColumn + " row: " + tryRow);
					
						print();
					
						break;
					}
				}
				
				tryRow++;
			}
			
			if(tryRow == N) {
				output("can not put new queen in: " + currentColumn + " column. got to step back");
				
				stepBack();
			}else
				currentColumn++;
		}			
	}
	
	// print the board
	public void print() {
		String borderString="";
		
		for(int x=0; x < N ; x++)
			borderString += "+-";
		
		borderString += "+";
		
		for(int y=0; y < N ; y++) {
			output(borderString);
			
			String thisLine = "";
			
			for(int x = 0 ; x < N ; x++) {
				thisLine += "|";
				
				if(board[x][y] == 0)
					thisLine += " ";
				
				if(board[x][y] > 0 && board[x][y] < 999)
					thisLine += "*";
				
				if(board[x][y] == -1)
					thisLine += "O";
				
				if(board[x][y] < -1)
					thisLine += board[x][y];
			}
			
			thisLine += "|";
			
			output(thisLine);
		}
						
		output(borderString);
	}

	private static void output(String s) {
		if(doOutput)
			System.out.println(s);
	}
	
	private static void errorExit() {
		System.exit(-1);
	}
	
	public static void main(String[] args) {
		if(args.length == 0) {
			NQueens.doOutput = true;
			output("please specify N number as first parameter. exiting");
			errorExit();
		}
		
		String NasString = args[0];
		
		int N=0;
		
		try {
			N=Integer.parseInt(NasString);
		} catch (NumberFormatException e) {
			NQueens.doOutput = true;
			output("can not parse given parameter (should be int). exiting");
			errorExit();
		}
		
		NQueens instance = new NQueens(N);
		
		instance.solve();
		
		NQueens.doOutput = true;
		
		output("");
		output("*********** DONE **************");
		output("");
		
		instance.print();
	}

}
