package com.jakewharton.rxbinding.widget;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.jakewharton.rxbinding.RecordingObserver;
import com.jakewharton.rxbinding.test.R;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
public final class RxAutoCompleteTextViewTest {
  @Rule public final ActivityTestRule<RxAutoCompleteTextViewTestActivity> activityRule =
      new ActivityTestRule<>(RxAutoCompleteTextViewTestActivity.class);

  private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

  private RxAutoCompleteTextViewTestActivity activity;
  private AutoCompleteTextView autoCompleteTextView;

  @Before public void setUp() {
    activity = activityRule.getActivity();
    autoCompleteTextView = activity.autoCompleteTextView;
  }

  @Ignore("Does not work on Travis CI")
  @Test public void itemClickEvents() {
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        autoCompleteTextView.setThreshold(1);

        List<String> values = Arrays.asList("Two", "Three", "Twenty");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(autoCompleteTextView.getContext(),
            android.R.layout.simple_list_item_1, values);
        autoCompleteTextView.setAdapter(adapter);
      }
    });

    RecordingObserver<AdapterViewItemClickEvent> o = new RecordingObserver<>();
    Subscription subscription = RxAutoCompleteTextView.itemClickEvents(autoCompleteTextView) //
      .subscribeOn(AndroidSchedulers.mainThread()) //
      .subscribe(o);
    o.assertNoMoreEvents();

    onView(withId(R.id.auto_complete)).perform(typeText("Tw"));
    onView(withText("Twenty"))
        .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))))
        .perform(click());

    AdapterViewItemClickEvent event = o.takeNext();
    assertThat(event.view()).isNotNull();
    assertThat(event.clickedView()).isNotNull();
    assertThat(event.position()).isEqualTo(1); // Second item in two-item filtered list.
    assertThat(event.id()).isEqualTo(1); // Second item in two-item filtered list.

    subscription.unsubscribe();

    onView(withId(R.id.auto_complete)).perform(clearText(), typeText("Tw"));
    onView(withText("Twenty"))
        .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))))
        .perform(click());

    o.assertNoMoreEvents();
  }
}