public class MotherCanvas
{
	public static MotherCanvas instance;

	public GameCanvas tCanvas;

	public int getWidth()
	{
		return (int)ScaleGUI.WIDTH;
	}

	public int getHeight()
	{
		return (int)ScaleGUI.HEIGHT;
	}

	public void setChildCanvas(GameCanvas tCanvas)
	{
		this.tCanvas = tCanvas;
	}

	public int getWidthz()
	{
		int width = getWidth();
		return (width + mGraphics.zoomLevel - 1) / mGraphics.zoomLevel;
	}

	public int getHeightz()
	{
		int height = getHeight();
		return (height + mGraphics.zoomLevel - 1) / mGraphics.zoomLevel;
	}
}
