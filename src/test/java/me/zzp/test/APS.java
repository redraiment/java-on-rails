package me.zzp.test;

import java.util.List;
import me.zzp.ar.DB;
import me.zzp.ar.Record;
import me.zzp.ar.Table;

public class APS {

  public static void main(String[] args) {
    // funds, categories, distributions, formulas, options, formula_x_refs, returns
    DB sqlite3 = DB.open("jdbc:sqlite::memory:");

    /* Tables */
    Table Fund = sqlite3.createTable("funds", "name text");
    Table Category = sqlite3.createTable("categories", "fund_id int", "name text");
    Table Distribution = sqlite3.createTable("distributions", "category_id int", "as_of_date date", "amount double");
    Table Formula = sqlite3.createTable("formulas", "name text");
    Table Option = sqlite3.createTable("options", "formula_id int", "with_nav_growth int", "with_reinvest int");
    Table FormulaXRef = sqlite3.createTable("formula_x_refs", "category_id int", "formula_id int");
    Table Return = sqlite3.createTable("returns", "formula_x_ref_id int", "as_of_date date", "rate double");

    /* Associations */
    Fund.hasMany("categories").by("fund_id");
    Fund.hasMany("distributions").by("category_id").through("categories");
    Fund.hasMany("formulaXRefs").by("category_id").in("formula_x_refs").through("categories");
    Fund.hasMany("formulas").by("formula_id").through("formulaXRefs");
    Fund.hasMany("returns").by("formula_x_ref_id").through("formulaXRefs");

    Category.belongsTo("fund").by("fund_id").in("funds");
    Category.hasMany("distributions").by("category_id");
    Category.hasMany("formulaXRefs").by("category_id").in("formula_x_refs");
    Category.hasAndBelongsToMany("formulas").by("formula_id").through("formulaXRefs");
    Category.hasMany("returns").by("formula_x_ref_id").through("formulaXRefs");

    Distribution.belongsTo("category").by("category_id").in("categories");
    Distribution.belongsTo("fund").by("fund_id").in("funds").through("category");

    Formula.hasOne("option").by("formula_id").in("options");
    Formula.hasMany("formulaXRefs").by("formula_id").in("formula_x_refs");
    Formula.hasAndBelongsToMany("categories").by("category_id").through("formulaXRefs");
    Formula.hasMany("funds").by("fund_id").through("categories");
    Formula.hasMany("returns").by("formula_x_ref_id").through("formulaXRefs");

    Option.belongsTo("formula").by("formula_id").in("formulas");

    FormulaXRef.belongsTo("category").by("category_id").in("categories");
    FormulaXRef.belongsTo("formula").by("formula_id").in("formulas");
    FormulaXRef.hasMany("returns").by("formula_x_ref_id");

    Return.belongsTo("formulaXRef").by("formula_x_ref_id").in("formula_x_refs");
    Return.belongsTo("formula").by("formula_id").in("formulas").through("formulaXRef");
    Return.belongsTo("category").by("category_id").in("categories").through("formulaXRef");
    Return.belongsTo("fund").by("fund_id").in("funds").through("category");

    /* Meta data */
    Record joe1 = Fund.create("name:", "JOE1");
    Table joe1Class = joe1.get("categories");
    joe1Class.create("name:", "A");

    Record joe2 = Fund.create("name:", "JOE2");
    Table joe2Class = joe2.get("categories");
    joe2Class.create("name:", "X");

    Record joe3 = Fund.create("name:", "JOE3");
    Table joe3Class = joe3.get("categories");
    joe3Class.create("name:", "A");
    joe3Class.create("name:", "X");

    boolean[][] switches = new boolean[][]{
      {true, true},
      {true, false},
      {false, true},
      {false, false}
    };
    for (boolean[] options : switches) {
      String name = String.format("Total Return %s nav growth %s reinvest",
                                  options[0] ? "with" : "without",
                                  options[1] ? "with" : "without");
      int withNavGrowth = options[0] ? 1 : 0;
      int withReinvest = options[1] ? 1 : 0;
      Record formula = Formula.create("name:", name);
      Option.create("formula_id", formula.getInt("id"),
                    "with_nav_growth", withNavGrowth,
                    "with_reinvest", withReinvest);
    }

    List<Record> categories = Category.all();
    List<Record> formulas = Formula.all();
    String[] periods = new String[]{
      "2014-03-24", "2014-03-25", "2014-03-26",
      "2014-03-27", "2014-03-28", "2014-03-29",
      "2014-03-30", "2014-03-31", "2014-04-01"
    };
    for (String period : periods) {
      for (Record category : categories) {
        Table distributions = category.get("distributions");
        distributions.create("as_of_date:", period,
                             "amount:", Math.random());
        for (Record formula : formulas) {
          Table returns = FormulaXRef.create(
                  "category_id:", category.getInt("id"),
                  "formula_id:", formula.getInt("id")
          ).get("returns");
          returns.create("as_of_date:", period, "rate:", Math.random());
        }
      }
    }

    /* Validation */
    for (Record distribution : Distribution.all()) {
      Record fund = distribution.get("fund");
      Record category = distribution.get("category");
      System.out.printf("[%s]%s/%s(%f)\n", distribution.get("as_of_date"),
                                           fund.get("name"),
                                           category.get("name"),
                                           distribution.getDouble("amount"));
    }
    System.out.println("---");
    
    for (Record gain : Return.all()) {
      Record fund = gain.get("fund");
      Record category = gain.get("category");
      Record formula = gain.get("formula");
      Record option = formula.get("option");
      System.out.printf("[%s]%s/%s|%s|%d|%d|(%f)\n", gain.get("as_of_date"),
                                           fund.get("name"),
                                           category.get("name"),
                                           formula.get("name"),
                                           option.getInt("with_nav_growth"),
                                           option.getInt("with_reinvest"),
                                           gain.getDouble("rate"));
    }
    System.out.println("---");

    Table returns = Fund.findBy("name", "JOE2").get(0).get("returns");
    for (Record gain : returns.all()) {
      Record fund = gain.get("fund");
      Record category = gain.get("category");
      Record formula = gain.get("formula");
      Record option = formula.get("option");
      System.out.printf("[%s]%s/%s|%s|%d|%d|(%f)\n", gain.get("as_of_date"),
                                           fund.get("name"),
                                           category.get("name"),
                                           formula.get("name"),
                                           option.getInt("with_nav_growth"),
                                           option.getInt("with_reinvest"),
                                           gain.getDouble("rate"));
    }

    sqlite3.close();
  }
}
