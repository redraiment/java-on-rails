package me.zzp.jac;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import me.zzp.jac.ex.RedirectionException;

public abstract class Service {
  protected final ServletContext app;
  protected final HttpSession session;
  protected final HttpServletRequest request;
  protected final HttpServletResponse response;
  protected final Map<String, Object> params;

  public Service(HttpServletRequest request, HttpServletResponse response) {
    this.request = request;
    this.response = response;
    params = new HashMap<>();
    session = request.getSession();
    app = request.getServletContext();
    
    for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
      String name = entry.getKey();
      String[] values = entry.getValue();
      if (values.length == 1) {
        params.put(name, values[0]);
      } else {
        params.put(name, Arrays.asList(values));
      }
    }
  }

  protected <E> E get(String key) {
    Object value = null;
    if (params.containsKey(key)) {
      value = params.get(key);
    } else if (request.getAttribute(key) != null) {
      value = request.getAttribute(key);
    } else if (session.getAttribute(key) != null) {
      value =  session.getAttribute(key);
    } else if (app.getAttribute(key) != null) {
      value = app.getAttribute(key);
    }
    return (E)value;
  }

  protected String getStr(String key) {
    Object value = get(key);
    return value != null? value.toString(): null;
  }

  protected int getInt(String key) {
    String value = getStr(key);
    try {
      return Integer.parseInt(value);
    } catch(NumberFormatException e) {
      return 0;
    }
  }
  
  protected void set(String key, Object value) {
    request.setAttribute(key, value);
  }
  
  protected void forward(String path) {
    try {
      request.getRequestDispatcher(path).forward(request, response);
    } catch (ServletException | IOException e) {
      throw new RedirectionException("forward", path, e);
    }
  }
  
  protected void redirect(String path) {
    if (path.startsWith("/")) {
      path = request.getContextPath().concat(path);
    }

    try {
      response.sendRedirect(path);
    } catch (IOException e) {
      throw new RedirectionException("redirect", path, e);
    }
  }
  
  public void create() {
  }

  public void update() {
  }

  public void delete() {
  }

  public void query() {
  }
}
