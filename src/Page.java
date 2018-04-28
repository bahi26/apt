public class Page implements Comparable<Page>  {
    public String page;
    public int img,positian;
    public double rank;

    public Page(String page,double rank,int img,int position)
    {
        this.page=page;
        this.rank=rank;
        this.img=img;
        this.positian=position;
    }
    public int compareTo(Page page2)
    {
        if(page2.rank<this.rank)
            return 1;
        else
            return -1;
    }
}
