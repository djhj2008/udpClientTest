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

            //datareportbuf(ch);
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
    public static int parseCharValue(int num){
        int ret = 0;
        char tmp = (char) num;
        String str = String.valueOf(tmp);
        ret = Integer.valueOf(str);
        return ret;
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
        tmpbuf[9] = '0';
        byte[] value;
        value = intToBytel(1000);
        System.arraycopy(value, 0, tmpbuf, 10, 4);

        sendbuf(ctx,tmpbuf,len,6);
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
        tmpbuf[9] = '1';
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
        buf[9] = '1';
        buf[10] = '0';
        buf[11] = '1';
        try {
            is = new FileInputStream(fd);
            try {
                int read_len = is.read(buf,12,file_len);
                log.debug("read len:"+read_len);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String filename2 = path+File.separator+"tmp.txt";
        File fd2 = new File(filename2);
        FileOutputStream os = null;
        try {
            os= new FileOutputStream(fd2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for(int i=0;i<file_len;i++){
            try {
                //byte[] tmp = Integer.toHexString(buf[12+i]&0xff).getBytes();
                byte[] tmp = String.format("0x%02x",buf[12+i]&0xff).getBytes();
                os.write(tmp);
                byte[] doc = new byte[1];
                doc[0]=',';
                os.write(doc);

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //sendbuf(ctx,buf,len,7);
    }

    public static void datareportbuf(Channel ctx){
        String buf_str = "43322d262222252b2f1f1a1f3c2f371e172b444c4635151d3c3537211e3f535c534a211f3c3a301f" +
                "21505c645759261d3c38291d1f56596256561f173c38281e2356555e564d1d193c35291d2450525a" +
                "554a1f1f3c332b192447505450431e223c383219254350504c38181e3a3e2b191c32494b4223191f" +
                "3c3f3a260f152b3226121d2f3c42493e23151a1c140f2b463c3a414135241815192a42533a454b4d" +
                "4a423e41474850543835464f4a4850534d52504d373548534d484c4d494d4d4c373c41443f322b32" +
                "3f4a4f4d37383a35261e1e1f1d2f434f55483a302515292d30261c1f4d3c383a231f3c5450412a1f" +
                "4d413c371f30576a6355371c4d443f321e3f6969655e3f194c44412b1c496b6a6b6745194b423f25" +
                "1c4d64676b633f1a4b413f1f1c505d5e655a351d4d443f1e1c53575d675733214d47431d1f425a5a" +
                "5a4f3023484a3f1f19314b545241211f46474125191c303f3c2d1521464546322917191f1f1d1930" +
                "49484b43412b1f19182230464c494b4d544a3a3a333c49544c47485056594d57505456574544494d" +
                "50595357555755573e434d4a4c47444147525a56413a424c2f241f1c2332454d5e544337352d2929" +
                "1f3031385d4d444344291a221f1e1c325c4d4d4d4326192d301c0f2a5d52574d2f26324b4f381f22" +
                "5c50534622265465655c3a1d594a463c252a646d696b4d1d56493f3225326469696752225550432d" +
                "1e3a5e656b645026534a3c2b1e325c646b614a22534a3f2f21375d63645a4823534a3f30222f525a" +
                "5a523f22504b43311f254353564b321e504d4835211f3041463a1f1e504f4d413017182326251528" +
                "504d5250482a1f1e1925263f4d49525a59534135263842554f494d565956545355575756504a4d53" +
                "5453525255595a5a6a6a6952575956525356575d675d524b494746484741383a6b6053575e615e5a" +
                "5756554b655e56646e726f69676e7471635c55676a6b6b6d70717172635e59646b706f6b6d717471" +
                "5d5a555c64675a4b48525c6a635c52615a4b352521262b4b605d4d5d43281f282b2418215e59504d" +
                "2b1828424d3f291c5c5553533a384d5a5e57471f5c52555a535a635a595950285d53555e5a5e615d" +
                "5c5342265c55545d6061605c4f38291d5956525660605e54311719195656505a5e5a605d3215261f" +
                "565052555a5e5c5750381922564f4d4d46495459543e2426656a63574c45413e3a3f454865676057" +
                "504b46423f3a3f416a63606064656360605e5e5c69635e626b716f6e7172706b67645e626f757275" +
                "7972726e64635d616e71676365646f7160615a5d65604938353c5667605e5d5e564135353533233a" +
                "625a5956493a3c484c412929615e57504846535e5e573c235e61544d50575e5d5460502b5d5c5050" +
                "5550473f3a47482f5e5752534c382d2f322b32255c57544b382b323f42332e1f5957503e282a4759" +
                "52523c2357574d32283e53575a574f2a57564f372635525e5d5e4b265a544d3a282b495c57543f29";
        byte[] bmpall = HexString2Byte(buf_str);
        byte[] buf = new byte[bmpall.length+10];
        buf[0] = '1';
        buf[1] = '1';
        buf[2] = '0';
        buf[3] = '0';
        buf[4] = '0';
        buf[5] = '0';
        buf[6] = '0';
        buf[7] = '0';
        buf[8] = '3';
        buf[9] = '1';
        System.arraycopy(bmpall, 0, buf, 10, bmpall.length);
        sendbuf(ctx,buf,bmpall.length+10,11);
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

    public static byte[] HexString2Byte(String buf){
        byte[] ret=null;
        log.debug(buf);
        log.debug("len:"+buf.length());
        if(buf.length()%2!=0){
            return null;
        }else{
            ret = new byte[buf.length()/2];
        }

        for(int i=0;i<buf.length()/2;i++){
            int begin = i*2;
            int end = begin+2;
            String substr= buf.substring(begin,end);
            int tmp = Integer.decode("0x"+substr);
            ret[i]=(byte)tmp;
        }
        return ret;
    }
}
