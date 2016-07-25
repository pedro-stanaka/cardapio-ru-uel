package br.uel;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.uel.easymenu.BuildConfig;
import br.uel.easymenu.dao.DishDao;
import br.uel.easymenu.dao.MealDao;
import br.uel.easymenu.dao.SqliteDishDao;
import br.uel.easymenu.dao.SqliteMealDao;
import br.uel.easymenu.model.Dish;
import br.uel.easymenu.model.Meal;
import br.uel.easymenu.tables.DbHelper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
// TODO: Refactor this test
public class TestDaoMeal {

    private MealDao mealDao = new SqliteMealDao(RuntimeEnvironment.application);

    private DishDao dishDao = new SqliteDishDao(RuntimeEnvironment.application);

    @After
    public void closeDatabase() {
        DbHelper.getInstance(RuntimeEnvironment.application).getWritableDatabase().close();
    }

    @Test
    public void testNumberMealCreation() {

        assertThat(mealDao.count(), equalTo(0));

        List<Meal> meals = createMeals(3);
        mealDao.insert(meals);

        // Same date and period are replaced.
        // That's why it is 1
        assertThat(mealDao.count(), equalTo(1));
    }

    @Test
    public void testMealCreationProperties() {
        Meal meal = new Meal(Calendar.getInstance(), Meal.LUNCH);
        long id = mealDao.insert(meal);

        Meal newMeal = mealDao.findById(id);

        assertThat(newMeal.getDate().get(Calendar.DAY_OF_YEAR), equalTo(meal.getDate().get(Calendar.DAY_OF_YEAR)));
        assertThat(newMeal.getDate().get(Calendar.MONTH), equalTo(meal.getDate().get(Calendar.MONTH)));
        assertThat(newMeal.getDate().get(Calendar.YEAR), equalTo(meal.getDate().get(Calendar.YEAR)));
        assertTrue(newMeal.isLunch());
    }

    @Test
    public void testMealCreationWithDishes() {
        MealDao mealDao = new SqliteMealDao(RuntimeEnvironment.application);
        Meal meal = new Meal(Calendar.getInstance(), Meal.BOTH);

        Dish dish1 = new Dish("Beans");
        Dish dish2 = new Dish("Rice");
        Dish dish3 = new Dish("Pasta");

        meal.addDish(dish1);
        meal.addDish(dish2);
        meal.addDish(dish3);

        long mealId = mealDao.insert(meal);

        assertThat(dishDao.count(), equalTo(3));

        Dish dish = dishDao.findById(dish2.getId());
        assertThat(dish.getDishName(), equalTo("Rice"));

        List<Dish> dishes = dishDao.findDishesByMealId(mealId);
        assertThat(dishes.get(1).getDishName(), equalTo("Rice"));

        Meal newMeal = mealDao.findById(mealId);
        assertThat(newMeal.getDishes().get(1).getDishName(), equalTo("Rice"));
    }

    @Test
    public void testMealDeletion() {
        Meal meal = new Meal(Calendar.getInstance(), "BOTH");
        long id = mealDao.insert(meal);
        mealDao.delete(id);

        assertThat(mealDao.count(), equalTo(0));
    }

    @Test
    public void testMealGetByDate() {
        List<Meal> meals = createMeals("2014-02-05", "2014-02-10", "2014-02-12");
        mealDao.insert(meals);

        List<Meal> queryMeals = mealDao.mealsOfTheWeek(fromStringToCalendar("2014-02-07"));
        assertThat(queryMeals.size(), equalTo(1));
        assertThat(queryMeals.get(0).getDate(), equalTo(meals.get(0).getDate()));

        List<Meal> newMeals = mealDao.mealsOfTheWeek(fromStringToCalendar("2014-02-14"));
        assertThat(newMeals.size(), equalTo(2));
        assertThat(newMeals.get(0).getDate(), equalTo(meals.get(1).getDate()));
    }

    @Test
    public void testMealDifferentYear() throws Exception {
        List<Meal> meals = createMeals("2013-02-10", "2014-02-11", "2015-02-12");
        mealDao.insert(meals);

        List<Meal> queryMeals = mealDao.mealsOfTheWeek(fromStringToCalendar("2014-02-10"));
        assertThat(queryMeals.size(), equalTo(1));
        assertThat(queryMeals.get(0).getDate(), equalTo(meals.get(1).getDate()));
    }

    @Test
    public void testMealSameValues() throws Exception {
        List<Meal> meals = createMeals("2014-02-10", "2014-02-10", "2014-02-10");
        mealDao.insert(meals);

        List<Meal> queryMeals = mealDao.mealsOfTheWeek(fromStringToCalendar("2014-02-12"));
        assertThat(queryMeals.size(), equalTo(1));
        assertThat(queryMeals.get(0).getDate(), equalTo(meals.get(0).getDate()));
    }

    @Test
    public void testNewYearWeek() throws Exception {
        List<Meal> meals = createMeals("2014-12-27", "2014-12-31", "2015-01-01", "2015-01-02", "2015-01-04", "2016-01-01");
        mealDao.insert(meals);

        List<Meal> queryMeals = mealDao.mealsOfTheWeek(fromStringToCalendar("2015-01-02"));
        assertThat(queryMeals.size(), equalTo(3));
        assertThat(queryMeals.get(0).getDate(), equalTo(meals.get(1).getDate()));

        queryMeals = mealDao.mealsOfTheWeek(fromStringToCalendar("2014-12-30"));
        assertThat(queryMeals.size(), equalTo(3));
        assertThat(queryMeals.get(1).getDate(), equalTo(meals.get(2).getDate()));
    }

    private List<Meal> createMeals(int number) {
        List<Meal> meals = new ArrayList<Meal>();

        for (int i = 0; i < number; i++) {
            meals.add(createMeal(Calendar.getInstance(), Meal.BOTH));
        }

        return meals;
    }

    private List<Meal> createMeals(String... calendars) {
        List<Meal> meals = new ArrayList<Meal>();

        for (String calendarString : calendars) {
            Calendar calendar = fromStringToCalendar(calendarString);
            meals.add(createMeal(calendar, Meal.BOTH));
        }

        return meals;
    }

    private Calendar fromStringToCalendar(String calendarStr) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            calendar.setTime(sdf.parse(calendarStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    private Meal createMeal(Calendar calendar, String period) {
        Meal meal = new Meal();
        meal.setDate(calendar);
        meal.setPeriod(period);
        return meal;
    }
}
