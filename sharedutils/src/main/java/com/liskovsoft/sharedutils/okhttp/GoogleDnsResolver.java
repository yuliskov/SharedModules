package com.liskovsoft.sharedutils.okhttp;

import androidx.annotation.NonNull;

import okhttp3.Dns;
import org.xbill.DNS.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class GoogleDnsResolver implements Dns {
    private final Resolver primaryResolver;
    private final Resolver fallbackResolver;

    public GoogleDnsResolver() {
        try {
            primaryResolver = new SimpleResolver("8.8.8.8"); // Google DNS
            primaryResolver.setTimeout(5);

            fallbackResolver = new SimpleResolver("8.8.4.4"); // fallback Google DNS
            fallbackResolver.setTimeout(5);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Cannot create Google DNS resolvers", e);
        }
    }

    @NonNull
    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
        List<InetAddress> result = new ArrayList<>();

        // IPv4
        result.addAll(queryDns(hostname, Type.A, primaryResolver));
        if (result.isEmpty()) result.addAll(queryDns(hostname, Type.A, fallbackResolver));

        // IPv6
        result.addAll(queryDns(hostname, Type.AAAA, primaryResolver));
        if (result.isEmpty()) result.addAll(queryDns(hostname, Type.AAAA, fallbackResolver));

        // fallback to the system DNS, if nothing found
        if (result.isEmpty()) {
            return Dns.SYSTEM.lookup(hostname);
        }

        return result;
    }

    private List<InetAddress> queryDns(String hostname, int type, Resolver resolver) {
        try {
            Lookup lookup = new Lookup(hostname, type);
            lookup.setResolver(resolver);
            Record[] records = lookup.run();
            List<InetAddress> addresses = new ArrayList<>();
            if (records != null) {
                for (Record record : records) {
                    if (record instanceof ARecord && type == Type.A) {
                        addresses.add(((ARecord) record).getAddress());
                    } else if (record instanceof AAAARecord && type == Type.AAAA) {
                        addresses.add(((AAAARecord) record).getAddress());
                    }
                }
            }
            return addresses;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

