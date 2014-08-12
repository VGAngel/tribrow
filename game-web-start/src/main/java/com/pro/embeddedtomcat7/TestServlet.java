package com.pro.embeddedtomcat7;

/**
 * Created with IntelliJ IDEA.
 * User: valpol
 * Date: 10/8/13
 * Time: 1:31 PM
 */
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=utf-8");
        PrintWriter writer = resp.getWriter();
        writer.println("<h1>" + req.getPathInfo() + "</h1>");
        writer.close();
    }
}
