
public class BF2StringSearchAlgorithm extends StringSearchAbsAlgorithm {
    private String pat;      // the pattern
    private int M;           // pattern length

    public BF2StringSearchAlgorithm() {}

    public BF2StringSearchAlgorithm(String pat) {
        this.pat = pat;
        this.M = pat.length();
    }

    // check for exact match
    public int search(String txt) {
        int M = pat.length();
        int N = txt.length();
        int i, j;
        for (i = 0, j = 0; i < N && j < M; i++) {
            if (txt.charAt(i) == pat.charAt(j)) j++;
            else { i -= j; j = 0;  }
        }
        if (j == M) return i - M;    // found
        else        return N;        // not found
    }
    @Override
    protected void execute(String text, String pattern, int offset){
        BF2StringSearchAlgorithm searcher = new BF2StringSearchAlgorithm(pattern);
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

        BF2StringSearchAlgorithm searcher = new BF2StringSearchAlgorithm(pat);
        int offset = searcher.search(txt);

        // print results
        System.out.println("text:    " + txt);

        System.out.print("pattern: ");
        for (int i = 0; i < offset; i++)
            System.out.print(" ");
        System.out.println(pat);
    }
}