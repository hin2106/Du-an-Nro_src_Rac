package utils;
import consts.ConstPlayer;
import java.util.ArrayList;
import java.util.List;
import player.Player;
import server.Manager;
import skill.NClass;
import skill.Skill;
import system.Template.SkillTemplate;

public class SkillUtil {

    private static final NClass nClassTD;
    private static final NClass nClassNM;
    private static final NClass nClassXD;

    public static int getTimett() {
        return 300000;
    }

    static {
        nClassTD = Manager.NCLASS.get(0);
        nClassNM = Manager.NCLASS.get(1);
        nClassXD = Manager.NCLASS.get(2);
    }

    public static Skill createSkill(int tempId, int level) {
        SkillTemplate template = findSkillTemplate(tempId);
        if (template != null) {
            List<Skill> skills = template.skillss;
            if (skills != null && level >= 1 && level <= skills.size()) {
                Skill skill = skills.get(level - 1);
                return new Skill(skill);
            }
        }
        return null;
    }

    public static SkillTemplate findSkillTemplate(int tempId) {
        SkillTemplate template = nClassTD.getSkillTemplate(tempId);
        if (template == null) {
            template = nClassNM.getSkillTemplate(tempId);
        }
        if (template == null) {
            template = nClassXD.getSkillTemplate(tempId);
        }
        return template;
    }

    @SuppressWarnings("unchecked")
    public static List<Skill> findSkills(int tempId) {
        List<Skill> skills = nClassTD.getSkills(tempId);
        if (skills == null) {
            skills = nClassNM.getSkills(tempId);
        }
        if (skills == null) {
            skills = nClassXD.getSkills(tempId);
        }
        return skills;
    }

    @SuppressWarnings("unchecked")
    public static List<Skill> findPointSkill(int skillId) {
        return nClassTD.getSkills(skillId);
    }

    public static Skill createEmptySkill() {
        Skill skill = new Skill();
        skill.skillId = -1;
        return skill;
    }

    public static Skill createSkillLevel0(int tempId) {
        Skill skill = createEmptySkill();
        skill.template = new SkillTemplate();
        skill.template.id = (byte) tempId;
        return skill;
    }

    public static boolean isUseSkillDam(Player player) {
        int skillId = player.playerSkill.skillSelect.template.id;
        return (skillId == Skill.DRAGON || skillId == Skill.DEMON
                || skillId == Skill.GALICK || skillId == Skill.KAIOKEN
                || skillId == Skill.LIEN_HOAN);
    }

    public static boolean isUseSkillChuong(Player player) {
        int skillId = player.playerSkill.skillSelect.template.id;
        return (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC);
    }

    public static int getTimeMonkey(int level) {
        return (level + 5) * 10000;
    }

    public static int getPercentHpMonkey(int level) {
        return (level + 3) * 10;
    }

    public static int getPercentDameMonkey(int level) {
        return (level + 3);
    }

    public static int getTimeStun(int level) {
        return (level + 2) * 1000;
    }

    public static int getTimeSocola() {
        return 30000;
    }

    public static int getTimeShield(int level) {
        return (level + 2) * 5000;
    }

    public static int getTimeTroi(int level) {
        return level * 5000;
    }

    public static int getTimeDCTT(int level) {
        return (level + 1) * 500;
    }

    public static int getTimeThoiMien(int level) {
        return (level + 4) * 1000;
    }

    public static int getRangeStun(int level) {
        return 120 + level * 30;
    }

    public static int getRangeBom(int level) {
        int tambomtheocap;
        switch (level) {
            case 1 -> tambomtheocap = 200;
            case 2 -> tambomtheocap = 300;
            case 3 -> tambomtheocap = 400;
            case 4 -> tambomtheocap = 500;
            case 5 -> tambomtheocap = 600;
            case 6 -> tambomtheocap = 700;
            case 7 -> tambomtheocap = 900;
            default -> tambomtheocap = 200;
        }
        return 0 + tambomtheocap;

    }

    public static int getRangeQCKK(int level) {
        return 350 + level * 30;
    }

    public static int getPercentHPHuytSao(int level) {
        return (level + 3) * 10;
    }

    public static int getPercentTriThuong(int level) {
        return (level + 9) * 5;
    }

    public static int getPercentCharge(int level) {
        return level + 3;
    }

    public static int getTempMobMe(int level) {
        int[] temp = { 8, 11, 32, 25, 43, 49, 50 };
        return temp[level - 1];
    }

    public static int getTimeSurviveMobMe(int level) {
        return getTimeMonkey(level) * 2;
    }

