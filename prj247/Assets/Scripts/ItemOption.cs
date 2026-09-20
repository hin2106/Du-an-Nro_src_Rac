using System;

public class ItemOption
{
	public int param;

	public sbyte active;

	public sbyte activeCard;

	public ItemOptionTemplate optionTemplate;

	public ItemOption()
	{
	}

	public bool IsValidOption()
	{
		if (this != null && optionTemplate != null && optionTemplate.id != 21 && optionTemplate.id != 200 && optionTemplate.id != 72 && optionTemplate.id != 57 && optionTemplate.id != 58 && optionTemplate.id != 34 && optionTemplate.id != 35 && optionTemplate.id != 36 && optionTemplate.id != 102 && optionTemplate.id != 107)
		{
			return true;
		}
		return false;
	}

	public ItemOption(int optionTemplateId, int param)
	{
		if (optionTemplateId == 22)
		{
			optionTemplateId = 6;
			param *= 1000;
		}
		if (optionTemplateId == 23)
		{
			optionTemplateId = 7;
			param *= 1000;
		}
		this.param = param;
		try
		{
			GameScr gameScr = GameScr.gI();
			if (gameScr != null && gameScr.iOptionTemplates != null && optionTemplateId >= 0 && optionTemplateId < gameScr.iOptionTemplates.Length)
			{
				optionTemplate = gameScr.iOptionTemplates[optionTemplateId];
			}
			else
			{
				Res.err(">>>>ItemOption constructor errr: GameScr or iOptionTemplates is null, or optionTemplateId out of bounds. optionTemplateId=" + optionTemplateId);
				optionTemplate = null;
			}
		}
		catch (Exception ex)
		{
			Res.err(">>>>ItemOption constructor errr: " + ex.Message + " StackTrace: " + ex.StackTrace);
			optionTemplate = null;
		}
	}

	public string getOptionString()
	{
		if (optionTemplate == null || optionTemplate.name == null)
		{
			return string.Empty;
		}
		return NinjaUtil.Replace(optionTemplate.name, "#", param + string.Empty);
	}

	public string getOptiongColor()
	{
		if (optionTemplate == null || optionTemplate.name == null)
		{
			return string.Empty;
		}
		return NinjaUtil.Replace(optionTemplate.name, "$", string.Empty);
	}
}
