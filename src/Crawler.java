import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;



public class Crawler implements Runnable {

    private static Queue<String>URLs=new LinkedList<String>();

    public static Map <String,Integer> ForFiles= new HashMap<String,Integer>();
    public static String Indexer="G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\ForIndexer.txt";
    private static String Data="G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\URLs.txt";
    public static String Updates="G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\Updates.txt";
    public static Map <String,Integer> Visited= new HashMap<String,Integer>();
    private  static int NoCrawledPage=new Integer(0);         //ab2a a3mlo private
    private static Map <String,String> downloadedPages= new HashMap<String,String>();   //url -> html page
    public static Object lock = new Object();
    private static Object lock2 = new Object();
    public static Object lock_ExitRecrawl = new Object();
    private static Object lock_URL = new Object();
    private static Object lock_NoLinks= new Object();
    private static Object lock_Contain_vids= new Object();
    public static Object lock_UrlFromTo =new Object();
    public static Queue<String>EnterExitRecrawl=new LinkedList<String>();
    private int times_reCrawl = 0;       //msh static 3shan msh kolo y increment in same instance
    public static AtomicInteger Cnt=new AtomicInteger(0);
    public static  String[] Arr_url=new  String[5001];
    public static Map <String,Integer> NoLinks= new HashMap<String,Integer>();  //url -> no.of links
    private static Map <String,Integer> Contain_vids= new HashMap<String,Integer>();  //url -> no.of links
    private static Object lock_write =new Object();
    public static Map<String, ArrayList<String>> UrlFromTo = new HashMap<>();



