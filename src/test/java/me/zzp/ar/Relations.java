package me.zzp.ar;

import java.util.HashMap;
import java.util.Map;

public class Relations {
  private static final Map<String, Association> relations = new HashMap<String, Association>();

  public static void put(Association assoc) {
//    relations.put(assoc.getName(), assoc);
  }

  public static void main(String[] args) {
    // funds, categories, formulas, categories_formulas, details, distributions
//    put(new Association(relations, "funds", "categories", false, true).by("fund_id"));
//    System.out.println(relations.get("funds#categories").assoc("funds"));
//
//    put(new Association(relations, "categories", "fund", true, false).by("fund_id").in("funds"));
//    System.out.println(relations.get("categories#fund").assoc("categories"));
//
//    put(new Association(relations, "formulas", "detail", true, true).by("formula_id").in("details"));
//    System.out.println(relations.get("formulas#detail").assoc("formulas"));
//
//    put(new Association(relations, "details", "formula", true, false).by("formula_id").in("formulas"));
//    System.out.println(relations.get("details#formula").assoc("details"));
//
//    put(new Association(relations, "categories", "categoriesFormulas", false, true).by("category_id").in("categories_formulas"));
//    put(new Association(relations, "categories", "formulas", false, false).by("formula_id").in("formulas").through("categoriesFormulas"));
//    System.out.println(relations.get("categories#formulas").assoc("categories"));
//
//    put(new Association(relations, "categories", "distributions", false, true).by("category_id"));
//    put(new Association(relations, "funds", "distributions", false, true).by("category_id").through("categories"));
//    System.out.println(relations.get("funds#distributions").assoc("funds"));
  }
}
