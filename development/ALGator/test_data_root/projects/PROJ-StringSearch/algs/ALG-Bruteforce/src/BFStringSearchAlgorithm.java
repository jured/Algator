
public class BFStringSearchAlgorithm extends StringSearchAbsAlgorithm {
    private String pat;      // the pattern
    private int M;           // pattern length

    public BFStringSearchAlgorithm() {}

    public BFStringSearchAlgorithm(String pat) {
        this.pat = pat;
        this.M = pat.length();
    }

    // check for exact match
    public int search(String txt) {
        int N = txt.length(); 
        // no match, pattern longer then text
        if (N < M) return N;
        
        for(int i = 0; i < N-M; i++ ){
            int j = 0;
            while(j < M && txt.charAt(i+j) == pat.charAt(j)){
                j++;
            }
            if(j == M) return i;
        }
        // no match
        return N;
    }
    @Override
    protected void execute(String text, String pattern, int offset){
        BFStringSearchAlgorithm searcher = new BFStringSearchAlgorithm(pattern);
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

        BFStringSearchAlgorithm searcher = new BFStringSearchAlgorithm(pat);
        int offset = searcher.search(txt);

        // print results
        System.out.println("text:    " + txt);

        System.out.print("pattern: ");
        for (int i = 0; i < offset; i++)
            System.out.print(" ");
        System.out.println(pat);
    }
}