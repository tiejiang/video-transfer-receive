package com.nercms.receive;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;
import org.sipdroid.net.SipdroidSocket;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class Receive extends Activity
{
	static
	{
		System.loadLibrary("H264Decoder_neon");
	}
	private boolean isRunning; //程序运行标志

	private RtpSocket rtp_socket = null; //创建RTP套接字
	private RtpPacket rtp_packet = null; //创建RTP包
	private byte[] socketBuffer =new byte[2048]; //包缓存
	private byte [] buffer = new byte [2048];
	private long handle = 0; //拼帧器的句柄
	private byte [] frmbuf = new byte[65536]; //帧缓存

	Videoplay view = null;

	public native long CreateH264Packer();
	public native int PackH264Frame(long handle,byte[] pPayload,int payloadlen,int bMark,int pts, int sequence,byte[]frmbuf);
	public native void DestroyH264Packer(long handle);

	public native int CreateDecoder(int width, int height);
	public native int DecoderNal(byte[] in, int insize, byte[] out);
	public native int DestoryDecoder();


	public void close()
	{
		isRunning = false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		view = (Videoplay) this.findViewById(R.id.video_play);
		if (rtp_socket == null)
		{
			try
			{
				rtp_socket = new RtpSocket(new SipdroidSocket(20000)); //初始化套接字，20000为接收端口号
			}
			catch (SocketException e)
			{
				e.printStackTrace();
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
			rtp_packet = new RtpPacket(socketBuffer,0); //初始化 ,socketBuffer改变时rtp_Packet也跟着改变
			handle = CreateH264Packer(); //创建拼帧器
			CreateDecoder(352,288); //创建解码器
			isRunning = true;
			Decoder decoder=new Decoder();
			decoder.start(); //启动一个线程
		}
	}

	@Override
	public void finalize() //在退出界面的时候自动调用
	{
		try
		{
			super.finalize();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		close();
	}

	class Decoder extends Thread
	{
		public void run()
		{
			while (isRunning)
			{
				try
				{
					rtp_socket.receive(rtp_packet); //接收一个包
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				int packetSize = rtp_packet.getPayloadLength(); //获取包的大小

				if(packetSize<=0)
					continue;
				if(rtp_packet.getPayloadType()!=2) //确认负载类型为2
					continue;
				System.arraycopy(socketBuffer,12, buffer, 0, packetSize); //socketBuffer->buffer
				int sequence=rtp_packet.getSequenceNumber(); //获取序列号
				long timestamp=rtp_packet.getTimestamp(); //获取时间戳
				int bMark=rtp_packet.hasMarker()==true?1:0; //是否是最后一个包
				int frmSize=PackH264Frame(handle, buffer, packetSize, bMark, (int)timestamp, sequence, frmbuf); //packer=拼帧器，frmbuf=帧缓存
				if(frmSize<=0)
					continue;
				DecoderNal(frmbuf, frmSize, view.mPixel);//解码后的图像存在mPixel中
				view.postInvalidate();
				Log.d("TIEJIANG", "packetSize= " + packetSize+" sequence= "+sequence+" timestamp= "+timestamp+" bMark= "+bMark+" frmSize= "+frmSize);
			}

			//关闭
			if(handle != 0)
			{
				DestroyH264Packer(handle);
				handle = 0;
			}
			if(rtp_socket!=null)
			{
				rtp_socket.close();
				rtp_socket=null;
			}
			DestoryDecoder();
		}
	}
}