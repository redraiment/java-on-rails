package me.zzp.jactiverecord.el;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

public class ResolverSetup implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent e) {
    JspApplicationContext context = JspFactory.getDefaultFactory().getJspApplicationContext(e.getServletContext());
    context.addELResolver(new TableELResolver());
    context.addELResolver(new RecordELResolver());
  }

  @Override
  public void contextDestroyed(ServletContextEvent e) {
  }
}
