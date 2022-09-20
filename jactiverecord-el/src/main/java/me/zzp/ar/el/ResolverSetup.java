package me.zzp.ar.el;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

/**
 * <p>加载自定义的EL表达式解析器。</p>
 * <p>1. 欲使用<code>jActiveRecord-EL</code>，需要在<code>web.xml</code>添加此监听器：</p>
 * <blockquote>
 * <pre>&lt;listener&gt;
  &lt;listener-class&gt;me.zzp.ar.el.ResolverSetup&lt;/listener-class&gt;
&lt;/listener&gt;</pre>
 * </blockquote>
 * <p>之后在EL表达式中就能像操作普通<code>JavaBean</code>一样操作 {@link me.zzp.ar.Table}
 * 和 {@link me.zzp.ar.Record}。</p>
 * <p>2. <code>jActiveRecord-EL</code>会自动将骆驼法命名的属性转换为下划线命名，
 * 欲停止这个特性，可在<code>web.xml</code>中添加上下文参数：</p>
 * <blockquote>
 * <pre>&lt;context-param&gt;
  &lt;param-name&gt;jactiverecord-el-camel-case&lt;/param-name&gt;
  &lt;param-value&gt;false&lt;/param-value&gt;
&lt;/context-param&gt;</pre>
 * </blockquote>
 * <p>开启了该选项之后，<code>${user.createdAt}</code>等价于<code>${user.created_at}</code>。</p>
 * @author redraiment
 * @since 1.0
 * @see TableELResolver
 * @see RecordELResolver
 */
public class ResolverSetup implements ServletContextListener {
  /**
   * <p>加载<code>TableELResolver</code>和<code>RecordELResolver</code>。</p>
   * @param e 上下文事件对象
   */
  @Override
  public void contextInitialized(ServletContextEvent e) {
    ServletContext context = e.getServletContext();
    String param = context.getInitParameter("jactiverecord-el-camel-case");
    boolean camelCase = !"false".equalsIgnoreCase(param);

    JspFactory factory = JspFactory.getDefaultFactory();
    JspApplicationContext resolvers = factory.getJspApplicationContext(context);
    resolvers.addELResolver(new TableELResolver());
    resolvers.addELResolver(new RecordELResolver(camelCase));
  }

  /**
   * 什么都没做。
   * @param e 上下文事件对象
   */
  @Override
  public void contextDestroyed(ServletContextEvent e) {
  }
}