    public static long getHPMobMe(long hpMaxPlayer, int level) {
        long[] perHPs = { 30, 40, 50, 60, 70, 80, 90 };
        return hpMaxPlayer * perHPs[level - 1] / 100L;
    }

    public static Skill getSkillbyId(Player player, int id) {
        if (player == null || player.playerSkill == null || player.playerSkill.skills == null) {
            return null;
        }
        for (Skill skill : player.playerSkill.skills) {
            if (skill != null && skill.template != null && skill.template.id == id) {
                return skill;
            }
        }
        return null;
    }

    public static boolean upSkillPet(List<Skill> skills, int index) {
        int tempId = skills.get(index).template.id;
        int level = skills.get(index).point + 1;
        if (level > 7) {
            return false;
        }

        SkillTemplate template = findSkillTemplate(tempId);
        if (template != null) {
            List<Skill> skillss = template.skillss;
            if (skillss != null && level >= 1 && level <= skillss.size()) {
                Skill skill = new Skill(skillss.get(level - 1));
                if (index == 1) {
                    skill.coolDown = 1000;
                }
                skills.set(index, skill);
                return true;
            }
        }
        return false;
    }

    public static byte getTempSkillSkillByItemID(int id) {
        if (id >= 65 && id <= 72) {
            return Skill.DRAGON;
        } else if (id >= 79 && id <= 84 || id == 86) {
            return Skill.DEMON;
        } else if (id >= 87 && id <= 93) {
            return Skill.GALICK;
        } else if (id >= 94 && id <= 100) {
            return Skill.KAMEJOKO;
        } else if (id >= 101 && id <= 107) {
            return Skill.MASENKO;
        } else if (id >= 108 && id <= 114) {
            return Skill.ANTOMIC;
        } else if (id >= 115 && id <= 121) {
            return Skill.THAI_DUONG_HA_SAN;
        } else if (id >= 122 && id <= 128) {
            return Skill.TRI_THUONG;
        } else if (id >= 129 && id <= 135) {
            return Skill.TAI_TAO_NANG_LUONG;
        } else if (id >= 300 && id <= 306) {
            return Skill.KAIOKEN;
        } else if (id >= 307 && id <= 313) {
            return Skill.QUA_CAU_KENH_KHI;
        } else if (id >= 314 && id <= 320) {
            return Skill.BIEN_KHI;
        } else if (id >= 321 && id <= 327) {
            return Skill.TU_SAT;
        } else if (id >= 328 && id <= 334) {
            return Skill.MAKANKOSAPPO;
        } else if (id >= 335 && id <= 341) {
            return Skill.DE_TRUNG;
        } else if (id >= 434 && id <= 440) {
            return Skill.KHIEN_NANG_LUONG;
        } else if (id >= 474 && id <= 480) {
            return Skill.SOCOLA;
        } else if (id >= 481 && id <= 487) {
            return Skill.LIEN_HOAN;
        } else if (id >= 488 && id <= 494) {
            return Skill.DICH_CHUYEN_TUC_THOI;
        } else if (id >= 495 && id <= 501) {
            return Skill.THOI_MIEN;
        } else if (id >= 502 && id <= 508) {
            return Skill.TROI;
        } else if (id >= 509 && id <= 515) {
            return Skill.HUYT_SAO;
        } else {
            return -1;
        }
    }

