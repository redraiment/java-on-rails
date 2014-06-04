package me.zzp.jac;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Dispatcher extends HttpServlet {
  private static final Map<Controller, Constructor<? extends Service>> services = new LinkedHashMap<>();
  
  public static synchronized void add(String pattern, Class<? extends Service> type) {
    if (Service.class.isAssignableFrom(type)) {
      try {
        Constructor<? extends Service> c = type.getConstructor(HttpServletRequest.class, HttpServletResponse.class);
        services.put(new Controller(pattern), c);
      } catch (NoSuchMethodException | SecurityException e) {
        System.err.println(e.getMessage());
      }
    }
  }

  protected Service match(HttpServletRequest request, HttpServletResponse response) {
    String path = request.getPathInfo();

    for (Map.Entry<Controller, Constructor<? extends Service>> service : services.entrySet()) {
      Map<String, String> url = service.getKey().parse(path);
      if (url != null) {
        for (Map.Entry<String, String> info : url.entrySet()) {
          request.setAttribute(info.getKey(), info.getValue());
        }

        try {
          return service.getValue().newInstance(request, response);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          System.err.println(e.getMessage());
        }
      }
    }
    
    return null;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    match(request, response).query();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    match(request, response).create();
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    match(request, response).update();
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    match(request, response).delete();
  }
}
