import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.Integer.min;

public class Snippt {
    public static void main(String args[]) throws IOException, NoSuchFieldException, IllegalAccessException {
  /*      Wordhashing.intialize();
        File file2 = new File("G:\\3rd_year\\2nd_term\\APT\\project\\URLs\\httphcapacheorghttpcomponents-client-ga.txt");
        String data2 = new Scanner(file2).useDelimiter("\\A").next();

        ArrayList<String>temp=Wordhashing.escapeHtml(data2);
        Snippt. save_file( temp,"G:\\3rd_year\\2nd_term\\APT\\project\\txt\\www.fg.com.txt");
*/
     // get_snippt("G:\\\\3rd_year\\\\2nd_term\\\\APT\\\\project\\\\txt\\\\www.fg.com.txt",24);

    }
   public static void save_file (ArrayList<String> data,String link){

String filename=main.URLsFiles.get(link).toString();
String sy="G:\\\\3rd_year\\\\2nd_term\\\\APT\\\\project\\\\txt\\\\"+filename+".txt";
       BufferedWriter writer = null;
       try
       {
           writer = new BufferedWriter( new FileWriter( sy));
           String s="";
           for(int i=0;i<data.size();++i) {
               String[] temp= data.get(i).split(" ");
               if (temp[3].equals("9")&& data.get(i+1).charAt(data.get(i+1).length()-1)!='9')
               {

                   s+= temp[0]+" "+ '\n';
               }
             else{
                   s+=temp[0]+" ";


               }
           }
           writer.write( s);

       }
       catch ( IOException e)
       {
       }
       finally
       {
           try
           {
               if ( writer != null)
                   writer.close( );
           }
           catch ( IOException e)
           {
           }
       }

}


    public static Link get_snippt(String link,int position) throws IOException {

     String filename=main.URLsFiles.get(link).toString();
        File file = new File("G:\\\\3rd_year\\\\2nd_term\\\\APT\\\\project\\\\txt\\\\"+filename+".txt");

        BufferedReader br = new BufferedReader( new InputStreamReader(
                      new FileInputStream(file), "UTF8"));

        String st;
        String title="";
        String snippt="";
        if((st = br.readLine()) != null){
                title=st;
                snippt+=st;

        }
        while ((st = br.readLine()) != null)
          snippt+=st;

       String[] words= snippt.split(" ");
       snippt="";
       for(int i=position-1;i<Math.min(position+29,words.length);i++)
       {
           snippt+=words[i]+" ";
       }
       snippt+="...";
     return (new Link(link,snippt,title));
    }


}
