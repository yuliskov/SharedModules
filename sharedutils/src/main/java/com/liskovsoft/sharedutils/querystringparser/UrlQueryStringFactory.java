package com.liskovsoft.sharedutils.querystringparser;

import com.liskovsoft.sharedutils.helpers.Helpers;

import java.io.InputStream;

public class UrlQueryStringFactory {
    public static UrlQueryString parse(InputStream urlContent) {
        return parse(Helpers.toString(urlContent));
    }

    public static UrlQueryString parse(String url) {
        UrlQueryString queryString = UrlEncodedQueryString.parse(url);

        if (queryString.isValid()) {
            return queryString;
        }

        queryString = PathQueryString.parse(url);

        if (queryString.isValid()) {
            return queryString;
        }

        return NullQueryString.parse(url);
    }
}
