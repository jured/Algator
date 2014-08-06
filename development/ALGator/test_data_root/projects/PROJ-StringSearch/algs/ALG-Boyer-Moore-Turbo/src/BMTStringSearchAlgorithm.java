public class BMTStringSearchAlgorithm extends StringSearchAbsAlgorithm {
    private int M;           // pattern length
    
    private int shift;
    private char[] x;
    private int[] bmBc;

    public BMTStringSearchAlgorithm() {}

    public BMTStringSearchAlgorithm(String pat) {
        this.x = pat.toCharArray();
        M = x.length;
        
        bmBc = new int[65536];
//        preBmBc(x);
//        shift = bmBc[x[M - 1]];
//        bmBc[x[M - 1]] = 0;
        
    }
    
    private static int arrayCmp(char[] a, int aIdx, char[] b, int bIdx, int length) {
        int i = 0;
        for (i = 0; i < length && aIdx + i < a.length && bIdx + i < b.length; i++) {
            if (a[aIdx + i] == b[bIdx + i]){
                int temp = 1;
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
    
    private void preBmBc(char[] x) {
        int i;
        for (i = 0; i < bmBc.length; ++i)
            bmBc[i] = M;
        for (i = 0; i < M - 1; ++i)
            bmBc[x[i]] = M - i - 1;
    }

    // check for exact match
    public int search(String txt) {
        int N = txt.length(); 
        // no match, pattern longer then text
        if (N < M) return N;
        
        char[] src = txt.toCharArray();
        char[] y = new char[src.length + x.length];
        System.arraycopy(src, 0, y, 0, src.length);
        int j, k;

        /* Preprocessing */
        preBmBc(x);
        shift = bmBc[x[M - 1]];
        bmBc[x[M - 1]] = 0;
        for(int i = 0; i < M; i++)
            y[N + i] = x[M - 1];

        /* Searching */
        j = 0;
        while (j < N) {
            k = bmBc[y[j + M - 1]];
            while (k != 0) {
                j += k;
                k = bmBc[y[j + M - 1]];
                j += k;
                k = bmBc[y[j + M - 1]];
                j += k;
                k = bmBc[y[j + M - 1]];
            }
            if (arrayCmp(x, 0, y, j, (M - 1)) == 0 && j + M - 1 < N)
                return j;
            j += shift; /* shift */
        }        
        // no match
        return N;
    }
    
    @Override
    protected void execute(String text, String pattern, int offset){
        BMTStringSearchAlgorithm searcher = new BMTStringSearchAlgorithm(pattern);
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

        BMTStringSearchAlgorithm searcher = new BMTStringSearchAlgorithm(pat);
        int offset = searcher.search(txt);

        // print results
        System.out.println("text:    " + txt);

        System.out.print("pattern: ");
        for (int i = 0; i < offset; i++)
            System.out.print(" ");
        System.out.println(pat);
    }
}