package DataStructure;

public class TripartiteSet {
    public int size;
    public BipartiteSet bipartiteSet;

    public TripartiteSet(int[] value){
        this.size = value.length;
        this.bipartiteSet = new BipartiteSet(size);
        for(int i=0; i<size;i++){
            if(value[i]==0){
                bipartiteSet.remove(i);
            }
        }
    }
}
