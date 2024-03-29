package com.ytfs.common.conf;

public class ServerConfig {

    //*************************不可配置参数*************************************
    //每个文件在上传后必须存储的最短周期，如60个周期
    public final static long PMS = 60;

    //单位空间（如16K）周期费用，如100000000 nHDD=1G使用365天，如果每周期1天
    public static long unitcost = 4;

    //计费周期如:1天
    public static long PPC = 1000 * 60 * 60 * 24;

    //元数据空间
    public final static long PCM = 16 * 1024;

    //小于PL2的数据块，直接记录在元数据库中
    public final static int PL2 = 256;

    //存储节点验签失败,拒绝存储,超过3次,惩罚
    public final static int PNF = 3;

    //上传DNI
    public final static int SENDDNI_QUEUE = 2000;

    //重建
    public final static int REBULIDTHREAD = 100;

    //**************************可配置参数********************************
    //服务端超级节点编号,本服务节点编号
    public static int superNodeID;

    //超级节点私钥
    public static String privateKey;
    public static byte[] SNDSP;

    //端口
    public static int port = 9999;

    //eos ADD
    public static String eosURI;
    public static String BPAccount;
    public static String BPPriKey;
    public static String contractAccount;
    public static String contractOwnerD;

    //端口
    public static int httpPort = 8080;

    //http绑定ip
    public static String httpBindip = "";

}
