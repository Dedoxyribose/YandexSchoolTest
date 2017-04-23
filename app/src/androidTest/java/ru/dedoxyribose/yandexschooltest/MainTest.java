package ru.dedoxyribose.yandexschooltest;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.Checks;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.test.ApplicationTestCase;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ru.dedoxyribose.yandexschooltest.ui.chooselang.ChooseLangActivity;
import ru.dedoxyribose.yandexschooltest.ui.main.MainActivity;
import ru.dedoxyribose.yandexschooltest.ui.recordlist.RecordListFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by Ryan on 04.04.2017.
 */

public class MainTest  {

    public static Matcher<View> withDrawable(final int resourceId) {
        return new DrawableMatcher(resourceId);
    }

    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    public static Matcher<RecyclerView.ViewHolder> withLangInHolder(final String title)
    {
        return new BoundedMatcher<RecyclerView.ViewHolder, ChooseLangActivity.RecyclerAdapter.RecycleViewHolder>(ChooseLangActivity.RecyclerAdapter.RecycleViewHolder.class)
        {
            @Override
            protected boolean matchesSafely(ChooseLangActivity.RecyclerAdapter.RecycleViewHolder item)
            {
                return item.isLang(title);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("view holder with title: " + title);
            }
        };
    }

    public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with "+childPosition+" child view of type parentMatcher");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) {
                    return parentMatcher.matches(view.getParent());
                }

                ViewGroup group = (ViewGroup) view.getParent();
                return parentMatcher.matches(view.getParent()) && group.getChildAt(childPosition).equals(view);
            }
        };
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void registerIdlingResource() {
        Espresso.registerIdlingResources(
                mActivityRule.getActivity().getIdlingResource());
    }

    @Test
    public void checkFavorite() {

        //данный тест проверяет взаимодействие с вкладкой Избранное
        //добавляем слово в избранное с разных вкладок и проверяем, как оно появляется и исчезает на вкладке Избранное

        onView(withId(R.id.tvFrom)).perform(click());

        onView(withId(R.id.rvList)).perform(RecyclerViewActions.scrollToHolder(withLangInHolder("Русский")));
        onView(withText("Русский")).perform(click());

        onView(withId(R.id.editText)).perform(click(), replaceText("яблоко"));
        pressBack();
        onView(withId(R.id.rvDefs)).perform(click());

        //SystemClock.sleep(800);

        onView(nthChildOf(nthChildOf(withId(R.id.tabPages), 0), 1)).perform(click());

        onView(withText(R.string.History)).perform(click());

        onView(withRecyclerView(R.id.rvList).atPosition(0))
                .check(matches(hasDescendant(withText("яблоко"))));

        onView(nthChildOf(withRecyclerView(R.id.rvList).atPosition(0), 0)).perform(click());

        onView(withText(R.string.Favorite)).perform(click());

        onView(withRecyclerView(R.id.rvList).atPosition(0))
                .check(matches(hasDescendant(withText("яблоко"))));

        onView(nthChildOf(withRecyclerView(R.id.rvList).atPosition(0), 0))
                .check(matches(withContentDescription("on")));

        onView(nthChildOf(withRecyclerView(R.id.rvList).atPosition(0), 0)).perform(click());

        onView(nthChildOf(withRecyclerView(R.id.rvList).atPosition(0), 0))
                .check(matches(withContentDescription("off")));

        onView(withText(R.string.History)).perform(click());

        onView(nthChildOf(withRecyclerView(R.id.rvList).atPosition(0), 0))
                .check(matches(withContentDescription("off")));

        onView(withText(R.string.Favorite)).perform(click());

        onView(withRecyclerView(R.id.rvList).atPosition(0))
                .check(matches(not(hasDescendant(withText("яблоко")))));

        onView(nthChildOf(nthChildOf(withId(R.id.tabPages), 0), 0)).perform(click());

        onView(withId(R.id.ivFavorite)).check(matches(withContentDescription("off")));

        onView(withId(R.id.ivFavorite)).perform(click());

        onView(withId(R.id.ivFavorite)).check(matches(withContentDescription("on")));

        onView(nthChildOf(nthChildOf(withId(R.id.tabPages), 0), 1)).perform(click());

        onView(withRecyclerView(R.id.rvList).atPosition(0))
                .check(matches(hasDescendant(withText("яблоко"))));

        onView(nthChildOf(withRecyclerView(R.id.rvList).atPosition(0), 0))
                .check(matches(withContentDescription("on")));

        onView(withText(R.string.History)).perform(click());

        onView(nthChildOf(withRecyclerView(R.id.rvList).atPosition(0), 0))
                .check(matches(withContentDescription("on")));

        onView(nthChildOf(withRecyclerView(R.id.rvList).atPosition(0), 0)).perform(click());

        onView(nthChildOf(nthChildOf(withId(R.id.tabPages), 0), 0)).perform(click());

        onView(withId(R.id.ivFavorite)).check(matches(withContentDescription("off")));

    }

    @Test
    public void checkDictionary() {

        onView(withId(R.id.tvFrom)).perform(click());

        onView(withId(R.id.rvList)).perform(RecyclerViewActions.scrollToHolder(withLangInHolder("Русский")));
        onView(withText("Русский")).perform(click());

        onView(withId(R.id.tvTo)).perform(click());
        onView(withId(R.id.rvList)).perform(RecyclerViewActions.scrollToHolder(withLangInHolder("Английский")));
        onView(withIndex(withText("Английский"), 0)).perform(click());

        onView(withId(R.id.editText)).perform(click(), replaceText("щука"));
        pressBack();
        onView(withId(R.id.rvDefs)).perform(click());

        onView(withText("Luce")).check(matches(isDisplayed()));


    }


}