    public static Skill getSkillByItemID(Player pl, int tempId) {
        if (tempId >= 65 && tempId <= 72) {
            return getSkillbyId(pl, Skill.DRAGON);
        } else if (tempId >= 79 && tempId <= 84 || tempId == 86) {
            return getSkillbyId(pl, Skill.DEMON);
        } else if (tempId >= 87 && tempId <= 93) {
            return getSkillbyId(pl, Skill.GALICK);
        } else if (tempId >= 94 && tempId <= 100) {
            return getSkillbyId(pl, Skill.KAMEJOKO);
        } else if (tempId >= 101 && tempId <= 107) {
            return getSkillbyId(pl, Skill.MASENKO);
        } else if (tempId >= 108 && tempId <= 114) {
            return getSkillbyId(pl, Skill.ANTOMIC);
        } else if (tempId >= 115 && tempId <= 121) {
            return getSkillbyId(pl, Skill.THAI_DUONG_HA_SAN);
        } else if (tempId >= 122 && tempId <= 128) {
            return getSkillbyId(pl, Skill.TRI_THUONG);
        } else if (tempId >= 129 && tempId <= 135) {
            return getSkillbyId(pl, Skill.TAI_TAO_NANG_LUONG);
        } else if (tempId >= 300 && tempId <= 306) {
            return getSkillbyId(pl, Skill.KAIOKEN);
        } else if (tempId >= 307 && tempId <= 313) {
            return getSkillbyId(pl, Skill.QUA_CAU_KENH_KHI);
        } else if (tempId >= 314 && tempId <= 320) {
            return getSkillbyId(pl, Skill.BIEN_KHI);
        } else if (tempId >= 321 && tempId <= 327) {
            return getSkillbyId(pl, Skill.TU_SAT);
        } else if (tempId >= 328 && tempId <= 334) {
            return getSkillbyId(pl, Skill.MAKANKOSAPPO);
        } else if (tempId >= 335 && tempId <= 341) {
            return getSkillbyId(pl, Skill.DE_TRUNG);
        } else if (tempId >= 434 && tempId <= 440) {
            return getSkillbyId(pl, Skill.KHIEN_NANG_LUONG);
        } else if (tempId >= 474 && tempId <= 480) {
            return getSkillbyId(pl, Skill.SOCOLA);
        } else if (tempId >= 481 && tempId <= 487) {
            return getSkillbyId(pl, Skill.LIEN_HOAN);
        } else if (tempId >= 488 && tempId <= 494) {
            return getSkillbyId(pl, Skill.DICH_CHUYEN_TUC_THOI);
        } else if (tempId >= 495 && tempId <= 501) {
            return getSkillbyId(pl, Skill.THOI_MIEN);
        } else if (tempId >= 502 && tempId <= 508) {
            return getSkillbyId(pl, Skill.TROI);
        } else if (tempId >= 509 && tempId <= 515) {
            return getSkillbyId(pl, Skill.HUYT_SAO);
        } else {
            return null;
        }
    }

    public static void setSkill(Player pl, Skill newSkill) {
        for (int i = 0; i < pl.playerSkill.skills.size(); i++) {
            Skill oldSkill = pl.playerSkill.skills.get(i);
            if (oldSkill.template.id == newSkill.template.id) {
                if (newSkill.point > oldSkill.point) {
                    pl.playerSkill.skills.set(i, newSkill);
                }
                return;
            }
        }
        pl.playerSkill.skills.add(newSkill);
    }

    public static byte getTyleSkillAttack(Skill skill) {
        return switch (skill.template.id) {
            case Skill.TRI_THUONG ->
                2;
            case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC ->
                1;
            default ->
                0;
        };
    }

    public static int[] getValidSkillsForGender(byte gender) {
        return switch (gender) {
            case ConstPlayer.TRAI_DAT -> new int[] { 0, 1, 6, 9, 10, 20, 22, 19, 24 };
            case ConstPlayer.NAMEC -> new int[] { 2, 3, 7, 11, 12, 17, 18, 19, 26 };
            case ConstPlayer.XAYDA -> new int[] { 4, 5, 8, 13, 14, 21, 23, 19, 25 };
            default -> new int[] {};
        };
    }

