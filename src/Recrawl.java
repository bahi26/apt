import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;


public class Recrawl implements Runnable{



    public void run()
    {
        System.out.println("I am thread "+ Thread.currentThread().getName()+ " in reCrawl");
        Set<String> Keys=Crawler.Visited.keySet();   // keys of Visited is url string
        FileWriter fil;
        Crawler.Arr_url= Keys.toArray(new String[Keys.size()]);


        while(Crawler.Cnt.get()< 5000)
        {
            String key=null;
            synchronized(Crawler.lock)        //to guarantee each thread will check and download for different page
            {

                System.out.println("Cnt is "+ Crawler.Cnt.get()+ " in reCrawl");
                key=Crawler.Arr_url[Crawler.Cnt.get()];
                Crawler.Cnt.incrementAndGet();

            }
            String Page=null;
            try {

                Page = Crawler.downloadPage( key);
                int newCompactString_URL=Page.hashCode();
                if( ! Crawler.Visited.containsValue(newCompactString_URL))
                {

                    ArrayList<String> VisitedUrls_FromFrontUrl = new ArrayList<String>();

                    VisitedUrls_FromFrontUrl=Crawler.ExtractLinks(key,Page);

                    synchronized(Crawler.lock_UrlFromTo)
                    {

                        Crawler.UrlFromTo.put(key, VisitedUrls_FromFrontUrl);
                    }

                    String lineseparator=System.getProperty("line.separator");
                    FileWriter fw = new FileWriter(Crawler.Updates,true); //the true will append the new data
                    fw.write((String) key);//appends the string to the file
                    fw.write(lineseparator);
                    fw.close();

                    fil = new FileWriter("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\"+Crawler.ForFiles.get(key)+".txt");
                    BufferedWriter br = new BufferedWriter(fil);
                    br.write(Page);
                    br.close();
                    fil.close();


                }
            } catch (IOException e1) {

                Page=null;                                   //Akeed hy3rf y download l2n el url sleem

            }
        }

        System.out.println("a thread  with name : "+ Thread.currentThread().getName()+" finished reCrawling");



    }


}

