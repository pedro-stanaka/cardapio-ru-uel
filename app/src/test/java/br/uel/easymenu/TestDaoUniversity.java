package br.uel.easymenu;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import javax.inject.Inject;

import br.uel.easymenu.dao.MealDao;
import br.uel.easymenu.dao.UniversityDao;
import br.uel.easymenu.ioc.RobolectricApp;
import br.uel.easymenu.model.Meal;
import br.uel.easymenu.model.University;
import br.uel.easymenu.tables.DbHelper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class TestDaoUniversity {

    @Inject
    UniversityDao universityDao;

    @Inject
    MealDao mealDao;

    @Before
    public void setup() {
        DbHelper.resetConnection();
        RobolectricApp.component().inject(this);
    }

    @After
    public void closeDatabase() {
        DbHelper.getInstance(RuntimeEnvironment.application).getWritableDatabase().close();
    }

    @Test
    public void testUniversityCount() throws Exception {
        assertThat(universityDao.count(), equalTo(0));

        UniversityBuilder builder = new UniversityBuilder();
        universityDao.insert(builder.withName("Name1").withFullName("Name1").build());
        universityDao.insert(builder.withName("Name2").withFullName("Name2").build());
        universityDao.insert(builder.withName("Name3").withFullName("Name3").build());

        assertThat(universityDao.count(), equalTo(3));
    }

    @Test
    public void testUniversityValues() throws Exception {
        University university = new UniversityBuilder().build();
        long id = universityDao.insert(university);
        University universityDatabase = universityDao.findById(id);
        assertThat(university.getFullName(), equalTo(universityDatabase.getFullName()));
        assertThat(university.getName(), equalTo(universityDatabase.getName()));
    }
}
