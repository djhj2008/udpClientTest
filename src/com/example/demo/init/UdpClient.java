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

import java.io.*;

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

            sendjpg(ch);
            //bmpvalueconf(ch);
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
        int len = 14;
        byte[] tmpbuf = new byte[len];
        tmpbuf[0] = '1';
        tmpbuf[1] = '1';
        tmpbuf[2] = '0';
        tmpbuf[3] = '0';
        tmpbuf[4] = '0';
        tmpbuf[5] = '0';
        tmpbuf[6] = '0';
        tmpbuf[7] = '0';
        tmpbuf[8] = '3';
        tmpbuf[9] = 0;
        byte[] value;
        value = intToBytel(1000);
        System.arraycopy(value, 0, tmpbuf, 10, 4);

        sendbuf(ctx,tmpbuf,len,4);
    }


    public static void getdevicestate(Channel ctx){
        int len = 10;
        byte[] tmpbuf = new byte[len];
        tmpbuf[0] = '1';
        tmpbuf[1] = '1';
        tmpbuf[2] = '0';
        tmpbuf[3] = '0';
        tmpbuf[4] = '0';
        tmpbuf[5] = '0';
        tmpbuf[6] = '0';
        tmpbuf[7] = '0';
        tmpbuf[8] = '3';
        tmpbuf[9] = 0;
        sendbuf(ctx,tmpbuf,len,1);
    }
    public static void sendjpg(Channel ctx) {
        int len=12;
        String path = new File("normalup").getAbsolutePath();
        String filename = path+File.separator+"3.jpg";
        File fd = new File(filename);
        int file_len = (int)fd.length();
        len += file_len;
        InputStream is =null;
        byte[] buf = new byte[len];
        buf[0] = '1';
        buf[1] = '1';
        buf[2] = '0';
        buf[3] = '0';
        buf[4] = '0';
        buf[5] = '0';
        buf[6] = '0';
        buf[7] = '0';
        buf[8] = '3';
        buf[9] = 0;
        buf[10] = 0;
        buf[11] = 1;
        try {
            is = new FileInputStream(fd);
            try {
                int read_len = is.read(buf,12,file_len);
                log.debug("read len:"+read_len);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        sendbuf(ctx,buf,len,7);

    }


    public static void sendbuf(Channel ctx, byte[] tmpbuf, int len,int cmd){
        String url = "124.207.250.67";
        //String url = "localhost";
        byte[] buf = new byte[512];
        int sn = 110000003;
        int count = len/501+1;
        int index = 0;
        int send_len = 0;
        log.debug("send count:"+count+" len:"+len);
        for(int i=0;i<count;i++) {
            buf[0] = 'd';
            buf[1] = (byte)count;
            buf[2] = (byte) i;
            byte[] bufsn;
            bufsn = intToBytel(sn);
            System.arraycopy(bufsn, 0, buf, 3, 4);
            buf[7] = (byte)cmd;
            index = i*501;
            if(len -index >501){
                send_len = 501;
            }else{
                send_len =len%501;
            }
            log.debug("send ack:"+i+" send_len:"+send_len+" index："+index);
            byte[] buflen;
            buflen = shortToBytel((short)send_len );
            System.arraycopy(buflen, 0, buf, 8, 2);
            System.arraycopy(tmpbuf, index, buf, 10, send_len);
            int sum=0;
            for(int j=0;j<send_len;j++){
                sum+=buf[10+j];
            }
            buf[10+send_len]=(byte)sum;
            log.debug("checksum:"+Integer.toHexString(sum));
            try {
                //"124.207.250.67"
                ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(buf,0,send_len+11), SocketUtils.socketAddress(url, PORT))).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