    public void run() {

        if(NoCrawledPage >= 4999)
        {
            System.out.println("Crawler Finished");
            return;

        }


        String front_URL = null;
        int CompactString_URL=0;

        synchronized(lock)      //all thread have the same lock (static lock)
        {

            front_URL=URLs.poll();

        }


        if(Visited.containsKey(front_URL))
        {
            synchronized(lock_NoLinks)
            {
                NoLinks.put(front_URL,NoLinks.get(front_URL) + 1);
            }
        }

        if(front_URL != null && !Visited.containsKey(front_URL))    /*       edited       */
        {

            if(NoCrawledPage  < 44)     //44 is the number of seed set
                synchronized(lock_NoLinks)
                {
                    NoLinks.put(front_URL, 0);
                }
            else
                synchronized(lock_NoLinks)
                {
                    NoLinks.put(front_URL, 1);
                }
            String Page=null;
            try {
                Page = downloadPage(front_URL);
                if(Page.length()<1024)
                    Page= null;
                else
                    CompactString_URL=Page.hashCode();
            } catch (IOException e1) {

                Page=null;
                //e1.printStackTrace();
            }
            boolean safe=false;
            try {

                safe = robotSafe(front_URL);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }




            //System.out.println(front_URL);


            if(safe&&Page!=null &&!Visited.containsValue(front_URL) &&NoCrawledPage < 5000)
            {

                //   System.out.println("I'll go to ExtractLinks");

                Document doc = Jsoup.parse(Page);
                Elements vids_iframe = doc.select("iframe");
                Elements vids_video = doc.select("video");

                //System.out.println(vids.size());
                if(vids_iframe.size() == 0 && vids_video.size() == 0)   //no video tag found
                {

                    synchronized(lock_Contain_vids)
                    {
                        Contain_vids.put(front_URL, 0);
                    }
                }
                else
                {
                    //System.out.println("vids  not Found");
                    synchronized(lock_Contain_vids)
                    {
                        Contain_vids.put(front_URL, 1);
                    }
                }


                int t;

                ArrayList<String> VisitedUrls_FromFrontUrl = new ArrayList<String>();

                VisitedUrls_FromFrontUrl=ExtractLinks(front_URL,Page);

                synchronized(lock_UrlFromTo)
                {

                    UrlFromTo.put(front_URL, VisitedUrls_FromFrontUrl);

                    FileWriter fr;
                    try{
                        fr=new FileWriter("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\list.txt",true);
                        BufferedWriter br=new BufferedWriter(fr);
                        String temp= front_URL+" ";
                        for(String g: VisitedUrls_FromFrontUrl){

                            temp+=(g+" ");

                        }
                        br.write(temp+"\n");
                        br.close();
                        fr.close();
                    }
                    catch(Exception e){}
                }

                synchronized(lock2)
                {
                    t= ++NoCrawledPage;

                }


                if(NoCrawledPage >= 4999)
                {
                    System.out.println("Crawler Finished");
                    return;
                }


                Visited.put(front_URL , CompactString_URL);


                FileWriter fr;
                try {

                    synchronized(lock_write)
                    {

                        fr = new FileWriter("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\"+t+".txt");
                        ForFiles.put(front_URL, t);
                        BufferedWriter br = new BufferedWriter(fr);
                        br.write(Page);
                        br.close();
                        fr.close();

                        String lineseparator=System.getProperty("line.separator");
                        FileWriter URL_For_Indexer = new FileWriter(Indexer,true); //the true will append the new data
                        URL_For_Indexer.write(front_URL+" "+t+" "+Contain_vids.get(front_URL));//appends the string to the file
                        URL_For_Indexer.write(lineseparator);
                        URL_For_Indexer.close();


                        FileWriter fw = new FileWriter(Data,true); //the true will append the new data
                        fw.write(front_URL+" 1 "+CompactString_URL +" "+ NoLinks.get(front_URL));//appends the string to the file
                        fw.write(lineseparator);
                        fw.close();
                    }

                } catch (IOException ex) {
                    //Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
                    synchronized(lock2)
                    {NoCrawledPage--;}
                    System.err.println("There is a problem in crawling this URL"+front_URL);
                }



                System.out.println("NoCrawledPage : " +t+"in thread "+Thread.currentThread().getName() );


            }
        }

        while(true)
        {

            System.out.println("Crawler here  " + URLs.size());
            System.out.println("NoCrawledPage "+ NoCrawledPage);
            if(URLs.size()>0)      //msh 3aizah y run tol ma mafish links 3shan my3mlsh overflow
                break;
        }
        run();
    }



    public static String downloadPage(final String URL) throws IOException {
        String line = "", all = "";
        URL myUrl = null;
        BufferedReader in = null;

        myUrl = new URL(URL);
        in = new BufferedReader(new InputStreamReader(myUrl.openStream()));

        while ((line = in.readLine()) != null) {
            all += line;
        }

        if (in != null) {
            in.close();
        }


        // System.out.println(downloadedPages.size());
        return all;

    }

    public static  ArrayList<String> ExtractLinks(String FrontUrl,String Page)
    {


        //System.out.println("I'll add new link " + b);
        synchronized(lock_URL)                                              /*edited*/
        {
            Document doc = Jsoup.parse(Page);
            Elements links = doc.select("a[href]");

            ArrayList<String> VisitedUrls = new ArrayList<String>();   //gehad

            for (Element link : links) {

                String ExtractedLink= link.attr("abs:href");


                // add in UrlFromTo
                if( !link.attr("abs:href").contains(" ")&&(link.attr("abs:href").startsWith("https")||link.attr("abs:href").startsWith("http")))
                {
                    if(!ExtractedLink.contains(".pdf")&&!ExtractedLink.contains(".PDF"))
                    {
                        VisitedUrls.add(ExtractedLink);
                    }
                }



                if(!URLs.contains(link.attr("abs:href"))&&!link.attr("abs:href").contains(" ")&&(link.attr("abs:href").startsWith("https")||link.attr("abs:href").startsWith("http")))
                {
                    if(!ExtractedLink.contains(".pdf")&&!ExtractedLink.contains(".PDF"))
                    {
                        URLs.add(ExtractedLink);
                        try
                        {

                            String lineseparator=System.getProperty("line.separator");
                            FileWriter fw = new FileWriter(Data,true); //the true will append the new data
                            fw.write(ExtractedLink+" 0");//appends the string to the file
                            fw.write(lineseparator);
                            fw.close();
                        }
                        catch(IOException ioe)
                        {
                            System.err.println("IOException: " + ioe.getMessage());
                        }
                    }
                }
            }

            return    VisitedUrls;

        }



        //System.out.println(Visited.size());
    }

    public static boolean robotSafe(String myurl) throws MalformedURLException
    {
        //System.out.println(myurl);
        URL url= new URL(myurl);

        String strHost = url.getHost();
        String strRobot = "https://" + strHost + "/robots.txt";


        URL urlRobot;
        try { urlRobot = new URL(strRobot);
        } catch (MalformedURLException e) {
            return false;
        }

        String strCommands;
        try {
            strCommands = downloadPage(strRobot);

        } catch (IOException ex) {
            //System.err.println("No Robot.txt for this URL  "+ myurl);
            return true;
        }

        if (strCommands.contains("Disallow")) // if there are no "disallow" values, then they are not blocking anything.
        {
            String[] split = strCommands.split("\n");
            ArrayList<String> robotRules = new ArrayList<>();
            String mostRecentUserAgent = null;
            for (int i = 0; i < split.length; i++)
            {
                String line = split[i].trim();

                if (line.toLowerCase().startsWith("user-agent"))
                {

                    int start = line.indexOf(":") + 1;
                    int end   = line.length();
                    mostRecentUserAgent = line.substring(start, end).trim();
                    if(!mostRecentUserAgent.equals("*"))
                        mostRecentUserAgent=null;

                }
                else if (line.startsWith("Disallow")) {
                    if (mostRecentUserAgent != null) {
                        String r = new String();

                        int start = line.indexOf(":") + 1;
                        int end   = line.length();
                        r = line.substring(start, end).trim();
                        robotRules.add(r);
                        //System.out.println(r);
                    }
                }
            }

            for (String robotRule : robotRules)
            {

                String path = url.getPath();
                //System.out.println(path);
                if (robotRule.length() == 0) return true; // allows everything if BLANK
                if (robotRule == "/") return false;       // allows nothing if /

                if (robotRule.length() <= path.length())
                {
                    String pathCompare = path.substring(0, robotRule.length());


                    if (pathCompare.equals(robotRule)) { //System.out.println(robotRule);
                        return false;}
                }
            }
        }

        return true;
    }



    public static void main(String[] args) throws InterruptedException  {


        RankPopularity PR=new RankPopularity();

        String line = null;
        String line2 = null;

        try {


            FileReader fileReader2 =
                    new FileReader(Indexer);




            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader2 =
                    new BufferedReader(fileReader2);

            while((line2 = bufferedReader2.readLine()) != null) {

                String[] splitStr2 = line2.trim().split("\\s+");
                ForFiles.put(splitStr2[0],Integer.parseInt(splitStr2[1]));
            }

            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(Data);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                String[] splitStr = line.trim().split("\\s+");
                if(splitStr[1].equals("0"))
                {
                    URLs.add(splitStr[0]);
                }
                else
                {


                    Visited.put(splitStr[0], Integer.parseInt(splitStr[2]));
                    if(URLs.contains(splitStr[0]))
                        URLs.remove(splitStr[0]);

                    //NoLinks.put(splitStr[0], Integer.parseInt(splitStr[3]));

                }
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            Data + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + Data + "'");

        }


        try{

            FileReader fileReader3 =
                    new FileReader("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\list.txt");

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader3 =
                    new BufferedReader(fileReader3);
            String g;
            ArrayList<String> visited_links=new ArrayList<String>();
            String[] links = null;
            while((g= bufferedReader3.readLine())!=null){


            links=g.split(" ");
            String te="";
            if(!links[0].startsWith("h"))
                te=links[0].substring(1,links[0].length());
            else
                te=links[0];
                for(int i=1;i<links.length;++i){visited_links.add(links[i]); }


                UrlFromTo.put(te,visited_links);
            }

            bufferedReader3.close();
            fileReader3.close();
        }
        catch(Exception e){


        }

        NoCrawledPage=Visited.size();

        System.out.println("Visited URLs size is  : "+Visited.size());

        System.out.println("Enter number of the threads you want : ");
        Scanner reader = new Scanner(System.in);
        int Thread_num = reader.nextInt();

        Thread myThreads[] = new Thread[Thread_num];

        for(int i=0;i<Thread_num;i++)
        {

            myThreads[i] = new Thread(new Crawler());
            myThreads[i].start();

        }

        for(int i=0;i<Thread_num;i++)
        {

            myThreads[i].join();

        }


        PR.Calc_RankPopularity();

        for(int k=0;k<2;k++)       //no.of reCrawl
        {

            for(int i=0;i<Thread_num;i++)
            {

                myThreads[i] = new Thread(new Recrawl());
                myThreads[i].start();

            }

            for(int i=0;i<Thread_num;i++)
            {

                myThreads[i].join();

            }

            PR.Calc_RankPopularity();

        }



    }

}
