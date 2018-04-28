import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class RankPopularity {


    static boolean [][] Pages = new boolean[5001][5001];

    double [] Page_Rank =new double[5001];
    static Map <Integer,Integer> page_OutLinks=new HashMap<Integer,Integer>();  // page no. , no.of outLinks

    /****************************     Calculate Page Popularity    ***********************************************/

    public void Calc_RankPopularity(){

        File file_ForIndexer = new File(Crawler.Indexer);

        try {

            System.out.println("Enter Read from For Indexer1");
            Scanner scanner1 = new Scanner(file_ForIndexer);
            while (scanner1.hasNextLine()) {

                String[] splitStr= scanner1.nextLine().trim().split("\\s+");
                System.out.println(splitStr[0]);
                String Url_From_name = splitStr[0];


                int Url_From_num =Crawler.ForFiles.get(Url_From_name); 		// or can be get form splitStr[1]




                //ArrayList<String> Urls_Visited =Crawler.UrlFromTo.get(Url_From_name);
                ArrayList<String> Urls_Visited=new ArrayList<String>();
                Urls_Visited.addAll(Crawler.UrlFromTo.get(Url_From_name));
                System.out.println("Url_From_num " + Url_From_num);
                System.out.println("Urls_Visited " + Urls_Visited.size());


                page_OutLinks.put(Url_From_num, Urls_Visited.size());


                for(int i=0;i<Urls_Visited.size();i++)
                {

                    String to_Url_name =Urls_Visited.get(i);
                    System.out.println("to_Url_name "+to_Url_name);


                    if( Crawler.ForFiles.containsKey(to_Url_name))  //if found this url in 12 crawled
                    {
                        /* Fill adj Matrix*/
                        int to_Url_num =Crawler.ForFiles.get(to_Url_name);
                        System.out.println("Url_From_num is "+ Url_From_num + "to_Url_num is "+to_Url_num);
                        Pages[Url_From_num][to_Url_num] =true;

                    }

                }
            }
            scanner1.close();
        } catch (FileNotFoundException e1) {
// TODO Auto-generated catch block
            e1.printStackTrace();
            System.out.println("Enter Exception of files1");
        }


// initially PR(Page) = 1/ no.of crawled Pages

        for(int i=0;i<5001;i++)    //5001
        {
            Page_Rank[i]=1.0/5000;

        }

//////////////////////////////////


        for(int k=0;k<10;k++)   //to make values app. settle
        {

            for(int i=1;i<5001;i++)
            {

                for(int j=1;j<5001;j++)
                {
                    if(Pages[j][i] == true)
                    {

                        Page_Rank[i] += (Page_Rank[j]/page_OutLinks.get(j));

                    }

                }
            }

        }


/*******************************************************************************************************/

/*********                                      write  OutLinks  and  PagePopularity Files              *******/

        FileWriter Fr1,Fr2;
//print file of no.of out links
        try {

            System.out.println("Enter Read from For Indexer");
            Fr1 = new FileWriter("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\"+"OutLinks.txt");
            Fr2 = new FileWriter("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\"+"PagePopularity.txt");

            File file = new File(Crawler.Indexer);
            Scanner scanner = new Scanner(file);
            int pageNum =1;
            BufferedWriter br1 = new BufferedWriter(Fr1);
            BufferedWriter br2 = new BufferedWriter(Fr2);

            while (scanner.hasNextLine()) {
                String[] splitStr= scanner.nextLine().trim().split("\\s+");
                String Url = splitStr[0];

                System.out.println("Rank " + pageNum + " "+Page_Rank[pageNum]);
                System.out.println(Url+" "+Crawler.NoLinks.get(Url));

                br1.write(Url+" "+Crawler.NoLinks.get(Url));
                br1.newLine();
                br2.write(pageNum + " "+Page_Rank[pageNum]);
                br2.newLine();
                pageNum++;


            }


            br1.close();
            br2.close();
            scanner.close();
            Fr1.close();
            Fr2.close();

        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Enter Exception of files");
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Enter Exception of files");
        }


    }

}

