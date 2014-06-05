package me.zzp.jav;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import org.apache.jasper.servlet.JspServlet;

public class ViewSetup implements ServletContextListener {

  private List<String> listJsp(ServletContext context, String root) {
    List<String> files = new LinkedList<>();
    Set<String> paths = context.getResourcePaths(root);
    if (paths != null) {
      for (String path : paths) {
        if (path.endsWith(".jsp")) {
          files.add(path);
        } else if (path.endsWith("/")) {
          files.addAll(listJsp(context, path));
        }
      }
    }
    return files;
  }

  @Override
  public void contextInitialized(ServletContextEvent e) {
    ServletContext context = e.getServletContext();
    String paths = context.getInitParameter("jactionview-paths");
    if (paths == null || paths.isEmpty()) {
      return;
    }

    for (String root : paths.split(",")) {
      if (!root.endsWith("/")) {
        root = root.concat("/");
      }

      for (String file : listJsp(context, root)) {
        ViewConfig view = new ViewConfig(context, file, root);
        try {
          JspServlet jsp = context.createServlet(JspServlet.class);
          jsp.init(view);
          Dynamic mapping = context.addServlet(view.getServletName(), jsp);
          mapping.addMapping(view.getUrl());
        } catch (ServletException ex) {
          System.err.println(ex.getMessage());
        }
      }
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent e) {
  }
}
