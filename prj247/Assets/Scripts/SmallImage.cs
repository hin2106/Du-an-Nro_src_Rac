using System;
using System.Collections.Generic;
using Assets.src.e;

public static class ClientAssetCache
{
	private const string VERSION_SUFFIX = ".version";

	public static bool TryLoadImage(string cacheKey, int expectedVersion, out Image image)
	{
		image = null;
		if (expectedVersion < 0 || expectedVersion == 255)
		{
			Delete(cacheKey);
			return false;
		}

		sbyte[] savedVersion = Rms.loadRMS(cacheKey + VERSION_SUFFIX);
		if (savedVersion == null || savedVersion.Length != 1 || ToUnsigned(savedVersion[0]) != expectedVersion)
		{
			Delete(cacheKey);
			return false;
		}

		sbyte[] data = Rms.loadRMS(cacheKey);
		if (data == null || data.Length == 0)
		{
			Delete(cacheKey);
			return false;
		}

		try
		{
			image = GameCanvas.prepareAssetImage(Image.createImage(data, 0, data.Length));
			if (image != null)
			{
				return true;
			}
		}
		catch (Exception)
		{
		}

		Delete(cacheKey);
		image = null;
		return false;
	}

	public static void Save(string cacheKey, sbyte[] data, int version)
	{
		if (data == null || data.Length == 0 || version < 0 || version == 255)
		{
			return;
		}

		Rms.saveRMS(cacheKey, data);
		Rms.saveRMS(cacheKey + VERSION_SUFFIX, new sbyte[1] { (sbyte)version });
	}

	public static int ToUnsigned(sbyte value)
	{
		return value < 0 ? value + 256 : value;
	}

	private static void Delete(string cacheKey)
	{
		Rms.DeleteStorage(cacheKey);
		Rms.DeleteStorage(cacheKey + VERSION_SUFFIX);
	}
}

public class SmallImage
{
	public static int[][] smallImg;

	public static SmallImage instance;

	public static Image[] imgbig;

	public static Small[] imgNew;

	public static MyVector vKeys = new MyVector();

	public static Image imgEmpty = null;

	public static sbyte[] newSmallVersion;

	public static int smallCount;

	public static short maxSmall;

	public static Dictionary<int, Image> imageRaw = new Dictionary<int, Image>();

	private static readonly Dictionary<int, long> iconRequestTimes = new Dictionary<int, long>();

	private const long ICON_RETRY_DELAY = 3000L;

	public SmallImage()
	{
		readImage();
	}

	public static void loadBigRMS()
	{
		if (imgbig == null)
		{
			imgbig = new Image[5]
			{
				GameCanvas.loadImageRMS("/img/Big0.png"),
				GameCanvas.loadImageRMS("/img/Big1.png"),
				GameCanvas.loadImageRMS("/img/Big2.png"),
				GameCanvas.loadImageRMS("/img/Big3.png"),
				GameCanvas.loadImageRMS("/img/Big4.png")
			};
		}
	}

	public static void loadBigImage()
	{
		imgEmpty = Image.createRGBImage(new int[16], 4, 4, bl: true);
	}

	public static void init()
	{
		instance = null;
		instance = new SmallImage();
	}

	public void readImage()
	{
		int num = 0;
		try
		{
			DataInputStream dataInputStream = new DataInputStream(Rms.loadRMS("NR_image"));
			short num2 = dataInputStream.readShort();
			smallImg = new int[num2][];
			for (int i = 0; i < smallImg.Length; i++)
			{
				smallImg[i] = new int[5];
			}
			for (int j = 0; j < num2; j++)
			{
				num++;
				smallImg[j][0] = dataInputStream.readUnsignedByte();
				smallImg[j][1] = dataInputStream.readShort();
				smallImg[j][2] = dataInputStream.readShort();
				smallImg[j][3] = dataInputStream.readShort();
				smallImg[j][4] = dataInputStream.readShort();
			}
		}
		catch (Exception ex)
		{
			Cout.LogError3("Loi readImage: " + ex.ToString() + "i= " + num);
		}
	}

	public static void clearHastable()
	{
	}

	private static bool tryLoadCachedImage(int id, out Image image)
	{
		image = null;
		if (newSmallVersion == null || id < 0 || id >= newSmallVersion.Length)
		{
			return false;
		}

		string cacheKey = mGraphics.assetZoomLevel + "Small" + id;
		int expectedVersion = ClientAssetCache.ToUnsigned(newSmallVersion[id]);
		if (!ClientAssetCache.TryLoadImage(cacheKey, expectedVersion, out image))
		{
			return false;
		}

		imageRaw[id] = image;
		return true;
	}

	private static void requestIconIfNeeded(int id)
	{
		long now = mSystem.currentTimeMillis();
		if (iconRequestTimes.TryGetValue(id, out long lastRequest) && now - lastRequest < ICON_RETRY_DELAY)
		{
			return;
		}
		iconRequestTimes[id] = now;
		Service.gI().requestIcon(id);
	}

	public static void markIconLoaded(int id)
	{
		iconRequestTimes.Remove(id);
	}

	public static void markIconFailed(int id)
	{
		if (imgNew != null && id >= 0 && id < imgNew.Length)
		{
			imgNew[id] = new Small(imgEmpty, id);
		}
		iconRequestTimes[id] = 0L;
	}

	private static void retryPlaceholderIcon(int id)
	{
		if (imgNew != null && id >= 0 && id < imgNew.Length && imgNew[id] != null
			&& object.ReferenceEquals(imgNew[id].img, imgEmpty))
		{
			requestIconIfNeeded(id);
		}
	}

