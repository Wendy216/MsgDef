package com.ytfs.common.net;

import com.ytfs.common.SerializationUtil;
import static com.ytfs.common.ServiceErrorCode.SERVER_ERROR;
import com.ytfs.common.ServiceException;
import io.yottachain.nodemgmt.core.vo.Node;
import io.yottachain.nodemgmt.core.vo.SuperNode;
import io.yottachain.p2phost.YottaP2P;
import io.yottachain.p2phost.core.exception.P2pHostException;
import io.yottachain.p2phost.interfaces.BPNodeCallback;
import io.yottachain.p2phost.interfaces.NodeCallback;
import io.yottachain.p2phost.interfaces.UserCallback;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import static com.ytfs.common.ServiceErrorCode.COMM_ERROR;

public class P2PUtils {

    private static final Logger LOG = Logger.getLogger(P2PUtils.class);
    private static final Map<String, String> CONNECTS = new HashMap();

    /**
     * 初始化P2P工具
     *
     * @param port
     * @param privateKey
     * @throws java.lang.Exception
     */
    public static void start(int port, String privateKey) throws Exception {
        YottaP2P.start(port, privateKey);
        LOG.info("P2P initialization completed, port " + port);
        LOG.info("Super NodeID:" + YottaP2P.id());
        String[] addrs = YottaP2P.addrs();
        for (int ii = 0; ii < addrs.length; ii++) {
            LOG.info("Super Node Addrs " + ii + ":" + addrs[ii]);
        }
    }

    public static void register(UserCallback userCallback, BPNodeCallback bPNodeCallback, NodeCallback nodeCallback) {
        YottaP2P.registerUserCallback(userCallback);
        YottaP2P.registerBPNodeCallback(bPNodeCallback);
        YottaP2P.registerNodeCallback(nodeCallback);
    }

    public static final int MSG_2BPU = 0;
    public static final int MSG_2BP = 1;
    public static final int MSG_2NODE = 2;

    public static Object requestBPU(Object obj, SuperNode node) throws ServiceException {
        return requestBPU(obj, node, null);
    }

    public static Object requestBPU(Object obj, SuperNode node, String log_prefix) throws ServiceException {
        String log_pre = log_prefix == null
                ? ("[" + obj.getClass().getSimpleName() + "][" + node.getId() + "]")
                : ("[" + obj.getClass().getSimpleName() + "][" + node.getId() + "][" + log_prefix + "]");
        ServiceException err = null;
        for (int ii = 0; ii < 3; ii++) {
            try {
                if (ii > 0) {
                    LOG.info(log_pre + "Retry...");
                }
                return request(obj, node.getAddrs(), node.getNodeid(), MSG_2BPU, log_pre);
            } catch (ServiceException r) {
                if (!(r.getErrorCode() == COMM_ERROR || r.getErrorCode() == SERVER_ERROR)) {
                    throw r;
                }
                err = r;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                }
            }
        }
        throw err;
    }

    public static Object requestBP(Object obj, SuperNode node) throws ServiceException {
        return requestBP(obj, node, null);
    }

    public static Object requestBP(Object obj, SuperNode node, String log_prefix) throws ServiceException {
        String log_pre = log_prefix == null
                ? ("[" + obj.getClass().getSimpleName() + "][" + node.getId() + "]")
                : ("[" + obj.getClass().getSimpleName() + "][" + node.getId() + "][" + log_prefix + "]");
        ServiceException err = null;
        for (int ii = 0; ii < 3; ii++) {
            try {
                if (ii > 0) {
                    LOG.info(log_pre + "Retry...");
                }
                return request(obj, node.getAddrs(), node.getNodeid(), MSG_2BP, log_pre);
            } catch (ServiceException r) {
                if (!(r.getErrorCode() == COMM_ERROR || r.getErrorCode() == SERVER_ERROR)) {
                    throw r;
                }
                err = r;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                }
            }
        }
        throw err;
    }

    public static Object requestNode(Object obj, Node node) throws ServiceException {
        return requestNode(obj, node, null);
    }

    public static Object requestNode(Object obj, Node node, String log_prefix) throws ServiceException {
        String log_pre = log_prefix == null
                ? ("[" + obj.getClass().getSimpleName() + "][" + node.getId() + "]")
                : ("[" + obj.getClass().getSimpleName() + "][" + node.getId() + "][" + log_prefix + "]");
        return request(obj, node.getAddrs(), node.getNodeid(), MSG_2NODE, log_pre);
    }

    private static Object request(Object obj, List<String> addr, String key, int type, String log_pre) throws ServiceException {
        if (!CONNECTS.containsKey(key)) {
            synchronized (CONNECTS) {
                if (!CONNECTS.containsKey(key)) {
                    String addstr = getAddrString(addr);
                    try {
                        String[] strs = new String[addr.size()];
                        strs = addr.toArray(strs);
                        YottaP2P.connect(key, strs);
                        CONNECTS.put(key, addstr);
                    } catch (P2pHostException ex) {
                        LOG.info(log_pre + "Connect " + addstr + " Err.");
                        throw new ServiceException(COMM_ERROR, ex.getMessage());
                    }
                }
            }
        }
        byte[] data = SerializationUtil.serialize(obj);
        byte[] bs = null;
        try {                    //访问p2p网络
            switch (type) {
                case MSG_2BPU:
                    bs = YottaP2P.sendToBPUMsg(key, data);
                    break;
                case MSG_2BP:
                    bs = YottaP2P.sendToBPMsg(key, data);
                    break;
                default:
                    bs = YottaP2P.sendToNodeMsg(key, data);
                    break;
            }
        } catch (Throwable e) {
            String oldaddrString = CONNECTS.get(key);
            LOG.error(log_pre + "INTERNAL_ERROR:" + (oldaddrString == null ? ("[" + e.getMessage() + "]") : oldaddrString));
            String newaddrString = getAddrString(addr);
            synchronized (CONNECTS) {
                if (CONNECTS.containsKey(key)) {
                    CONNECTS.remove(key);
                    try {
                        YottaP2P.disconnect(key);
                    } catch (P2pHostException r) {
                    }
                }
            }
            if (oldaddrString == null || !oldaddrString.equals(newaddrString)) {
                LOG.info(log_pre + "Retry...");
                return request(obj, addr, key, type, log_pre);
            } else {
                throw new ServiceException(COMM_ERROR, e.getMessage());
            }
        }
        Object res = SerializationUtil.deserialize(bs);
        if (res instanceof ServiceException) {
            throw (ServiceException) res;
        }
        return res;
    }

    public static String getAddrString(List<String> ls) {
        StringBuilder res = null;
        for (String s : ls) {
            if (res == null) {
                res = (new StringBuilder("[")).append(s);
            } else {
                res.append(",").append(s);
            }
        }
        return res == null ? "" : res.append("]").toString();
    }

    /**
     * 销毁
     */
    public static void stop() {
        try {
            YottaP2P.close();
        } catch (P2pHostException ex) {
        }
    }
}
