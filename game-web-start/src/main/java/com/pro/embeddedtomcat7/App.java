package com.pro.embeddedtomcat7;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

/**
 * Created with IntelliJ IDEA.
 * User: valpol
 * Date: 10/8/13
 * Time: 1:30 PM
 */
public class App {

    public static void main(String[] args) throws LifecycleException, ServletException, IOException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File webappDir = new File(tmpDir, "embeddedtomcat7");
        webappDir.mkdir();

        final Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setBaseDir(tmpDir.getAbsolutePath());
        tomcat.getConnector().setURIEncoding("UTF-8");

        String contextPath = "/";

        Context context = tomcat.addContext(contextPath, webappDir.getAbsolutePath());
        Wrapper wrapper = tomcat.addServlet(contextPath, "Test", new TestServlet());
        //Wrapper wrapper = tomcat.addServlet(contextPath, "Async", new AsyncServlet());
        //wrapper.setAsyncSupported(true);

        wrapper.addMapping("/*");

        tomcat.start();

        tomcat.getServer().await();
    }
}