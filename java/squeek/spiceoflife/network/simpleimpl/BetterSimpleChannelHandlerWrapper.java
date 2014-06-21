package squeek.spiceoflife.network.simpleimpl;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.reflect.Constructor;
import net.minecraft.network.INetHandler;
import org.apache.logging.log4j.Level;
import com.google.common.base.Throwables;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class BetterSimpleChannelHandlerWrapper<REQ extends IMessage, REPLY extends IMessage> extends SimpleChannelInboundHandler<REQ> {
    private IMessageHandler<REQ, REPLY> messageHandler;
    private Side side;
    public BetterSimpleChannelHandlerWrapper(Class<? extends IMessageHandler<REQ, REPLY>> handler, Side side, Class<? extends REQ> messageType)
    {
        super(messageType);
        try
        {
            messageHandler = handler.newInstance();
        } catch (Exception e)
        {
            Throwables.propagate(e);
        }
        this.side = side;
    }
    
    protected static Constructor<MessageContext> messageContextConstructor = null;
    static
    {
    	try
    	{
    		messageContextConstructor = MessageContext.class.getDeclaredConstructor(INetHandler.class, Side.class);
    		messageContextConstructor.setAccessible(true);
    	}
    	catch (Exception e)
    	{
    	}
    }
    protected MessageContext getMessageContext(INetHandler netHandler, Side side)
    {
    	try
		{
			return messageContextConstructor.newInstance(netHandler, side);
		}
		catch (Exception e)
		{
	    	return null;
		}
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, REQ msg) throws Exception
    {
        INetHandler iNetHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        MessageContext context = getMessageContext(iNetHandler, side);
        REPLY result = messageHandler.onMessage(msg, context);
        if (result != null)
        {
            ctx.channel().attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.REPLY);
            ctx.writeAndFlush(result).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        FMLLog.log(Level.ERROR, cause, "SimpleChannelHandlerWrapper exception");
        super.exceptionCaught(ctx, cause);
    }
}
