import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



@WebServlet(name = "Search",urlPatterns = {"/Searching"})
public class Search extends HttpServlet {
    private static Queue<Link>URLs=new LinkedList<Link>();
    private static String query=null;
    Map<String,Integer>test=null;
    Page_data P;
    String htmlButton="";
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String Starting="<html>\n" +
                "<body background=\"background.jpg\" > <form action=\"Searching\"  method=\"GET\">";
        String submit= request.getParameter("search");
        boolean phraseS=false;
        int image=0,video=0;
             this.query=request.getParameter("query");


        String username=request.getParameter("user");
        int pagebutton=0;


        if(request.getParameter("button")!=null)
        {


            pagebutton=Integer.parseInt(request.getParameter("button"))-1;

        }



        if(submit!=null )
        {
            P=new Page_data();
            if(query!=null && username!=null){

                Recommendations.insertion(query,username);

                    if(query.startsWith("\"")&&query.endsWith("\""))
                        phraseS=true;
                    if(submit.equals("Image"))
                        image=1;
                    if(submit.equals("Video"))
                        video=1;
           test= main.search(this.query,image,video,phraseS );

          }
           // URLs.add(Snippt.  get_snippt("www.fg.com",24));
            for(Map.Entry<String,Integer> entry : test.entrySet())
            {
                URLs.add(Snippt.get_snippt(entry.getKey(),entry.getValue()));
            }
            if(URLs.size()==0)
            {
                Link temp=new Link("","","NO result found");
                URLs.add(temp);
            }
            int No_Pages=(int) Math.ceil((float)URLs.size()/10);
            String[] buttons=new String[No_Pages];

             htmlButton="";
            for(int i=0;i<No_Pages;i++)
            {

                htmlButton+="<input type=\"submit\" name=\""+"button"+"\"value=\""+(i+1)+"\"/>";
            }
            String pages="";

            int button_no=0;

              while (URLs.size() > 0) {
                  int page_no = 0;
                  while (page_no < 10 && !URLs.isEmpty()) {
                      Link k = URLs.poll();
                      pages += "<a href=" + k.get_link() + "><h2>" + k.get_title() + "</h2></a>"
                              + "<a href=" + k.get_link() + "><h5>" + k.get_link() +
                              "</h5></a>" + "<p>" + k.get_text() + "</p>";
                      page_no++;
                  }
                 P.buttons.add( pages);
                  //  pages="";
              }





            out.println(Starting+P.buttons.get(pagebutton)+htmlButton+"</body>\n" +
                    "</html>");




            /* ServletContext sc = getServletContext();
             sc.getRequestDispatcher("/redirect.html").forward(request, response);
             */
        }
else if(pagebutton>=0){

            out.println(Starting+P.buttons.get(pagebutton)+htmlButton+"</body>\n" +
                    "</html>");

        }

        /*


         */


        //processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
