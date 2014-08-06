public class BRStringSearchAlgorithm extends StringSearchAbsAlgorithm {
    private int M;           // pattern length
    
    private long[] s;
    private char[] x;
    private int[][] brBc;

    public BRStringSearchAlgorithm() {}

    public BRStringSearchAlgorithm(String pat) {
        this.x = pat.toCharArray();
        this.s = new long[65536];
        M = x.length;
        
        brBc = new int[256][256];
        //preBrBc(x);        
        
    }
    
    private static int arrayCmp(char[] a, int aIdx, char[] b, int bIdx, int length) {
        int i = 0;
        for (i = 0; i < length && aIdx + i < a.length && bIdx + i < b.length; i++) {
            if (a[aIdx + i] == b[bIdx + i]){
                ;
            }
            else if (a[aIdx + i] > b[bIdx + i])
                return 1;
            else
                return 2;
        }

        if (i < length)
            if (a.length - aIdx == b.length - bIdx)
                return 0;
            else if (a.length - aIdx > b.length - bIdx)
                return 1;
            else
                return 2;
        else
            return 0;
    }
    
    private static int calculateBrBcSize(char[] x, char[] y) {
        int i;
        char maxChar = 0;
        for (i = 0; i < x.length; i++)
            if (x[i] > maxChar)
                maxChar = x[i];
        for (i = 0; i < y.length; i++)
            if (y[i] > maxChar)
                maxChar = y[i];
        maxChar++;
        return maxChar;
    }
    
    private  void preBrBc(char[] x) {
        int a, b, i;

        for (a = 0; a < brBc.length; ++a)
            for (b = 0; b < brBc.length; ++b)
                brBc[a][b] = M + 2;
        
        for (a = 0; a < brBc.length; ++a)
            brBc[a][x[0]] = M + 1;
        
        for (i = 0; i < M - 1; ++i)
            brBc[x[i]][x[i + 1]] = M - i;
        
        for (a = 0; a < brBc.length; ++a)
            brBc[x[M - 1]][a] = 1;
        
    }    


    // check for exact match
    public int search(String txt) {
        int N = txt.length(); 
        // no match, pattern longer then text
        if (N < M) return N;
        
        char[] src = txt.toCharArray();
        char[] y = new char[src.length + 2];
        System.arraycopy(src, 0, y, 0, src.length);
        
        if(calculateBrBcSize(x, src) > 256)
            return N;

        /* Preprocessing */
        preBrBc(x);

        /* Searching */
        int j = 0;
        y[N + 1] = '\0';
        while (j < N - M) {
                if (arrayCmp(x, 0, y, j, M) == 0)
                        return j;
                j += brBc[y[j + M]][y[j + M + 1]];
        }
        if(j == N - M && arrayCmp(x, 0, y, j, M) == 0)
                return j;        
        // no match
        return N;
    }
    
    @Override
    protected void execute(String text, String pattern, int offset){
        BRStringSearchAlgorithm searcher = new BRStringSearchAlgorithm(pattern);
        offset = searcher.search(text);
    }
    
    // test client
    public static void main(String[] args) {
        String pat, txt;
        
        if(args.length > 1){
            pat = args[0];
            txt = args[1];
        }
        else{
            pat = "abc";
            txt = "fgdfsgfabcsdfgsdf";
        }

        BRStringSearchAlgorithm searcher = new BRStringSearchAlgorithm(pat);
        int offset = searcher.search(txt);

        // print results
        System.out.println("text:    " + txt);

        System.out.print("pattern: ");
        for (int i = 0; i < offset; i++)
            System.out.print(" ");
        System.out.println(pat);
    }
}