package com.liskovsoft.sharedutils.querystringparser;

import com.liskovsoft.sharedutils.helpers.Helpers;

import java.io.InputStream;

public class MyQueryStringFactory {
    public static MyQueryString parse(InputStream urlContent) {
        return parse(Helpers.toString(urlContent));
    }

    public static MyQueryString parse(String url) {
        MyQueryString queryString = MyUrlEncodedQueryString.parse(url);

        if (queryString.isValid()) {
            return queryString;
        }

        queryString = MyPathQueryString.parse(url);

        if (queryString.isValid()) {
            return queryString;
        }

        return MyNullQueryString.parse(url);
    }
}
