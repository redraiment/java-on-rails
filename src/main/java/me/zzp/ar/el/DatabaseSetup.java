package me.zzp.ar.el;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import me.zzp.ar.DB;

/**
 * <p>在上下文属性中添加数据库对象。</p>
 * <p>1. 欲自动生成数据库对象，需要在<code>web.xml</code>添加此监听器：</p>
 * <blockquote>
 * <pre>&lt;listener&gt;
  &lt;listener-class&gt;me.zzp.ar.el.DatabaseSetup&lt;/listener-class&gt;
&lt;/listener&gt;</pre>
 * </blockquote>
 * <p>同时添加上下文参数，指定数据源的路径：</p>
 * <blockquote>
 * <pre>&lt;context-param&gt;
  &lt;param-name&gt;jactiverecord-el-data-source&lt;/param-name&gt;
  &lt;param-value&gt;java:/comp/env/jdbc/DataSource&lt;/param-value&gt;
&lt;/context-param&gt;</pre>
 * </blockquote>
 * <p>此时，在<code>Servlet</code>中调用 <code>getServletContext().getAttribute("dbo")</code>
 * 就能获得{@link me.zzp.ar.DB}对象。</p>
 * <p>2. 其中“dbo”为默认的属性名，要修改这个名字可通过在<code>web.xml</code>中添加上下文参数：</p>
 * <blockquote>
 * <pre>&lt;context-param&gt;
  &lt;param-name&gt;jactiverecord-el-attribute-name&lt;/param-name&gt;
  &lt;param-value&gt;database&lt;/param-value&gt;
&lt;/context-param&gt;</pre>
 * </blockquote>
 * <p>这样，属性名就改成了<code>database</code>。</p>
 * @author redraiment
 * @since 1.1
 * @see me.zzp.ar.DB
 */
public class DatabaseSetup implements ServletContextListener {
  /**
   * <p>初始化数据库对象。</p>
   * @param e 上下文事件对象
   */
  @Override
  public void contextInitialized(ServletContextEvent e) {
    ServletContext context = e.getServletContext();
    String path = context.getInitParameter("jactiverecord-el-data-source");
    if (path == null || path.isEmpty()) {
      return;
    }
    String name = context.getInitParameter("jactiverecord-el-attribute-name");
    if (name == null || name.isEmpty()) {
      name = "dbo";
    }

    try {
      DataSource pool = InitialContext.doLookup(path);
      DB dbo = DB.open(pool);
      context.setAttribute(name, dbo);
    } catch (NamingException ex) {
      System.err.println(ex.getMessage());
    }
  }

  /**
   * 什么都没做。
   * @param e 上下文事件对象
   */
  @Override
  public void contextDestroyed(ServletContextEvent e) {
  }
}
