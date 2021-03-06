package br.uel.easymenu.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import br.uel.easymenu.utils.CalendarUtils;

// We don't use Calendar as a key because the same day could have different hashCode because of the seconds, etc.
public class GroupedMeals {

    private LinkedHashMap<String, ArrayList<Meal>> mealsMap = new LinkedHashMap<>();

    public GroupedMeals(List<Meal> weeklyMeals) {
        for (Meal meal : weeklyMeals) {

            String stringDate = meal.getStringDate();

            if (!mealsMap.containsKey(stringDate)) {
                ArrayList<Meal> firstMeal = new ArrayList<>();
                firstMeal.add(meal);
                mealsMap.put(stringDate, firstMeal);
            } else {
                mealsMap.get(stringDate).add(meal);
            }
        }
    }

    public ArrayList<Meal> getMealsByIndex(int index) {
        List<ArrayList<Meal>> meals = new ArrayList<>(mealsMap.values());
        return meals.get(index);
    }

    public DateTime getDateByIndex(int index) {
        List<String> keys = new ArrayList<>(mealsMap.keySet());
        String calendar = keys.get(index);
        return CalendarUtils.fromStringToCalendar(calendar);
    }

    public boolean hasDate(DateTime dateTime) {
        String dateString = CalendarUtils.fromCalendarToString(dateTime);
        return mealsMap.containsKey(dateString);
    }

    public int getPositionByDay(DateTime dateTime) {
        String dateString = CalendarUtils.fromCalendarToString(dateTime);
        int index = 0;
        for (String key : mealsMap.keySet()) {
            if (key.equals(dateString)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public int size() {
        return mealsMap.size();
    }
}
