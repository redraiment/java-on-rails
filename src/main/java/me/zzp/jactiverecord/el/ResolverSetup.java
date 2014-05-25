package me.zzp.jactiverecord.el;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

public class ResolverSetup implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent e) {
    ServletContext context = e.getServletContext();
    String param = context.getInitParameter("jactiverecord-el-camel-case");
    boolean camelCase = "true".equalsIgnoreCase(param);

    JspFactory factory = JspFactory.getDefaultFactory();
    JspApplicationContext resolvers = factory.getJspApplicationContext(context);
    resolvers.addELResolver(new TableELResolver());
    resolvers.addELResolver(new RecordELResolver(camelCase));
  }

  @Override
  public void contextDestroyed(ServletContextEvent e) {
  }
}
