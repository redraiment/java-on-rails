package me.zzp.jav;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

final class ViewConfig implements ServletConfig {
  private final ServletContext context;
  private final Map<String, String> params;
  private final String name;
  private final String url;

  ViewConfig(ServletContext context, String jsp, String prefix) {
    this.context = context;
    this.name = jsp.substring(prefix.length())
                   .replaceAll("/", "-")
                   .replaceAll("\\.jsp", "-view")
                   .toUpperCase();
    this.url = "/".concat(name.toLowerCase());
    
    params = new HashMap<>();
    params.put("jsp-file", jsp);
    params.put("jspFile", jsp);
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String getServletName() {
    return name;
  }

  @Override
  public ServletContext getServletContext() {
    return context;
  }

  @Override
  public String getInitParameter(String key) {
    return params.get(key);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return Collections.enumeration(params.keySet());
  }
}
