namespace Assets.src.g
{
	public class RegisterScreen : mScreen, IActionListener
	{
		public TField tfPassword;

		public TField tfUsername;

		public TField tfConfirmPassword;

		private int focus;

		private readonly Command cmdExit;

		private readonly Command cmdOK;

		private readonly Command cmdShowPassword;

		private readonly Command cmdShowConfirmPassword;

		private bool showPassword;

		private bool showConfirmPassword;

		public static string serverName;

		public static Image imgTitle;

		public int plX;

		public int plY;

		public int lY;

		public int lX;

		public int logoDes;

		public int lineX;

		public int lineY;

		public static int[] bgId = new int[5] { 0, 8, 2, 6, 9 };

		public static bool isTryGetIPFromWap;

		public static short timeLogin;

		public static long lastTimeLogin;

		public static long currTimeLogin;

		private int xLog;

		private int yLog;

		private readonly int v = 2;

		private int g;

		private int ylogo = -40;

		private int dir = 1;

		public RegisterScreen()
		{
			yLog = 130;
			TileMap.bgID = (sbyte)(mSystem.currentTimeMillis() % 9);
			if (TileMap.bgID == 5 || TileMap.bgID == 6)
			{
				TileMap.bgID = 4;
			}
			GameScr.loadCamera(fullmScreen: true, -1, -1);
			GameScr.cmx = 100;
			GameScr.cmy = 200;
			tfUsername = new TField
			{
				width = 220,
				height = mScreen.ITEM_HEIGHT + 2,
				name = "Tên tài khoản",
				isFocus = true
			};
			tfPassword = new TField
			{
				width = 220,
				height = mScreen.ITEM_HEIGHT + 2,
				name = "Mật khẩu"
			};
			tfPassword.setIputType(TField.INPUT_TYPE_PASSWORD);
			tfConfirmPassword = new TField
			{
				width = 220,
				height = mScreen.ITEM_HEIGHT + 2,
				name = "Nhập lại mật khẩu"
			};
			tfConfirmPassword.setIputType(TField.INPUT_TYPE_PASSWORD);
			tfUsername.setIputType(TField.INPUT_ALPHA_NUMBER_ONLY);
			tfUsername.setMaxTextLenght(20);
			tfPassword.setMaxTextLenght(18);
			tfConfirmPassword.setMaxTextLenght(18);
			focus = 0;
			int num = 4;
			int num2 = num * 32 + 23 + 33;
			if (num2 >= GameCanvas.w)
			{
				num--;
				num2 = num * 32 + 23 + 33;
			}
			xLog = GameCanvas.w / 2 - num2 / 2;
			yLog = 5;
			lY = ((GameCanvas.w < 200) ? (tfPassword.y - 30) : (yLog - 30));
			tfPassword.x = xLog + 10;
			tfPassword.y = yLog + 20;
			cmdOK = new Command("Đăng ký", this, 2008, null)
			{
				w = 76,
				h = mScreen.cmdH
			};
			cmdExit = new Command("Thoát", this, 1003, null)
			{
				w = 76,
				h = mScreen.cmdH
			};
			cmdShowPassword = new Command("Hiện", this, 2009, null)
			{
				type = 2,
				w = 42,
				h = mScreen.cmdH,
				hw = 21
			};
			cmdShowConfirmPassword = new Command("Hiện", this, 2010, null)
			{
				type = 2,
				w = 42,
				h = mScreen.cmdH,
				hw = 21
			};
			center = cmdOK;
			left = cmdExit;
			imgTitle = ModFunc.imgLogoBig;
		}

		public new void switchToMe()
		{
			SoundMn.gI().stopAll();
			focus = 0;
			if (GameCanvas.isTouch)
			{
				tfUsername.isFocus = false;
				focus = -1;
			}
			base.switchToMe();
		}

		public override void update()
		{
			tfPassword.update();
			tfUsername.update();
			tfConfirmPassword.update();
			cmdShowPassword.isFocus = GameCanvas.isPointerDown
				&& GameCanvas.isPointerHoldIn(cmdShowPassword.x, cmdShowPassword.y, cmdShowPassword.w, cmdShowPassword.h);
			cmdShowConfirmPassword.isFocus = GameCanvas.isPointerDown
				&& GameCanvas.isPointerHoldIn(cmdShowConfirmPassword.x, cmdShowConfirmPassword.y,
					cmdShowConfirmPassword.w, cmdShowConfirmPassword.h);
			for (int i = 0; i < Effect2.vEffect2.size(); i++)
			{
				((Effect2)Effect2.vEffect2.elementAt(i)).update();
			}
			GameScr.cmx++;
			if (GameScr.cmx > GameCanvas.w * 3 + 100)
			{
				GameScr.cmx = 100;
			}
			if (ChatPopup.currChatPopup == null && g >= 0)
			{
				ylogo += dir * g;
				g += dir * v;
				if (g <= 0)
				{
					dir *= -1;
				}
				if (ylogo > 0)
				{
					dir *= -1;
					g -= 2 * v;
				}
			}
		}

