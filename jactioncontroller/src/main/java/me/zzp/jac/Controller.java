package me.zzp.jac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.zzp.jac.ex.IllegalPathException;

final class Controller {
  private final Pattern pattern;
  private final List<String> names;

  Controller(String path) {
    names = new ArrayList<>();
    
    StringBuilder regex = new StringBuilder();
    int count = 0;
    int from = -1;
    boolean inside = false;
    for (int i = 0; i < path.length(); i++) {
      char c = path.charAt(i);
      switch (c) {
        case '{': {
          if (count == 0) {
            from = i + 1;
            inside = true;
          }
          count++;
        } break;
        case '}': {
          count--;
          if (count == 0) {
            String[] values = path.substring(from, i).split(":", 2);
            names.add(values[0]);
            if (values.length == 1) {
              regex.append("([^/]+?)");
            } else {
              regex.append('(').append(values[1]).append(')');
            }
            inside = false;
          } else if (count < 0) {
            throw new IllegalPathException(path);
          }
        } break;
        default: {
          if (!inside) {
            regex.append(c);
            if (c == '\\') {
              i++;
              regex.append(path.charAt(i));
            }
          }
        } break;
      }
    }

    pattern = Pattern.compile(regex.toString());
  }

  Map<String, String> parse(String path) {
    Matcher matcher = pattern.matcher(path);
    if (matcher.matches() && matcher.groupCount() == names.size()) {
      Map<String, String> params = new HashMap<>();
      for (int i = 0; i < matcher.groupCount(); i++) {
        params.put(names.get(i), matcher.group(i + 1));
      }
      return params;
    } else {
      return null;
    }
  }
}
