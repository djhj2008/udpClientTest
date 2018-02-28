package com.example.demo.init;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger log= LoggerFactory.getLogger(UdpClient.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        String response = msg.content().toString(CharsetUtil.UTF_8);
        log.debug(response);
        int len = msg.content().readableBytes();
        byte[] buf = new byte[len];
        msg.content().getBytes(0,buf);
        for(int i=0;i<len;i++){
            log.debug(i+":0x" + Integer.toHexString(buf[i]));
        }
        if(response.startsWith("d")) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
