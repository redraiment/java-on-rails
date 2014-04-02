package me.zzp.test;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import me.zzp.ar.DB;
import me.zzp.ar.Record;
import me.zzp.ar.Table;

public class APS {
  public static Date date(int year, int month, int day) {
    Calendar c = Calendar.getInstance();
    c.set(year, month, day, 0, 0, 0);
    c.set(Calendar.MILLISECOND, 0);
    return new Date(c.getTimeInMillis());
  }

  public static void main(String[] args) {
    // funds, categories, distributions, formulas, options, formula_x_refs, returns
    DB dbo = DB.open("jdbc:sqlite::memory:"); // SQLite
    //DB dbo = DB.open("jdbc:mysql://localhost/aps", "redraiment", ""); // MySQL
    //DB dbo = DB.open("jdbc:postgresql://localhost/aps", "postgres", "123456");  // PostgreSQL

    try {
      /* Tables */
      Table Fund = dbo.createTable("funds", "name text");
      Table Category = dbo.createTable("categories", "fund_id int", "name text");
      Table Distribution = dbo.createTable("distributions", "category_id int", "as_of_date date", "amount numeric");
      Table Formula = dbo.createTable("formulas", "name text");
      Table Option = dbo.createTable("options", "formula_id int", "with_nav_growth int", "with_reinvest int");
      Table FormulaXRef = dbo.createTable("formula_x_refs", "category_id int", "formula_id int");
      Table Return = dbo.createTable("returns", "formula_x_ref_id int", "as_of_date date", "rate numeric");

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
      Date[] periods = new Date[]{
        date(2014, Calendar.MARCH, 24),
        date(2014, Calendar.MARCH, 25),
        date(2014, Calendar.MARCH, 26),
        date(2014, Calendar.MARCH, 27),
        date(2014, Calendar.MARCH, 28),
        date(2014, Calendar.MARCH, 29),
        date(2014, Calendar.MARCH, 30),
        date(2014, Calendar.MARCH, 31),
        date(2014, Calendar.APRIL, 1)
      };
      for (Date period : periods) {
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
        System.out.printf("[%s]%s/%s(%s)\n", distribution.get("as_of_date"),
                                             fund.get("name"),
                                             category.get("name"),
                                             distribution.get("amount"));
      }
      System.out.println("---");

      for (Record gain : Return.all()) {
        Record fund = gain.get("fund");
        Record category = gain.get("category");
        Record formula = gain.get("formula");
        Record option = formula.get("option");
        System.out.printf("[%s]%s/%s|%s|%d|%d|(%s)\n", gain.get("as_of_date"),
                                                       fund.get("name"),
                                                       category.get("name"),
                                                       formula.get("name"),
                                                       option.getInt("with_nav_growth"),
                                                       option.getInt("with_reinvest"),
                                                       gain.get("rate"));
      }
      System.out.println("---");

      Table returns = Fund.findBy("name", "JOE2").get(0).get("returns");
      for (Record gain : returns.all()) {
        Record fund = gain.get("fund");
        Record category = gain.get("category");
        Record formula = gain.get("formula");
        Record option = formula.get("option");
        System.out.printf("[%s]%s/%s|%s|%d|%d|(%s)\n", gain.get("as_of_date"),
                                                       fund.get("name"),
                                                       category.get("name"),
                                                       formula.get("name"),
                                                       option.getInt("with_nav_growth"),
                                                       option.getInt("with_reinvest"),
                                                       gain.get("rate"));
      }
    } finally {
      dbo.close();
    }
  }
}
