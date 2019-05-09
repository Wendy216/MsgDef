package com.ytfs.service.packet;

import java.util.List;

public class NodeRegReq {

    private String nodeid;
    private String owner;
    private long maxDataSpace;
    private List<String> addrs;

    /**
     * @return the nodeid
     */
    public String getNodeid() {
        return nodeid;
    }

    /**
     * @param nodeid the nodeid to set
     */
    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return the maxDataSpace
     */
    public long getMaxDataSpace() {
        return maxDataSpace;
    }

    /**
     * @param maxDataSpace the maxDataSpace to set
     */
    public void setMaxDataSpace(long maxDataSpace) {
        this.maxDataSpace = maxDataSpace;
    }

    /**
     * @return the addrs
     */
    public List<String> getAddrs() {
        return addrs;
    }

    /**
     * @param addrs the addrs to set
     */
    public void setAddrs(List<String> addrs) {
        this.addrs = addrs;
    }

}