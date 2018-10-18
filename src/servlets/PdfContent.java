package servlets;

import controllers.SearchController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

@WebServlet(name = "PdfContent", urlPatterns = "/PdfContent")
public class PdfContent extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        processRequest(request, response);

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        try (OutputStream out = response.getOutputStream()) {

            SearchController searchController = (SearchController) request.getSession(false).getAttribute("searchController");

            Integer id = Integer.valueOf(request.getParameter("id"));
            Boolean save = Boolean.valueOf(request.getParameter("download"));
            String filename = String.valueOf(request.getParameter("filename"));

            byte[] content = searchController.getContent(id);
            response.setContentLength(content.length);

            if (save) {
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName(filename), "UTF-8") + ".pdf");
            }
            out.write(content);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        doPost(request, response);
    }

    private String fileName(String str) {
        String[] arr = str.split(" ");
        String val = arr[0];
        if (arr.length > 1) {
            for (int i = 1; i < arr.length; i++) {
                arr[i] = ("" + arr[i].charAt(0)).toUpperCase() + arr[i].substring(1);
                val += arr[i];
            }
        }
        return val;
    }
}