		public override void keyPress(int keyCode)
		{
			if (tfPassword.isFocus)
			{
				tfPassword.keyPressed(keyCode);
			}
			else if (tfUsername.isFocus)
			{
				tfUsername.keyPressed(keyCode);
			}
			else if (tfConfirmPassword.isFocus)
			{
				tfConfirmPassword.keyPressed(keyCode);
			}
			base.keyPress(keyCode);
		}

		public override void unLoad()
		{
			base.unLoad();
		}

		public override void paint(mGraphics g)
		{
			GameCanvas.paintBGGameScr(g);
			if (ChatPopup.currChatPopup != null || ChatPopup.serverChatPopUp != null)
			{
				return;
			}
			if (GameCanvas.currentDialog == null)
			{
				int panelWidth = (GameCanvas.w < 250) ? (GameCanvas.w - 10) : 240;
				int fieldStep = tfUsername.height + 10;
				int panelHeight = 157;
				xLog = (GameCanvas.w - panelWidth) / 2;
				yLog = (GameCanvas.h - panelHeight) / 2;
				if (yLog < 2)
				{
					yLog = 2;
				}
				PopUp.paintPopUp(g, xLog, yLog, panelWidth, panelHeight, -1, isButton: true);
				mFont.tahoma_7b_dark.drawString(g, "Đăng ký tài khoản", GameCanvas.hw, yLog + 6, mFont.CENTER);

				int fieldWidth = panelWidth - 20;
				tfUsername.width = fieldWidth;
				tfPassword.width = fieldWidth - 45;
				tfConfirmPassword.width = fieldWidth - 45;
				tfUsername.x = xLog + 10;
				tfUsername.y = yLog + 23;

				tfPassword.x = tfUsername.x;
				tfPassword.y = tfUsername.y + fieldStep;

				tfConfirmPassword.x = tfUsername.x;
				tfConfirmPassword.y = tfPassword.y + fieldStep;

				cmdShowPassword.x = tfPassword.x + tfPassword.width + 3;
				cmdShowPassword.y = tfPassword.y;
				cmdShowConfirmPassword.x = tfConfirmPassword.x + tfConfirmPassword.width + 3;
				cmdShowConfirmPassword.y = tfConfirmPassword.y;

				int buttonY = tfConfirmPassword.y + tfConfirmPassword.height + 12;
				cmdOK.x = GameCanvas.hw - 79;
				cmdOK.y = buttonY;
				cmdExit.x = GameCanvas.hw + 3;
				cmdExit.y = buttonY;

				tfUsername.paint(g);
				tfPassword.paint(g);
				tfConfirmPassword.paint(g);
				g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
				cmdShowPassword.paint(g);
				cmdShowConfirmPassword.paint(g);
				if (GameCanvas.w < 176)
				{
					mFont.tahoma_7b_green2.drawString(g, mResources.acc + ":", tfUsername.x - 35, tfUsername.y + 7, 0);
					mFont.tahoma_7b_green2.drawString(g, mResources.pwd + ":", tfPassword.x - 35, tfPassword.y + 7, 0);
					mFont.tahoma_7b_green2.drawString(g, mResources.server + ": " + serverName, GameCanvas.w / 2, tfPassword.y + 32, 2);
				}
			}
			GameCanvas.resetTrans(g);
			base.paint(g);
		}

		private void turnOffFocus()
		{
			tfPassword.isFocus = false;
			tfUsername.isFocus = false;
			tfConfirmPassword.isFocus = false;
		}

		private bool isAlphaNumeric(string value)
		{
			if (string.IsNullOrEmpty(value))
			{
				return false;
			}
			for (int i = 0; i < value.Length; i++)
			{
				char c = value[i];
				if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')))
				{
					return false;
				}
			}
			return true;
		}

		public void onRegisterSuccess()
		{
			string username = tfUsername.getText().Trim();
			string password = tfPassword.getText().Trim();
			Rms.saveRMSString("acc", username);
			Rms.saveRMSString("pass", password);
			Rms.saveRMSString("userAo" + ServerListScreen.ipSelect, string.Empty);
			Rms.saveRMSString("passAo" + ServerListScreen.ipSelect, string.Empty);
			GameCanvas.acc = username;
			GameCanvas.pass = password;
			if (GameCanvas.loginScr != null)
			{
				GameCanvas.loginScr.setCurrentTabAccount(username, password);
			}
			else
			{
				GameCanvas.loginScr = new LoginScr();
				GameCanvas.loginScr.setCurrentTabAccount(username, password);
			}
			GameCanvas.loginScr.switchToMe();
		}

