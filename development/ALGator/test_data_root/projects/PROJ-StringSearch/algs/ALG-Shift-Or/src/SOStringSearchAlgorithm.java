public class SOStringSearchAlgorithm extends StringSearchAbsAlgorithm {
    private String pat;      // the pattern
    private int M;           // pattern length
    
    private long[] s;
    private long lim;
    private char[] x;

    public SOStringSearchAlgorithm() {}

    public SOStringSearchAlgorithm(String pat) {
        this.x = pat.toCharArray();
        this.pat = pat;
        this.s = new long[65536];
        M = x.length;
        this.lim = preSo(x, s);
    }
    
    private long preSo(char[] x, long[] s) {
        long j;
        int i;
        for (i = 0; i < s.length; ++i)
                s[i] = ~0;
        for (lim = i = 0, j = 1; i < M; ++i, j <<= 1) {
                s[x[i]] &= ~j;
                lim |= j;
        }
        lim = ~(lim >> 1);
        return(lim);
    }

    // check for exact match
    public int search(String txt) {
        int N = txt.length(); 
        // no match, pattern longer then text
        if (N < M) return N;
        
        char[] y = txt.toCharArray();
        long state;
        s = new long[65536];
        int j = x.length, n = y.length;
        M = x.length;

        /* Preprocessing */
        lim = preSo(x, s);

        /* Searching */
        for (state = ~0, j = 0; j < n; ++j) {
                state = (state << 1) | s[y[j]];
                if (state < lim)
                        return j - M + 1;
        }        
        // no match
        return N;
    }
    

    
    @Override
    protected void execute(String text, String pattern, int offset){
        SOStringSearchAlgorithm searcher = new SOStringSearchAlgorithm(pattern);
        offset = searcher.search(text);
    }
    
    // test client
    public static void main(String[] args) {
        String pat;
        String txt;
        
        if(args.length > 1){
            pat = args[0];
            txt = args[1];
        }
        else{
            pat = "abc";
            txt = "fgdfsgfabcsdfgsdf";
        }

        SOStringSearchAlgorithm searcher = new SOStringSearchAlgorithm(pat);
        int offset = searcher.search(txt);

        // print results
        System.out.println("text:    " + txt);

        System.out.print("pattern: ");
        for (int i = 0; i < offset; i++)
            System.out.print(" ");
        System.out.println(pat);
    }
}