package com.ytfs.common.eos;

import com.ytfs.common.conf.ServerConfig;
import io.yottachain.nodemgmt.core.vo.SuperNode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class BpList {

    private static final List<EOSURI> bplist = new ArrayList();

    private static EOSURI localURI;

    public static EOSURI getEOSURI() {
        if (!localURI.err.get()) {
            return localURI;
        } else {
            if (System.currentTimeMillis() - localURI.errTime.get() > 1000 * 60 * 60) {
                localURI.err.set(false);
                localURI.errTime.set(System.currentTimeMillis());
                return localURI;
            }
        }
        for (EOSURI eos : bplist) {
            if (!eos.err.get()) {
                return eos;
            } else {
                if (System.currentTimeMillis() - eos.errTime.get() > 1000 * 60 * 60) {
                    eos.err.set(false);
                    eos.errTime.set(System.currentTimeMillis());
                    return eos;
                }
            }
        }
        return localURI;
    }

    public static void init(SuperNode[] snlist) {
        localURI = new EOSURI();
        localURI.url = ServerConfig.eosURI;
        try {
            URL url = new URL(ServerConfig.eosURI);
            String localIp = url.getHost();
            for (SuperNode sn : snlist) {
                parseUrl(sn.getAddrs(), localIp);
            }
            Collections.shuffle(bplist);
        } catch (MalformedURLException ex) {
        }
    }

    private static void parseUrl(List<String> addrs, String localIp) {
        for (String addr : addrs) {
            if (addr.toLowerCase().startsWith("/ip4/")) {
                addr = addr.substring(5);
                int index = addr.indexOf("/");
                if (index > 0) {
                    addr = addr.substring(0, index);
                    if (!localIp.equalsIgnoreCase(addr)) {
                        String url = ServerConfig.eosURI.replace(localIp, addr);
                        EOSURI uri = new EOSURI();
                        uri.url = url;
                        bplist.add(uri);
                        break;
                    }
                }
            }
        }
    }

    public static class EOSURI {

        public String url;
        public AtomicBoolean err = new AtomicBoolean(false);
        public AtomicLong errTime = new AtomicLong(0);

        public boolean setErr(Exception r) {
            if (r.getMessage() != null && (r.getMessage().contains("java.net.ConnectException") || r.getMessage().contains("java.net.SocketTimeoutException"))) {
                err.set(true);
                errTime.set(System.currentTimeMillis());
                return true;
            }
            return false;
        }
    }
}
