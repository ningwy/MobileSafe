package io.github.ningwy;

import android.app.Application;
import android.test.ApplicationTestCase;

import io.github.ningwy.db.dao.BlackNumberDao;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void test() {
        BlackNumberDao dao = new BlackNumberDao(getContext());
    }

}