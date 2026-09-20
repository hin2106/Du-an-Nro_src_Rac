using System;
using System.Text;
using UnityEngine;

public class myReader
{
	public sbyte[] buffer;

	private int posRead;

	private int posMark;

	private static string fileName;

	private static int status;

	public myReader()
	{
	}

	public myReader(sbyte[] data)
	{
		buffer = data;
	}

	public myReader(string filename)
	{
		TextAsset textAsset = (TextAsset)Resources.Load(filename, typeof(TextAsset));
		buffer = mSystem.convertToSbyte(textAsset.bytes);
	}

	public sbyte readSByte()
	{
		if (posRead < buffer.Length)
		{
			return buffer[posRead++];
		}
		posRead = buffer.Length;
		throw new Exception(" loi doc sbyte eof ");
	}

	public sbyte readsbyte()
	{
		return readSByte();
	}

	public sbyte readByte()
	{
		return readSByte();
	}

	public void mark(int readlimit)
	{
		posMark = posRead;
	}

	public void reset()
	{
		posRead = posMark;
	}

	public byte readUnsignedByte()
	{
		return convertSbyteToByte(readSByte());
	}

	public short readShort()
	{
		if (posRead + 2 > buffer.Length)
		{
			posRead = buffer.Length;
			throw new Exception(" loi doc short eof: posRead=" + posRead + " buffer.Length=" + buffer.Length);
		}
		short num = 0;
		for (int i = 0; i < 2; i++)
		{
			num <<= 8;
			num |= (short)(0xFF & buffer[posRead++]);
		}
		return num;
	}

	public ushort readUnsignedShort()
	{
		if (posRead + 2 > buffer.Length)
		{
			posRead = buffer.Length;
			throw new Exception(" loi doc unsigned short eof: posRead=" + posRead + " buffer.Length=" + buffer.Length);
		}
		ushort num = 0;
		for (int i = 0; i < 2; i++)
		{
			num <<= 8;
			num |= (ushort)(0xFFu & (uint)buffer[posRead++]);
		}
		return num;
	}

	public int readInt()
	{
		if (posRead + 4 > buffer.Length)
		{
			posRead = buffer.Length;
			throw new Exception(" loi doc int eof: posRead=" + posRead + " buffer.Length=" + buffer.Length);
		}
		int num = 0;
		for (int i = 0; i < 4; i++)
		{
			num <<= 8;
			num |= 0xFF & buffer[posRead++];
		}
		return num;
	}

	public long readLong()
	{
		long num = 0L;
		for (int i = 0; i < 8; i++)
		{
			num <<= 8;
			num |= 0xFF & buffer[posRead++];
		}
		return num;
	}
	public double readDouble()
	{
		if (ModFunc.isReadInt)
		{

			return readInt();
		}
		else if (ModFunc.isReadDouble)
		{
			return readDouble2();
		}
		else
		{

			return readLong();
		}

	}
	public double readDouble2()
	{
		return BitConverter.Int64BitsToDouble(readLong());
	}
	public bool readBool()
	{
		return (readSByte() > 0) ? true : false;
	}

	public bool readBoolean()
	{
		return (readSByte() > 0) ? true : false;
	}

	public string readString()
	{
		short num = readShort();
		byte[] array = new byte[num];
		for (int i = 0; i < num; i++)
		{
			array[i] = convertSbyteToByte(readSByte());
		}
		UTF8Encoding uTF8Encoding = new UTF8Encoding();
		return uTF8Encoding.GetString(array);
	}

	public string readStringUTF()
	{
		short num = readShort();
		byte[] array = new byte[num];
		for (int i = 0; i < num; i++)
		{
			array[i] = convertSbyteToByte(readSByte());
		}
		UTF8Encoding uTF8Encoding = new UTF8Encoding();
		return uTF8Encoding.GetString(array);
	}

	public string readUTF()
	{
		return readStringUTF();
	}

	public int read()
	{
		if (posRead < buffer.Length)
		{
			return readSByte();
		}
		return -1;
	}

	public int read(ref sbyte[] data)
	{
		if (data == null)
		{
			return 0;
		}
		int availableBytes = buffer.Length - posRead;
		int bytesToRead = (data.Length < availableBytes) ? data.Length : availableBytes;
		if (bytesToRead > 0)
		{
			Buffer.BlockCopy(buffer, posRead, data, 0, bytesToRead);
			posRead += bytesToRead;
			return bytesToRead;
		}
		return -1;
	}

	public void readFully(ref sbyte[] data)
	{
		if (data != null && data.Length + posRead <= buffer.Length)
		{

			Buffer.BlockCopy(buffer, posRead, data, 0, data.Length);
			posRead += data.Length;
		}
	}

	public int available()
	{
		return buffer.Length - posRead;
	}

	public static byte convertSbyteToByte(sbyte var)
	{
		if (var > 0)
		{
			return (byte)var;
		}
		return (byte)(var + 256);
	}

	public static byte[] convertSbyteToByte(sbyte[] var)
	{
		byte[] array = new byte[var.Length];
		for (int i = 0; i < var.Length; i++)
		{
			if (var[i] > 0)
			{
				array[i] = (byte)var[i];
			}
			else
			{
				array[i] = (byte)(var[i] + 256);
			}
		}
		return array;
	}

	public void Close()
	{
		buffer = null;
	}

	public void close()
	{
		buffer = null;
	}

	public void read(ref sbyte[] data, int arg1, int arg2)
	{
		if (data == null)
		{
			return;
		}

		int availableBytes = buffer.Length - posRead;
		int bytesToRead = (arg2 < availableBytes) ? arg2 : availableBytes;
		if (bytesToRead > 0)
		{
			Buffer.BlockCopy(buffer, posRead, data, arg1, bytesToRead);
			posRead += bytesToRead;
		}
	}
}