		private void processFocus()
		{
			turnOffFocus();
			switch (focus)
			{
				case 0:
					tfUsername.isFocus = true;
					break;
				case 1:
					tfPassword.isFocus = true;
					break;
				case 2:
					tfConfirmPassword.isFocus = true;
					break;
			}
		}

		public override void updateKey()
		{
			if (!GameCanvas.isTouch)
			{
				if (tfPassword.isFocus)
				{
					right = tfPassword.cmdClear;
				}
				else if (tfUsername.isFocus)
				{
					right = tfUsername.cmdClear;
				}
			else if (tfConfirmPassword.isFocus)
			{
				right = tfConfirmPassword.cmdClear;
			}
			}
			if (GameCanvas.keyPressed[21])
			{
				focus--;
				if (focus < 0)
				{
					focus = 2;
				}
				processFocus();
			}
			else if (GameCanvas.keyPressed[22])
			{
				focus++;
				if (focus > 2)
				{
					focus = 0;
				}
				processFocus();
			}
			if (GameCanvas.isPointerJustRelease)
			{
				if (GameCanvas.isPointerHoldIn(cmdShowPassword.x, cmdShowPassword.y, cmdShowPassword.w, cmdShowPassword.h))
				{
					cmdShowPassword.performAction();
					return;
				}
				if (GameCanvas.isPointerHoldIn(cmdShowConfirmPassword.x, cmdShowConfirmPassword.y,
					cmdShowConfirmPassword.w, cmdShowConfirmPassword.h))
				{
					cmdShowConfirmPassword.performAction();
					return;
				}
				if (GameCanvas.isPointerHoldIn(tfPassword.x, tfPassword.y, tfPassword.width, tfPassword.height))
				{
					focus = 1;
					processFocus();
				}
				else if (GameCanvas.isPointerHoldIn(tfUsername.x, tfUsername.y, tfUsername.width, tfUsername.height))
				{
					focus = 0;
					processFocus();
				}
				else if (GameCanvas.isPointerHoldIn(tfConfirmPassword.x, tfConfirmPassword.y,
					tfConfirmPassword.width, tfConfirmPassword.height))
				{
					focus = 2;
					processFocus();
				}
			}
			base.updateKey();
			GameCanvas.clearKeyPressed();
		}

		public void perform(int idAction, object p)
		{
			switch (idAction)
			{
			case 1003:
				if (GameCanvas.loginScr == null)
				{
					GameCanvas.loginScr = new LoginScr();
				}
				GameCanvas.loginScr.switchToMe();
				break;
			case 2008:
				string username = tfUsername.getText().Trim();
				string password = tfPassword.getText().Trim();
				string confirmPassword = tfConfirmPassword.getText().Trim();
				if (username.Length < 5 || username.Length > 20 || !isAlphaNumeric(username))
				{
					GameCanvas.startOKDlg("Tài khoản phải có 5-20 ký tự chữ hoặc số");
					break;
				}
				if (password.Length < 3 || password.Length > 18 || !isAlphaNumeric(password))
				{
					GameCanvas.startOKDlg("Mật khẩu phải có 3-18 ký tự chữ hoặc số");
					break;
				}
				if (!password.Equals(confirmPassword))
				{
					GameCanvas.startOKDlg("Mật khẩu nhập lại không khớp");
					break;
				}
				GameCanvas.startOKDlg(mResources.PLEASEWAIT);
				Service.gI().charInfo(
					"1","1","1","1","1",
					username,
					password
					);
				break;
			case 2009:
				showPassword = !showPassword;
				cmdShowPassword.caption = showPassword ? "Ẩn" : "Hiện";
				tfPassword.setIputType(showPassword ? TField.INPUT_ALPHA_NUMBER_ONLY : TField.INPUT_TYPE_PASSWORD);
				tfPassword.setMaxTextLenght(18);
				tfPassword.setText(tfPassword.getText());
				break;
			case 2010:
				showConfirmPassword = !showConfirmPassword;
				cmdShowConfirmPassword.caption = showConfirmPassword ? "Ẩn" : "Hiện";
				tfConfirmPassword.setIputType(showConfirmPassword ? TField.INPUT_ALPHA_NUMBER_ONLY : TField.INPUT_TYPE_PASSWORD);
				tfConfirmPassword.setMaxTextLenght(18);
				tfConfirmPassword.setText(tfConfirmPassword.getText());
				break;
			}
		}
	}
}
