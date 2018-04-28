import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.json.JSONArray;
import java.io.PrintWriter;





@WebServlet(name = "AjaxRequest",urlPatterns = {"/JSON/AjaxRequest"})
public class AjaxRequest extends HttpServlet {


    private static  String[] COUNTRIES  ;
    public AjaxRequest() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setHeader("Cache-control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "-1");

        JSONArray arrayObj=new JSONArray();

        String query = request.getParameter("term");
        System.out.println(query);
        query = query.toLowerCase();
        COUNTRIES=Recommendations.autocomplete(query);
        for(int i=0; i<COUNTRIES.length; i++) {
            String country = COUNTRIES[i].toLowerCase();
            if(country.startsWith(query)) {
                arrayObj.put(COUNTRIES[i]);
            }
        }

        out.println(arrayObj.toString());
        out.close();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Do something
    }



}
