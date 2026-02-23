package com.liskovsoft.sharedutils.okhttp;

import androidx.annotation.NonNull;

import okhttp3.Dns;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Queries AAAA first, then A — avoids redirect loops on some CDNs
 */
public class PublicDnsResolver implements Dns {
    private final Resolver primaryResolver;
    private final Resolver fallbackResolver;

    private PublicDnsResolver(String primaryDns, String fallbackDns, int timeoutSec) {
        try {
            primaryResolver = new SimpleResolver(primaryDns);
            primaryResolver.setTimeout(timeoutSec);

            fallbackResolver = new SimpleResolver(fallbackDns);
            fallbackResolver.setTimeout(timeoutSec);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Cannot create DNS resolvers", e);
        }
    }

    @NonNull
    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
        List<InetAddress> result = new ArrayList<>();

        List<InetAddress> ipv6Addresses = queryWithFallback(hostname, Type.AAAA);
        List<InetAddress> ipv4Addresses = queryWithFallback(hostname, Type.A);

        // IPv6 first (an essential part)
        if (isReachable(ipv6Addresses)) {
            result.addAll(ipv6Addresses);
            result.addAll(ipv4Addresses);
        } else {
            result.addAll(ipv4Addresses);
            result.addAll(ipv6Addresses);
        }

        // Fallback to system DNS only if nothing was resolved
        if (result.isEmpty()) {
            return Dns.SYSTEM.lookup(hostname);
        }

        return result;
    }

    private boolean isReachable(List<InetAddress> addresses) {
        for (InetAddress address : addresses) {
            try {
                if (address.isReachable(500)) {
                    return true;
                }
            } catch (IOException ignored) {}
            break;
        }

        return false;
    }

    private List<InetAddress> getReachable(List<InetAddress> addresses) {
        List<InetAddress> reachable = new ArrayList<>();
        for (InetAddress address : addresses) {
            try {
                if (address.isReachable(500)) {
                    reachable.add(address);
                }
            } catch (IOException ignored) {}
        }
        return reachable;
    }

    private List<InetAddress> queryWithFallback(String hostname, int type) {
        List<InetAddress> addresses = query(hostname, type, primaryResolver);
        if (addresses.isEmpty()) {
            addresses = query(hostname, type, fallbackResolver);
        }
        return addresses;
    }

    private List<InetAddress> query(String hostname, int type, Resolver resolver) {
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
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public static Dns google() {
        return new PublicDnsResolver("8.8.8.8", "8.8.4.4", 5);
    }

    public static Dns cloudflare() {
        return new PublicDnsResolver("1.1.1.1", "1.0.0.1", 5);
    }
}

