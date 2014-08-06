public class ACStringSearchAlgorithm extends StringSearchAbsAlgorithm {
    private int M;           // pattern length
    private char[] x;        // pattern
    public int offset;

    public ACStringSearchAlgorithm() {}

    public ACStringSearchAlgorithm(String pat) {
        char[] ptrn = pat.toCharArray();
        this.x = new char[ptrn.length+1];        
        System.arraycopy(ptrn, 0, x, 0, ptrn.length);
        
        this.M = ptrn.length;                
    }

    private static void preKmp(char[] x, int[] kmpNext) {
        int i, j, m = (x.length - 1);
        i = 0;
        j = kmpNext[0] = -1;
        while (i < m) {
            while (j > -1 && x[i] != x[j])
                j = kmpNext[j];
            i++;
            j++;
            if (x[i] == x[j])
                kmpNext[i] = kmpNext[j];
            else
                kmpNext[i] = j;
        }
    }

    // check for exact match
    public int search(String txt) {
        int N = txt.length(); 
        // no match, pattern longer then text
        if (N < M) return N;
        
        char[] y = txt.toCharArray(); //text to search in

        int i, j, k, ell = x.length;

        int[] kmpNext = new int[x.length];

        /* Preprocessing */
        preKmp(x, kmpNext);
        for (ell = 1; x[ell - 1] == x[ell]; ell++)
            ;
        if (ell == M)
            ell = 0;

        /* Searching */
        i = ell;
        j = k = 0;
        while (j <= N - M) {
            while (i < M && x[i] == y[i + j])
                    ++i;
            if (i >= M) {
                    while (k < ell && x[k] == y[j + k])
                            ++k;
                    if (k >= ell)
                            return j;
            }
            j += (i - kmpNext[i]);
            if (i == ell)
                    k = Math.max(0, k - 1);
            else if (kmpNext[i] <= ell) {
                    k = Math.max(0, kmpNext[i]);
                    i = ell;
            } else {
                    k = ell;
                    i = kmpNext[i];
            }
        }      
        // no match
        return N;
    }
    
    @Override
    protected void execute(String text, String pattern, int offset){
        ACStringSearchAlgorithm searcher = new ACStringSearchAlgorithm(pattern);
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

        ACStringSearchAlgorithm searcher = new ACStringSearchAlgorithm(pat);
        int offset = searcher.search(txt);

        // print results
        System.out.println("text:    " + txt);

        System.out.print("pattern: ");
        for (int i = 0; i < offset; i++)
            System.out.print(" ");
        System.out.println(pat);
    }
}