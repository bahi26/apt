import com.mongodb.*;
import com.mongodb.client.model.Filters;
import com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import org.json.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;




@WebServlet(name = "Recommendations",urlPatterns = {"/Recommend"})
public class Recommendations extends HttpServlet {


    static public void main(String args[]){
//String [] test= autocomplete("com");
//System.out.println(test[0]);
    //reco_list(" ");

    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
String user=request.getParameter("user");
        PrintWriter out = response.getWriter();

       ArrayList<String> data=reco_list(user);
        JSONArray arrayObj=new JSONArray();

        for (String temp:data) {
            arrayObj.put(temp);
        }
        out.println(arrayObj.toString());
        out.close();
    }
    static public ArrayList<String> reco_list ( String user_name) throws IOException {
        String temp="";
        Set<String> recommend= new HashSet<>();

        DBCursor cursorDocMap = intialize().find((DBObject) new BasicDBObject("user", user_name)).limit(20);

        while (cursorDocMap.hasNext()) {
          temp+=cursorDocMap.next().get("query").toString()+" ";

        }
        String temp2=Stemmer.stem(temp);

        //stemming
  String []words=temp2.trim().split(" ");

        for(int i=0;i<words.length;++i) {
            recommend.add(words[i]);

        }

        ArrayList<String>urls=new ArrayList<String>();
        String to_indexer="";
        Iterator iter = recommend.iterator();
        while (iter.hasNext()) {
           to_indexer+=(iter.next()+" ");
        }

        Map<String,Integer>test=new HashMap<>();
        int count=0;
        test= main.search(to_indexer,0,0,false );

        for(Map.Entry<String,Integer> entry : test.entrySet())
        {

        urls.add(entry.getKey());
        count++;
        if(count==6)
            break;
        }
        System.out.println("list: "+ urls.toString());
        //
        // send to indexer to make query
        //indexer
        return urls;
    }

static public void insertion(String query, String user){
    BasicDBObject entry=new BasicDBObject();
    entry.put("user",user.trim());
    entry.put("query",query.trim().toLowerCase());
        intialize().insert(entry);

}
    static public DBCollection intialize(){
        MongoClient mongo=new MongoClient("localhost",27017);
        DB db =mongo.getDB("search_engine");
        DBCollection collection =db.getCollection("recommendations");
        /*
    MongoClient client= new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        MongoDatabase mydb= client.getDatabase("search_engine");
        MongoCollection collection =mydb.getCollection("recommendations");
*/
  return collection;

}
static public String[] autocomplete(String s){
    Set<String> data= new HashSet<String>();
    DBCursor cursorDocMap = intialize().find((DBObject)new BasicDBObject("query",Pattern.compile(Pattern.quote( s))));
    while (cursorDocMap.hasNext()) {
        data.add(cursorDocMap.next().get("query").toString());
    }
    String[] stockArr = new String[data.size()];

//    System.out.println((String[]) data.toArray());
        return  data.toArray(stockArr);
    }


}
