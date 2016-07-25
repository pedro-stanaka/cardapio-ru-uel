package br.uel.easymenu.dao;

import br.uel.easymenu.model.Dish;

import java.util.List;

public interface DishDao extends Dao<Dish> {

    public List<Dish> findDishesByMealId(long mealId);

}