    /**
     * Kiểm tra skill ID có thuộc gender không
     * 
     * @param skillId ID của skill
     * @param gender  0 = Trái Đất, 1 = Namếc, 2 = Xayda
     * @return true nếu skill hợp lệ với gender
     */
    public static boolean isSkillValidForGender(int skillId, byte gender) {
        int[] validSkills = getValidSkillsForGender(gender);
        for (int validSkillId : validSkills) {
            if (validSkillId == skillId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate và filter skills theo gender của player
     * Loại bỏ các skills không thuộc gender hiện tại
     * 
     * @param player Player cần validate skills
     * @return Số lượng skills bị loại bỏ
     */
    public static int validateAndFilterSkillsByGender(Player player) {
        if (player == null || player.playerSkill == null || player.playerSkill.skills == null) {
            return 0;
        }

        int removedCount = 0;
        List<Skill> skillsToRemove = new ArrayList<>();
        int[] validSkills = getValidSkillsForGender(player.gender);

        // Kiểm tra từng skill
        for (Skill skill : player.playerSkill.skills) {
            if (skill == null || skill.template == null) {
                skillsToRemove.add(skill);
                removedCount++;
                continue;
            }

            int skillId = skill.template.id;
            boolean isValid = false;
            for (int validSkillId : validSkills) {
                if (validSkillId == skillId) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid && skillId != -1) {
                // Skill không hợp lệ với gender hiện tại
                Logger.warning("Player " + player.name + " (gender: " + player.gender +
                        ") có skill không hợp lệ: " + skillId + " - sẽ bị loại bỏ");
                skillsToRemove.add(skill);
                removedCount++;
            }
        }

        // Loại bỏ các skills không hợp lệ
        for (Skill skill : skillsToRemove) {
            player.playerSkill.skills.remove(skill);
        }

        // Đảm bảo có ít nhất skill cơ bản của gender
        if (player.playerSkill.skills.isEmpty() ||
                !hasBasicSkill(player.playerSkill.skills, player.gender)) {
            addDefaultSkillsForGender(player);
        }

        // Fix skill shortcut nếu có skill không hợp lệ
        fixSkillShortcut(player);

        return removedCount;
    }

    /**
     * Kiểm tra player có skill cơ bản của gender chưa
     */
    private static boolean hasBasicSkill(List<Skill> skills, byte gender) {
        int basicSkillId = getBasicSkillIdForGender(gender);
        for (Skill skill : skills) {
            if (skill != null && skill.template != null &&
                    skill.template.id == basicSkillId && skill.point > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lấy skill ID cơ bản của gender (skill đầu tiên)
     */
    private static int getBasicSkillIdForGender(byte gender) {
        return switch (gender) {
            case ConstPlayer.TRAI_DAT -> Skill.DRAGON;
            case ConstPlayer.NAMEC -> Skill.DEMON;
            case ConstPlayer.XAYDA -> Skill.GALICK;
            default -> -1;
        };
    }

    /**
     * Thêm skills mặc định cho gender nếu thiếu
     */
    private static void addDefaultSkillsForGender(Player player) {
        int[] defaultSkills = getValidSkillsForGender(player.gender);

        for (int skillId : defaultSkills) {
            boolean exists = false;
            for (Skill skill : player.playerSkill.skills) {
                if (skill != null && skill.template != null && skill.template.id == skillId) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                // Tạo skill level 0 nếu là skill mới, level 1 nếu là skill đầu tiên
                Skill newSkill = createSkillLevel0(skillId);
                if (skillId == defaultSkills[0]) {
                    // Skill đầu tiên mặc định level 1
                    newSkill = createSkill(skillId, 1);
                    if (newSkill == null) {
                        newSkill = createSkillLevel0(skillId);
                    }
                }
                if (newSkill != null) {
                    player.playerSkill.skills.add(newSkill);
                }
            }
        }

        // Đảm bảo skillSelect được set đúng
        if (player.playerSkill.skillSelect == null ||
                player.playerSkill.skillSelect.template == null ||
                !isSkillValidForGender(player.playerSkill.skillSelect.template.id, player.gender)) {
            int basicSkillId = getBasicSkillIdForGender(player.gender);
            Skill basicSkill = getSkillbyId(player, basicSkillId);
            if (basicSkill != null && basicSkill.point > 0) {
                player.playerSkill.skillSelect = basicSkill;
            }
        }
    }

    /**
     * Fix skill shortcut - loại bỏ các shortcut trỏ đến skill không hợp lệ
     */
    private static void fixSkillShortcut(Player player) {
        if (player.playerSkill.skillShortCut == null) {
            return;
        }

        int[] validSkills = getValidSkillsForGender(player.gender);
        boolean hasValidShortcut = false;

        // Kiểm tra và fix từng shortcut
        for (int i = 0; i < player.playerSkill.skillShortCut.length; i++) {
            int skillId = player.playerSkill.skillShortCut[i];
            if (skillId == -1) {
                continue;
            }

            boolean isValid = false;
            for (int validSkillId : validSkills) {
                if (validSkillId == skillId) {
                    Skill skill = getSkillbyId(player, skillId);
                    if (skill != null && skill.point > 0) {
                        isValid = true;
                        hasValidShortcut = true;
                        break;
                    }
                }
            }

            if (!isValid) {
                player.playerSkill.skillShortCut[i] = -1;
            }
        }

        // Nếu không có shortcut hợp lệ, set shortcut đầu tiên
        if (!hasValidShortcut) {
            int basicSkillId = getBasicSkillIdForGender(player.gender);
            Skill basicSkill = getSkillbyId(player, basicSkillId);
            if (basicSkill != null && basicSkill.point > 0) {
                player.playerSkill.skillShortCut[0] = (byte) basicSkillId;
            }
        }
    }

    /**
     * Validate skill trước khi học/mua
     * 
     * @param player  Player đang học skill
     * @param skillId ID của skill muốn học
     * @return true nếu skill hợp lệ với gender của player
     */
    public static boolean validateSkillBeforeLearn(Player player, int skillId) {
        if (player == null) {
            return false;
        }
        return isSkillValidForGender(skillId, player.gender);
    }
}
