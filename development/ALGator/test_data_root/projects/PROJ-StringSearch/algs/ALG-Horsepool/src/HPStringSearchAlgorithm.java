public class HPStringSearchAlgorithm extends StringSearchAbsAlgorithm {
    private int M;           // pattern length

    private char[] x;
    private int[] bmBc;

    public HPStringSearchAlgorithm() {}

    public HPStringSearchAlgorithm(String pat) {
        this.x = pat.toCharArray();
        M = x.length;      
        bmBc = new int[65536];       
    }

    private void preBmBc(char[] x) {
            int i = x.length;
            //M = x.length;
            for (i = 0; i < bmBc.length; ++i)
                    bmBc[i] = M;
            for (i = 0; i < M - 1; ++i)
                    bmBc[x[i]] = M - i - 1;
    }
    
    private static int arrayCmp(char[] a, int aIdx, char[] b, int bIdx, int length) {
        int i = 0;

        for (i = 0; i < length && aIdx + i < a.length && bIdx + i < b.length; i++) {
            if (a[aIdx + i] == b[bIdx + i])
                    ;
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
    
    // check for exact match
    public int search(String txt) {
        int N = txt.length();
        // no match, pattern longer then text
        if (N < M) return N;
        
        char[] y = txt.toCharArray();
        char c;
        
        /* Preprocessing */
        preBmBc(x);

        /* Searching */
        int j = 0;
        while (j <= N - M) {
            c = y[j + M - 1];
            if (x[M - 1] == c && arrayCmp(x, 0, y, j, (M - 1)) == 0)
                return j;
            j += bmBc[c];
        }
        // no match
        return N;
    }


    @Override
    protected void execute(String text, String pattern, int offset){
        HPStringSearchAlgorithm searcher = new HPStringSearchAlgorithm(pattern);
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

        HPStringSearchAlgorithm searcher = new HPStringSearchAlgorithm(pat);
        int offset = searcher.search(txt);

        // print results
        System.out.println("text:    " + txt);

        System.out.print("pattern: ");
        for (int i = 0; i < offset; i++)
            System.out.print(" ");
        System.out.println(pat);
    }
}