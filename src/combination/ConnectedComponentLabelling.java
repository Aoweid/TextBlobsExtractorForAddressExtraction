package combination;


public class ConnectedComponentLabelling {
	//initiation
	// dx and dy show the 4 directions when doing neighborhood walking through
	int[] dx={+1, 0, -1, 0};
	int[] dy={0, +1, 0, -1};
	int row_count=0;
	int col_count=0;
	int[][] m;
	int[][] label;
	/**
	 * constructor
	 * @param row_count
	 * @param col_count
	 */
	public ConnectedComponentLabelling(int row_count,int col_count) {
		this.row_count=row_count;
		this.col_count=col_count;
		//m is for comparing
		m=new int[row_count][col_count];
		label=new int[row_count][col_count];
	}
	
	void dfs(int x, int y, int current_label) {
		  if (x < 0 || x == row_count) return; // out of bounds
		  if (y < 0 || y == col_count) return; // out of bounds
		  if (label[x][y]!=0 || m[x][y]!=1) return; // already labeled or not marked with 1 in m

		  // mark the current cell
		  label[x][y] = current_label;

		  // recursively mark the neighbors
		  int direction = 0;
		  for (direction = 0; direction < 4; ++direction)
		    dfs(x + dx[direction], y + dy[direction], current_label);
		}
	/**
	 * mark through label array
	 */
	void find_components() {
		  int component = 0;
		  for (int i = 0; i < row_count; ++i) 
		    for (int j = 0; j < col_count; ++j) 
		      if (label[i][j]==0 && m[i][j]==1) dfs(i, j, ++component);
		}
}