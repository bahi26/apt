import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import com.mongodb.*;
import com.mongodb.client.model.*;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;

public class main {
    static boolean check=false;
    static MongoClient mongoClient = new MongoClient();
     public static  Map<String,Integer>URLsFiles =null;

    static DB database = mongoClient.getDB("search_engine");
    static DBCollection pages=database.getCollection("pages");
    static DBCollection words=database.getCollection("words");
    static indexer Indexer = new indexer(pages.find().toArray(),words);

    static void set_Data(String [] links)
    {
        try
        {
            Document doc;
            BufferedWriter  file;
            for (String link : links)
            {
                doc = (Document)Jsoup.connect(link).get();
                String name=link.replaceAll("[\\.$|,|;|'?*/:]", "");
                file = new BufferedWriter( new FileWriter("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\"+name+".txt"));
                file.write(doc.toString());
                file.close();
            }

        }
        catch(IOException ex)
        {
            System.out.println(ex.getMessage());
        }

    }


    static void update_Data(DBCollection words_collection,DBCollection pages_collection)
    {

        Map<String,ArrayList<DBObject>> innerObjects= new HashMap<String, ArrayList<DBObject>>() ;
        int index=0;
        Wordhashing.intialize();
        try{
            BufferedReader urls = new BufferedReader(new FileReader("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\ForIndexer.txt"));
            BufferedReader links = new BufferedReader(new FileReader("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\OutLinks.txt"));

            String line;
            List<DBObject> pages=new ArrayList();

            Map<String,List> words_list=new HashMap();
            List<String>page_urls=new ArrayList<String>();

            while ((line = urls.readLine()) != null)
            {

                String [] splitArray=line.trim().split(" ");

                pages.add(new BasicDBObject().append("url",splitArray[0]).append("video",splitArray[2]).append("popularity",links.readLine()));
                page_urls.add(splitArray[0]);

                File file = new File("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\"+splitArray[1]+".txt");
                String data = new Scanner(file).useDelimiter("\\A").next();

                //  System.out.println(data);

                ArrayList<String> words=(ArrayList<String>) Wordhashing.escapeHtml(data);
                Snippt.save_file(words,splitArray[0]);
                System.gc();

                //  System.out.println(words);

                int words_size=words.size();
                for(int i=0;i<words_size;i++)
                {
                    String [] word_data=words.get(i).split(" ");
                    DBObject innerObject=new BasicDBObject("url",splitArray[0]).append("origin",word_data[1]).append("position",word_data[2]).append("rank",word_data[3]).append("size",words_size);
                    if(innerObjects.get(word_data[0])==null)
                        innerObjects.put(word_data[0],new ArrayList<DBObject>());

                    innerObjects.get(word_data[0]).add(innerObject);

                }
            }

            //remove url words
            DBObject updateQuery = new BasicDBObject("$pull",new BasicDBObject("list",new BasicDBObject("url",new BasicDBObject("$in",page_urls))));
            DBCollectionUpdateOptions options= new DBCollectionUpdateOptions().multi(true);
            words_collection.update(new BasicDBObject(),updateQuery,options);

            //remove old urls data and add the new ones
            BasicDBObject query = new BasicDBObject();
            query.put("url",new BasicDBObject("$in",page_urls));
            pages_collection.remove(query);
            pages_collection.insert(pages);
            page_urls.clear();
            System.gc();



            options= new DBCollectionUpdateOptions().upsert(true);
            int someIndex=0;
            System.out.println(innerObjects.size());
            for(Map.Entry<String,ArrayList<DBObject>>entry:innerObjects.entrySet())
            {
                entry.getKey();
                DBObject findQuery = new BasicDBObject("word",entry.getKey());
                words_collection.update(findQuery,new BasicDBObject("$push",new BasicDBObject("list",new BasicDBObject("$each",entry.getValue()))),options);
                System.out.println(++someIndex);
            }

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static Map<String,Integer> search (String query,int image,int video,boolean phrase) throws IOException {
        if(URLsFiles==null)
        {
            URLsFiles=new HashMap<>();
            BufferedReader urls = new BufferedReader(new FileReader("G:\\3rd_year\\2nd_term\\APT\\project\\edition\\input\\ForIndexer.txt"));
            String line;
            while ((line = urls.readLine()) != null)
            {
                String [] splitarray=line.split(" ");
                URLsFiles.put(splitarray[0],Integer.parseInt(splitarray[1]));

            }

        }
        return Indexer.search(query,image,video,phrase);
    }

    public static void main(String[] args) {

        try {

                update_Data(words,pages);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

}
