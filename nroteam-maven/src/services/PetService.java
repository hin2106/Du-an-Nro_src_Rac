package services;
import consts.ConstPlayer;
import player.Pet;
import player.Player;
import services.map.ChangeMapService;
import services.player.InventoryService;
import utils.SkillUtil;
import utils.Util;

public class PetService {

    private static PetService instance;

    public static PetService gI() {
        if (instance == null) {
            instance = new PetService();
        }
        return instance;
    }

    public void createNormalPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                if (player == null || player.pet != null) {
                    return;
                }
                createNewPet(player, false, false, (byte) gender);
                if (player.pet == null) {
                    return;
                }
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                if (player.zone != null && (player.pet.zone == null || player.pet.zone != player.zone)) {
                    player.pet.joinMapMaster();
                }
                Thread.sleep(1000);
                if (player.pet != null && !player.isDie() && !player.pet.isDie()) {
                    Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận con làm đệ tử");
                }
            } catch (Exception e) {
            }
        }).start();
    }

    public void createNormalPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createMabuPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, true, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Oa oa oa...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createFideNhiPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet1(player, false, true, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createXenNhiPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet1(player, false, false, true, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createMabuNhiPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet1(player, true, false, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void changeNormalPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createNormalPet(player, gender, limitPower);
    }

    public void changeNormalPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createNormalPet(player, limitPower);
    }

    public void changeMabuPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createMabuPet(player, limitPower);
    }

    public void changeMabuPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createMabuPet(player, gender, limitPower);
    }

    public void changeFideNhiPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createFideNhiPet(player, gender, limitPower);
    }

    public void changeMabuNhiPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createMabuNhiPet(player, gender, limitPower);
    }

    public void changeXenNhiPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createXenNhiPet(player, gender, limitPower);
    }

    public void setChildPet(Player player, int variant, int gender, byte... limitPower) {
        switch (variant) {
            case 1: {
                if (player.pet == null) {
                    createFideNhiPet(player, gender, limitPower);
                } else {
                    changeFideNhiPet(player, gender);
                }
                break;
            }
            case 2: {
                if (player.pet == null) {
                    createXenNhiPet(player, gender, limitPower);
                } else {
                    changeXenNhiPet(player, gender);
                }
                break;
            }
            case 3: {
                if (player.pet == null) {
                    createMabuNhiPet(player, gender, limitPower);
                } else {
                    changeMabuNhiPet(player, gender);
                }
                break;
            }
            default:
                break;
        }
    }

    public void changeNamePet(Player player, String name) {
        try {
            if (!InventoryService.gI().isExistItemBag(player, 400)) {
                Service.gI().sendThongBao(player, "Bạn cần thẻ đặt tên đệ tử, mua tại Santa");
                return;
            } else if (Util.haveSpecialCharacter(name)) {
                Service.gI().sendThongBao(player, "Tên không được chứa ký tự đặc biệt");
                return;
            } else if (name.length() > 10) {
                Service.gI().sendThongBao(player, "Tên quá dài");
                return;
            }
            ChangeMapService.gI().exitMap(player.pet);
            player.pet.name = "$" + name.toLowerCase().trim();
            InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, 400), 1);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Service.gI().chatJustForMe(player, player.pet, "Cảm ơn sư phụ đã đặt cho con tên " + name);
                } catch (Exception e) {
                }
            }).start();
        } catch (Exception ex) {

        }
    }

    private int[] getDataPetNormal() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; // hp
        petData[1] = Util.nextInt(40, 105) * 20; // mp
        petData[2] = Util.nextInt(20, 45); // dame
        petData[3] = Util.nextInt(9, 50); // def
        petData[4] = Util.nextInt(0, 2); // crit
        return petData;
    }

    private int[] getDataPetMabu() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; // hp
        petData[1] = Util.nextInt(40, 105) * 20; // mp
        petData[2] = Util.nextInt(50, 120); // dame
        petData[3] = Util.nextInt(9, 50); // def
        petData[4] = Util.nextInt(0, 2); // crit
        return petData;
    }

    private int[] getDataPetNhi() {
        int[] petData = new int[5];
        petData[0] = 747500;
        petData[1] = 747500;
        petData[2] = 33800;
        petData[3] = 800;
        petData[4] = 3;
        return petData;
    }

    private void createNewPet(Player player, boolean isMabu, boolean isBeerus, byte... gender) {
        int[] data = isMabu ? getDataPetMabu() : getDataPetNormal();
        Pet pet = new Pet(player);
        pet.name = "$" + (isMabu ? "Mabư" : isBeerus ? "Beerus" : "Đệ tử");
        pet.gender = (gender != null && gender.length != 0) ? gender[0] : (byte) Util.nextInt(0, 2);
        pet.id = player.isPl() ? -player.id : -Math.abs(player.id) - 100000;
        pet.nPoint.power = isMabu || isBeerus ? 1500000 : 2000;
        pet.typePet = (byte) (isMabu ? 1 : isBeerus ? 2 : 0);
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];
        for (int i = 0; i < 6; i++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        pet.playerSkill.skills.add(SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1));
        for (int i = 0; i < 6; i++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        player.pet = pet;
    }

    private void createNewPet1(Player player, boolean isUub, boolean isKidJiren, boolean isKidBeer, byte... gender) {
        int[] data = getDataPetNhi();
        Pet pet = new Pet(player);
        pet.name = "$" + (isKidJiren ? "Kid Jiren" : isKidBeer ? "Kid Beer" : isUub ? "Uub" : "Đệ tử");
        pet.gender = isUub ? 0 : isKidJiren ? 1 : isKidBeer ? 2 : (byte) Util.nextInt(0, 2);
        pet.id = player.isPl() ? -player.id : -Math.abs(player.id) - 100000;
        pet.nPoint.power = 40_000_000_000L;
        pet.typePet = (byte) (isKidJiren ? 5 : isKidBeer ? 6 : isUub ? 4 : 0);
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];
        for (int i = 0; i < 6; i++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        pet.playerSkill.skills.add(SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1));
        for (int i = 0; i < 6; i++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        player.pet = pet;
    }

    // public static void Pet2(Player pl, int h, int b, int l) {
    //     try {
    //         if (pl.newPet != null) {
    //             pl.newPet.dispose();
    //             pl.newPet = null;
    //         }
    //         int itemId = -1;
    //         String petName = "$ ";
    //         if (pl.inventory != null && pl.inventory.itemsBody != null
    //                 && pl.inventory.itemsBody.size() > 7 && pl.inventory.itemsBody.get(9) != null) {

    //             var item = pl.inventory.itemsBody.get(9);// bo_pet
    //             if (item != null && item.template != null) {
    //                 itemId = item.template.id;
    //                 petName = "#" + item.template.name;
    //             }
    //         }

    //         pl.newPet = new NewPet(pl, (short) h, (short) b, (short) l, itemId);
    //         pl.newPet.name = petName;
    //         pl.newPet.gender = pl.gender;

    //         pl.newPet.nPoint.tiemNang = 1;
    //         pl.newPet.nPoint.power = 1;
    //         pl.newPet.nPoint.limitPower = 1;
    //         pl.newPet.nPoint.hpg = 500_000_000;
    //         pl.newPet.nPoint.mpg = 500_000_000;
    //         pl.newPet.nPoint.hp = 500_000_000;
    //         pl.newPet.nPoint.mp = 500_000_000;
    //         pl.newPet.nPoint.dameg = 1;
    //         pl.newPet.nPoint.defg = 1;
    //         pl.newPet.nPoint.critg = 1;
    //         pl.newPet.nPoint.stamina = 1;
    //         pl.newPet.nPoint.setBasePoint();
    //         pl.newPet.nPoint.setFullHpMp();

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

}