	public static void createImage(int id)
	{
		if (imgNew == null || id < 0 || id >= imgNew.Length)
		{
			return;
		}
		Image image2 = GameCanvas.loadImage("/SmallImage/Small" + id + ".png");
		if (image2 != null)
		{
			imgNew[id] = new Small(image2, id);
			markIconLoaded(id);
			return;
		}
		if (tryLoadCachedImage(id, out Image cachedImage))
		{
			imgNew[id] = new Small(cachedImage, id);
			markIconLoaded(id);
			return;
		}
		bool flag = false;
		if (imageRaw.ContainsKey(id))
		{
			Image img = null;
			imageRaw.TryGetValue(id, out img);
			if (img != null)
			{
				imgNew[id] = new Small(img, id);
				markIconLoaded(id);
			}
			else
			{
				flag = true;
			}
		}
		else
		{
			flag = true;
		}
		if (flag)
		{
			imgNew[id] = new Small(imgEmpty, id);
			requestIconIfNeeded(id);
		}
	}

	public static void drawSmallImage(mGraphics g, int id, int x, int y, int transform, int anchor)
	{
		retryPlaceholderIcon(id);
		if (imgbig == null)
		{
			if (id >= 0 && imgNew != null && id < imgNew.Length)
			{
				Small small = imgNew[id];
				if (small == null)
				{
					createImage(id);
				}
				else if (small.img != null && small.img.texture != null)
				{
					g.drawRegion(small, 0, 0, mGraphics.getImageWidth(small.img), mGraphics.getImageHeight(small.img), transform, x, y, anchor);
				}
			}
		}
		else if (smallImg != null)
		{
			if (id < 0 || id >= smallImg.Length || smallImg[id] == null || smallImg[id].Length < 5 || smallImg[id][1] >= 256 || smallImg[id][3] >= 256 || smallImg[id][2] >= 256 || smallImg[id][4] >= 256)
			{
				if (id >= 0 && imgNew != null && id < imgNew.Length)
				{
					Small small2 = imgNew[id];
					if (small2 == null)
					{
						createImage(id);
					}
					else
					{
						small2.paint(g, transform, x, y, anchor);
					}
				}
			}
			else if (smallImg[id][0] >= 0 && smallImg[id][0] < imgbig.Length && imgbig[smallImg[id][0]] != null)
			{
				g.drawRegion(imgbig[smallImg[id][0]], smallImg[id][1], smallImg[id][2], smallImg[id][3], smallImg[id][4], transform, x, y, anchor);
			}
			else
			{
				if (id >= 0 && imgNew != null && id < imgNew.Length)
				{
					Small small2 = imgNew[id];
					if (small2 == null)
					{
						createImage(id);
					}
					else
					{
						small2.paint(g, transform, x, y, anchor);
					}
				}
				else
				{
					createImage(id);
				}
			}
		}
		else if (GameCanvas.currentScreen != GameScr.gI())
		{
			if (id >= 0 && imgNew != null && id < imgNew.Length)
			{
				Small small3 = imgNew[id];
				if (small3 == null)
				{
					createImage(id);
				}
				else
				{
					small3.paint(g, transform, x, y, anchor);
				}
			}
		}
	}

	public static void drawSmallImage(mGraphics g, int id, int f, int x, int y, int w, int h, int transform, int anchor)
	{
		retryPlaceholderIcon(id);
		if (imgbig == null)
		{
			if (id >= 0 && imgNew != null && id < imgNew.Length)
			{
				Small small = imgNew[id];
				if (small == null)
				{
					createImage(id);
				}
				else
				{
					g.drawRegion(small.img, 0, f * w, w, h, transform, x, y, anchor);
				}
			}
		}
		else if (smallImg != null)
		{
			if (id < 0 || id >= smallImg.Length || smallImg[id] == null || smallImg[id].Length < 5 || smallImg[id][1] >= 256 || smallImg[id][3] >= 256 || smallImg[id][2] >= 256 || smallImg[id][4] >= 256)
			{
				if (id >= 0 && imgNew != null && id < imgNew.Length)
				{
					Small small2 = imgNew[id];
					if (small2 == null)
					{
						createImage(id);
					}
					else
					{
						small2.paint(g, transform, f, x, y, w, h, anchor);
					}
				}
				else
				{
					createImage(id);
				}
			}
			else if (smallImg[id][0] >= 0 && smallImg[id][0] < imgbig.Length && smallImg[id][0] != 4 && imgbig[smallImg[id][0]] != null)
			{
				g.drawRegion(imgbig[smallImg[id][0]], 0, f * w, w, h, transform, x, y, anchor);
			}
			else
			{
				if (id >= 0 && imgNew != null && id < imgNew.Length)
				{
					Small small3 = imgNew[id];
					if (small3 == null)
					{
						createImage(id);
					}
					else
					{
						small3.paint(g, transform, f, x, y, w, h, anchor);
					}
				}
				else
				{
					createImage(id);
				}
			}
		}
		else if (GameCanvas.currentScreen != GameScr.gI())
		{
			if (id >= 0 && imgNew != null && id < imgNew.Length)
			{
				Small small4 = imgNew[id];
				if (small4 == null)
				{
					createImage(id);
				}
				else
				{
					small4.paint(g, transform, f, x, y, w, h, anchor);
				}
			}
		}
	}

	public static void update()
	{
		int num = 0;
		if (GameCanvas.gameTick % 1000 != 0)
		{
			return;
		}
		for (int i = 0; i < imgNew.Length; i++)
		{
			if (imgNew[i] != null)
			{
				num++;
				imgNew[i].update();
				smallCount++;
			}
		}
		if (num > 200 && GameCanvas.lowGraphic)
		{
			imgNew = new Small[maxSmall];
		}
	}
}
