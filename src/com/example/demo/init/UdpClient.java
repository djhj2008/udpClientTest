package com.example.demo.init;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpClient {
    static final int PORT = 16001;
    private static final Logger log= LoggerFactory.getLogger(UdpClient.class);
    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UdpClientHandler());

            Channel ch = b.bind(0).sync().channel();

            bmpvalueconf(ch);
            //getdevicestate(ch);
            // QuoteOfTheMomentClientHandler will close the DatagramChannel when a
            // response is received.  If the channel is not closed within 5 seconds,
            // print an error message and quit.
            if (!ch.closeFuture().await(30000)) {
                System.err.println("QOTM request timed out.");
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void bmpvalueconf(Channel ctx){
        int sn = 110000003;
        int count =0;
        int len = 25;

        byte[] tmpbuf = new byte[25];
        tmpbuf[0] = 'd';
        tmpbuf[1] = 1;
        tmpbuf[2] = (byte)count;
        byte[] bufsn;
        bufsn = intToBytel(sn);
        System.arraycopy(bufsn, 0, tmpbuf, 3, 4);
        tmpbuf[7] = 4;
        byte[] buflen;
        buflen = shortToBytel((short) 14);
        System.arraycopy(buflen, 0, tmpbuf, 8, 2);
        tmpbuf[10] = '1';
        tmpbuf[11] = '1';
        tmpbuf[12] = '0';
        tmpbuf[13] = '0';
        tmpbuf[14] = '0';
        tmpbuf[15] = '0';
        tmpbuf[16] = '0';
        tmpbuf[17] = '0';
        tmpbuf[18] = '3';
        tmpbuf[19] = 0;
        byte[] value;
        value = intToBytel(1000);
        System.arraycopy(value, 0, tmpbuf, 20, 4);
        tmpbuf[24] = (byte)160;

        sendbuf(ctx,tmpbuf,len);
    }


    public static void getdevicestate(Channel ctx){
        int sn = 110000003;
        int count =0;
        int len = 16;

        byte[] tmpbuf = new byte[16];
        tmpbuf[0] = 'd';
        tmpbuf[1] = 1;
        tmpbuf[2] = (byte)count;
        byte[] bufsn;
        bufsn = intToBytel(sn);
        System.arraycopy(bufsn, 0, tmpbuf, 3, 4);
        tmpbuf[7] = 1;
        byte[] buflen;
        buflen = shortToBytel((short) 0);
        System.arraycopy(buflen, 0, tmpbuf, 8, 2);
        tmpbuf[10] = 0;

        sendbuf(ctx,tmpbuf,len);
    }

    public static void sendbuf(Channel ctx, byte[] tmpbuf, int len){
        try {
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(tmpbuf,0,len), SocketUtils.socketAddress("124.207.250.67", PORT))).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static byte[] intToBytel(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = b.length-1; i >=0; i--) {
            b[i] = new Integer(temp & 0xff).byteValue();
            temp = temp >> 8; // 向右移8位
            //log.debug("buf:"+Integer.toHexString(b[i]));
        }
        return b;
    }

    public static byte[] shortToBytel(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = b.length-1; i >=0; i--)  {
            b[i] = new Integer(temp & 0xff).byteValue();
            temp = temp >> 8; // 向右移8位
            //log.debug("buf:"+Integer.toHexString(b[i]));
        }
        return b;
    }
}
