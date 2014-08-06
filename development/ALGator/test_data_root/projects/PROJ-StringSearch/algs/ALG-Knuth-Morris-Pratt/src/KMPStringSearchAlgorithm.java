/***************************************************************
 *  Compilation:  javac KMP.java
 *  Execution:    java KMP pattern text
 *
 *  Reads in two strings, the pattern and the input text, and
 *  searches for the pattern in the input text using the
 *  KMP algorithm.
 *
 *  % java KMP abracadabra abacadabrabracabracadabrabrabracad
 *  text:    abacadabrabracabracadabrabrabracad 
 *  pattern:               abracadabra          
 ***************************************************************/

public class KMPStringSearchAlgorithm extends StringSearchAbsAlgorithm{
    private int R;       // the radix
    private int[][] dfa;       // the KMP automoton

    private String pat;        // the pattern string

    // create the DFA from a String
    public KMPStringSearchAlgorithm(String pat) {
        this.R = 256;
        this.pat = pat;

        // build DFA from pattern
        int M = pat.length();
        dfa = new int[R][M]; 
        dfa[pat.charAt(0)][0] = 1; 
        for (int X = 0, j = 1; j < M; j++) {
            for (int c = 0; c < R; c++) 
                dfa[c][j] = dfa[c][X];     // Copy mismatch cases. 
            dfa[pat.charAt(j)][j] = j+1;   // Set match case. 
            X = dfa[pat.charAt(j)][X];     // Update restart state. 
        } 
    } 

    // empty constructor
    public KMPStringSearchAlgorithm() {
    } 

    // return offset of first match; N if no match
    public int search(String txt) {
        // simulate operation of DFA on text
        int M = pat.length();
        int N = txt.length();
        int i, j;
        for (i = 0, j = 0; i < N && j < M; i++) {
            j = dfa[txt.charAt(i)][j];
        }
        if (j == M) return i - M;    // found
        return N;                    // not found
    }

    @Override
    protected void execute(String text, String pattern, int offset){
        KMPStringSearchAlgorithm searcher = new KMPStringSearchAlgorithm(pattern);
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

        KMPStringSearchAlgorithm kmp = new KMPStringSearchAlgorithm(pat);
        int offset = kmp.search(txt);
        
        // print results
        System.out.println("text:    " + txt);

        System.out.print("pattern: ");
        for (int i = 0; i < offset; i++)
            System.out.print(" ");
        System.out.println(pat);
    }
}
