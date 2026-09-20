using System.Threading;
using Mod.XMAP;

public class InfoMe
{
	public static InfoMe me;

	public int[][] charId = new int[3][];

	public Info info = new Info();

	public int dir;

	public int f;

	public int tF;

	public int cmtoY;

	public int cmy;

	public int cmdy;

	public int cmvy;

	public int cmtoX;

	public int cmx;

	public int cmdx;

	public int cmvx;

	public bool isDone;

	public bool isUpdate = true;

	public int timeDelay;

	public int playerID;

	public Command cmdChat;

	public InfoMe()
	{
		for (int i = 0; i < charId.Length; i++)
		{
			charId[i] = new int[3];
		}
	}

	public static InfoMe gI()
	{
		if (me == null)
		{
			me = new InfoMe();
		}
		return me;
	}

	public void loadCharId()
	{
		for (int i = 0; i < charId.Length; i++)
		{
			charId[i] = new int[3];
		}
	}

	public void paint(mGraphics g)
	{
		if ((Equals(GameScr.info2) && GameScr.gI().isVS()) || (Equals(GameScr.info2) && GameScr.gI().popUpYesNo != null) || !GameScr.isPaint || (GameCanvas.currentScreen != GameScr.gI() && GameCanvas.currentScreen != CrackBallScr.gI()) || ChatPopup.serverChatPopUp != null || !isUpdate || Char.ischangingMap || (GameCanvas.panel.isShow && Equals(GameScr.info2)))
		{
			return;
		}
		g.translate(-g.getTranslateX(), -g.getTranslateY());
		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
		bool isWorldChatHud = Equals(GameScr.info2);
		bool hasWorldChat = isWorldChatHud && info != null && info.info != null && info.info.charInfo != null && info.infoWaitToShow.size() != 0;
		if (isWorldChatHud && (hasWorldChat || ModFunc.isEditButton))
		{
			paintWorldChatHud(g, hasWorldChat);
			return;
		}
		if (info != null)
		{
			info.paint(g, cmx, cmy, dir);
			if (info.info != null && info.info.charInfo != null && cmdChat == null)
			{
				_ = GameCanvas.isTouch;
			}
			if (info.info != null && info.info.charInfo != null)
			{
				_ = cmdChat;
			}
		}
		if (info.info != null && info.info.charInfo == null && charId != null)
		{
			SmallImage.drawSmallImage(g, charId[Char.myCharz().cgender][f], cmx, cmy + 3 + ((GameCanvas.gameTick % 10 > 5) ? 1 : 0), (dir != 1) ? 2 : 0, StaticObj.VCENTER_HCENTER);
		}
		g.translate(-g.getTranslateX(), -g.getTranslateY());
	}

	private void paintWorldChatHud(mGraphics g, bool hasWorldChat)
	{
		int zoomOffset = (mGraphics.zoomLevel != 1) ? 10 : 0;
		int hudX;
		int hudY;
		int hudWidth;
		int hudHeight;
		if (hasWorldChat)
		{
			hudX = cmx + info.X - 23;
			hudY = cmy + info.Y - zoomOffset / 2;
			hudWidth = ((mSystem.clientType != 1) ? (info.W + 25) : (info.W + 28));
			hudHeight = info.H + ((!GameCanvas.isTouch) ? 14 : 0) + zoomOffset + 2;
		}
		else
		{
			hudWidth = 130;
			hudHeight = 45;
			hudX = System.Math.Max(0, GameCanvas.w - hudWidth - 35);
			hudY = 4;
		}

		ModFunc.HudPaintState hudState = ModFunc.BeginHudElement(g, ModFunc.HUD_WORLD_CHAT, "Khung chat kênh thế giới", hudX, hudY, hudWidth, hudHeight);
		if (hasWorldChat)
		{
			info.paint(g, cmx, cmy, dir);
			g.translate(-g.getTranslateX(), -g.getTranslateY());
			if (hudState.translateX != 0 || hudState.translateY != 0)
			{
				g.translate(hudState.translateX, hudState.translateY);
			}
		}
		else
		{
			mSystem.paintPopUp2(g, hudX, hudY, hudWidth, hudHeight - 2);
			Char.myCharz().paintCharBody(g, hudX + 16, hudY + hudHeight - 5, 1, 0, true);
			mFont.tahoma_7b_greenSmall.drawString(g, Char.myCharz().cName, hudX + 34, hudY + 4, 0);
			mFont.tahoma_7_whiteSmall.drawString(g, "Nội dung chat thế giới", hudX + 34, hudY + 18, 0);
		}
		ModFunc.EndHudElement(hudState);
		g.translate(-g.getTranslateX(), -g.getTranslateY());
	}

	public void hide()
	{
		info.hide();
	}

	public void moveCamera()
	{
		if (cmy != cmtoY)
		{
			cmvy = cmtoY - cmy << 2;
			cmdy += cmvy;
			cmy += cmdy >> 4;
			cmdy &= 15;
		}
		if (cmx != cmtoX)
		{
			cmvx = cmtoX - cmx << 2;
			cmdx += cmvx;
			cmx += cmdx >> 4;
			cmdx &= 15;
		}
		tF++;
		if (tF == 5)
		{
			tF = 0;
			if (f == 0)
			{
				f = 1;
			}
			else
			{
				f = 0;
			}
		}
	}

	public void doClick(int t)
	{
		timeDelay = t;
	}

	public void update()
	{
		if (info != null && info.infoWaitToShow != null && info.infoWaitToShow.size() == 0 && cmy != -40)
		{
			info.timeW--;
			if (info.timeW <= 0)
			{
				cmy = -40;
				info.time = 0;
				info.infoWaitToShow.removeAllElements();
				info.says = null;
				info.timeW = 200;
			}
		}
		if ((Equals(GameScr.info2) && GameScr.gI().popUpYesNo != null) || !isUpdate)
		{
			return;
		}
		moveCamera();
		if (info == null || (info != null && info.info == null))
		{
			return;
		}
		if (!isDone)
		{
			if (timeDelay > 0)
			{
				timeDelay--;
				if (timeDelay == 0)
				{
					GameCanvas.panel.setTypeMessage();
					GameCanvas.panel.show();
				}
			}
			if (GameCanvas.gameTick % 3 == 0)
			{
				if (Char.myCharz().cdir == 1)
				{
					cmtoX = Char.myCharz().cx - 20 - GameScr.cmx;
				}
				if (Char.myCharz().cdir == -1)
				{
					cmtoX = Char.myCharz().cx + 20 - GameScr.cmx;
				}
				if (cmtoX <= 24)
				{
					cmtoX += info.sayWidth / 2;
				}
				if (cmtoX >= GameCanvas.w - 24)
				{
					cmtoX -= info.sayWidth / 2;
				}
				cmtoY = Char.myCharz().cy - 40 - GameScr.cmy;
				if (info.says != null && cmtoY < (info.says.Length + 1) * 12 + 10)
				{
					cmtoY = (info.says.Length + 1) * 12 + 10;
				}
				if (info.info.charInfo != null)
				{
					if (GameCanvas.w - 50 > 155 + info.W)
					{
						cmtoX = GameCanvas.w - 60 - info.W / 2;
						cmtoY = info.H + 10;
					}
					else
					{
						cmtoX = GameCanvas.w - 20 - info.W / 2;
						cmtoY = 45 + info.H;
						if (GameCanvas.w > GameCanvas.h || GameCanvas.w < 220)
						{
							cmtoX = GameCanvas.w - 20 - info.W / 2;
							cmtoY = info.H + 10;
						}
					}
				}
			}
			if (cmx > Char.myCharz().cx - GameScr.cmx)
			{
				dir = -1;
			}
			else
			{
				dir = 1;
			}
		}
		if (info.info == null)
		{
			return;
		}
		if (info.infoWaitToShow.size() > 1)
		{
			if (info.info.timeCount == 0)
			{
				info.time++;
				if (info.time >= info.info.speed)
				{
					info.time = 0;
					info.infoWaitToShow.removeElementAt(0);
					InfoItem infoItem = (InfoItem)info.infoWaitToShow.firstElement();
					info.info = infoItem;
					info.getInfo();
				}
				return;
			}
			info.info.curr = mSystem.currentTimeMillis();
			if (info.info.curr - info.info.last >= 100)
			{
				info.info.last = mSystem.currentTimeMillis();
				info.info.timeCount--;
			}
			if (info.info.timeCount == 0)
			{
				info.infoWaitToShow.removeElementAt(0);
				if (info.infoWaitToShow.size() != 0)
				{
					InfoItem infoItem2 = (InfoItem)info.infoWaitToShow.firstElement();
					info.info = infoItem2;
					info.getInfo();
				}
			}
		}
		else
		{
			if (info.infoWaitToShow.size() != 1)
			{
				return;
			}
			if (info.info.timeCount == 0)
			{
				info.time++;
				if (info.time >= info.info.speed)
				{
					isDone = true;
				}
				if (info.time == info.info.speed)
				{
					cmtoY = -40;
					cmtoX = Char.myCharz().cx - GameScr.cmx + ((Char.myCharz().cdir != 1) ? 20 : (-20));
				}
				if (info.time >= info.info.speed + 20)
				{
					info.time = 0;
					info.infoWaitToShow.removeAllElements();
					info.says = null;
					info.timeW = 200;
				}
			}
			else
			{
				info.info.curr = mSystem.currentTimeMillis();
				if (info.info.curr - info.info.last >= 100)
				{
					info.info.last = mSystem.currentTimeMillis();
					info.info.timeCount--;
				}
				if (info.info.timeCount == 0)
				{
					isDone = true;
					cmtoY = -40;
					cmtoX = Char.myCharz().cx - GameScr.cmx + ((Char.myCharz().cdir != 1) ? 20 : (-20));
					info.time = 0;
					info.infoWaitToShow.removeAllElements();
					info.says = null;
					cmdChat = null;
				}
			}
		}
	}

	public void addInfoWithChar(string s, Char c, bool isChatServer)
	{
		playerID = c.charID;
		info.addInfo(s, 3, c, isChatServer);
		isDone = false;
	}

	public void addInfo(string s, int Type)
	{
		s = Res.changeString(s);
		AutoXmap.Info(s);
		if (info.infoWaitToShow.size() > 0 && s.Equals(((InfoItem)info.infoWaitToShow.lastElement()).s))
		{
			return;
		}
		if (info.infoWaitToShow.size() > 10)
		{
			for (int i = 0; i < 5; i++)
			{
				info.infoWaitToShow.removeElementAt(0);
			}
		}
		Char cInfo = null;
		info.addInfo(s, Type, cInfo, isChatServer: false);
		if (info.infoWaitToShow.size() == 1)
		{
			cmy = 0;
			cmx = Char.myCharz().cx - GameScr.cmx + ((Char.myCharz().cdir != 1) ? 20 : (-20));
		}
		isDone = false;
	}
}
